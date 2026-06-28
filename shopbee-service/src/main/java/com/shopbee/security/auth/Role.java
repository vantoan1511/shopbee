package com.shopbee.security.auth;

public enum Role {
    BUSINESS_ADMINISTRATION(RoleName.BUSINESS_ADMINISTRATION),
    APPLICATION_ADMINISTRATION(RoleName.APPLICATION_ADMINISTRATION);

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
