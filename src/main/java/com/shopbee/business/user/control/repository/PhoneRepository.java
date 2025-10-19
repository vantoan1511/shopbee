/*
 * PhoneRepository.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.user.control.repository;

import com.shopbee.business.user.entity.Phone;
import com.shopbee.business.user.entity.PhoneId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for performing CRUD operations on Phone entities.
 */
@ApplicationScoped
public class PhoneRepository implements PanacheRepositoryBase<Phone, PhoneId> {

    /**
     * Checks if a phone exists by its ID.
     *
     * @param phoneId the phone ID
     * @return true if the phone exists, false otherwise
     */
    public boolean existedById(PhoneId phoneId) {
        return findByIdOptional(phoneId).isPresent();
    }

    /**
     * Checks if a phone exists by its ID, excluding a specific user ID.
     *
     * @param phoneId the phone ID
     * @param userId the user ID to exclude
     * @return true if the phone exists and is not associated with the specified user ID, false otherwise
     */
    public boolean existedByIdExcludedByUserId(PhoneId phoneId, String userId) {
        return find("user.id != ?1 and id = ?2", userId, phoneId).count() > 0;
    }

}