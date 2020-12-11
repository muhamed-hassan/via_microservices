#!/bin/sh

kubectl delete deployments employee-api smtp-server employee-api-db currency-conversion-api

kubectl delete services employee-api smtp-server employee-api-db currency-conversion-api

kubectl delete servicemonitors employee-api-monitor currency-conversion-api-monitor

kubectl delete configmaps employee-api currency-conversion-api
