/*
 * ViolationError.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception.dto;

public class ValidationError extends Error {

    private String property;
    private String value;

    public ValidationError() {
        super();
    }

    public ValidationError(String property, String value, String message) {
        super(message);
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
