/*
 * UsersRepository.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.user.control.repository;

import com.shopbee.business.user.entity.Address;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AddressRepository implements PanacheRepositoryBase<Address, String> {

    public List<Address> findByUserId(String tenantId, String userId) {
        return find("tenantId = ?1 AND user.id = ?2", tenantId, userId).list();
    }

    public Address findByIdAndUserId(String tenantId, String userId, String addressId) {
        return find("tenantId = ?1 AND id = ?2 and user.id = ?3", tenantId, addressId, userId).firstResult();
    }

    public void deleteByIdAndUserId(String tenantId, String userId, String addressId) {
        delete("tenantId = ?1 AND id = ?2 and user.id = ?3", tenantId, addressId, userId);
    }

}