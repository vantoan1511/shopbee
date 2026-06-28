import type {
    KeycloakLoginOptions,
    KeycloakLogoutOptions,
    KeycloakTokenParsed as BaseKeycloakTokenParsed
} from 'keycloak-js';

export interface Tenant {
    name: string;
    status: 'ACTIVE' | 'INACTIVE' | 'DELETED' | string;
    createdAt: string;
    updatedAt: string;
}

export interface KeycloakTokenParsed extends BaseKeycloakTokenParsed {
    preferred_username?: string;
    email?: string;
    given_name?: string;
    family_name?: string;
    tenantId?: string;
    tenant_id?: string;
}

export type AuthRole =
    'PLATFORM_ADMIN'
    | 'STORE_OWNER'
    | 'STORE_SUPERVISOR'
    | 'platform-administrator'
    | 'store-owner'
    | 'store-supervisor';

export interface UserInfo {
    username?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    roles: AuthRole[];
}

export type {KeycloakLoginOptions, KeycloakLogoutOptions};
