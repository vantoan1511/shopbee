// https://nuxt.com/docs/api/configuration/nuxt-config
import Shopbee from './app/assets/presets/shopbee';
import tailwindcss from "@tailwindcss/vite";
import Aura from "@primeuix/themes/aura";

export default defineNuxtConfig({
    compatibilityDate: '2025-07-15',
    devtools: {enabled: false},
    vite: {
        optimizeDeps: {
            include: [
                '@vue/devtools-core',
                '@vue/devtools-kit',
            ]
        },
        plugins: [
            tailwindcss(),
        ],
        css: {
            preprocessorOptions: {
                scss: {
                    additionalData: '@use "~/assets/_colors.scss" as *;',
                }
            }
        }
    },

    ssr: true,

    app: {
        head: {
            title: 'Shopbee Administration UI', // default fallback title
            htmlAttrs: {
                lang: 'en',
            },
            link: [
                {rel: 'icon', type: 'image/x-icon', href: '/favicon.ico'},
            ],
        },
        pageTransition: {name: 'page', mode: 'out-in'},
        layoutTransition: {name: 'layout', mode: 'out-in'},
    },

    css: ['~/assets/css/main.css'],

    runtimeConfig: {
        public: {
            apiUrl: '/api', // Change to relative path for proxy
            keycloakDisabled: false,
            keycloakUrl: 'http://shopbee.com.local',
            keycloakRealm: 'shopbee',
            keycloakClientId: 'shopbee-application',
        }
    },

    // Proxy configuration to bypass CORS during development
    routeRules: {
        '/api/**': {
            proxy: 'http://shopbee.com.local/api/**'
        }
    },

    modules: ['@primevue/nuxt-module', '@pinia/nuxt', '@nuxtjs/color-mode'],

    colorMode: {
        preference: 'system',
        fallback: 'light',
        globalName: '__NUXT_COLOR_MODE__',
        componentName: 'ColorScheme',
        classPrefix: '',
        classSuffix: '',
        storage: 'localStorage',
        storageKey: 'shopbee-color-mode'
    },

    primevue: {
        autoImport: true,
        options: {
            ripple: true,
            theme: {
                preset: Shopbee,
                options: {
                    darkModeSelector: '.dark',
                    cssLayer: {
                        name: 'primevue',
                        order: 'theme, base, primevue'
                    }
                }
            },
        }
    }
})