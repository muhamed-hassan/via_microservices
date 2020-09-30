package com.practice.interfaces.rest.validators;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CurrencyCodeRuleValidator implements ConstraintValidator<CurrencyCodeRule, String> {

    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("[A-Za-z]{3}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && CURRENCY_CODE_PATTERN.matcher(value).matches();
    }

}
