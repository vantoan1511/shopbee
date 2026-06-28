import Keycloak, {type KeycloakInitOptions, type KeycloakServerConfig} from "keycloak-js";

export default defineNuxtPlugin(async () => {
    if (import.meta.server) return;

    const config = useRuntimeConfig();
    const keycloakStore = useKeycloakStore();

    const keycloakConfig: KeycloakServerConfig = {
        url: config.public.keycloakUrl as string,
        realm: config.public.keycloakRealm as string,
        clientId: config.public.keycloakClientId as string,
    }

    const keycloak = new Keycloak(keycloakConfig);

    // Change onLoad to 'check-sso' to allow the app to load even if not logged in
    // This prevents the infinite redirect loop and allows us to use our own middleware/pages
    const initOptions: KeycloakInitOptions = {
        onLoad: "check-sso",
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        checkLoginIframe: false,
    }

    try {
        const authenticated = await keycloak.init(initOptions);
        keycloakStore.setInstance(keycloak);

        // Clean up the URL: Remove Keycloak-specific parameters (state, code, etc.)
        // from the hash fragment after successful processing.
        if (window.location.hash.includes('state=') || window.location.hash.includes('code=')) {
            // Use replaceState to clean the URL without triggering a route change or reload
            window.history.replaceState(
                window.history.state,
                document.title,
                window.location.pathname + window.location.search
            );
        }

        // Refresh token logic
        setInterval(async () => {
            if (keycloak.authenticated) {
                const refreshed = await keycloak.updateToken(70);
                if (refreshed) keycloakStore.updateTokens();
            }
        }, 60000);

    } catch (error) {
        console.error('Keycloak initialization failed', error);
    }
})
