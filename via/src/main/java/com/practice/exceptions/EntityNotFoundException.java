package com.practice.exceptions;

import com.practice.configs.constants.ExceptionMessages;

@SuppressWarnings("serial")
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException() {
        super(ExceptionMessages.ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(Throwable cause) {
        super(ExceptionMessages.ENTITY_NOT_FOUND, cause);
    }

}
