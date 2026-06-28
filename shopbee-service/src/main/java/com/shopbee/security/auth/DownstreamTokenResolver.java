package com.shopbee.security.auth;

import com.shopbee.security.context.ExecutionPolicyContext;
import com.shopbee.security.context.UserSecurityContext;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DownstreamTokenResolver {

    private final ExecutionPolicyContext executionPolicyContext;
    private final UserSecurityContext userSecurityContext;
    private final TokenProvider tokenProvider;

    public DownstreamTokenResolver(ExecutionPolicyContext executionPolicyContext,
                                   UserSecurityContext userSecurityContext,
                                   TokenProvider tokenProvider) {
        this.executionPolicyContext = executionPolicyContext;
        this.userSecurityContext = userSecurityContext;
        this.tokenProvider = tokenProvider;
    }

    public String resolve() {
        return switch (executionPolicyContext.current()) {
            case USER_CONTEXT ->
                    userSecurityContext.accessToken().orElseThrow(() -> new SecurityException("No user access token available"));
            case SYSTEM_CONTEXT -> tokenProvider.getBusinessAdministrationToken();
        };
    }
}
