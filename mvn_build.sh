#!/bin/sh

mvn clean package -DskipTests -DskipITs -f ./service_registry/pom.xml && \
    mvn clean package -DskipTests -DskipITs -f ./config_server/pom.xml && \
    mvn clean package -DskipTests -DskipITs -f ./api_gateway/pom.xml && \
    mvn clean package -DskipTests -DskipITs -f ./currency_conversion_api/pom.xml && \
    mvn clean package -DskipTests -DskipITs -f ./employee_api/pom.xml 