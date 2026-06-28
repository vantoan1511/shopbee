<template>
  <header class="h-16 flex justify-between items-center px-6 border-none z-20 shadow">
    <div class="flex items-center gap-4">
      <!-- Toggle button always visible to allow collapsing/expanding on all screens -->
      <Button icon="pi pi-bars" severity="secondary" variant="text" @click="toggleSidebar" class=""/>

      <div class="flex items-center gap-2">
        <div class="w-7 h-7 bg-primary rounded flex items-center justify-center">
          <i class="pi pi-shop text-white text-xs"></i>
        </div>
        <h1 class="text-lg font-bold text-primary hidden sm:block">Shopbee</h1>
      </div>
      <!-- Breadcrumb placeholder -->
      <nav class="hidden md:flex items-center text-sm">
        <Breadcrumb :home="home" :model="items">
          <template #item="{ item, props }">
            <NuxtLink v-if="item.url" v-ripple :to="item.url" v-bind="props.action" class="flex items-center group">
              <span v-if="item.icon"
                    :class="[item.icon, 'text-surface-400 group-hover:text-primary transition-colors mr-2']"/>
              <span
                  class="font-medium text-surface-600 dark:text-surface-400 group-hover:text-primary transition-colors">{{
                  item.label
                }}</span>
            </NuxtLink>
            <span v-else class="flex items-center cursor-default">
              <span v-if="item.icon" :class="[item.icon, 'text-surface-400 mr-2']"/>
              <span class="font-bold text-surface-900 dark:text-surface-0">{{ item.label }}</span>
            </span>
          </template>
        </Breadcrumb>
      </nav>
    </div>

    <div class="flex items-center gap-3">
      <ClientOnly>
        <div v-if="isInitialized" class="flex items-center gap-2">
          <!-- Dark Mode Toggle -->
          <Button :icon="isDarkMode ? 'pi pi-sun' : 'pi pi-moon'" severity="secondary" variant="text" rounded
                  @click="toggleDarkMode" class=""/>

          <!-- Notification Bell -->
          <Button icon="pi pi-bell" severity="secondary" variant="text" rounded class=""/>


          <div class="h-8 w-[1px]  mx-1"></div>

          <!-- User Profile Dropdown -->
          <div aria-controls="overlay_menu" aria-haspopup="true"
               class="flex items-center gap-3 pl-2 pr-1 py-1 rounded-full hover:bg-surface-100 dark:hover:bg-surface-800 cursor-pointer transition-all duration-300 border border-dashed border-primary/70"
               @click="toggleMenu">
            <div class="hidden md:flex flex-col items-end leading-tight">
              <span class="text-sm font-bold ">{{ username }}</span>
              <span class="text-[.5rem] font-bold text-primary uppercase tracking-wider">{{ primaryRoleLabel }}</span>
            </div>

            <Avatar :label="username.charAt(0).toUpperCase()" class="bg-primary-500 text-primary-contrast font-bold"
                    shape="circle"/>
            <i :class="userMenuOpen ? 'pi pi-chevron-up' : 'pi pi-chevron-down'"
               class="text-[10px] text-surface-400 mr-1"></i>
          </div>

          <Menu id="overlay_menu" ref="menu" :model="menuItems" :popup="true"
                class="mt-2 min-w-[240px] shadow-md border-none">
            <template #start>
              <div class="p-4 bg-surface-100 dark:bg-surface-800/80 rounded-b-3xl">
                <div class="flex items-center gap-3 mb-3">
                  <Avatar :label="username.charAt(0).toUpperCase()"
                          class="bg-primary text-white font-bold shadow-lg shadow-primary/20"
                          shape="circle" size="large"/>
                  <div class="flex flex-col overflow-hidden">
                    <span class="text-sm font-bold  truncate">{{ username }}</span>
                    <span class="text-xs  truncate">{{ userEmail }}</span>
                  </div>
                </div>

                <div class="space-y-2 p-2">
                  <div class="flex items-center justify-between">
                    <span class="text-[10px] font-bold  uppercase">Context</span>
                    <Tag :value="primaryRoleLabel" severity="primary" size="small" class="text-[9px] px-1.5 py-0"/>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-[10px] font-bold  uppercase">Tenant</span>
                    <span class="text-[11px] font-mono font-bold text-surface-700 dark:text-surface-300">{{
                        tenantId || 'Platform'
                      }}</span>
                  </div>
                </div>
              </div>
            </template>

            <template #item="{ item, props }">
              <a v-ripple
                 class="flex items-center px-4 py-3 cursor-pointer group hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors"
                 v-bind="props.action">
                <span
                    :class="[item.icon, 'text-surface-500 group-hover:text-primary group-hover:scale-110 transition-all duration-300']"/>
                <span class="ml-3 text-sm font-medium text-surface-700 dark:text-surface-300">{{
                    item.label
                  }}</span>
                <Badge v-if="item.badge" class="ml-auto" :value="item.badge"/>
              </a>
            </template>
          </Menu>
        </div>

        <template #fallback>
          <div class="flex items-center gap-3">
            <div class="h-8 w-24 bg-surface-100 dark:bg-surface-800 animate-pulse rounded-lg hidden sm:block"></div>
            <div class="h-10 w-10 bg-surface-100 dark:bg-surface-800 animate-pulse rounded-full"></div>
          </div>
        </template>
      </ClientOnly>
    </div>
  </header>
