package com.shopbee.security.auth;

import java.util.Arrays;

public enum Permission {
    USER_VIEW("user-view"),
    USER_CREATE("user-create"),
    USER_UPDATE("user-update"),
    USER_DELETE("user-delete"),
    USER_MANAGE("user-manage"),
    PRODUCT_MANAGE("product-manage"),
    PRODUCT_VIEW("product-view"),
    PRODUCT_CREATE("product-create"),
    PRODUCT_MODIFY("product-modify");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public static Permission fromValue(String value) {
        return Arrays.stream(values()).filter(permission -> permission.getValue().equalsIgnoreCase(value)).findFirst().orElse(null);
    }

    public String getValue() {
        return value;
    }
}
