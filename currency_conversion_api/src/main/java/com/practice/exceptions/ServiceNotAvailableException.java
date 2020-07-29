package com.practice.exceptions;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException() {
        super("service not available");
    }

}
