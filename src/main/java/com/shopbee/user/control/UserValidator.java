/*
 * UserValidator.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.user.control.repository.UserRepository;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.UpdateUserByIdRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserValidator {

    private final UserRepository userRepository;

    @Inject
    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateCreateUserRequest(String tenantId, CreateUserRequest createUserRequest) {
        validateUsernameExists(tenantId, createUserRequest.getUsername());
        validateEmailExists(tenantId, createUserRequest.getEmail());
    }

    public void validateUpdateUserRequest(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
    }

    private void validateEmailExists(String tenantId, String email) {
        if (userRepository.countByEmail(tenantId, email) > 0) {
            throw ApiServiceException.conflict("Email [{}] already exists", email);
        }
    }

    private void validateUsernameExists(String tenantId, String username) {
        if (userRepository.countByUsername(tenantId, username) > 0) {
            throw ApiServiceException.conflict("Username [{}] already exists", username);
        }
    }

}
