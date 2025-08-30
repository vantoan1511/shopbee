/*
 * UsersRepository.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.repository;

import com.shopbee.user.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, String> {

    public List<User> findAll(String tenantId, int page, int size) {
        return find("tenantId", tenantId).page(page, size).list();
    }

    public User findById(String tenantId, String id) {
        return find("tenantId = ?1 AND id = ?2", tenantId, id).firstResult();
    }

    public User findByIdAndEmail(String tenantId, String id, String email) {
        return find("tenantId = ?1 AND id = ?2 AND email = ?3", tenantId, id, email).firstResult();
    }

    public User findByUsername(String tenantId, String username) {
        return find("tenantId = ?1 AND username = ?2", tenantId, username).firstResult();
    }

    public User findByEmail(String tenantId, String email) {
        return find("tenantId = ?1 AND email = ?2", tenantId, email).firstResult();
    }

}
