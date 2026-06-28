package com.shopbee.security;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "shopbee.security")
public interface ShopbeeSecurityConfig {
    String authServerUrl();

    String issuer();

    String clientId();

    @WithDefault("none")
    String tlsVerification();

    @WithDefault("true")
    boolean trustAll();
}
