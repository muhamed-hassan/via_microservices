package com.practice.exceptions;

public class ServiceNotAvailable extends RuntimeException {

    public ServiceNotAvailable() {
        super("service not available");
    }

}
