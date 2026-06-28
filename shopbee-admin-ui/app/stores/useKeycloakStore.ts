import {defineStore} from 'pinia'
import type Keycloak from "keycloak-js";

export interface KeycloakState {
    instance: Keycloak | null;
    isInitialized: boolean;
    authenticated: boolean;
    token: string | null;
    refreshToken: string | null;
    idToken: string | null;
    roles: string[];
    userInfo: UserInfo | null;
}

export const useKeycloakStore = defineStore('keycloak', {
    state: (): KeycloakState => ({
        instance: null,
        isInitialized: false,
        authenticated: false,
        token: null,
        refreshToken: null,
        idToken: null,
        roles: [],
        userInfo: null,
    }),
    actions: {
        setInstance(kc: Keycloak) {
            this.instance = kc;
            this.authenticated = kc.authenticated ?? false;
            this.token = kc.token ?? null;
            this.refreshToken = kc.refreshToken ?? null;
            this.idToken = kc.idToken ?? null;
            this.roles = kc.realmAccess?.roles ?? [];
            this.isInitialized = true;
        },
        updateTokens() {
            if (!this.instance) return;
            this.token = this.instance.token ?? null;
            this.refreshToken = this.instance.refreshToken ?? null;
            this.idToken = this.instance.idToken ?? null;
            this.authenticated = this.instance.authenticated ?? false;
        }
    },
    getters: {
        hasRole: (state) => (role: string) => state.roles.includes(role),
        isPlatformAdmin: (state) => state.roles.includes('platform-administrator') || state.roles.includes('PLATFORM_ADMIN'),
        isStoreOwner: (state) => state.roles.includes('store-owner') || state.roles.includes('STORE_OWNER'),
        isStoreSupervisor: (state) => state.roles.includes('store-supervisor') || state.roles.includes('STORE_SUPERVISOR'),
    }
})