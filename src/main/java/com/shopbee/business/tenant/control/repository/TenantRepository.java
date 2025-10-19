/*
 * TenantRepository.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.tenant.control.repository;

import com.shopbee.business.tenant.entity.Tenant;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantRepository implements PanacheRepositoryBase<Tenant, String> {

    /**
     * Find by name tenant.
     *
     * @param name the name
     * @return the tenant
     */
    @CacheResult(cacheName = "tenants")
    public Tenant findByName(String name) {
        return find("name", name).firstResult();
    }
}
