package com.practice.integration.constants;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

public final class Responses {

    private Responses() {}

    private static final ResponseEntity<String> EMPTY_RESPONSE = ResponseEntity.of(Optional.of(StringUtils.EMPTY));

    public static ResponseEntity<String> getEmptyResponse() {
        return EMPTY_RESPONSE;
    }

}
