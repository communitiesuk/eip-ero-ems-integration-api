#!/bin/bash

echo "Stopping docker containers but not removing them to maintain their state"
docker compose stop
