package com.shopbee.security.context;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class TenantContextHolder {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    String get() {
        String tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException("TenantContext not initialized");
        }
        return tenantId;
    }

    Optional<String> find() {
        return Optional.ofNullable(CURRENT.get());
    }

    void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    void clear() {
        CURRENT.remove();
    }
}
