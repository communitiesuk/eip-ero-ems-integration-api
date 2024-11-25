#!/bin/bash


function get_localstack_container_state {
  if [[ $(docker ps -f "name=^/localstack$" -q -a) == "" ]]; then
    echo "false"
  elif [[ $(docker ps -f "name=^/localstack$" -f "status=exited" -q) != "" ]]; then
    echo "stopped"
  elif [[ $(docker ps -f "name=^/localstack$" -f "status=running" -q) != "" ]]; then
    echo "running"
  else
    echo "unknown"
  fi
}

function start_whole_environment {
  # Start the localstack container only first as we need to create the user pools and user pool clients from it once started
  echo "Starting localstack"
  docker compose up -d localstack
  printf "\nWaiting for localstack API within the started localstack container to be up ."
  while true; do
    # Issue a simple cognito query - if the localstack API is up and operational it will return a 0 status code, else we sleep and try again
    docker exec -it localstack awslocal sqs list-queues --max-results 10 2>&1 > /dev/null
    if [ $? -eq 0 ]; then
      printf "\n"
      break
    fi
    printf "."
    sleep 1
  done

  echo "Creating SQS queues"
  docker exec -it localstack awslocal sqs create-queue --queue-name "ems-proxy-application" 2>&1 > /dev/null
  docker exec -it localstack awslocal sqs create-queue --queue-name "ems-postal-application" 2>&1 > /dev/null
  docker exec -it localstack awslocal sqs create-queue --queue-name "delete-proxy-application" 2>&1 > /dev/null
  docker exec -it localstack awslocal sqs create-queue --queue-name "delete-postal-application" 2>&1 > /dev/null

  echo "Starting remaining containers"
  docker compose up -d nginx ems-integration wiremock
}

function restart_environment {
  docker compose start
  docker exec -it localstack awslocal sqs list-queues --max-results 10 2>&1 > /dev/null
}

localstack_state="$(get_localstack_container_state)"
echo "localstack_state is $localstack_state"
if [[ "$localstack_state" == "false" ]]; then
  start_whole_environment
elif [[ "$localstack_state" == "stopped" ]]; then
  restart_environment
elif [[ "$localstack_state" == "running" ]]; then
  echo "The docker environment is already running. Use './stop-docker.sh' to stop it; or 'docker compose down' to remove it altogether"
else
  echo "The docker environment containing localstack is in an unknown state. Get your docker foo on and sort it manually!"
fi

exit

