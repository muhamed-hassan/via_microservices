package com.practice.application.employee;

public class EntityNotFoundException extends RuntimeException {

    private static final String ENTITY_NOT_FOUND = "entity not found";

    public EntityNotFoundException() {
        super(ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(Throwable cause) {
        super(ENTITY_NOT_FOUND, cause);
    }

}
