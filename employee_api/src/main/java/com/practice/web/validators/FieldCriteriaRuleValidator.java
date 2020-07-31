package com.practice.web.validators;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class FieldCriteriaRuleValidator implements ConstraintValidator<FieldCriteriaRule, String>  {

    private static final Pattern FIELD_CRITERIA_PATTERN = Pattern.compile("(id:[0-9]+)|"
        + "(email:[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6})|"
        + "(username:[A-Za-z_]+)");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && FIELD_CRITERIA_PATTERN.matcher(value).matches();
    }

}
