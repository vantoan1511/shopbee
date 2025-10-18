package com.shopbee.user.control.service.impl;

import com.shopbee.business.user.control.service.impl.UserServiceImpl;
import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.business.user.control.mapper.AddressMapper;
import com.shopbee.business.user.control.mapper.UserMapper;
import com.shopbee.business.user.control.repository.AddressRepository;
import com.shopbee.business.user.control.repository.UserRepository;
import com.shopbee.business.user.entity.Address;
import com.shopbee.business.user.entity.User;
import com.shopbee.user.model.AddressDTO;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import com.shopbee.user.model.PatchUserByIdRequest;
import com.shopbee.user.model.PhoneDTO;
import com.shopbee.user.model.UpdateUserByIdRequest;
import com.shopbee.user.model.UserDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    private static final String TENANT_ID = "test-tenant";
    private static final String USER_ID = "test-user-id";
    private static final String ADDRESS_ID = "test-address-id";

    @Inject
    UserServiceImpl userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    AddressRepository addressRepository;

    @InjectMock
    UserMapper userMapper;

    @InjectMock
    AddressMapper addressMapper;

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setTenantId(TENANT_ID);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setAddresses(new ArrayList<>());
        return user;
    }

    private Address createAddress() {
        Address address = new Address();
        address.setId(ADDRESS_ID);
        address.setTenantId(TENANT_ID);
        return address;
    }

    @Test
    @DisplayName("GetUsers: should return a list of users")
    void getUsers_shouldReturnListOfUsers() {
        User user = createUser();
        UserDTO userDTO = new UserDTO();
        when(userRepository.findAll(TENANT_ID, 0, 20)).thenReturn(Collections.singletonList(user));
        when(userMapper.toUsers(Collections.singletonList(user))).thenReturn(Collections.singletonList(userDTO));

        List<UserDTO> result = userService.getUsers(TENANT_ID, null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository).findAll(TENANT_ID, 0, 20);
    }

    @Test
    @DisplayName("GetUserById: should return user when found")
    void getUserById_shouldReturnUser_whenFound() {
        User user = createUser();
        UserDTO userDTO = new UserDTO();
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(TENANT_ID, USER_ID);

        assertNotNull(result);
        verify(userRepository).findById(TENANT_ID, USER_ID);
    }

    @Test
    @DisplayName("GetUserById: should throw not found exception when user not found")
    void getUserById_shouldThrowNotFound_whenUserNotFound() {
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(null);

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.getUserById(TENANT_ID, USER_ID));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @DisplayName("CreateUser: should create user successfully")
    void createUser_shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest().username("newuser").email("new@example.com");
        User user = new User();
        user.setId("new-id");
        when(userRepository.countByUsername(TENANT_ID, "newuser")).thenReturn(0L);
        when(userRepository.countByEmail(TENANT_ID, "new@example.com")).thenReturn(0L);
        when(userMapper.toUser(TENANT_ID, request)).thenReturn(user);

        String newUserId = userService.createUser(TENANT_ID, request);

        assertEquals("new-id", newUserId);
        verify(userRepository).persist(user);
    }

    @Test
    @DisplayName("CreateUser: should throw conflict when username exists")
    void createUser_shouldThrowConflict_whenUsernameExists() {
        CreateUserRequest request = new CreateUserRequest().username("existinguser").email("new@example.com");
        when(userRepository.countByUsername(TENANT_ID, "existinguser")).thenReturn(1L);

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.createUser(TENANT_ID, request));
        assertEquals(409, ex.getResponse().getStatus());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    @DisplayName("UpdateUserById: should update user successfully")
    void updateUserById_shouldUpdateUserSuccessfully() {
        User user = createUser();
        UpdateUserByIdRequest request = new UpdateUserByIdRequest().email("updated@example.com");
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);
        when(userRepository.countByEmailExcludeUserId(TENANT_ID, "updated@example.com", USER_ID)).thenReturn(0L);

        userService.updateUserById(TENANT_ID, USER_ID, request);

        verify(userMapper).updateUser(request, user, TENANT_ID);
    }

    @Test
    @DisplayName("UpdateUserById: should throw conflict on email update if email belongs to another user")
    void updateUserById_shouldThrowConflict_whenEmailBelongsToAnotherUser() {
        User user = createUser();
        UpdateUserByIdRequest request = new UpdateUserByIdRequest().email("another@example.com");
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);
        when(userRepository.countByEmailExcludeUserId(TENANT_ID, "another@example.com", USER_ID)).thenReturn(1L);

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.updateUserById(TENANT_ID, USER_ID, request));
        assertEquals(409, ex.getResponse().getStatus());
    }

    @Test
    @DisplayName("PatchUserById: should patch user successfully")
    void patchUserById_shouldPatchUserSuccessfully() {
        User user = createUser();
        PatchUserByIdRequest request = new PatchUserByIdRequest().firstName("Patched");
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);

        userService.patchUserById(TENANT_ID, USER_ID, request);

        verify(userMapper).patchUser(request, user, TENANT_ID);
    }

    @Test
    @DisplayName("PatchUserById: should throw conflict on phone patch if phone belongs to another user")
    void patchUserById_shouldThrowConflict_whenPhoneBelongsToAnotherUser() {
        User user = createUser();
        PhoneDTO phone = new PhoneDTO().countryCode("+1").number("1234567890");
        PatchUserByIdRequest request = new PatchUserByIdRequest().phone(phone);
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);
        when(userRepository.countByPhoneExcludeUserId(TENANT_ID, "+1", "1234567890", USER_ID)).thenReturn(1L);

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.patchUserById(TENANT_ID, USER_ID, request));
        assertEquals(409, ex.getResponse().getStatus());
    }

    @Test
    @DisplayName("DeleteUserById: should delete user successfully")
    void deleteUserById_shouldDeleteUserSuccessfully() {
        User user = createUser();
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);
        doNothing().when(userRepository).delete(user);

        assertDoesNotThrow(() -> userService.deleteUserById(TENANT_ID, USER_ID));

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("DeleteUserById: should throw not found when user to delete is not found")
    void deleteUserById_shouldThrowNotFound_whenUserNotFound() {
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(null);

        assertThrows(ApiServiceException.class, () -> userService.deleteUserById(TENANT_ID, USER_ID));
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("GetUserAddresses: should return user addresses")
    void getUserAddresses_shouldReturnUserAddresses() {
        Address address = createAddress();
        AddressDTO addressDTO = new AddressDTO();
        when(addressRepository.findByUserId(TENANT_ID, USER_ID)).thenReturn(Collections.singletonList(address));
        when(addressMapper.toAddressesDto(Collections.singletonList(address))).thenReturn(Collections.singletonList(addressDTO));

        List<AddressDTO> result = userService.getUserAddresses(TENANT_ID, USER_ID, null, null);

        assertFalse(result.isEmpty());
        verify(addressRepository).findByUserId(TENANT_ID, USER_ID);
    }

    @Test
    @DisplayName("CreateUserAddress: should create user address successfully")
    void createUserAddress_shouldCreateUserAddressSuccessfully() {
        User user = createUser();
        Address address = createAddress();
        CreateUserAddressRequest request = new CreateUserAddressRequest();
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);
        when(addressMapper.toAddress(TENANT_ID, request)).thenReturn(address);

        String newAddressId = userService.createUserAddress(TENANT_ID, USER_ID, request);

        assertEquals(ADDRESS_ID, newAddressId);
        verify(addressRepository).persist(address);
        assertThat(user.getAddresses()).contains(address);
    }

    @Test
    @DisplayName("UpdateUserAddress: should update user address successfully")
    void updateUserAddress_shouldUpdateUserAddressSuccessfully() {
        Address address = createAddress();
        CreateUserAddressRequest request = new CreateUserAddressRequest().city("UpdatedCity");
        when(addressRepository.findByIdAndUserId(TENANT_ID, USER_ID, ADDRESS_ID)).thenReturn(address);

        userService.updateUserAddress(TENANT_ID, USER_ID, ADDRESS_ID, request);

        verify(addressMapper).updateAddress(request, address);
    }

    @Test
    @DisplayName("UpdateUserAddress: should throw not found when address not found")
    void updateUserAddress_shouldThrowNotFound_whenAddressNotFound() {
        CreateUserAddressRequest request = new CreateUserAddressRequest();
        when(addressRepository.findByIdAndUserId(TENANT_ID, USER_ID, ADDRESS_ID)).thenReturn(null);

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.updateUserAddress(TENANT_ID, USER_ID, ADDRESS_ID, request));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @DisplayName("PatchUserAddress: should patch user address successfully")
    void patchUserAddress_shouldPatchUserAddressSuccessfully() {
        Address address = createAddress();
        PatchUserAddressRequest request = new PatchUserAddressRequest();
        when(addressRepository.findByIdAndUserId(TENANT_ID, USER_ID, ADDRESS_ID)).thenReturn(address);

        userService.patchUserAddress(TENANT_ID, USER_ID, ADDRESS_ID, request);

        verify(addressMapper).patchAddress(request, address);
    }

    @Test
    @DisplayName("DeleteUserAddress: should delete user address successfully")
    void deleteUserAddress_shouldDeleteUserAddressSuccessfully() {
        User user = createUser();
        Address address = createAddress();
        user.addAddress(address);
        when(userRepository.findById(TENANT_ID, USER_ID)).thenReturn(user);

        userService.deleteUserAddress(TENANT_ID, USER_ID, ADDRESS_ID);

        assertTrue(user.getAddresses().isEmpty());
    }
}
