#!/bin/bash

APPLICATION_ID=$(uuidgen | sed 's/-//g' | cut -c1-24)
ELECTOR_ID=$(uuidgen)
MESSAGE_BODY=$(cat ./sqs/proxy-application.json | sed "s/%%APPLICATION_ID%%/$APPLICATION_ID/g" | sed "s/%%ELECTOR_ID%%/$ELECTOR_ID/g")
echo "Sending proxy application message with application id $APPLICATION_ID"
docker exec -ti localstack awslocal sqs send-message --queue-url http://localhost:4566/000000000000/ems-proxy-application --message-body "$MESSAGE_BODY"
