import {defineStore} from 'pinia';

export const useAuthenticationStore = defineStore('auth', () => {
    const keycloakStore = useKeycloakStore();

    const isAuthenticated = computed(() => keycloakStore.authenticated);
    const roles = computed(() => keycloakStore.roles);
    const token = computed(() => keycloakStore.token);

    // Tenant ID extraction
    const tenantId = computed(() => {
        const tokenParsed = keycloakStore.instance?.tokenParsed as KeycloakTokenParsed;
        // 1. Try to get from JWT claims
        let tid = tokenParsed?.tenantId || tokenParsed?.tenant_id;

        // 2. Fallback to the realm name from the Keycloak instance
        if (!tid && keycloakStore.instance?.realm) {
            tid = keycloakStore.instance.realm;
        }

        return (tid as string) || null;
    });

    const login = async (options?: KeycloakLoginOptions) => {
        if (keycloakStore.instance) {
            await keycloakStore.instance.login(options);
        }
    };

    const logout = async (options?: KeycloakLogoutOptions) => {
        if (keycloakStore.instance) {
            await keycloakStore.instance.logout(options);
        }
    };

    const updateToken = async (minValidity: number = 30) => {
        if (keycloakStore.instance) {
            const refreshed = await keycloakStore.instance.updateToken(minValidity);
            if (refreshed) {
                keycloakStore.updateTokens();
            }
            return refreshed;
        }
        return false;
    };

    return {
        isAuthenticated,
        roles,
        token,
        tenantId,
        login,
        logout,
        updateToken
    };
});
