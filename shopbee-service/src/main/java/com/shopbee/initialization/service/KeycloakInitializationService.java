package com.shopbee.initialization.service;

import com.shopbee.common.StringFormatter;
import com.shopbee.common.Utils;
import com.shopbee.common.exception.BusinessException;
import com.shopbee.common.exception.NotFoundException;
import com.shopbee.security.auth.Permission;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.shopbee.security.auth.RoleName.APPLICATION_ADMINISTRATION;
import static com.shopbee.security.auth.RoleName.BUSINESS_ADMINISTRATION;

@ApplicationScoped
public class KeycloakInitializationService {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakInitializationService.class);
    private static final String DEFAULT_CLIENT_ID = "shopbee-application"; // Changed to avoid conflict with realm name
    private static final String VERIFY_PROFILE = "VERIFY_PROFILE";
    private static final String SERVICE_ACCOUNT_PREFIX = "service-account-";
    private static final String SHOPBEE_PERMISSIONS_CLIENT_SCOPE = "shopbee-permissions";
    private static final String KEYCLOAK_INITIALIZATION_ERROR = "KEYCLOAK_INITIALIZATION_ERROR";
    private final KeycloakConfig keycloakConfig;
    private Keycloak keycloak;

    public KeycloakInitializationService(KeycloakConfig keycloakConfig) {
        this.keycloakConfig = keycloakConfig;
    }

    @PostConstruct
    void initKeycloak() {
        keycloak = KeycloakBuilder
                .builder()
                .serverUrl("http://shopbee-keycloak:8080/")
                .realm("master")
                .username(keycloakConfig.admin().username())
                .password(keycloakConfig.admin().password())
                .clientId("admin-cli")
                .build();
    }

    @PreDestroy
    void closeKeycloak() {
        if (keycloak != null) {
            keycloak.close();
        }
    }

    public void initialize() {
        try {
            waitForKeycloakReadiness();
            ensureDefaultTenant();
            ensureDefaultClientScopes();
            ensureDefaultClients();
            ensureRoles();
            ensureUsers();
            ensureServiceAccountUserRoles(APPLICATION_ADMINISTRATION, APPLICATION_ADMINISTRATION);
            ensureServiceAccountUserRoles(BUSINESS_ADMINISTRATION, BUSINESS_ADMINISTRATION);
        } catch (Exception e) {
            LOG.error("Failed to initialize Keycloak: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    private void ensureDefaultClientScopes() {
        if (isClientScopeExisted(SHOPBEE_PERMISSIONS_CLIENT_SCOPE)) {
            LOG.debug("Client scope {} already exists. Skipping creation.", SHOPBEE_PERMISSIONS_CLIENT_SCOPE);
            return;
        }
        ClientScopeRepresentation scopeRepresentation = buildShopbeePermissionsClientScope();
        String clientScopeId = createClientScope(scopeRepresentation);
        String protocolMapperId = createProtocolMapper(clientScopeId, buildShopbeePermissionsProtocolMapper());
        keycloak.realm(keycloakConfig.defaultRealm()).addDefaultDefaultClientScope(clientScopeId);
        LOG.info("Client scope {} with protocol mapper {} created and added to default client scopes.", clientScopeId, protocolMapperId);
    }

    private boolean isClientScopeExisted(String clientScopeName) {
        try {
            List<ClientScopeRepresentation> clientScopes = keycloak.realm(keycloakConfig.defaultRealm()).clientScopes().findAll();
            for (ClientScopeRepresentation scope : clientScopes) {
                if (scope.getName().equals(clientScopeName)) {
                    return true;
                }
            }
        } catch (WebApplicationException e) {
            LOG.error("Failed to get client scopes: {}", e.getMessage(), e);
            throw e;
        }
        return false;
    }

    private ProtocolMapperRepresentation buildShopbeePermissionsProtocolMapper() {
        ProtocolMapperRepresentation representation = new ProtocolMapperRepresentation();
        representation.setName("shopbee-permissions-mapper");
        representation.setProtocol("openid-connect");
        representation.setProtocolMapper("oidc-usermodel-client-role-mapper");
        Map<String, String> config = Map.of(
                "multivalued", "true",
                "usermodel.clientRoleMapping.clientId", DEFAULT_CLIENT_ID,
                "usermodel.clientRoleMapping.rolePrefix", "",
                "claim.name", "permissions",
                "jsonType.label", "String",
                "id.token.claim", "false",
                "access.token.claim", "true",
                "lightweight.claim", "false",
                "userinfo.token.claim", "false",
                "introspection.token.claim", "false"
        );
        representation.setConfig(config);
        return representation;
    }

    private String createProtocolMapper(String clientScopeId, ProtocolMapperRepresentation protocolMapperRepresentation) {
        try (Response createProtocolMapperResponse = keycloak.realm(keycloakConfig.defaultRealm()).clientScopes().get(clientScopeId).getProtocolMappers().createMapper(protocolMapperRepresentation);) {
            if (createProtocolMapperResponse.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                String message = createProtocolMapperResponse.readEntity(String.class);
                throw new BusinessException(KEYCLOAK_INITIALIZATION_ERROR, StringFormatter.format("Failed to create protocol mapper for client scope {}: {}", clientScopeId, message));
            }
            LOG.info("Protocol mapper for client scope {} created.", clientScopeId);
            return Utils.getResourceIdFromResponse(createProtocolMapperResponse);
        } catch (WebApplicationException e) {
            LOG.warn("Failed to create protocol mapper for client scope {}: {}", clientScopeId, e.getMessage());
            throw e;
        }
    }

    private static ClientScopeRepresentation buildShopbeePermissionsClientScope() {
        ClientScopeRepresentation scopeRepresentation = new ClientScopeRepresentation();
        scopeRepresentation.setName(SHOPBEE_PERMISSIONS_CLIENT_SCOPE);
        scopeRepresentation.setProtocol("openid-connect");
        Map<String, String> attributes = Map.of(
                "display.on.consent.screen", "true",
                "consent.screen.text", "Shopbee Permissions",
                "include.in.token.scope", "false",
                "include.in.openid.provider.metadata", "true");
        scopeRepresentation.setAttributes(attributes);
        return scopeRepresentation;
    }

    private String createClientScope(ClientScopeRepresentation clientScopeRepresentation) {
        try (Response createClientScopeResponse = keycloak.realm(keycloakConfig.defaultRealm()).clientScopes().create(clientScopeRepresentation);) {
            if (createClientScopeResponse.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                String message = createClientScopeResponse.readEntity(String.class);
                throw new BusinessException(KEYCLOAK_INITIALIZATION_ERROR, StringFormatter.format("Failed to create client scope {}: {}", clientScopeRepresentation.getName(), message));
            }
            LOG.info("Client scope {} created.", clientScopeRepresentation.getName());
            return Utils.getResourceIdFromResponse(createClientScopeResponse);
        } catch (WebApplicationException e) {
            LOG.warn("Failed to create client scope {}: {}", clientScopeRepresentation.getName(), e.getMessage());
            throw e;
        }
    }

    private static Set<String> clientRoles() {
        Set<String> result = new HashSet<>();
        for (Permission permission : Permission.values()) {
            result.add(permission.getValue());
        }
        return result;
    }

    private void ensureUsers() {
        if (isAdminUserExisted()) {
            LOG.debug("Admin user already exists. Skipping creation.");
            return;
        }

        UsersResource shopbeeUsersResource = keycloak.realm(keycloakConfig.defaultRealm()).users();
        String shopbeeAdminUserId = createUser(buildShopbeeAdminUser());
        List<RoleRepresentation> roles = List.of(getRealmRoleByName(APPLICATION_ADMINISTRATION), getRealmRoleByName(BUSINESS_ADMINISTRATION));
        shopbeeUsersResource.get(shopbeeAdminUserId).roles().realmLevel().add(roles);
    }

    private RoleRepresentation getRealmRoleByName(String roleName) {
        try {
            return keycloak.realm(keycloakConfig.defaultRealm()).roles().get(roleName).toRepresentation();
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException(KEYCLOAK_INITIALIZATION_ERROR, "Role not found: " + roleName);
            }
            throw e;
        }
    }

    private String createUser(UserRepresentation userRepresentation) {
        UsersResource usersResource = keycloak.realm(keycloakConfig.defaultRealm()).users();
        try (Response createUserResponse = usersResource.create(userRepresentation);) {
            if (createUserResponse.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                String message = createUserResponse.readEntity(String.class);
                throw new BusinessException(KEYCLOAK_INITIALIZATION_ERROR, StringFormatter.format("Failed to create user {}: {}", userRepresentation.getUsername(), message));
            }
            LOG.info("User {} created.", userRepresentation.getUsername());
            return Utils.getResourceIdFromResponse(createUserResponse);
        } catch (WebApplicationException e) {
            LOG.warn("Failed to create user {}: {}", userRepresentation.getUsername(), e.getMessage());
            throw e;
        }
    }

    private boolean isAdminUserExisted() {
        return isUserExisted(keycloakConfig.admin().username());
    }

    private boolean isUserExisted(String username) {
        UsersResource shopbeeUsersResource = keycloak.realm(keycloakConfig.defaultRealm()).users();
        try {
            List<UserRepresentation> usersByUsername = shopbeeUsersResource.searchByUsername(username, true);
            return !usersByUsername.isEmpty();
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR) {
                return false;
            }
            throw e;
        }
    }

    private void ensureRoles() {
        ensureApplicationAdministrationRole();
        ensureBusinessAdministrationRole();
    }

    private void ensureBusinessAdministrationRole() {
        if (isBusinessAdministrationRoleExisted()) {
            return;
        }

        RoleRepresentation businessAdministrationRoleRepresentation = buildBusinessAdministrationRole();
        keycloak.realm(keycloakConfig.defaultRealm()).roles().create(businessAdministrationRoleRepresentation);

        keycloak.realm(keycloakConfig.defaultRealm()).roles().get(BUSINESS_ADMINISTRATION).addComposites(List.of(getClientRole(DEFAULT_CLIENT_ID, Permission.PRODUCT_MANAGE.getValue()), getClientRole(DEFAULT_CLIENT_ID, Permission.USER_MANAGE.getValue())));
    }

    private void ensureApplicationAdministrationRole() {
        if (isApplicationAdministrationRoleExisted()) {
            return;
        }

        RoleRepresentation administrationRoleRepresentation = buildApplicationAdministrationRole();
        keycloak.realm(keycloakConfig.defaultRealm()).roles().create(administrationRoleRepresentation);

        RoleRepresentation realmAdminRole = getClientRoleByName("realm-management", "realm-admin");
        keycloak.realm(keycloakConfig.defaultRealm()).roles().get(APPLICATION_ADMINISTRATION).addComposites(List.of(realmAdminRole));
    }

    private RoleRepresentation getClientRoleByName(String clientName, String roleName) {
        List<RoleRepresentation> clientRoles = getClientRoles(clientName);
        for (RoleRepresentation role : clientRoles) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        throw new WebApplicationException("Role " + roleName + " not found in client " + clientName, Response.Status.NOT_FOUND);
    }

    private List<RoleRepresentation> getClientRoles(String clientName) {
        ClientRepresentation client = getClientByName(clientName);
        String clientId = client.getId();
        ClientResource clientResource = keycloak.realm(keycloakConfig.defaultRealm()).clients().get(clientId);
        return clientResource.roles().list();
    }

    private ClientRepresentation getClientByName(String clientName) {
        try {
            List<ClientRepresentation> clients = keycloak.realm(keycloakConfig.defaultRealm()).clients().findByClientId(clientName);
            if (!clients.isEmpty()) {
                return clients.getFirst();
            }
        } catch (WebApplicationException e) {
            LOG.error("Failed to get client {}: {}", clientName, e.getMessage(), e);
            throw e;
        }
        throw new WebApplicationException("Client " + clientName + " not found", Response.Status.NOT_FOUND);
    }

    private boolean isBusinessAdministrationRoleExisted() {
        return isRoleExisted(BUSINESS_ADMINISTRATION);
    }

    private boolean isApplicationAdministrationRoleExisted() {
        return isRoleExisted(APPLICATION_ADMINISTRATION);
    }

    private boolean isRoleExisted(String roleName) {
        RolesResource shopbeeRolesResource = keycloak.realm(keycloakConfig.defaultRealm()).roles();
        try {
            return shopbeeRolesResource.get(roleName).toRepresentation() != null;
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return false;
            }
            throw e;
        }
    }

    private void ensureDefaultClients() {
        ensureShopbeeClient();

        if (isClientExisted(BUSINESS_ADMINISTRATION)) {
            LOG.debug("Client {} already exists. Skipping creation.", BUSINESS_ADMINISTRATION);
        } else {
            String businessAdminClientId = createClient(buildBusinessAdministrationClient());
        }

        if (isClientExisted(APPLICATION_ADMINISTRATION)) {
            LOG.debug("Client {} already exists. Skipping creation.", APPLICATION_ADMINISTRATION);
        } else {
            String applicationAdminClientId = createClient(buildApplicationAdminClient());
        }
    }

    private void ensureServiceAccountUserRoles(String clientName, String roleName) {
        String serviceAccountUsername = SERVICE_ACCOUNT_PREFIX + clientName;
        UserRepresentation serviceAccountUser = getUserByUsername(serviceAccountUsername);
        String userId = serviceAccountUser.getId();
        keycloak.realm(keycloakConfig.defaultRealm()).users().get(userId).roles().realmLevel().add(List.of(getRealmRoleByName(roleName)));
    }

    private UserRepresentation getUserByUsername(String username) {
        try {
            List<UserRepresentation> users = keycloak.realm(keycloakConfig.defaultRealm()).users().searchByUsername(username, true);
            if (!users.isEmpty()) {
                return users.getFirst();
            }
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatusInfo().getFamily() != Response.Status.Family.CLIENT_ERROR) {
                String message = e.getResponse().readEntity(String.class);
                throw new BusinessException(KEYCLOAK_INITIALIZATION_ERROR, StringFormatter.format("Failed to get user by username {}: {}", username, message));
            }
        }
        throw new NotFoundException(KEYCLOAK_INITIALIZATION_ERROR, "User not found: " + username);
    }

    private void ensureShopbeeClient() {
        if (isClientShopbeeExisted()) {
            LOG.debug("Client {} already exists. Skipping creation.", DEFAULT_CLIENT_ID);
            return;
        }

        String clientId = createClient(buildShopbeeClientRepresentation());
        RolesResource rolesResource = keycloak.realm(keycloakConfig.defaultRealm()).clients().get(clientId).roles();

        for (String clientRole : clientRoles()) {
            RoleRepresentation roleRepresentation = buildRoleRepresentation(clientRole);
            rolesResource.create(roleRepresentation);
        }

        rolesResource.get(Permission.PRODUCT_MANAGE.getValue()).addComposites(List.of(getClientRole(DEFAULT_CLIENT_ID, Permission.PRODUCT_CREATE.getValue()), getClientRole(DEFAULT_CLIENT_ID, Permission.PRODUCT_MODIFY.getValue()), getClientRole(DEFAULT_CLIENT_ID, Permission.PRODUCT_VIEW.getValue())));
        rolesResource.get(Permission.PRODUCT_MODIFY.getValue()).addComposites(List.of(getClientRole(DEFAULT_CLIENT_ID, Permission.PRODUCT_VIEW.getValue())));
        rolesResource.get(Permission.USER_MANAGE.getValue()).addComposites(List.of(getClientRole(DEFAULT_CLIENT_ID, Permission.USER_VIEW.getValue()), getClientRole(DEFAULT_CLIENT_ID, Permission.USER_CREATE.getValue()), getClientRole(DEFAULT_CLIENT_ID, Permission.USER_UPDATE.getValue()), getClientRole(DEFAULT_CLIENT_ID, Permission.USER_DELETE.getValue())));
        rolesResource.get(Permission.USER_UPDATE.getValue()).addComposites(List.of(getClientRole(DEFAULT_CLIENT_ID, Permission.USER_VIEW.getValue())));
    }

    private RoleRepresentation getClientRole(String clientId, String roleName) {
        try {
            ClientResource clientResource = keycloak.realm(keycloakConfig.defaultRealm()).clients().get(clientId);
            return clientResource.roles().get(roleName).toRepresentation();
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException(KEYCLOAK_INITIALIZATION_ERROR, StringFormatter.format("Role {} not found in client {}.", roleName, clientId));
            }
            throw e;
        }
    }

    private String createClient(ClientRepresentation clientRepresentation) {
        ClientsResource clientsResource = keycloak.realm(keycloakConfig.defaultRealm()).clients();
        String clientName = clientRepresentation.getName();
        try (Response createShopbeeClientResponse = clientsResource.create(clientRepresentation);) {
            if (createShopbeeClientResponse.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                String message = createShopbeeClientResponse.readEntity(String.class);
                throw new BusinessException(KEYCLOAK_INITIALIZATION_ERROR, StringFormatter.format("Failed to create client {}: {}", clientName, message));
            }
            LOG.info("Client {} created.", clientName);
            return Utils.getResourceIdFromResponse(createShopbeeClientResponse);
        } catch (WebApplicationException e) {
            LOG.warn("Failed to create client {}: {}", clientName, e.getMessage());
            throw e;
        }
    }

    private boolean isClientShopbeeExisted() {
        return isClientExisted(DEFAULT_CLIENT_ID);
    }

    private boolean isClientExisted(String clientName) {
        ClientsResource clientsResource = keycloak.realm(keycloakConfig.defaultRealm()).clients();
        try {
            List<ClientRepresentation> clientsByClientId = clientsResource.findByClientId(clientName);
            if (!clientsByClientId.isEmpty()) {
                return clientsByClientId.getFirst() != null;
            }
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return false;
            }
            throw e;
        }
        return false;
    }

    private void ensureDefaultTenant() {
        try {
            if (isTenantShopbeeExisted()) {
                LOG.debug("Tenant {} already exists. Skipping creation.", keycloakConfig.defaultRealm());
                return;
            }

            LOG.debug("Tenant {} does not exist. Creating...", keycloakConfig.defaultRealm());
            RealmRepresentation shopbeeRepresentation = buildShopBeeRealmRepresentation();
            keycloak.realms().create(shopbeeRepresentation);

            AuthenticationManagementResource shopbeeFlowsResource = keycloak.realm(keycloakConfig.defaultRealm()).flows();
            RequiredActionProviderRepresentation verifyProfileAction = shopbeeFlowsResource.getRequiredAction(VERIFY_PROFILE);
            verifyProfileAction.setEnabled(false);
            shopbeeFlowsResource.updateRequiredAction(VERIFY_PROFILE, verifyProfileAction);

            LOG.info("Tenant {} created.", keycloakConfig.defaultRealm());
        } catch (WebApplicationException exception) {
            LOG.error("Failed to create tenant {}: {}", keycloakConfig.defaultRealm(), exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }
    }

    private boolean isTenantShopbeeExisted() {
        try {
            return keycloak.realm(keycloakConfig.defaultRealm()).toRepresentation() != null;
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return false;
            }
            throw exception;
        }
    }

    private void waitForKeycloakReadiness() {
        int maxRetries = 10;
        int retryCount = 0;
        int waitTimeMs = 5000; // 5 seconds

        while (retryCount < maxRetries) {
            try {
                keycloak.realms().findAll();
                LOG.info("Keycloak is ready.");
                return;
            } catch (Exception e) {
                LOG.info("Waiting for Keycloak to be ready... (attempt {}/{})", retryCount + 1, maxRetries);
                try {
                    Thread.sleep(waitTimeMs);
                } catch (InterruptedException ignored) {
                }
                retryCount++;
            }
        }
        throw new IllegalStateException("Keycloak is not ready after " + maxRetries + " attempts");
    }

    private ClientRepresentation buildShopbeeClientRepresentation() {
        ClientRepresentation result = new ClientRepresentation();
        result.setId(DEFAULT_CLIENT_ID);
        result.setName(DEFAULT_CLIENT_ID);
        result.setEnabled(true);
        result.setPublicClient(true);
        result.setDirectAccessGrantsEnabled(true);
        return result;
    }

    private ClientRepresentation buildBusinessAdministrationClient() {
        return buildServiceAccountClient(BUSINESS_ADMINISTRATION, keycloakConfig.clients().businessAdministration().secret());
    }

    private ClientRepresentation buildApplicationAdminClient() {
        return buildServiceAccountClient(APPLICATION_ADMINISTRATION, keycloakConfig.clients().applicationAdministration().secret());
    }

    private ClientRepresentation buildServiceAccountClient(String name, String secret) {
        ClientRepresentation result = new ClientRepresentation();
        result.setId(name);
        result.setName(name);
        result.setEnabled(true);
        result.setServiceAccountsEnabled(true);
        result.setPublicClient(false);
        result.setDirectAccessGrantsEnabled(false);
        result.setImplicitFlowEnabled(false);
        result.setStandardFlowEnabled(false);
        result.setSecret(secret);
        return result;
    }

    private RealmRepresentation buildShopBeeRealmRepresentation() {
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(keycloakConfig.defaultRealm());
        realmRepresentation.setEnabled(true);
        realmRepresentation.setDisplayName("ShopBee");
        realmRepresentation.setId(keycloakConfig.defaultRealm());
        realmRepresentation.setSslRequired("external");
        realmRepresentation.setRegistrationAllowed(true);
        realmRepresentation.setRegistrationEmailAsUsername(false);
        realmRepresentation.setRememberMe(true);
        realmRepresentation.setVerifyEmail(false);
        realmRepresentation.setLoginWithEmailAllowed(false);
        realmRepresentation.setResetPasswordAllowed(true);
        realmRepresentation.setAccessTokenLifespan(28800); // 8 hours
        realmRepresentation.setClientSessionMaxLifespan(28800); // 8 hours
        return realmRepresentation;
    }

    private RoleRepresentation buildApplicationAdministrationRole() {
        return buildRoleRepresentation(APPLICATION_ADMINISTRATION, "Application Administration role.");
    }

    private RoleRepresentation buildBusinessAdministrationRole() {
        return buildRoleRepresentation(BUSINESS_ADMINISTRATION, "Business Administration role.");
    }

    private RoleRepresentation buildRoleRepresentation(String roleName) {
        return buildRoleRepresentation(roleName, roleName);
    }

    private RoleRepresentation buildRoleRepresentation(String roleName, String description) {
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);
        roleRepresentation.setDescription(description);
        return roleRepresentation;
    }

    private UserRepresentation buildShopbeeAdminUser() {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(keycloakConfig.admin().username());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(buildShopbeeAdminCredentials());
        return userRepresentation;
    }

    private List<CredentialRepresentation> buildShopbeeAdminCredentials() {
        CredentialRepresentation credential = buildShopbeeAdminPasswordCredential();
        return List.of(credential);
    }

    private CredentialRepresentation buildShopbeeAdminPasswordCredential() {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(keycloakConfig.admin().password());
        credential.setTemporary(true);
        return credential;
    }
}
