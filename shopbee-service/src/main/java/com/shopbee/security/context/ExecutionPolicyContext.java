package com.shopbee.security.context;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExecutionPolicyContext {
    private static final ThreadLocal<ExecutionPolicy> CURRENT = ThreadLocal.withInitial(() -> ExecutionPolicy.USER_CONTEXT);

    public ExecutionPolicy current() {
        return CURRENT.get();
    }

    void set(ExecutionPolicy policy) {
        CURRENT.set(policy);
    }

    void clear() {
        CURRENT.remove();
    }
}
