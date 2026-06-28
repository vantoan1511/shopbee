/*
 * TenantRepository.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.tenant.control.repository;

import com.shopbee.tenant.entity.Tenant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TenantRepository implements PanacheRepositoryBase<Tenant, String> {

    public List<Tenant> findAllTenants(int page, int size) {
        return findAll().page(page, size).list();
    }

    public Tenant findByName(String name) {
        return find("name", name).firstResult();
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }
}
