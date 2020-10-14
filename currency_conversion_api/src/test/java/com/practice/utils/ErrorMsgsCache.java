package com.practice.utils;

import java.io.InputStream;
import java.util.Properties;

public final class ErrorMsgsCache {

    private static Properties properties = new Properties();

    static {
        try (InputStream inputStream =
                ClassLoader.getSystemResourceAsStream("responses/internal_api/errors.properties")) {
            properties.load(inputStream);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ErrorMsgsCache() {}

    public static String getMessage(String key) {
        return properties.getProperty(key);
    }

}
