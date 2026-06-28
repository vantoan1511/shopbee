package com.shopbee.security.context;

import jakarta.enterprise.inject.spi.CDI;

import java.util.function.Supplier;

public class TenantContextExecutor {

    private final TenantContextHolder holder;

    private TenantContextExecutor() {
        this.holder = CDI.current().select(TenantContextHolder.class).get();
    }

    private static class HOLDER {
        private static TenantContextExecutor INSTANCE;

        public static TenantContextExecutor getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new TenantContextExecutor();
            }
            return INSTANCE;
        }
    }

    public static TenantContextExecutor getInstance() {
        return HOLDER.getInstance();
    }

    public void runWithTenant(String tenantId, Runnable action) {
        runWithTenant(tenantId, () -> {
            action.run();
            return null;
        });
    }

    public <T> T runWithTenant(String tenantId, Supplier<T> action) {
        String previous = holder.find().orElse(null);
        try {
            holder.set(validate(tenantId));
            return action.get();
        } finally {
            if (previous == null) {
                holder.clear();
            } else {
                holder.set(previous);
            }
        }
    }

    private static String validate(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Invalid tenantId");
        }
        return tenantId;
    }
}
