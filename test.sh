#!/bin/sh

# `kubectl get configmap currency-conversion-api-config | grep 'Error'` && echo $?
# "$STR" == *"$SUB"* 
configMapNotFound=`kubectl get configmap currency-conversion-api-configs | grep 'Error'`
if [[ $configMapNotFound = *Error* ]]; 
    then echo 'Error';
else echo 'OK';
fi


# c=`kubectl get configmap currency-conversion-api-config | grep 'Error'`

# trap 

# `kubectl get configmap currency-conversion-api-config | grep 'Error'` || `Error`