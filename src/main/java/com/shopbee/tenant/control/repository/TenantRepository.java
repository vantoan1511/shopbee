/*
 * TenantRepository.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.tenant.control.repository;

import com.shopbee.tenant.entity.Tenant;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class TenantRepository implements PanacheRepositoryBase<Tenant, String> {

    @CacheResult(cacheName = "tenants")
    public Optional<Tenant> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
