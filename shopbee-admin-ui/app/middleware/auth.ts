export default defineNuxtRouteMiddleware((to, from) => {
    // Keycloak-js only works on the client side
    if (import.meta.server) return;

    const authStore = useAuthenticationStore();

    // If the user is not authenticated and not already on the login page
    if (!authStore.isAuthenticated) {
        // Option A: Simply trigger Keycloak redirect immediately
        // authStore.login();

        // Option B: Redirect to your custom login page (if you want a landing page)
        if (to.path !== '/login') {
            return navigateTo('/login');
        }
    }
})
