#!/bin/sh

ps kill -9 $(netstat -ao | grep :8580 | awk '{print $5}')

docker kill $(docker ps | grep smtp | awk '{print $1}')
docker kill $(docker ps | grep postgres | awk '{print $1}')
ps kill -9 $(netstat -ao | grep :8680 | awk '{print $5}')

