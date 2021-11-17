#!/usr/bin/bash

jid=$(curl --silent http://localhost:8081/jobs | jq -r ".jobs[0].id")
# Only 2nd vertx is important for us
vertex=$(curl --silent http://localhost:8081/jobs/$jid | jq -r ".vertices[1].id")

# RUNNING - INITIALIZING timestamps is the failover recovery time (in millis)
curl --silent http://localhost:8081/jobs/$jid/vertices/$vertex/subtasktimes | jq .
echo ""
subtasktimes=$(curl --silent http://localhost:8081/jobs/$jid/vertices/$vertex/subtasktimes | jq -c -r ".subtasks[]")
for subtask in ${subtasktimes[@]}; do
  runningts=$(echo "$subtask" | jq -r ".timestamps.RUNNING")
  initialingts=$(echo "$subtask" | jq -r ".timestamps.INITIALIZING")
  recoverytime=$((runningts-initialingts))
  subtaskhost=$(echo "$subtask" | jq -r ".host")
  echo "'$subtaskhost' recovery time: $recoverytime"
done