package com.practice.services.utils;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ServiceErrorHandler {

    public <T> IllegalArgumentException wrapDataIntegrityViolationException(DataIntegrityViolationException e, Class<T> entity) {
        String errorMsg = e.getMessage();
        String violatedField = Stream.of(entity.getDeclaredFields())
                                        .map(Field::getName)
                                        .filter(fieldName -> errorMsg.matches(".*(_"+fieldName+"_).*"))
                                        .findFirst()
                                        .get();
        throw new IllegalArgumentException("DB constraint is violated for this field: " + violatedField);
    }

}
