package com.shopbee.user.control.service;

import com.shopbee.user.dto.AddressDTO;
import com.shopbee.user.dto.CreateUserAddressRequest;
import com.shopbee.user.dto.CreateUserRequest;
import com.shopbee.user.dto.PatchUserAddressRequest;
import com.shopbee.user.dto.PatchUserByIdRequest;
import com.shopbee.user.dto.UpdateUserByIdRequest;
import com.shopbee.user.dto.UserDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> getUsers(String tenantId, Integer offset, Integer limit);

    UserDTO getUserById(String tenantId, String userId);

    String createUser(String tenantId, CreateUserRequest createUserRequest);

    void updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest);

    void patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest);

    void deleteUserById(String tenantId, String userId);

    List<AddressDTO> getUserAddresses(String tenantId, String userId, Integer offset, Integer limit);

    String createUserAddress(String tenantId, String userId, CreateUserAddressRequest createUserAddressRequest);

    void updateUserAddress(String tenantId, String userId, String addressId, CreateUserAddressRequest createUserAddressRequest);

    void patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest);

    void deleteUserAddress(String tenantId, String userId, String addressId);
}
