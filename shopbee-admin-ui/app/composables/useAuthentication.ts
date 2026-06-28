import {storeToRefs} from 'pinia';

export function useAuthentication() {
    const authStore = useAuthenticationStore();
    const {isAuthenticated, roles, token, tenantId} = storeToRefs(authStore);

    const login = async (options?: KeycloakLoginOptions) => {
        await authStore.login(options);
    };

    const logout = async (options?: KeycloakLogoutOptions) => {
        await authStore.logout(options);
    };

    const hasRole = (role: string) => {
        return roles.value.includes(role);
    };

    return {
        isAuthenticated,
        roles,
        token,
        tenantId,
        login,
        logout,
        hasRole
    };
}

export function useApi() {
    const authStore = useAuthenticationStore();
    const config = useRuntimeConfig();

    type FetchOptions = Parameters<typeof $fetch>[1];

    const fetchApi = async <T>(url: string, options?: FetchOptions): Promise<T> => {
        // Ensure token is fresh before critical API calls
        await authStore.updateToken();

        const headers: Record<string, string> = {
            ...(options?.headers as Record<string, string>),
            Authorization: `Bearer ${authStore.token}`,
        };

        // Propagate tenantId if it exists
        if (authStore.tenantId) {
            headers['tenantId'] = String(authStore.tenantId);
        }

        return $fetch<T>(url, {
            baseURL: config.public.apiUrl || '/api',
            ...options,
            headers
        } as any);
    };

    return {
        fetchApi,
    };
}
