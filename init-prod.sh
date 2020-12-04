#!/bin/sh

# mvn clean package -DskipTests -DskipITs -f ./service_registry/pom.xml && \
#    mvn clean package -DskipTests -DskipITs -f ./config_server/pom.xml && \
#    mvn clean package -DskipTests -DskipITs -f ./api_gateway/pom.xml && \

mvn clean package -DskipTests -DskipITs -f ./currency_conversion_api/pom.xml && \
    mvn clean package -DskipTests -DskipITs -f ./employee_api/pom.xml 

# build docker images as well

cd currency_conversion_api
docker build -t currency-conversion-api:1 .
cd ..

cd employee_api
docker build -t employee-api:1 .
cd ..

cd smtp_server
docker build -t smtp-server:1 .
cd ..

kubectl create configmap currency-conversion-api --from-file=configs/currency-conversion-api/currency-conversion-api.yaml
kubectl create configmap employee-api --from-file=configs/employee-api/employee-api.yaml
#kubectl create configmap employee-api-db-configmap --from-file=configs/employee-api/employee-api-db-configs.yaml


kubectl apply -f k8s_deployments/prod/

