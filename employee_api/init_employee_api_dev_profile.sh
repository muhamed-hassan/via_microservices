#!/bin/sh

docker build -t smtp-server:1 ../smtp_server/ && \
    docker run -d -p 25:25 smtp-server:1

docker run -d -p 5432:5432 -e POSTGRES_DB=employees -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=root postgres:12.2-alpine

mvn clean package && \
    java -jar -Dspring.profiles.active=dev target/employee_api-1.0.jar

