#!/bin/sh

kubectl apply -f k8s_deployments/employee-api-db-deployment.yaml && \
    printf '\n>>>> Employee API DB is starting ...\n\n' && \
    echo '#######################################################################################'

docker build -t smtp-server:1 smtp_server/ && \
    kubectl apply -f k8s_deployments/smtp-server-deployment.yaml && \
    printf '\n>>>> SMTP Server is starting ...\n\n' && \
    echo '#######################################################################################'

kubectl create configmap currency-conversion-api --from-file=configs/currency-conversion-api.yaml -o yaml | kubectl apply -f - && \
    mvn clean package -DskipTests -DskipITs -f ./currency_conversion_api/pom.xml && \
    docker build -t currency-conversion-api:1 currency_conversion_api/ && \
    kubectl apply -f k8s_deployments/currency-conversion-deployment.yaml && \
    printf '\n>>>> Currency Conversion API is starting ...\n\n' && \
    echo '#######################################################################################'

kubectl create configmap employee-api --from-file=configs/employee-api.yaml -o yaml | kubectl apply -f - && \
    mvn clean package -DskipTests -DskipITs -f ./employee_api/pom.xml && \
    docker build -t employee-api:1 employee_api/ && \
    kubectl apply -f k8s_deployments/employee-api-deployment.yaml && \
    printf '\n>>>> Employee API is starting ...!\n\n' && \
    echo '#######################################################################################'
