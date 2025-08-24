package com.shopbee.user.control.service.impl;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.user.control.repository.AddressRepository;
import com.shopbee.user.control.repository.PhoneRepository;
import com.shopbee.user.control.repository.UserRepository;
import com.shopbee.user.control.service.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
class UserServiceImplTest {

    @InjectMock
    private UserRepository userRepository;

    @InjectMock
    private PhoneRepository phoneRepository;

    @InjectMock
    private AddressRepository addressRepository;

    @Inject
    private UserService userService;

    @Test
    void getUsers_tenantIdEmpty_throwException() {
        assertThrows(ApiServiceException.class, () -> userService.getUsers(null, null, null));
        assertThrows(ApiServiceException.class, () -> userService.getUsers("", null, null));
    }

    @Test
    void testDeleteUserAddress() {
        userService.deleteUserAddress("test-tenant-id", "test-user-id", "test-address-id");

        verify(addressRepository, times(1)).deleteByIdAndUserId("test-tenant-id", "test-user-id", "test-address-id");
    }
}