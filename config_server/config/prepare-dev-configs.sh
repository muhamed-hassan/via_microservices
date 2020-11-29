#!/bin/sh

kubectl create configmap currency-conversion-api-dev-configs --from-file=currency-conversion-api/currency-conversion-api-dev.yaml
kubectl create configmap employee-api-dev-configs --from-file=employee-api/employee-api-dev.yaml