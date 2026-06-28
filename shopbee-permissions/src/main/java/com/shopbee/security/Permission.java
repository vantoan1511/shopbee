package com.shopbee.security;

import java.util.Set;

public enum Permission {
    IDENTITY_READ("identity.read"),
    IDENTITY_CREATE("identity.create"),
    IDENTITY_UPDATE("identity.update"),
    IDENTITY_DELETE("identity.delete"),
    IDENTITY_MANAGE("identity.manage", Set.of(IDENTITY_READ, IDENTITY_CREATE, IDENTITY_UPDATE, IDENTITY_DELETE)),

    TENANT_READ("tenant.read"),
    TENANT_CREATE("tenant.create"),
    TENANT_UPDATE("tenant.update"),
    TENANT_DELETE("tenant.delete"),
    TENANT_MANAGE("tenant.manage", Set.of(TENANT_READ, TENANT_CREATE, TENANT_UPDATE, TENANT_DELETE)),

    PRODUCT_READ("product.read"),
    PRODUCT_CREATE("product.create"),
    PRODUCT_UPDATE("product.update"),
    PRODUCT_DELETE("product.delete"),
    PRODUCT_MANAGE("product.manage", Set.of(PRODUCT_READ, PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_DELETE)),

    ORDER_READ("order.read"),
    ORDER_CREATE("order.create"),
    ORDER_UPDATE("order.update"),
    ORDER_DELETE("order.delete"),
    ORDER_MANAGE("order.manage", Set.of(ORDER_READ, ORDER_CREATE, ORDER_UPDATE, ORDER_DELETE)),
    ;

    private final String value;

    private final Set<Permission> compositePermissions;

    Permission(String value) {
        this.value = value;
        this.compositePermissions = Set.of();
    }

    Permission(String value, Set<Permission> compositePermissions) {
        this.value = value;
        this.compositePermissions = compositePermissions;
    }

    public static Permission fromValue(String value) {
        for (Permission permission : values()) {
            if (permission.getValue().equalsIgnoreCase(value)) {
                return permission;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public Set<Permission> getCompositePermissions() {
        return compositePermissions;
    }
}
