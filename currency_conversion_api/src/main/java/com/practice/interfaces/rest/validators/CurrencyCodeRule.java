package com.practice.interfaces.rest.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = CurrencyCodeRuleValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrencyCodeRule {

    String message() default "currency code should be 3 letters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

