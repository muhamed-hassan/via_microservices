package com.practice.application.shared;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.persistence.Column;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ServiceErrorHandler {

    public <T> IllegalArgumentException wrapDataIntegrityViolationException(DataIntegrityViolationException e, Class<T> entity) {
        String errorMsg = e.getMessage();
        String violatedField = Stream.of(entity.getDeclaredFields())
            .map(field -> field.getName().matches("^[a-z]+([A-Z][a-z]+)+$") ?
                Arrays.stream(field.getDeclaredAnnotationsByType(Column.class))
                    .filter(annotation -> StringUtils.isNotEmpty(annotation.name()))
                    .map(annotation -> annotation.name())
                    .findFirst().get() :
                field.getName()
            ).filter(fieldName -> errorMsg.matches(".*(_"+fieldName+"_).*"))
            .findFirst()
            .get();
        throw new IllegalArgumentException("DB constraint is violated for this field: " + violatedField.replaceAll("_", " "));
    }

}
