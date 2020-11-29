#!/bin/sh

kubectl create configmap currency-conversion-api --from-file=currency-conversion-api/currency-conversion-api.yaml
kubectl create configmap employee-api --from-file=employee-api/employee-api.yaml