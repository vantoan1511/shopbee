/*
 * UsersRepository.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.repository;

import com.shopbee.user.entity.Address;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * The type Address repository.
 * Provides methods to perform CRUD operations on Address entities.
 */
@ApplicationScoped
public class AddressRepository implements PanacheRepositoryBase<Address, String> {

    /**
     * Finds a list of addresses by user ID with pagination.
     *
     * @param userId    the ID of the user
     * @param pageIndex the starting point of the list
     * @param pageLimit the maximum number of addresses to return
     * @return the list of addresses for the specified user
     */
    public List<Address> findByUserId(String tenantId, String userId, int pageIndex, int pageLimit) {
        return find("tenantId = ?1 AND user.id = ?2", tenantId, userId).page(pageIndex, pageLimit).list();
    }

    /**
     * Finds an address by its ID and user ID.
     *
     * @param userId    the ID of the user
     * @param addressId the ID of the address
     * @return the address with the specified ID and user ID, or null if not found
     */
    public Address findByIdAndUserId(String tenantId, String userId, String addressId) {
        return find("tenantId = ?1 AND id = ?2 and user.id = ?3", tenantId, addressId, userId).firstResult();
    }

    public void deleteByIdAndUserId(String tenantId, String userId, String addressId) {
        delete("tenantId = ?1 AND id = ?2 and user.id = ?3", tenantId, addressId, userId);
    }

}