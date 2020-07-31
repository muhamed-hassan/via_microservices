package com.practice.exceptions;

public class NoResultException extends RuntimeException {

    public NoResultException() {
        super("no data found");
    }

}