</template>

<script setup lang="ts">
import type {MenuItem} from "primevue/menuitem";

const {toggleSidebar, toggleDarkMode, isDarkMode} = useLayout();
const route = useRoute();
const router = useRouter();
const {roles, tenantId, logout: authLogout} = useAuthentication();
const authStore = useAuthenticationStore();
const keycloakStore = useKeycloakStore();
const {isInitialized} = storeToRefs(keycloakStore);

const home: MenuItem = {
  icon: 'pi pi-home',
  url: '/'
}

/**
 * Dynamic breadcrumb logic
 */
const items: any = computed(() => {
  const path = route.path;
  if (path === '/' || path === '/login') return [];

  const parts = path.split('/').filter(p => p);
  const breadcrumbs: MenuItem[] = [];
  let currentPath = '';

  const t = (key: string, defaultValue: string) => {
    const labels: Record<string, string> = {
      'tenants': 'Tenants',
      'products': 'Products',
      'settings': 'Settings',
      'profile': 'My Profile'
    };
    return labels[key.toLowerCase()] || defaultValue;
  };

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i] || '';
    currentPath += `/${part}`;

    const resolved = router.resolve(currentPath);
    const matchedRecord = resolved.matched[resolved.matched.length - 1];

    let label = '';

    if (matchedRecord?.meta?.breadcrumb) {
      label = typeof matchedRecord.meta.breadcrumb === 'function'
          ? matchedRecord.meta.breadcrumb(route)
          : matchedRecord.meta.breadcrumb as string;
    }

    if (!label) {
      label = t(part, part.charAt(0).toUpperCase() + part.slice(1));
    }

    breadcrumbs.push({
      label,
      url: currentPath,
      disabled: i === parts.length - 1
    } as MenuItem);
  }

  return breadcrumbs;
})
const menu = ref();
const userMenuOpen = ref(false);
const toggleMenu = (event: Event) => {
  menu.value.toggle(event);
  userMenuOpen.value = !userMenuOpen.value;
};

const tokenParsed = computed(() => keycloakStore.instance?.tokenParsed as KeycloakTokenParsed);
const username = computed(() => tokenParsed.value?.preferred_username || 'User');
const userEmail = computed(() => tokenParsed.value?.email || 'user@shopbee.com');

const primaryRoleLabel = computed(() => {
  if (roles.value.includes('platform-administrator') || roles.value.includes('PLATFORM_ADMIN')) return 'Platform Admin';
  if (roles.value.includes('STORE_OWNER')) return 'Store Owner';
  if (roles.value.includes('STORE_SUPERVISOR')) return 'Supervisor';
  return 'Member';
});

const menuItems = ref([
  {
    label: 'My Profile',
    icon: 'pi pi-user',
  },
  {
    label: 'System Settings',
    icon: 'pi pi-cog',
    visible: () => roles.value.includes('PLATFORM_ADMIN')
  },
  {
    separator: true
  },
  {
    label: 'Refresh Session',
    icon: 'pi pi-refresh',
    command: async () => {
      const refreshed = await authStore.updateToken(0);
      if (refreshed) {
        // Toast notification would be better
      }
    }
  },
  {
    label: 'Logout',
    icon: 'pi pi-power-off',
    class: 'text-red-500',
    command: async () => {
      await authLogout();
    }
  }
]);
</script>

<style scoped>
:deep(.p-menu) {
  padding: 0;
  overflow: hidden;
}

:deep(.p-menu-list) {
  padding: 0.5rem 0;
}
</style>
