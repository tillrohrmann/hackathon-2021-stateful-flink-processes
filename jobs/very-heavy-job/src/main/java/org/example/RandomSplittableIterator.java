package org.example;

import org.apache.flink.util.SplittableIterator;

import java.util.Iterator;
import java.util.Random;

public class RandomSplittableIterator extends SplittableIterator<Integer> {

    private final int max;
    private final Random random;

    public RandomSplittableIterator(int max) {
        this.max = max;
        random = new Random();
    }

    @Override
    public Iterator<Integer>[] split(int partitions) {
        Iterator<Integer>[] arr = new Iterator[partitions];
        for (int i = 0; i < partitions; i++) {
            arr[i] = new RandomIter(max);
        }
        return arr;
    }

    @Override
    public int getMaximumNumberOfSplits() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Integer next() {
        return random.nextInt(max);
    }

    private static class RandomIter implements Iterator<Integer> {

        private final int max;
        private final Random random;

        private RandomIter(int max) {
            this.max = max;
            this.random = new Random();
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Integer next() {
            return random.nextInt(max);
        }
    }


}
