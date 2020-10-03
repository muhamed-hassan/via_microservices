package com.practice.application.employee;

public class NoResultException extends RuntimeException {

    private static final String NO_DATA_FOUND = "no data found";

    public NoResultException() {
        super(NO_DATA_FOUND);
    }

}
