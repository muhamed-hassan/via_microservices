package com.practice.web.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = FieldCriteriaRuleValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldCriteriaRule {

    String message() default "invalid criteria allowed criteria are id, email and username in the form of fieldName:validValue";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
