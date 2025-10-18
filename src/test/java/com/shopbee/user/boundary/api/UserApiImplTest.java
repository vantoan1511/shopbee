package com.shopbee.user.boundary.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.business.user.control.service.UserService;
import com.shopbee.business.user.entity.Address;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import com.shopbee.user.model.PatchUserByIdRequest;
import com.shopbee.user.model.UpdateUserByIdRequest;
import com.shopbee.user.model.UserDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.shopbee.common.TestUtils.getMockData;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * The type User api impl test.
 */
@QuarkusTest
class UserApiImplTest {

    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    private static final String TENANT_ID = "tenantId";
    private static final String SHOPBEE = "shopbee";
    private static final String USERS_API_PATH = "/users";
    private static final String USERS_JSON = "/users.json";
    private static final String NON_EXISTING_USER_ID = "non-existing-user";
    public static final String TEST_CREATED_USER_LOCATION = "http://localhost:8080/api/users/new-user-id";
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_USER_MAIL = "test@test.com";
    public static final String NEW_USER_ID = "new-user-id";
    public static final String TEST_CREATED_USER_ADDRESS_LOCATION = "http://localhost:8080/api/users/some-user-id/addresses/new-address-id";
    public static final String TEST_STREET = "123 Main St";
    public static final String NEW_ADDRESS_ID = "new-address-id";
    public static final String TEST_CITY = "Test City";
    public static final String TEST_DISTRICT = "Test District";
    public static final String POSTAL_CODE = "12345";
    public static final String TEST_WARD = "Test Ward";

    @InjectMock
    private UserService userService;

    @InjectMock
    private UriInfo uriInfo;

    private UriBuilder uriBuilderMock;

