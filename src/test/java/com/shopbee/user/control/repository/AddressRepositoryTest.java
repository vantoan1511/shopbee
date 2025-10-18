package com.shopbee.user.control.repository;

import com.shopbee.business.user.control.repository.AddressRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AddressRepositoryTest {

    @Inject
    AddressRepository addressRepository;

    @Test
    @TestTransaction
    void deleteByIdAndUserId() {
        addressRepository.deleteByIdAndUserId("test-tenant-id", "test-user-id", "test-address-id");
    }
}