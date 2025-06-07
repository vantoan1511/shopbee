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
     * @param userId the ID of the user
     * @param offset the starting point of the list
     * @param limit  the maximum number of addresses to return
     * @return the list of addresses for the specified user
     */
    public List<Address> findByUserId(String tenantId, String userId, int offset, int limit) {
        return find("tenantId = ?1 AND user.id = ?2", tenantId, userId).page(offset, limit).list();
    }

    /**
     * Finds an address by its ID and user ID.
     *
     * @param id     the ID of the address
     * @param userId the ID of the user
     * @return the address with the specified ID and user ID, or null if not found
     */
    public Address findByIdAndUserId(String tenantId, String id, String userId) {
        return find("tenantId = ?1 AND id = ?2 and user.id = ?3", tenantId, id, userId).firstResult();
    }

}