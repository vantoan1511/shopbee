package com.shopbee.initialization.service;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "shopbee.platform.initialization")
public interface InitializationConfig {

    @WithDefault("false")
    boolean keycloak();

    String adminSecret();
}