    /**
     * Sets .
     */
    @BeforeEach
    void setup() {
        uriBuilderMock = Mockito.mock(UriBuilder.class);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilderMock);
        when(uriBuilderMock.path(anyString())).thenReturn(uriBuilderMock);
    }

    /**
     * Gets users success.
     */
    @Test
    void getUsers_success() {
        int offset = 0;
        int limit = 5;
        List<UserDTO> users = getMockUsers(offset, limit);
        when(userService.getUsers(SHOPBEE, offset, limit)).thenReturn(users);
        given()
                .when()
                .queryParams(Map.of(OFFSET, offset, LIMIT, limit))
                .header(TENANT_ID, SHOPBEE)
                .get(USERS_API_PATH)
                .then()
                .statusCode(200)
                .body("size()", is(limit));
    }

    /**
     * Gets users empty offset success.
     */
    @Test
    void getUsers_emptyOffset_success() {
        given()
                .when()
                .queryParams(Map.of(OFFSET, "", LIMIT, "5"))
                .header(TENANT_ID, SHOPBEE)
                .get(USERS_API_PATH)
                .then()
                .statusCode(200);
    }

    /**
     * Gets users empty limit success.
     */
    @Test
    void getUsers_emptyLimit_success() {
        given()
                .when()
                .queryParams(Map.of(OFFSET, "0", LIMIT, ""))
                .header(TENANT_ID, SHOPBEE)
                .get(USERS_API_PATH)
                .then()
                .statusCode(200);

    }

    /**
     * Gets users missing tenant id bad request.
     */
    @Test
    void getUsers_missingTenantId_badRequest() {
        given()
                .when()
                .queryParam(OFFSET, 0, LIMIT, 20)
                .header(TENANT_ID, "")
                .get(USERS_API_PATH)
                .then()
                .statusCode(400);

    }

    /**
     * Create user.
     */
    @Test
    void createUser() {
        CreateUserRequest request = new CreateUserRequest().username(TEST_USERNAME).email(TEST_USER_MAIL);
        doReturn(URI.create(TEST_CREATED_USER_LOCATION)).when(uriBuilderMock).build();
        when(userService.createUser(eq(SHOPBEE), any(CreateUserRequest.class))).thenReturn(NEW_USER_ID);
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(USERS_API_PATH)
                .then()
                .statusCode(201)
                .header("Location", is(TEST_CREATED_USER_LOCATION));
    }

    /**
     * Create user username exists.
     */
    @Test
    void createUser_usernameExists() {
        CreateUserRequest request = new CreateUserRequest().username(TEST_USERNAME).email(TEST_USER_MAIL);
        when(userService.createUser(eq(SHOPBEE), any(CreateUserRequest.class))).thenThrow(ApiServiceException.conflict("User with username already exists"));
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(USERS_API_PATH)
                .then()
                .statusCode(409);
    }

    /**
     * Create user address.
     */
    @Test
    void createUserAddress() {
        doReturn(URI.create(TEST_CREATED_USER_ADDRESS_LOCATION)).when(uriBuilderMock).build();
        CreateUserAddressRequest request = new CreateUserAddressRequest()
                .type(Address.Type.WORK.name())
                .street(TEST_STREET)
                .city(TEST_CITY)
                .district(TEST_DISTRICT)
                .postalCode(POSTAL_CODE)
                .ward(TEST_WARD);
        when(userService.createUserAddress(eq(SHOPBEE), anyString(), any(CreateUserAddressRequest.class))).thenReturn(NEW_ADDRESS_ID);
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(USERS_API_PATH + "/some-user-id/addresses")
                .then()
                .statusCode(201)
                .header("Location", is(TEST_CREATED_USER_ADDRESS_LOCATION));
    }

    /**
     * Delete user address.
     */
    @Test
    void deleteUserAddress() {
        doNothing().when(userService).deleteUserAddress(eq(SHOPBEE), anyString(), anyString());
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .delete(USERS_API_PATH + "/some-user-id/addresses/some-address-id")
                .then()
                .statusCode(204);
    }

    /**
     * Delete user by id.
     */
    @Test
    void deleteUserById() {
        doNothing().when(userService).deleteUserById(eq(SHOPBEE), anyString());
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .delete(USERS_API_PATH + "/some-user-id")
                .then()
                .statusCode(204);
    }

    /**
     * Gets user addresses.
     */
    @Test
    void getUserAddresses() {
        when(userService.getUserAddresses(eq(SHOPBEE), anyString(), any(), any())).thenReturn(List.of());
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .get(USERS_API_PATH + "/some-user-id/addresses")
                .then()
                .statusCode(200);
    }

    /**
     * Gets user by id.
     */
    @Test
    void getUserById() {
        UserDTO user = getMockUsers(0, 1).get(0);
        when(userService.getUserById(SHOPBEE, user.getId())).thenReturn(user);
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .get(USERS_API_PATH + "/" + user.getId())
                .then()
                .statusCode(200)
                .body("id", is(user.getId()));
    }

    /**
     * Gets user by id not found.
     */
    @Test
    void getUserById_notFound() {
        when(userService.getUserById(SHOPBEE, NON_EXISTING_USER_ID)).thenThrow(ApiServiceException.notFound("User not found"));
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .get(USERS_API_PATH + "/" + NON_EXISTING_USER_ID)
                .then()
                .statusCode(404);
    }


    /**
     * Patch user address.
     */
    @Test
    void patchUserAddress() {
        PatchUserAddressRequest request = new PatchUserAddressRequest();
        request.setStreet("789 Main St");
        doNothing().when(userService).patchUserAddress(eq(SHOPBEE), anyString(), anyString(), any(PatchUserAddressRequest.class));
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .contentType("application/json")
                .body(request)
                .patch(USERS_API_PATH + "/some-user-id/addresses/some-address-id")
                .then()
                .statusCode(200);
    }

    /**
     * Patch user by id.
     */
    @Test
    void patchUserById() {
        PatchUserByIdRequest request = new PatchUserByIdRequest();
        request.setEmail("new@email.com");
        doNothing().when(userService).patchUserById(eq(SHOPBEE), anyString(), any(PatchUserByIdRequest.class));
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .contentType("application/json")
                .body(request)
                .patch(USERS_API_PATH + "/some-user-id")
                .then()
                .statusCode(200);
    }

    /**
     * Update user address.
     */
    @Test
    void updateUserAddress() {
        CreateUserAddressRequest request = new CreateUserAddressRequest()
                .type(Address.Type.WORK.name())
                .street(TEST_STREET)
                .city(TEST_CITY)
                .district(TEST_DISTRICT)
                .postalCode(POSTAL_CODE)
                .ward(TEST_WARD);
        doNothing().when(userService).updateUserAddress(eq(SHOPBEE), anyString(), anyString(), any(CreateUserAddressRequest.class));
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .contentType("application/json")
                .body(request)
                .put(USERS_API_PATH + "/some-user-id/addresses/some-address-id")
                .then()
                .statusCode(200);
    }

    /**
     * Update user by id.
     */
    @Test
    void updateUserById() {
        UpdateUserByIdRequest request = new UpdateUserByIdRequest();
        request.setEmail("new@email.com");
        doNothing().when(userService).updateUserById(eq(SHOPBEE), anyString(), any(UpdateUserByIdRequest.class));
        given()
                .when()
                .header(TENANT_ID, SHOPBEE)
                .contentType("application/json")
                .body(request)
                .put(USERS_API_PATH + "/some-user-id")
                .then()
                .statusCode(200);
    }

    /**
     * Gets mock users.
     *
     * @param offset the offset
     * @param limit  the limit
     * @return the mock users
     */
    private List<UserDTO> getMockUsers(int offset, int limit) {
        List<UserDTO> users = getMockData(USERS_JSON, new TypeReference<>() {
        });
        return users.stream().skip(offset).limit(limit).toList();
    }
}