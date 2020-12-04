#!/bin/sh

kubectl create configmap currency-conversion-api-dev --from-file=currency-conversion-api/currency-conversion-api-dev.yaml
kubectl create configmap employee-api-dev --from-file=employee-api/employee-api-dev.yaml