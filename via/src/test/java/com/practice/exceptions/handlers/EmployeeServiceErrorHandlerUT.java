package com.practice.exceptions.handlers;

import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL;
import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER;
import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME;
import static com.practice.persistence.entities.RateAlertEntity.Constraints.RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL;
import static com.practice.configs.constants.Messages.EMAIL_ALREADY_EXIST;
import static com.practice.configs.constants.Messages.PHONE_NUMBER_ALREADY_EXIST;
import static com.practice.configs.constants.Messages.USER_NAME_ALREADY_EXIST;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.dao.DataIntegrityViolationException;

import com.practice.exceptions.DbConstraintViolationException;

public class EmployeeServiceErrorHandlerUT {

    @ParameterizedTest
    @CsvSource({ "..." + EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME + "..." + "," + USER_NAME_ALREADY_EXIST,
                    "..." + EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL + "..." + "," + EMAIL_ALREADY_EXIST,
                    "..." + EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER + "..." + "," + PHONE_NUMBER_ALREADY_EXIST,
                    "..." + RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL + "..." + "," + EMAIL_ALREADY_EXIST })
    public void testHandleDataIntegrityViolationException_WhenDataIntegrityViolationExceptionIsUsed_ThenThrowDbConstraintViolationException(
                    String violatedConstraint, String errorMsg) {
        EmployeeServiceErrorHandler employeeServiceErrorHandler = new EmployeeServiceErrorHandler();
        DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(violatedConstraint);

        DbConstraintViolationException thrown = assertThrows(DbConstraintViolationException.class,
                                        () -> employeeServiceErrorHandler.handleDataIntegrityViolationException(dataIntegrityViolationException));
        assertTrue(thrown.getMessage().equals(errorMsg));
    }

}
