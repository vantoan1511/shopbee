/*
 * TestUtils.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;

public class TestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        SimpleModule module = new JavaTimeModule();
        OBJECT_MAPPER.registerModule(module);
        OBJECT_MAPPER.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
    }

    private TestUtils() {
        // Utility class, prevent instantiation
    }

    public static <T> T getMockData(String filePath, TypeReference<T> typeReference) {
        try (InputStream stream = TestUtils.class.getResourceAsStream(filePath)) {
            return new ObjectMapper().readValue(stream, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getMapper() {
        return OBJECT_MAPPER;
    }
}
