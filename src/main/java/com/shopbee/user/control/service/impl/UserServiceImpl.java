/*
 * UsersServiceImpl.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.service.impl;

import com.shopbee.user.control.exception.UserServiceException;
import com.shopbee.user.control.mapper.AddressMapper;
import com.shopbee.user.control.mapper.UserMapper;
import com.shopbee.user.control.repository.AddressRepository;
import com.shopbee.user.control.repository.UsersRepository;
import com.shopbee.user.control.service.UserService;
import com.shopbee.user.model.Address;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import com.shopbee.user.model.PatchUserByIdRequest;
import com.shopbee.user.model.UpdateUserByIdRequest;
import com.shopbee.user.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Users service.
 */
@ApplicationScoped
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_PAGE_INDEX = 0;

    private final UsersRepository usersRepository;
    private final AddressRepository addressRepository;

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    /**
     * Instantiates a new User service.
     *
     * @param usersRepository   the users repository
     * @param addressRepository the address repository
     * @param userMapper        the user mapper
     * @param addressMapper     the address mapper
     */
    @Inject
    public UserServiceImpl(UsersRepository usersRepository,
                           AddressRepository addressRepository,
                           UserMapper userMapper,
                           AddressMapper addressMapper) {
        this.usersRepository = usersRepository;
        this.addressRepository = addressRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
    }

    @Override
    public List<User> getUsers(String tenantId, Integer offset, Integer limit) {
        List<com.shopbee.user.entity.User> users = usersRepository.findAll(tenantId)
                .page(getPageIndex(offset), getPageSize(limit))
                .list();

        return userMapper.toUsers(users);
    }

    @Override
    public User getUserById(String tenantId, String userId) {
        return Optional.ofNullable(usersRepository.findById(tenantId, userId))
                .map(userMapper::toUser)
                .orElseThrow(this::getUserNotFound);
    }

    @Override
    @Transactional
    public String createUser(String tenantId, CreateUserRequest createUserRequest) {
        com.shopbee.user.entity.User user = userMapper.toUser(tenantId, createUserRequest);

        validateCreateUsername(tenantId, user.getUsername());
        validateCreateEmail(tenantId, user.getEmail());

        usersRepository.persist(user);

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        validateUpdateEmail(tenantId, userId, updateUserByIdRequest.getEmail());

        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to update non - existing user [{}]", userId);
            throw getUserNotFound();
        }

        userMapper.updateUser(updateUserByIdRequest, user);
    }

    @Override
    @Transactional
    public void patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
        validateUpdateEmail(tenantId, userId, patchUserByIdRequest.getEmail());

        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to patch non - existing user [{}]", userId);
            throw getUserNotFound();
        }

        userMapper.patchUser(patchUserByIdRequest, user);
    }

    @Override
    @Transactional
    public void deleteUserById(String tenantId, String userId) {
        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to delete non - existing user [{}]", userId);
            throw getUserNotFound();
        }

        usersRepository.delete(user);
    }

    @Override
    public List<Address> getUserAddresses(String tenantId, String userId, Integer offset, Integer limit) {
        int pageIndex = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
        int pageSize = Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);
        return addressMapper.toAddresses(addressRepository.findByUserId(tenantId, userId, pageIndex, pageSize));
    }

    @Override
    @Transactional
    public String createUserAddress(String tenantId, String userId, CreateUserAddressRequest createUserAddressRequest) {
        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to create address for non - existing user [{}]", userId);
            throw getUserNotFound();
        }

        com.shopbee.user.entity.Address address = addressMapper.toAddress(tenantId, createUserAddressRequest);

        user.addAddress(address);

        addressRepository.persist(address);

        return address.getId();
    }

    @Override
    @Transactional
    public void updateUserAddress(String tenantId, String userId, String addressId, CreateUserAddressRequest createUserAddressRequest) {
        com.shopbee.user.entity.Address address = addressRepository.findByIdAndUserId(tenantId, addressId, userId);

        if (Objects.isNull(address)) {
            LOG.warn("Attempts to update user address with non - existing address [{}]", addressId);
            throw UserServiceException.notFound("Address not found");
        }

        addressMapper.updateAddress(createUserAddressRequest, address);
    }

    @Override
    @Transactional
    public void patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest) {
        com.shopbee.user.entity.Address address = addressRepository.findByIdAndUserId(tenantId, addressId, userId);

        if (Objects.isNull(address)) {
            LOG.warn("Attempts to patch user address with non - existing address [{}]", addressId);
            throw UserServiceException.notFound("Address not found");
        }

        addressMapper.patchAddress(patchUserAddressRequest, address);
    }

    @Override
    @Transactional
    public void deleteUserAddress(String tenantId, String userId, String addressId) {
        addressRepository.deleteById(addressId);
    }

    /**
     * Gets page index.
     *
     * @param offset the offset
     * @return the page index
     */
    private int getPageIndex(Integer offset) {
        return Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
    }

    /**
     * Gets page size.
     *
     * @param limit the limit
     * @return the page size
     */
    private int getPageSize(Integer limit) {
        return Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);
    }

    /**
     * Validates the uniqueness of the email.
     *
     * @param tenantId the tenant id
     * @param email    the email
     */
    private void validateCreateEmail(String tenantId, String email) {
        if (usersRepository.existedByEmail(tenantId, email)) {
            LOG.warn("Attempts to create user with existing email [{}]", email);
            throw UserServiceException.conflict("User with email already exists");
        }
    }

    /**
     * Validates the uniqueness of the username.
     *
     * @param tenantId the tenant id
     * @param username the username
     */
    private void validateCreateUsername(String tenantId, String username) {
        if (usersRepository.existedByUsername(tenantId, username)) {
            LOG.warn("Attempts to create user with existing username [{}]", username);
            throw UserServiceException.conflict("User with username already exists");
        }
    }

    /**
     * Validates the email update.
     *
     * @param tenantId the tenant id
     * @param userId   the ID of the user
     * @param email    the email to validate
     */
    private void validateUpdateEmail(String tenantId, String userId, String email) {
        if (usersRepository.existedByEmailExcludedById(tenantId, email, userId)) {
            LOG.warn("Attempts to update user with existing email [{}]", email);
            throw UserServiceException.conflict("User with email already exists");
        }
    }

    /**
     * Gets the user not found exception.
     *
     * @return the user not found exception
     */
    private UserServiceException getUserNotFound() {
        return UserServiceException.notFound("User not found");
    }
}
