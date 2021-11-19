/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.api.scala.typeutils.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.functions.source.FromSplittableIteratorFunction;
import org.apache.flink.util.Collector;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingJob {

    public static void main(String[] args) throws Exception {
        ParameterTool params = ParameterTool.fromArgs(args);

        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(TimeUnit.SECONDS.toMillis(
                Integer.parseInt(params.get("checkpoint-period", "10"))));
        env.getCheckpointConfig().setCheckpointTimeout(TimeUnit.SECONDS.toMillis(
                Integer.parseInt(params.get("checkpoint-timeout", "60"))));

        final int keysNumber = Integer.parseInt(params.get("keys", "65536"));
        final int stateSize = (int) (1024L * 1024L * 1024L * Long.parseLong(params.get("total-state-size", "12")) / keysNumber);

        // execute program
        env
                .addSource(new FromSplittableIteratorFunction<>(new RandomSplittableIterator(keysNumber)), Types.INT())
                .keyBy(i -> i) // Use the int as key
                .process(new StateAllocatorFunction(stateSize), Types.INT())
                .addSink(new DiscardingSink<>());

        env.execute("Very heavy job");
    }


    public static class StateAllocatorFunction
            extends KeyedProcessFunction<Integer, Integer, Integer> {

        private final int stateSize;
        private final Random random;

        private ValueState<byte[]> state;

        public StateAllocatorFunction(int stateSize) {
            this.stateSize = stateSize;
            this.random = new Random();
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ValueStateDescriptor<byte[]> desc = new ValueStateDescriptor<>("valueState", byte[].class);
            state = getRuntimeContext().getState(desc);
        }

        @Override
        public void processElement(Integer value, Context ctx, Collector<Integer> out)
                throws Exception {
            // Update the state
            byte[] newState = new byte[stateSize];
            random.nextBytes(newState);
            state.update(newState);

            out.collect(random.nextInt() + value);
        }
    }
}
