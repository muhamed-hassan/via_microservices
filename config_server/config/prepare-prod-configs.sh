#!/bin/sh

kubectl create configmap currency-conversion-api-configs --from-file=currency-conversion-api/currency-conversion-api.yaml
kubectl create configmap employee-api-configs --from-file=employee-api/employee-api.yaml