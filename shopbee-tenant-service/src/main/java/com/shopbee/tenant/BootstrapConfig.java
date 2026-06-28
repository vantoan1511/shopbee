package com.shopbee.tenant;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = "shopbee.tenants")
public interface BootstrapConfig {

    String defaultTenant();

    Defaults defaults();

    Map<String, Tenant> tenants();

    interface Defaults {
        Map<String, Client> client();
    }

    interface Tenant {
        Map<String, Client> client();
    }

    interface Client {
        String secret();
    }
}
