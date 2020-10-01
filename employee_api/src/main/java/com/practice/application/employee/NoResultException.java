package com.practice.application.employee;

import static com.practice.application.shared.ExceptionMessages.NO_DATA_FOUND;

public class NoResultException extends RuntimeException {

    public NoResultException() {
        super(NO_DATA_FOUND);
    }

}
