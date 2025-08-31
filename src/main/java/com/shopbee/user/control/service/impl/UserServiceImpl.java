/*
 * UsersServiceImpl.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.service.impl;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.user.control.UserValidator;
import com.shopbee.user.control.mapper.AddressMapper;
import com.shopbee.user.control.mapper.PhoneMapper;
import com.shopbee.user.control.mapper.UserMapper;
import com.shopbee.user.control.repository.AddressRepository;
import com.shopbee.user.control.repository.PhoneRepository;
import com.shopbee.user.control.repository.UserRepository;
import com.shopbee.user.control.service.UserService;
import com.shopbee.user.entity.User;
import com.shopbee.user.model.AddressDTO;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import com.shopbee.user.model.PatchUserByIdRequest;
import com.shopbee.user.model.PhoneDTO;
import com.shopbee.user.model.UpdateUserByIdRequest;
import com.shopbee.user.model.UserDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_PAGE_INDEX = 0;

    private final UserValidator userValidator;

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PhoneRepository phoneRepository;

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;

    @Inject
    public UserServiceImpl(UserValidator userValidator,
                           UserRepository userRepository,
                           AddressRepository addressRepository,
                           PhoneRepository phoneRepository,
                           UserMapper userMapper,
                           AddressMapper addressMapper,
                           PhoneMapper phoneMapper) {
        this.userValidator = userValidator;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.phoneRepository = phoneRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
    }

    @Override
    public List<UserDTO> getUsers(String tenantId, Integer offset, Integer limit) {
        int page = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
        int size = Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);

        LOG.info("Getting users with page [{}] and size [{}]...", page, size);
        List<com.shopbee.user.entity.User> foundUsers = userRepository.findAll(tenantId, page, size);
        LOG.info("Got [{}] users", foundUsers.size());

        return userMapper.toUsers(foundUsers);
    }

    @Override
    public UserDTO getUserById(String tenantId, String userId) {
        LOG.debug("Getting user by id [{}]", userId);
        User foundUser = userRepository.findById(tenantId, userId);
        UserDTO userDTO = Optional.ofNullable(foundUser)
                .map(userMapper::toUserDTO)
                .orElseThrow(this::userNotFoundException);
        LOG.debug("Got user [id={}, username={}, status={}]", userDTO.getId(), userDTO.getUsername(), userDTO.getStatus());
        return userDTO;
    }

    @Override
    @Transactional
    public String createUser(String tenantId, CreateUserRequest createUserRequest) {
        userValidator.validateCreateUserRequest(tenantId, createUserRequest);
        User user = userMapper.toUser(tenantId, createUserRequest);
        userRepository.persist(user);
        return user.getId();
    }

    @Override
    @Transactional
    public void updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        User user = Optional.ofNullable(userRepository.findById(tenantId, userId)).orElseThrow(this::userNotFoundException);

        if (userRepository.countByEmailExcludeUserId(tenantId, updateUserByIdRequest.getEmail(), userId) > 0) {
            throw emailExistsException();
        }
        PhoneDTO phoneDTO = updateUserByIdRequest.getPhone();
        if (phoneDTO != null) {
            String countryCode = phoneDTO.getCountryCode();
            if (StringUtils.isBlank(countryCode)) {
                throw ApiServiceException.badRequest("Country code is required");
            }
            String number = phoneDTO.getNumber();
            if (StringUtils.isBlank(number)) {
                throw ApiServiceException.badRequest("Phone number is required");
            }

            long countByPhone = userRepository.countByPhoneExcludeUserId(tenantId, countryCode, number, userId);
            if (countByPhone > 0) {
                throw phoneExistsException();
            }
        }

        userMapper.updateUser(updateUserByIdRequest, user, tenantId);
    }

    @Override
    @Transactional
    public void patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
    }

    @Override
    @Transactional
    public void deleteUserById(String tenantId, String userId) {
        LOG.debug("Deleting user with id [{}]...", userId);

        com.shopbee.user.entity.User user = userRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to delete non - existing user [{}]", userId);
            throw userNotFoundException();
        }

        userRepository.delete(user);

        LOG.debug("Deleted user with id [{}]", userId);
    }

    @Override
    public List<AddressDTO> getUserAddresses(String tenantId, String userId, Integer offset, Integer limit) {
        LOG.debug("Getting user [{}] addresses with offset [{}] and limit [{}]...", userId, offset, limit);
        List<AddressDTO> addresses = addressMapper.toAddresses(addressRepository.findByUserId(tenantId, userId));
        LOG.debug("Got [{}] addresses for user [{}]", addresses.size(), userId);
        return addresses;
    }

    @Override
    @Transactional
    public String createUserAddress(String tenantId, String userId, CreateUserAddressRequest createUserAddressRequest) {
        LOG.debug("Creating address for user [{}]...", userId);

        com.shopbee.user.entity.User user = userRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to create address for non - existing user [{}]", userId);
            throw userNotFoundException();
        }

        com.shopbee.user.entity.Address address = addressMapper.toAddress(tenantId, createUserAddressRequest);

        user.addAddress(address);

        addressRepository.persist(address);

        String addressId = address.getId();
        LOG.debug("Created address with id [{}] for user [{}]", addressId, userId);

        return addressId;
    }

    @Override
    @Transactional
    public void updateUserAddress(String tenantId, String userId, String addressId, CreateUserAddressRequest createUserAddressRequest) {
        LOG.debug("Updating address [{}] for user [{}]...", addressId, userId);

        com.shopbee.user.entity.Address address = addressRepository.findByIdAndUserId(tenantId, userId, addressId);

        if (Objects.isNull(address)) {
            LOG.warn("Attempts to update user address with non - existing address [{}]", addressId);
            throw ApiServiceException.notFound("Address not found");
        }

        addressMapper.updateAddress(createUserAddressRequest, address);

        LOG.debug("Updated address with id [{}] for user [{}]", addressId, userId);
    }

    @Override
    @Transactional
    public void patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest) {
        LOG.debug("Patching address [{}] for user [{}]...", addressId, userId);

        com.shopbee.user.entity.Address address = addressRepository.findByIdAndUserId(tenantId, userId, addressId);

        if (Objects.isNull(address)) {
            LOG.warn("Attempts to patch user address with non - existing address [{}]", addressId);
            throw ApiServiceException.notFound("Address not found");
        }

        addressMapper.patchAddress(patchUserAddressRequest, address);

        LOG.debug("Patched address with id [{}] for user [{}]", addressId, userId);
    }

    @Override
    @Transactional
    public void deleteUserAddress(String tenantId, String userId, String addressId) {
        LOG.debug("Deleting address id [{}] of user id [{}]", addressId, userId);

        addressRepository.deleteByIdAndUserId(tenantId, userId, addressId);

        LOG.debug("Deleted address id [{}] of user id [{}]", addressId, userId);
    }

    private ApiServiceException emailExistsException() {
        return ApiServiceException.conflict("User with email already exists");
    }

    private ApiServiceException userNotFoundException() {
        return ApiServiceException.notFound("User not found");
    }

    private ApiServiceException phoneExistsException() {
        return ApiServiceException.conflict("Phone already exists");
    }
}
