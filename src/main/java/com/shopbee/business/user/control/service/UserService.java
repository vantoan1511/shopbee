/*
 * UserService.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.user.control.service;

import com.shopbee.user.model.AddressDTO;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import com.shopbee.user.model.PatchUserByIdRequest;
import com.shopbee.user.model.UpdateUserByIdRequest;
import com.shopbee.user.model.UserDTO;

import java.util.List;

/**
 * Service interface for managing users and their addresses.
 */
public interface UserService {

    /**
     * Retrieves a list of users with pagination.
     *
     * @param tenantId the tenant id
     * @param offset   the starting point of the list
     * @param limit    the maximum number of users to return
     * @return a list of users
     */
    List<UserDTO> getUsers(String tenantId, Integer offset, Integer limit);

    /**
     * Retrieves a user by their ID.
     *
     * @param tenantId the tenant id
     * @param userId   the ID of the user
     * @return the user with the specified ID
     */
    UserDTO getUserById(String tenantId, String userId);

    /**
     * Creates a new user.
     *
     * @param tenantId          the tenant id
     * @param createUserRequest the request object containing user details
     * @return the ID of the created user
     */
    String createUser(String tenantId, CreateUserRequest createUserRequest);

    /**
     * Updates a user by their ID.
     *
     * @param tenantId              the tenant id
     * @param userId                the ID of the user to update
     * @param updateUserByIdRequest the request object containing updated user details
     */
    void updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest);

    /**
     * Partially updates a user by their ID.
     *
     * @param tenantId             the tenant id
     * @param userId               the ID of the user to update
     * @param patchUserByIdRequest the request object containing partial user details
     */
    void patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest);

    /**
     * Deletes a user by their ID.
     *
     * @param tenantId the tenant id
     * @param userId   the ID of the user to delete
     */
    void deleteUserById(String tenantId, String userId);

    /**
     * Retrieves a list of addresses for a user with pagination.
     *
     * @param tenantId the tenant id
     * @param userId   the ID of the user
     * @param offset   the starting point of the list
     * @param limit    the maximum number of addresses to return
     * @return a list of addresses for the user
     */
    List<AddressDTO> getUserAddresses(String tenantId, String userId, Integer offset, Integer limit);

    /**
     * Creates a new address for a user.
     *
     * @param tenantId                 the tenant id
     * @param userId                   the ID of the user
     * @param createUserAddressRequest the request object containing address details
     * @return the ID of the created address
     */
    String createUserAddress(String tenantId, String userId, CreateUserAddressRequest createUserAddressRequest);

    /**
     * Updates an address for a user.
     *
     * @param tenantId                 the tenant id
     * @param userId                   the ID of the user
     * @param addressId                the ID of the address to update
     * @param createUserAddressRequest the request object containing updated address details
     */
    void updateUserAddress(String tenantId, String userId, String addressId, CreateUserAddressRequest createUserAddressRequest);

    /**
     * Partially updates an address for a user.
     *
     * @param tenantId                the tenant id
     * @param userId                  the ID of the user
     * @param addressId               the ID of the address to update
     * @param patchUserAddressRequest the request object containing partial address details
     */
    void patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest);

    /**
     * Deletes an address for a user.
     *
     * @param tenantId  the tenant id
     * @param userId    the ID of the user
     * @param addressId the ID of the address to delete
     */
    void deleteUserAddress(String tenantId, String userId, String addressId);
}
