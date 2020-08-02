package com.practice.exceptions;

import static com.practice.configs.constants.ExceptionMessages.SERVICE_NOT_AVAILABLE;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException() {
        super(SERVICE_NOT_AVAILABLE);
    }

}
