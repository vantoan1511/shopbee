import type {RouterConfig} from '@nuxt/schema'

// https://router.vuejs.org/api/#routeroptions
export default <RouterConfig>{
    scrollBehavior(to, from, savedPosition) {
        const nuxtApp = useNuxtApp()

        // If the hash contains Keycloak specific parameters (state, code, etc.),
        // we should NOT try to scroll to it as it's not a valid CSS selector.
        if (to.hash && (to.hash.includes('state=') || to.hash.includes('code='))) {
            return false
        }

        // Standard Nuxt scroll behavior below
        if (savedPosition) {
            return savedPosition
        }

        if (to.hash) {
            return {
                el: to.hash,
                top: 0,
                behavior: 'smooth'
            }
        }

        return {top: 0, left: 0}
    }
}
