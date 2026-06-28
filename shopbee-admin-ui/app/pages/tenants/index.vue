<template>
  <div v-if="isInitialized" class="p-6">
    <div
        class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8 p-4 rounded-2xl bg-primary-contrast shadow-sm border-none">
      <div>
        <div class="text-2xl font-bold  flex items-center gap-3">
          <div class="w-10 h-10 bg-primary/10 text-primary rounded-xl flex items-center justify-center">
            <i class="pi pi-building"></i>
          </div>
          <h1>Tenants</h1>
        </div>
        <p class=" text-sm mt-1 ml-13">
          {{ isPlatformAdmin ? 'Manage and monitor all Shopbee tenants' : 'Overview of your store performance' }}
        </p>
      </div>

      <div v-if="isPlatformAdmin" class="flex gap-2 ml-13 md:ml-0">
        <Button :loading="loading" icon="pi pi-refresh" variant="text" rounded
                @click="getTenants"/>
      </div>
      <div v-else class="flex gap-2 ml-13 md:ml-0">
        <Button icon="pi pi-calendar" label="Last 30 Days" severity="secondary" size="small" outlined/>
        <Button icon="pi pi-download" label="Export Report" severity="secondary" size="small" outlined/>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="!isInitialized || (isPlatformAdmin && loading && tenants.length === 0)"
         class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div v-for="i in 4" :key="i" class=" p-6 rounded-2xl shadow-sm">
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 rounded-xl bg-surface-100 dark:bg-surface-800 animate-pulse"></div>
          <div class="space-y-2 flex-1">
            <div class="h-4 w-24 bg-surface-100 dark:bg-surface-800 animate-pulse rounded"></div>
            <div class="h-3 w-16  animate-pulse rounded"></div>
          </div>
        </div>
        <div class="space-y-2">
          <div class="h-3 w-full  animate-pulse rounded"></div>
          <div class="h-3 w-4/5  animate-pulse rounded"></div>
        </div>
      </div>
    </div>

    <!-- Data State -->
    <div v-else class="space-y-8 animate-enter">
      <!-- Platform Admin View: Tenant Management Cards -->
      <div v-if="isPlatformAdmin">
        <div v-if="tenants.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <div v-for="tenant in tenants"
               :key="tenant.name"
               class="group p-6 rounded-2xl bg-primary-contrast hover:-translate-y-1 hover:border-primary shadow hover:shadow-xl transition-all duration-300 cursor-pointer relative overflow-hidden"
               @click="manageTenant(tenant)">
            <!-- Background Decoration -->
            <div
                class="absolute -right-4 -bottom-4 opacity-[0.03] group-hover:scale-110 transition-transform duration-500">
              <i class="pi pi-shop text-9xl"></i>
            </div>

            <!-- Status Badge -->
            <div class="absolute top-4 right-4">
              <Tag :value="tenant.status" :severity="getStatusSeverity(tenant.status)" size="small"
                   class="font-bold px-2"/>
            </div>

            <div class="flex items-center gap-4 mb-6">
              <div
                  class="w-14 h-14 flex items-center justify-center rounded-2xl group-hover:bg-primary/10 group-hover:text-primary transition-colors duration-300">
                <i class="pi pi-shop text-2xl"></i>
              </div>
              <div class="overflow-hidden">
                <h3 class="font-bold  group-hover:text-primary transition-colors truncate">
                  {{ tenant.name }}</h3>
                <p class="text-xs  font-medium">Retail & E-commerce</p>
              </div>
            </div>

            <div class="space-y-4 relative z-10">
              <div
                  class="flex items-center justify-between text-xs p-2 rounded-lg transition-colors border border-transparent">
                <div class="flex items-center ">
                  <i class="pi pi-calendar mr-2 text-[10px]"></i>
                  <span>Created</span>
                </div>
                <span class="font-bold ">{{ formatDate(tenant.createdAt) }}</span>
              </div>

              <div
                  class="pt-4 border-t border-dotted border-surface-500 flex justify-between items-center text-[10px] font-bold uppercase tracking-widest">
                <span
                    class="opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all">View Management</span>
                <i class="pi pi-arrow-right opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all"></i>
              </div>
            </div>
          </div>
        </div>

        <div v-else
             class=" p-16 rounded-3xl border-2 border-dashed text-center">
          <div class="w-20 h-20  rounded-full flex items-center justify-center mx-auto mb-6">
            <i class="pi pi-inbox text-4xl text-surface-300"></i>
          </div>
          <h2 class="text-xl font-bold  mb-2">No Tenants Found</h2>
          <p class=" mb-8 max-w-xs mx-auto text-sm">You haven't registered any tenants yet. Start by
            creating your first store instance.</p>
          <Button label="Create your first tenant" icon="pi pi-plus" class="px-6"/>
        </div>
      </div>

      <!-- Tenant Admin / Owner View -->
      <div v-else class="space-y-6">
        <!-- Stats Row -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="stat in storeStats" :key="stat.label"
               class=" p-6 rounded-2xl shadow-sm hover:shadow-md transition-shadow">
            <div class="flex justify-between items-start mb-4">
              <div
                  :class="['w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-lg', stat.colorClass]">
                <i :class="[stat.icon, 'text-sm']"></i>
              </div>
              <Tag :value="stat.trend" severity="success" size="small"
                   class="bg-green-50 text-green-600 border-none font-bold"/>
            </div>
            <div class="flex flex-col">
              <span class="text-xs font-bold  uppercase tracking-wider mb-1">{{ stat.label }}</span>
              <span class="text-2xl font-black ">{{ stat.value }}</span>
            </div>
          </div>
        </div>

        <!-- Charts / Main Content Placeholder -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2  p-8 rounded-3xl min-h-[400px] flex flex-col">
            <div class="flex justify-between items-center mb-8">
              <h3 class="font-bold ">Revenue Overview</h3>
              <SelectButton v-model="chartPeriod" :options="['Week', 'Month', 'Year']" size="small"/>
            </div>
            <div
                class="flex-1 w-full  rounded-2xl border  flex items-center justify-center overflow-hidden relative">
              <!-- Abstract Chart Mockup -->
              <div class="absolute inset-0 flex items-end justify-around px-8 pb-4">
                <div v-for="h in [40, 70, 45, 90, 65, 80, 55]" :key="h"
                     :style="{height: h + '%'}"
                     class="w-12 bg-primary/20 rounded-t-lg border-x border-t border-primary/30 relative group">
                  <div
                      class="absolute inset-x-0 top-0 h-1 bg-primary rounded-full -mt-0.5 shadow-[0_0_10px_rgba(var(--p-primary-rgb),0.5)]"></div>
                </div>
              </div>
              <div class="text-center z-10">
                <i class="pi pi-chart-line text-primary/20 text-5xl mb-2"></i>
                <p class=" font-medium">Live Analytics Powered by Shopbee</p>
              </div>
            </div>
          </div>

          <div
              class="bg-surface-900 p-8 rounded-3xl text-white flex flex-col overflow-hidden relative border border-surface-800">
            <div class="absolute -top-10 -right-10 w-40 h-40 bg-primary/20 rounded-full blur-3xl"></div>
            <h3 class="font-bold mb-6 flex items-center gap-2">
              <i class="pi pi-bolt text-yellow-400"></i>
              Recent Activity
            </h3>
            <div class="space-y-6 flex-1">
              <div v-for="i in 4" :key="i" class="flex gap-4 items-start">
                <div class="w-2 h-2 rounded-full bg-primary mt-1.5 shadow-[0_0_8px_var(--p-primary-color)]"></div>
                <div class="flex flex-col">
                  <span class="text-sm font-bold text-white">New order #{{ 1024 + i }}</span>
                  <span class="text-xs text-surface-400">by Alex Morgan • 2 min ago</span>
                </div>
              </div>
            </div>
            <Button label="View All Activity" severity="secondary" variant="text" size="small"
                    class="mt-8 text-surface-300"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {storeToRefs} from 'pinia';

