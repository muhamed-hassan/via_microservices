package com.practice.application.employee;

import static com.practice.application.shared.ExceptionMessages.ENTITY_NOT_FOUND;


public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException() {
        super(ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(Throwable cause) {
        super(ENTITY_NOT_FOUND, cause);
    }

}
