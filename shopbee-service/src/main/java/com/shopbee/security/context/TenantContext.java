package com.shopbee.security.context;

import java.util.Optional;

public interface TenantContext {

    String getTenantId();

    Optional<String> findTenantId();
}
