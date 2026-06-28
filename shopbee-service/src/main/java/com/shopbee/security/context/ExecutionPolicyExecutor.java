package com.shopbee.security.context;

import jakarta.enterprise.inject.spi.CDI;

import java.util.function.Supplier;

public final class ExecutionPolicyExecutor {

    private final ExecutionPolicyContext context;

    private ExecutionPolicyExecutor() {
        this.context = CDI.current().select(ExecutionPolicyContext.class).get();
    }

    private static class HOLDER {
        private static ExecutionPolicyExecutor INSTANCE;

        public static ExecutionPolicyExecutor getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new ExecutionPolicyExecutor();
            }
            return INSTANCE;
        }
    }

    public static ExecutionPolicyExecutor getInstance() {
        return HOLDER.getInstance();
    }

    public <T> T runAsSystem(Supplier<T> action) {
        return execute(ExecutionPolicy.SYSTEM_CONTEXT, action);
    }

    public void runAsSystem(Runnable action) {
        execute(ExecutionPolicy.SYSTEM_CONTEXT, () -> {
            action.run();
            return null;
        });
    }

    private <T> T execute(ExecutionPolicy policy, Supplier<T> action) {
        ExecutionPolicy previous = context.current();
        try {
            context.set(policy);
            return action.get();
        } finally {
            context.set(previous);
        }
    }
}
