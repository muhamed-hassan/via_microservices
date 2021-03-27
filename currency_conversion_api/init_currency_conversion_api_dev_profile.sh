#!/bin/sh

mvn clean package && \
    java -jar -Dspring.profiles.active=dev target/currency_conversion_api-1.0.jar

