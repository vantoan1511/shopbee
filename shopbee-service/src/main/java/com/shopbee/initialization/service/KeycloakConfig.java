package com.shopbee.initialization.service;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

@ConfigMapping(prefix = "shopbee.keycloak")
public interface KeycloakConfig {

    @WithDefault("shopbee")
    String defaultRealm();

    Optional<String> url();

    Admin admin();

    Clients clients();

    interface Admin {
        @WithDefault("admin")
        String username();

        @WithDefault("admin")
        String password();
    }

    interface Clients {
        Client applicationAdministration();

        Client businessAdministration();
    }

    interface Client {
        String secret();
    }
}
