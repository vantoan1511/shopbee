package com.shopbee.security.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.json.JsonString;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
public class TokenPermissionExtractor {

    private final JsonWebToken jwt;

    public TokenPermissionExtractor(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    public Set<Permission> getPermissions() {
        List<JsonString> claims = jwt.getClaim("permissions");
        return CollectionUtils.emptyIfNull(claims).stream()
                .map(JsonString::getString)
                .map(Permission::fromValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
