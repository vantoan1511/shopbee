package com.shopbee.security.context;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

@ApplicationScoped
public class UserSecurityContext {

    private final JsonWebToken jwt;

    public UserSecurityContext(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    public Optional<String> accessToken() {
        return Optional.ofNullable(jwt).map(JsonWebToken::getRawToken);
    }
}
