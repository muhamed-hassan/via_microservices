package com.practice.interfaces.rest.validators;

import org.springframework.stereotype.Component;

@Component
public class FieldCriteriaValidator {

    public static final String ID = "id";

    public static final String EMAIL = "email";

    public static final String USERNAME = "username";

    public boolean isValid(String field) {
        return ID.equals(field) ||
                EMAIL.equals(field) ||
                USERNAME.equals(field);
    }

}
