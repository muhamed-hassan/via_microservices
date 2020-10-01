package com.practice.application.ratealert;

import static com.practice.application.shared.ExceptionMessages.SERVICE_NOT_AVAILABLE;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException() {
        super(SERVICE_NOT_AVAILABLE);
    }

}
