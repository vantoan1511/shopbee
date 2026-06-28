/*
 * PhoneRepository.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.repository;

import com.shopbee.user.entity.Phone;
import com.shopbee.user.entity.PhoneId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PhoneRepository implements PanacheRepositoryBase<Phone, PhoneId> {

    public boolean existedById(PhoneId phoneId) {
        return findByIdOptional(phoneId).isPresent();
    }

    public boolean existedByIdExcludedByUserId(PhoneId phoneId, String userId) {
        return find("user.id != ?1 and id = ?2", userId, phoneId).count() > 0;
    }

}