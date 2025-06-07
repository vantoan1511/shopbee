/*
 * ViolationError.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception.dto;

public class ViolationError extends Error {

    private String field;
    private String value;

    public ViolationError() {
        super();
    }

    public ViolationError(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
