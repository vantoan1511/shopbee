<template>
  <aside class="w-64 flex flex-col h-screen border-none shadow-xl transition-all duration-300">
    <!-- Brand Logo -->
    <div class="h-16 flex items-center px-6 rounded-b-2xl bg-surface-100 dark:bg-surface-800/50">
      <div class="flex items-center">
        <div class="w-8 h-8 bg-primary rounded-lg flex items-center justify-center mr-3 shadow-lg shadow-primary/20">
          <i class="pi pi-shop text-primary-contrast text-sm"></i>
        </div>
        <span class="text-xl font-bold tracking-tight text-primary">Shopbee</span>
        <span class="text-md font-bold text-muted-color">Admin</span>
        <span class="text-[.5rem] font-bold text-muted-color">v1.0.0</span>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto py-6 px-4 space-y-1 custom-scrollbar">
      <div v-for="section in navigation" :key="section.label" class="mb-6">
        <h3 v-if="section.items.length > 0"
            class="px-3 text-[10px] font-bold uppercase tracking-widest mb-2">
          {{ section.label }}
        </h3>

        <div class="space-y-1">
          <NuxtLink v-for="item in section.items" :key="item.to" :class="[
              route.path === item.to
                ? 'bg-gradient-to-r from-primary-500 to-primary-300 text-primary-contrast shadow-lg shadow-primary/20'
                : ' hover:bg-surface-100 dark:hover:bg-surface-800'
            ]"
                    :to="item.to"
                    class="flex items-center px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group">
            <i :class="[item.icon, 'mr-3 text-lg transition-transform group-hover:scale-110']"></i>
            <span>{{ item.label }}</span>
            <i v-if="route.path === item.to"
               class="pi pi-chevron-right ml-auto text-[10px] opacity-50 group-hover:translate-x-2 group-hover:scale-110 transition-all duration-150"></i>
          </NuxtLink>
        </div>
      </div>
    </nav>

    <!-- Sidebar Footer / Status -->
    <div class="p-4">
      <div class="flex items-center p-2 rounded-xl bg-surface-200/70 dark:bg-surface-800/50">
        <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse mr-3"></div>
        <div class="flex flex-col overflow-hidden">
          <span class="text-[10px] uppercase font-bold leading-none mb-1">System Status</span>
          <span class="text-xs font-medium truncate">Online</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
const route = useRoute()
const {roles} = useAuthentication();

const isPlatformAdmin = computed(() => {
  return roles.value.includes('platform-administrator') || roles.value.includes('PLATFORM_ADMIN');
});

const navigation = computed(() => [
  {
    label: 'Manage',
    items: [
      {label: 'Tenants', icon: 'pi pi-building', to: '/tenants'},
    ]
  },
  {
    label: 'Settings',
    items: [
      ...(isPlatformAdmin.value ? [
        {label: 'Platform configurations', icon: 'pi pi-cog', to: '/tenants/settings'},
      ] : [])
    ]
  }
]);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--p-surface-200);
  border-radius: 10px;
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--p-surface-800);
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--p-surface-300);
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--p-surface-700);
}
</style>
