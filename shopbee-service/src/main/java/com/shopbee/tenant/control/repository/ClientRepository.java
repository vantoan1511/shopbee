/*
 * TenantRepository.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.tenant.control.repository;

import com.shopbee.tenant.entity.Client;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ClientRepository implements PanacheRepositoryBase<Client, String> {

    public List<Client> findByTenantName(String tenantName, int page, int size) {
        return find("tenant.name = ?1", tenantName).page(page, size).list();
    }

    public Client findByIdAndTenantName(String id, String tenantName) {
        return find("id = ?1 and tenant.name = ?2", id, tenantName).firstResult();
    }

    public boolean existsByNameAndTenantName(String name, String tenantName) {
        return count("name = ?1 and tenant.name = ?2", name, tenantName) > 0;
    }

    public boolean existsByNameAndTenantNameExcludingId(String clientName, String tenantName, String clientId) {
        return count("name = ?1 and tenant.name = ?2 and id <> ?3", clientName, tenantName, clientId) > 0;
    }
}
