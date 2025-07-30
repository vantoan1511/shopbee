package com.shopbee.user.control.service.impl;

import com.shopbee.user.control.exception.UserServiceException;
import com.shopbee.user.control.repository.UsersRepository;
import com.shopbee.user.control.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * UserServiceImplTest.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */
@QuarkusTest
class UserServiceImplTest {

    @Inject
    private UserService userService;

    @Inject
    private UsersRepository usersRepository;

    @Test
    void getUsers_tenantIdEmpty_throwException() {
        assertThrows(UserServiceException.class, () -> userService.getUsers(null, null, null));
        assertThrows(UserServiceException.class, () -> userService.getUsers("", null, null));
    }

    @Test
    void getUserById() {
    }

    @Test
    void createUser() {
    }

    @Test
    void updateUserById() {
    }

    @Test
    void patchUserById() {
    }

    @Test
    void deleteUserById() {
    }

    @Test
    void getUserAddresses() {
    }

    @Test
    void createUserAddress() {
    }

    @Test
    void updateUserAddress() {
    }

    @Test
    void patchUserAddress() {
    }

    @Test
    void deleteUserAddress() {
    }
}