definePageMeta({
  middleware: 'auth',
  breadcrumb: 'Tenants'
});

const {fetchApi} = useApi();
const keycloakStore = useKeycloakStore();
const {isInitialized} = storeToRefs(keycloakStore);

const tenants = ref<Tenant[]>([]);
const loading = ref(false);
const chartPeriod = ref('Month');

const isPlatformAdmin = computed(() => keycloakStore.isPlatformAdmin);

const storeStats = [
  {label: 'Total Sales', value: '$24,500', trend: '+12%', icon: 'pi pi-dollar', colorClass: 'bg-emerald-500'},
  {label: 'Orders', value: '154', trend: '+8%', icon: 'pi pi-shopping-bag', colorClass: 'bg-blue-500'},
  {label: 'Customers', value: '1,204', trend: '+5%', icon: 'pi pi-users', colorClass: 'bg-violet-500'},
  {label: 'Avg. Order', value: '$159', trend: '+2%', icon: 'pi pi-tag', colorClass: 'bg-amber-500'},
];

const getTenants = async () => {
  if (!isPlatformAdmin.value) return;

  loading.value = true;
  try {
    tenants.value = await fetchApi<Tenant[]>('/tenants');
  } catch (error) {
    console.error('Failed to fetch tenants:', error);
  } finally {
    loading.value = false;
  }
};

const manageTenant = (tenant: Tenant) => {
  navigateTo(`/tenants/${tenant.name}`);
};

const getStatusSeverity = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'success';
    case 'INACTIVE':
      return 'warn';
    case 'DELETED':
      return 'danger';
    default:
      return 'info';
  }
};

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  });
};

// Fetch tenants exactly once when Keycloak is ready
watch(isInitialized, (initialized) => {
  if (initialized && isPlatformAdmin.value && tenants.value.length === 0 && !loading.value) {
    getTenants();
  }
}, {immediate: true});
</script>

<style scoped>
.ml-13 {
  margin-left: 3.25rem;
}
</style>
