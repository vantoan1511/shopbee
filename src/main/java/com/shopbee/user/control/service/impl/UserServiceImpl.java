/*
 * UsersServiceImpl.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.service.impl;

import com.shopbee.user.control.exception.UserServiceException;
import com.shopbee.user.control.mapper.AddressMapper;
import com.shopbee.user.control.mapper.PhoneMapper;
import com.shopbee.user.control.mapper.UserMapper;
import com.shopbee.user.control.repository.AddressRepository;
import com.shopbee.user.control.repository.PhoneRepository;
import com.shopbee.user.control.repository.UsersRepository;
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
    private final PhoneRepository phoneRepository;

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;

    /**
     * Instantiates a new User service.
     *
     * @param usersRepository   the users repository
     * @param addressRepository the address repository
     * @param phoneRepository   the phone repository
     * @param userMapper        the user mapper
     * @param addressMapper     the address mapper
     * @param phoneMapper       the phone mapper
     */
    @Inject
    public UserServiceImpl(UsersRepository usersRepository,
                           AddressRepository addressRepository,
                           PhoneRepository phoneRepository,
                           UserMapper userMapper,
                           AddressMapper addressMapper,
                           PhoneMapper phoneMapper) {
        this.usersRepository = usersRepository;
        this.addressRepository = addressRepository;
        this.phoneRepository = phoneRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
    }

    @Override
    public List<User> getUsers(String tenantId, Integer offset, Integer limit) {
        LOG.debug("Getting users with offset [{}] and limit [{}]...", offset, limit);

        List<com.shopbee.user.entity.User> users = usersRepository.findAll(tenantId)
                .page(getPageIndex(offset), getPageSize(limit))
                .list();

        LOG.debug("Got [{}] users", users.size());

        return userMapper.toUsers(users);
    }

    @Override
    public User getUserById(String tenantId, String userId) {
        LOG.debug("Getting user by id [{}]...", userId);

        User user = Optional.ofNullable(usersRepository.findById(tenantId, userId))
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

        usersRepository.persist(user);

        LOG.debug("Created user with id [{}]", user.getId());

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        LOG.debug("Updating user with id [{}]...", userId);

        validateUpdateEmail(tenantId, userId, updateUserByIdRequest.getEmail());

        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to update non - existing user [{}]", userId);
            throw userNotFoundException();
        }

        PhoneId phoneId = phoneMapper.toPhone(updateUserByIdRequest.getPhone());
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

        userMapper.updateUser(updateUserByIdRequest, user);

        LOG.debug("Updated user with id [{}]", user.getId());
    }

    @Override
    @Transactional
    public void patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
        LOG.debug("Patching user with id [{}]...", userId);

        validateUpdateEmail(tenantId, userId, patchUserByIdRequest.getEmail());

        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to patch non - existing user [{}]", userId);
            throw userNotFoundException();
        }

        PhoneId phoneId = phoneMapper.toPhone(patchUserByIdRequest.getPhone());
        if (Objects.nonNull(phoneId)) {
            Phone phone = phoneRepository.findById(phoneId);
            if (Objects.nonNull(phone)) {
                validateUpdatePhone(userId, phone);
            } else {
                phone = new Phone();
                phone.setId(phoneId);
                phone.setTenantId(tenantId);
                phone.setUser(user);
                user.setPhone(phone);
            }
        }

        userMapper.patchUser(patchUserByIdRequest, user);

        LOG.debug("Patched user with id [{}]", user.getId());
    }

    @Override
    @Transactional
    public void deleteUserById(String tenantId, String userId) {
        LOG.debug("Deleting user with id [{}]...", userId);

        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

        if (Objects.isNull(user)) {
            LOG.warn("Attempts to delete non - existing user [{}]", userId);
            throw userNotFoundException();
        }

        usersRepository.delete(user);

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

        com.shopbee.user.entity.User user = usersRepository.findById(tenantId, userId);

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

        com.shopbee.user.entity.Address address = addressRepository.findByIdAndUserId(tenantId, addressId, userId);

        if (Objects.isNull(address)) {
            LOG.warn("Attempts to update user address with non - existing address [{}]", addressId);
            throw UserServiceException.notFound("Address not found");
        }

        addressMapper.updateAddress(createUserAddressRequest, address);

        LOG.debug("Updated address with id [{}] for user [{}]", addressId, userId);
    }

    @Override
    @Transactional
    public void patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest) {
        LOG.debug("Patching address [{}] for user [{}]...", addressId, userId);

        com.shopbee.user.entity.Address address = addressRepository.findByIdAndUserId(tenantId, addressId, userId);

        if (Objects.isNull(address)) {
            LOG.warn("Attempts to patch user address with non - existing address [{}]", addressId);
            throw UserServiceException.notFound("Address not found");
        }

        addressMapper.patchAddress(patchUserAddressRequest, address);

        LOG.debug("Patched address with id [{}] for user [{}]", addressId, userId);
    }

    @Override
    @Transactional
    public void deleteUserAddress(String tenantId, String userId, String addressId) {
        LOG.debug("Deleting address [{}] for user [{}]...", addressId, userId);

        addressRepository.deleteById(addressId);

        LOG.debug("Deleted address with id [{}] for user [{}]", addressId, userId);
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
            throw emailExistsException();
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
            throw emailExistsException();
        }
    }

    /**
     * Validate create phone.
     *
     * @param phone the phone
     */
    private void validateCreatePhone(Phone phone) {
        if (Objects.nonNull(phoneRepository.findById(phone.getId()))) {
            LOG.warn("Attempts to create user with existing phone [{}]", phone.getPhoneNumber());
            throw phoneExistsException();
        }
    }

    /**
     * Validate update phone.
     *
     * @param userId the user id
     * @param phone  the phone
     */
    private void validateUpdatePhone(String userId, Phone phone) {
        if (!phone.getUser().getId().equals(userId)) {
            LOG.warn("Attempts to update user with existing phoneId [{}]", phone.getPhoneNumber());
            throw phoneExistsException();
        }
    }

    /**
     * Email exists exception user service exception.
     *
     * @return the user service exception
     */
    private UserServiceException emailExistsException() {
        return UserServiceException.conflict("User with email already exists");
    }

    /**
     * Phone exists exception user service exception.
     *
     * @return the user service exception
     */
    private UserServiceException phoneExistsException() {
        return UserServiceException.conflict("Phone already exists");
    }

    /**
     * Gets the user not found exception.
     *
     * @return the user not found exception
     */
    private UserServiceException userNotFoundException() {
        return UserServiceException.notFound("User not found");
    }
}
