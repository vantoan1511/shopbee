/*
 * UserPhone.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * The type Phone.
 */
@Entity
@Table(name = "shopbee_phone", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"country_code", "number", "tenant_id"})
})
public class Phone extends AbstractEntity {

    @EmbeddedId
    private PhoneId id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public PhoneId getId() {
        return id;
    }

    public void setId(PhoneId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
