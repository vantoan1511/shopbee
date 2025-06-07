/*
 * UsersRepository.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.repository;

import com.shopbee.user.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * The type Users repository.
 */
@ApplicationScoped
public class UsersRepository implements PanacheRepositoryBase<User, String> {

    /**
     * Find all by tenantId.
     *
     * @param tenantId the tenant id
     * @return the panache query
     */
    public PanacheQuery<User> findAll(String tenantId) {
        return find("tenantId", tenantId);
    }

    /**
     * Find by id optional.
     *
     * @param tenantId the tenant id
     * @param id       the id
     * @return the optional
     */
    public User findById(String tenantId, String id) {
        return find("tenantId = ?1 AND id = ?2", tenantId, id).firstResult();
    }

    /**
     * Existed by email excluded by id boolean.
     *
     * @param tenantId the tenant id
     * @param email    the email
     * @param id       the id
     * @return the boolean
     */
    public boolean existedByEmailExcludedById(String tenantId, String email, String id) {
        return count("tenantId = ?1 AND email = ?2 AND id != ?3", tenantId, email, id) > 0;
    }

    /**
     * Existed by id boolean.
     *
     * @param id the id
     * @return the boolean
     */
    public boolean existedById(String id) {
        return count("id", id) > 0;
    }

    /**
     * Existed by username boolean.
     *
     * @param tenantId the tenant id
     * @param username the username
     * @return the boolean
     */
    public boolean existedByUsername(String tenantId, String username) {
        return count("tenantId = ?1 AND username = ?2", tenantId, username) > 0;
    }

    /**
     * Existed by email boolean.
     *
     * @param tenantId the tenant id
     * @param email    the email
     * @return the boolean
     */
    public boolean existedByEmail(String tenantId, String email) {
        return count("tenantId = ?1 AND email = ?2", tenantId, email) > 0;
    }

}
