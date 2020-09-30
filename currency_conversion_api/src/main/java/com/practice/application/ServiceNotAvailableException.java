package com.practice.application;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException() {
        super("service not available");
    }

}
