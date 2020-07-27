package com.practice.utils;

import static com.practice.utils.Mappings.BASE_MAPPINGS_DIR;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ResponseHandler {

    private ResponseHandler() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String readJsonFrom(String responseLocation)
        throws Exception {
        String content = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(BASE_MAPPINGS_DIR + responseLocation).toURI()),
            Charset.forName(StandardCharsets.UTF_8.name()))
            .stream()
            .collect(Collectors.joining());
        return OBJECT_MAPPER.readTree(content).toString();
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

}
