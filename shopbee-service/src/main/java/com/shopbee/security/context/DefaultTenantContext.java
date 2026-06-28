package com.shopbee.security.context;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class DefaultTenantContext implements TenantContext {

    private final TenantContextHolder holder;

    public DefaultTenantContext(TenantContextHolder holder) {
        this.holder = holder;
    }

    @Override
    public String getTenantId() {
        return holder.get();
    }

    @Override
    public Optional<String> findTenantId() {
        return holder.find();
    }
}
