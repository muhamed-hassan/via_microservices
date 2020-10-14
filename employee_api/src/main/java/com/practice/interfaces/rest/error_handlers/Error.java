package com.practice.interfaces.rest.error_handlers;

import java.time.LocalDateTime;

public class Error {

    private final String error;

    private final LocalDateTime occurredOn;

    public Error(String error) {
        this.error = error;
        this.occurredOn = LocalDateTime.now();
    }

    public String getError() {
        return error;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

}
