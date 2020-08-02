package com.practice.exceptions;

import static com.practice.configs.constants.ExceptionMessages.ENTITY_NOT_FOUND;


public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException() {
        super(ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(Throwable cause) {
        super(ENTITY_NOT_FOUND, cause);
    }

}
