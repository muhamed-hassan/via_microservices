package com.practice.exceptions.handlers;

import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME;
import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL;
import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER;
import static com.practice.persistence.entities.RateAlertEntity.Constraints.RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.practice.exceptions.DbConstraintViolationException;
import com.practice.configs.constants.Messages;

@Component
public class EmployeeServiceErrorHandler {

    public void handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String exceptionMessage = e.getMostSpecificCause().getMessage();
        String errorMsg = null;
        if (exceptionMessage != null) {
            String lowerCaseExceptionMessage = exceptionMessage.toLowerCase();
            if (lowerCaseExceptionMessage.contains(EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME)) {
                errorMsg = Messages.USER_NAME_ALREADY_EXIST;
            } else if (lowerCaseExceptionMessage.contains(EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL)
                        || lowerCaseExceptionMessage.contains(RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL)) {
                errorMsg = Messages.EMAIL_ALREADY_EXIST;
            } else if (lowerCaseExceptionMessage.contains(EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER)) {
                errorMsg = Messages.PHONE_NUMBER_ALREADY_EXIST;
            }
        }
        throw new DbConstraintViolationException(errorMsg, e);
    }

}
