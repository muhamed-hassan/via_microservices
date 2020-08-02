package com.practice.exceptions;

import static com.practice.configs.constants.ExceptionMessages.NO_DATA_FOUND;

public class NoResultException extends RuntimeException {

    public NoResultException() {
        super(NO_DATA_FOUND);
    }

}
