/*
 * UsersServiceImpl.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.service.impl;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.user.control.mapper.AddressMapper;
import com.shopbee.user.control.mapper.PhoneMapper;
import com.shopbee.user.control.mapper.UserMapper;
import com.shopbee.user.control.repository.AddressRepository;
import com.shopbee.user.control.repository.PhoneRepository;
import com.shopbee.user.control.repository.UserRepository;
import com.shopbee.user.control.service.UserService;
import com.shopbee.user.entity.Phone;
import com.shopbee.user.entity.PhoneId;
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

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PhoneRepository phoneRepository;

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;

    /**
     * Instantiates a new User service.
     *
     * @param userRepository    the users repository
     * @param addressRepository the address repository
     * @param phoneRepository   the phone repository
     * @param userMapper        the user mapper
     * @param addressMapper     the address mapper
     * @param phoneMapper       the phone mapper
     */
    @Inject
    public UserServiceImpl(UserRepository userRepository,
                           AddressRepository addressRepository,
                           PhoneRepository phoneRepository,
                           UserMapper userMapper,
                           AddressMapper addressMapper,
                           PhoneMapper phoneMapper) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.phoneRepository = phoneRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
    }

    @Override
    public List<User> getUsers(String tenantId, Integer offset, Integer limit) {
        if (StringUtils.isBlank(tenantId)) {
            throw ApiServiceException.badRequest("tenantId must not be empty");
        }

        int page = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
        int size = Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);

        LOG.info("Getting users with page [{}] and size [{}]...", page, size);

        List<com.shopbee.user.entity.User> foundUsers = userRepository.findAll(tenantId, page, size);

        LOG.info("Got [{}] users", foundUsers.size());

        return userMapper.toUsers(foundUsers);
    }

    @Override
    public User getUserById(String tenantId, String userId) {
        LOG.debug("Getting user by id [{}]", userId);

        User user = Optional.ofNullable(userRepository.findById(tenantId, userId))
                .map(userMapper::toUser)
                .orElseThrow(this::userNotFoundException);

        LOG.debug("Got user [id={}, username={}, status={}]", user.getId(), user.getUsername(), user.getStatus());

        return user;
    }

    @Override
    @Transactional
    public String createUser(String tenantId, CreateUserRequest createUserRequest) {
        LOG.debug("Creating user with username [{}] and email [{}]...", createUserRequest.getUsername(), createUserRequest.getEmail());

        com.shopbee.user.entity.User user = userMapper.toUser(tenantId, createUserRequest);

        validateCreateUsername(tenantId, user.getUsername());
        validateCreateEmail(tenantId, user.getEmail());

        Phone phone = user.getPhone();
        if (Objects.nonNull(phone)) {
            validateCreatePhone(phone);
            phone.setUser(user);
        }

        userRepository.persist(user);

        LOG.debug("Created user with id [{}]", user.getId());

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        LOG.debug("Updating user with id [{}]...", userId);

        com.shopbee.user.entity.User user = findAndValidateUserForUpdate(tenantId, userId, updateUserByIdRequest.getEmail(), updateUserByIdRequest.getPhone());

        userMapper.updateUser(updateUserByIdRequest, user);

        LOG.debug("Updated user with id [{}]", user.getId());
    }

    @Override
    @Transactional
    public void patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
        LOG.debug("Patching user with id [{}]...", userId);

        com.shopbee.user.entity.User user = findAndValidateUserForUpdate(tenantId, userId, patchUserByIdRequest.getEmail(), patchUserByIdRequest.getPhone());

        userMapper.patchUser(patchUserByIdRequest, user);

        LOG.debug("Patched user with id [{}]", user.getId());
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
    public List<Address> getUserAddresses(String tenantId, String userId, Integer offset, Integer limit) {
        LOG.debug("Getting user [{}] addresses with offset [{}] and limit [{}]...", userId, offset, limit);

        int pageIndex = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
        int pageSize = Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);

        List<Address> addresses = addressMapper.toAddresses(addressRepository.findByUserId(tenantId, userId, pageIndex, pageSize));

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

    private com.shopbee.user.entity.User findAndValidateUserForUpdate(String tenantId, String userId, String email, com.shopbee.user.model.Phone phoneRequest) {
        validateUpdateEmail(tenantId, userId, email);

        com.shopbee.user.entity.User user = userRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to update non - existing user [{}]", userId);
            throw userNotFoundException();
        }

        PhoneId phoneId = phoneMapper.toPhone(phoneRequest);
        if (Objects.nonNull(phoneId)) {
            Phone existingPhone = phoneRepository.findById(phoneId);
            if (Objects.nonNull(existingPhone)) {
                validateUpdatePhone(userId, existingPhone);
            } else {
                Phone phone = new Phone();
                phone.setId(phoneId);
                phone.setTenantId(tenantId);
                phone.setUser(user);
                user.setPhone(phone);
            }
        }
        return user;
    }

    private void validateCreateEmail(String tenantId, String email) {
        if (userRepository.existedByEmail(tenantId, email)) {
            LOG.warn("Attempts to create user with existing email [{}]", email);
            throw emailExistsException();
        }
    }

    private void validateCreateUsername(String tenantId, String username) {
        if (userRepository.existedByUsername(tenantId, username)) {
            LOG.warn("Attempts to create user with existing username [{}]", username);
            throw ApiServiceException.conflict("User with username already exists");
        }
    }

    private void validateUpdateEmail(String tenantId, String userId, String email) {
        if (userRepository.existedByEmailExcludedById(tenantId, email, userId)) {
            LOG.warn("Attempts to update user with existing email [{}]", email);
            throw emailExistsException();
        }
    }

    private void validateCreatePhone(Phone phone) {
        if (Objects.nonNull(phoneRepository.findById(phone.getId()))) {
            LOG.warn("Attempts to create user with existing phone [{}]", phone.getPhoneNumber());
            throw phoneExistsException();
        }
    }

    private void validateUpdatePhone(String userId, Phone phone) {
        if (!phone.getUser().getId().equals(userId)) {
            LOG.warn("Attempts to update user with existing phoneId [{}]", phone.getPhoneNumber());
            throw phoneExistsException();
        }
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
