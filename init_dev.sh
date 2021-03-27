#!/bin/sh

cd currency_conversion_api
./init_currency_conversion_api_dev_profile.sh &

cd ../employee_api 
./init_employee_api_dev_profile.sh &