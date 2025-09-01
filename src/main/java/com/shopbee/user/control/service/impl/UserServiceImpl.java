/*
 * UsersServiceImpl.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.service.impl;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.user.control.mapper.AddressMapper;
import com.shopbee.user.control.mapper.UserMapper;
import com.shopbee.user.control.repository.AddressRepository;
import com.shopbee.user.control.repository.UserRepository;
import com.shopbee.user.control.service.UserService;
import com.shopbee.user.entity.Address;
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
import java.util.Optional;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_PAGE_INDEX = 0;

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Inject
    public UserServiceImpl(UserRepository userRepository,
                           AddressRepository addressRepository,
                           UserMapper userMapper,
                           AddressMapper addressMapper) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
    }

    // =================================================================================================================
    // PUBLIC API
    // =================================================================================================================

    @Override
    public List<UserDTO> getUsers(String tenantId, Integer offset, Integer limit) {
        LOG.info("Getting users with offset [{}] and limit [{}]", offset, limit);

        int page = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
        int size = Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);

        List<User> foundUsers = userRepository.findAll(tenantId, page, size);

        return userMapper.toUsers(foundUsers);
    }

    @Override
    public UserDTO getUserById(String tenantId, String userId) {
        LOG.info("Getting user by id [{}]", userId);

        User user = findUserByUserId(tenantId, userId);

        return userMapper.toUserDTO(user);
    }

    @Override
    @Transactional
    public String createUser(String tenantId, CreateUserRequest createUserRequest) {
        LOG.info("Creating new user with username [{}]", createUserRequest.getUsername());

        validateCreateUserRequest(tenantId, createUserRequest);

        User user = userMapper.toUser(tenantId, createUserRequest);

        return saveUser(user);
    }

    @Override
    @Transactional
    public void updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        LOG.info("Updating user with id [{}]", userId);

        User user = findUserByUserId(tenantId, userId);

        validateUpdateUserRequest(tenantId, userId, updateUserByIdRequest);

        userMapper.updateUser(updateUserByIdRequest, user, tenantId);
    }

    @Override
    @Transactional
    public void patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
        LOG.info("Patching user with id [{}]", userId);

        User user = findUserByUserId(tenantId, userId);

        validatePatchUserRequest(tenantId, userId, patchUserByIdRequest);

        userMapper.patchUser(patchUserByIdRequest, user, tenantId);
    }

    @Override
    @Transactional
    public void deleteUserById(String tenantId, String userId) {
        LOG.info("Deleting user with id [{}]", userId);

        User user = findUserByUserId(tenantId, userId);

        userRepository.delete(user);
    }

    @Override
    public List<AddressDTO> getUserAddresses(String tenantId, String userId, Integer offset, Integer limit) {
        LOG.info("Getting addresses of user [{}] with offset [{}] and limit [{}]", userId, offset, limit);

        List<Address> foundAddresses = addressRepository.findByUserId(tenantId, userId);

        return addressMapper.toAddressesDto(foundAddresses);
    }

    @Override
    @Transactional
    public String createUserAddress(String tenantId, String userId, CreateUserAddressRequest createUserAddressRequest) {
        LOG.info("Creating new address for user [{}]", userId);

        Address address = addressMapper.toAddress(tenantId, createUserAddressRequest);

        return saveAndAddAddressToUser(tenantId, userId, address);
    }

    @Override
    @Transactional
    public void updateUserAddress(String tenantId, String userId, String addressId, CreateUserAddressRequest createUserAddressRequest) {
        LOG.info("Updating address [{}] for user [{}]", addressId, userId);

        Address address = findAddressByUserIdAndAddressId(tenantId, userId, addressId);

        addressMapper.updateAddress(createUserAddressRequest, address);
    }

    @Override
    @Transactional
    public void patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest) {
        LOG.debug("Patching address [{}] for user [{}]", addressId, userId);

        Address address = findAddressByUserIdAndAddressId(tenantId, userId, addressId);

        addressMapper.patchAddress(patchUserAddressRequest, address);
    }

    @Override
    @Transactional
    public void deleteUserAddress(String tenantId, String userId, String addressId) {
        LOG.debug("Deleting address [{}] of user [{}]", addressId, userId);

        User user = findUserByUserId(tenantId, userId);

        user.removeAddress(addressId);
    }

    // =================================================================================================================
    // PRIVATE IMPLEMENTATION
    // =================================================================================================================

    private User findUserByUserId(String tenantId, String userId) {
        User foundUser = userRepository.findById(tenantId, userId);

        if (foundUser == null) {
            LOG.warn("User [{}] not found", userId);
            throw userNotFoundException(userId);
        }

        return foundUser;
    }

    private Address findAddressByUserIdAndAddressId(String tenantId, String userId, String addressId) {
        Address address = addressRepository.findByIdAndUserId(tenantId, userId, addressId);

        if (address == null) {
            LOG.warn("Address [{}] not found", addressId);
            throw ApiServiceException.notFound("Address [{}] not found", addressId);
        }

        return address;
    }

    private void validateCreateUserRequest(String tenantId, CreateUserRequest createUserRequest) {
        validateNewUsername(tenantId, createUserRequest.getUsername());
        validateNewEmail(tenantId, createUserRequest.getEmail());
    }

    private void validateUpdateUserRequest(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        validateUpdateEmail(tenantId, userId, updateUserByIdRequest.getEmail());
        validateUpdatePhone(tenantId, userId, updateUserByIdRequest.getPhone());
    }

    private void validatePatchUserRequest(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
        if (patchUserByIdRequest.getEmail() != null) {
            validateUpdateEmail(tenantId, userId, patchUserByIdRequest.getEmail());
        }
        if (patchUserByIdRequest.getPhone() != null) {
            validateUpdatePhone(tenantId, userId, patchUserByIdRequest.getPhone());
        }
    }

    private void validateNewUsername(String tenantId, String username) {
        if (isUsernameExisted(tenantId, username)) {
            throw ApiServiceException.conflict("Username [{}] already exists", username);
        }
    }

    private boolean isUsernameExisted(String tenantId, String username) {
        return userRepository.countByUsername(tenantId, username) > 0;
    }

    private void validateNewEmail(String tenantId, String email) {
        if (isEmailExisted(tenantId, email)) {
            throw emailExistedException(email);
        }
    }

    private boolean isEmailExisted(String tenantId, String email) {
        return userRepository.countByEmail(tenantId, email) > 0;
    }

    private void validateUpdateEmail(String tenantId, String userId, String email) {
        if (isEmailBelongedToAnotherUser(tenantId, userId, email)) {
            throw emailExistedException(email);
        }
    }

    private boolean isEmailBelongedToAnotherUser(String tenantId, String userId, String email) {
        return userRepository.countByEmailExcludeUserId(tenantId, email, userId) > 0;
    }

    private void validateUpdatePhone(String tenantId, String userId, PhoneDTO phoneDTO) {
        if (phoneDTO != null) {
            String countryCode = phoneDTO.getCountryCode();
            if (StringUtils.isBlank(countryCode)) {
                throw ApiServiceException.badRequest("Country code is required");
            }
            String number = phoneDTO.getNumber();
            if (StringUtils.isBlank(number)) {
                throw ApiServiceException.badRequest("Phone number is required");
            }

            if (isPhoneBelongedToAnotherUser(tenantId, userId, countryCode, number)) {
                throw phoneExistedException(countryCode, number);
            }
        }
    }

    private boolean isPhoneBelongedToAnotherUser(String tenantId, String userId, String countryCode, String number) {
        return userRepository.countByPhoneExcludeUserId(tenantId, countryCode, number, userId) > 0;
    }

    private String saveUser(User user) {
        try {
            userRepository.persist(user);
            return user.getId();
        } catch (Exception e) {
            String message = "Failed to save user. Reason: {}";
            LOG.warn(message, e.getMessage());
            throw ApiServiceException.internalServerError(message, e.getMessage());
        }
    }

    private String saveAddress(Address address) {
        addressRepository.persist(address);
        return address.getId();
    }

    private String saveAndAddAddressToUser(String tenantId, String userId, Address address) {
        User user = findUserByUserId(tenantId, userId);

        user.addAddress(address);

        return saveAddress(address);
    }

    private ApiServiceException userNotFoundException(String userId) {
        return ApiServiceException.notFound("User [{}] not found", userId);
    }

    private ApiServiceException emailExistedException(String email) {
        return ApiServiceException.conflict("Email [{}] linked to another user", email);
    }

    private ApiServiceException phoneExistedException(String countryCode, String number) {
        return ApiServiceException.conflict("Phone [{}{}] linked to another user", countryCode, number);
    }
}
