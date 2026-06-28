<template>
  <div class="p-6 max-w-6xl mx-auto my-2 border-none rounded-2xl shadow-none">
    <!-- Breadcrumb & Actions -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
      <div>
        <Button class="mb-2 -ml-2 " icon="pi pi-arrow-left" label="Back to Tenants" size="small"
                variant="text" @click="navigateTo('/tenants')"/>
        <h1 class="text-3xl font-black  flex items-center gap-3">
          {{ tenantName }}
          <Tag :severity="getStatusSeverity(tenant?.status)" :value="tenant?.status || 'LOADING'" class="text-xs"/>
        </h1>
        <p class=" mt-1">
          Tenant management and configuration
        </p>
      </div>

      <div class="flex gap-2">
        <Button v-if="hasUnsavedChanges" label="Discard" severity="secondary" variant="text" @click="resetForm"/>
        <Button :disabled="!hasUnsavedChanges" :loading="saving" icon="pi pi-check" label="Save Changes"
                severity="primary" @click="saveTenant"/>
      </div>
    </div>

    <div v-if="loading && !tenant" class="flex flex-col gap-6">
      <Skeleton height="200px" border-radius="24px"/>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Skeleton height="400px" border-radius="24px" class="md:col-span-2"/>
        <Skeleton height="400px" border-radius="24px"/>
      </div>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8 animate-enter">
      <!-- Left Column: Settings Form -->
      <div class="lg:col-span-2 space-y-6">
        <Card class="shadow-sm overflow-hidden">
          <template #title>
            <div class="flex items-center gap-2 text-lg">
              <i class="pi pi-cog"></i>
              General Settings
            </div>
          </template>
          <template #content>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4">
              <div class="flex flex-col gap-2">
                <label for="name" class="text-sm font-bold ">Tenant Name</label>
                <InputText id="name" v-model="formData.name" placeholder="Enter tenant name" class="w-full"/>
                <small class="">Unique identifier for this tenant.</small>
              </div>

              <div class="flex flex-col gap-2">
                <label for="status" class="text-sm font-bold ">Status</label>
                <Select id="status" v-model="formData.status" :options="statusOptions" optionLabel="label"
                        optionValue="value" class="w-full"/>
                <small class="">Control tenant access to the platform.</small>
              </div>

              <div class="flex flex-col gap-2 md:col-span-2">
                <label for="description" class="text-sm font-bold ">Internal Description</label>
                <Textarea id="description" v-model="formData.description" rows="3"
                          placeholder="Notes about this tenant..." class="w-full"/>
              </div>
            </div>
          </template>
        </Card>

        <!-- Advanced Config Placeholder -->
        <Card class="shadow-sm opacity-60">
          <template #title>
            <div class="flex items-center gap-2 text-lg ">
              <i class="pi pi-shield"></i>
              Security & Authentication
            </div>
          </template>
          <template #content>
            <div class="p-8 border-2 border-dashed  rounded-2xl text-center">
              <p class=" text-sm">Custom IDP and SSO configurations are available in Enterprise
                plan.</p>
              <Button label="Upgrade Plan" variant="text" size="small" class="mt-2"/>
            </div>
          </template>
        </Card>
      </div>

      <!-- Right Column: Info & Stats -->
      <div class="space-y-6">
        <Card class="shadow-sm relative overflow-hidden">
          <template #content>
            <div class="absolute -right-4 -top-4 w-24 h-24 bg-primary/10 rounded-full blur-2xl"></div>
            <h3 class="font-bold mb-4 flex items-center gap-2 relative z-10">
              <i class="pi pi-info-circle text-primary"></i>
              Tenant Metadata
            </h3>
            <div class="space-y-4 relative z-10">
              <div class="flex flex-col gap-1">
                <span class="text-[10px] font-bold  uppercase">Created On</span>
                <span class="text-sm font-mono ">{{ formatDate(tenant?.createdAt) }}</span>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-[10px] font-bold  uppercase">Last Updated</span>
                <span class="text-sm font-mono ">{{ formatDate(tenant?.updatedAt) }}</span>
              </div>
              <div class="pt-4 border-t  flex items-center justify-between">
                <div class="flex flex-col">
                  <span class="text-[10px] font-bold  uppercase">Active Users</span>
                  <span class="text-xl font-black ">24</span>
                </div>
                <div class="w-10 h-10 bg-primary/20 rounded-lg flex items-center justify-center text-primary">
                  <i class="pi pi-users"></i>
                </div>
              </div>
            </div>
          </template>
        </Card>

        <Card class="shadow-sm">
          <template #title><span class="text-base ">Danger Zone</span></template>
          <template #content>
            <div class="space-y-4 pt-2">
              <p class="text-xs ">Deleting a tenant is permanent and cannot be undone. All store data,
                orders, and products will be removed.</p>
              <Button label="Delete Tenant" severity="danger" variant="outlined" fluid size="small" icon="pi pi-trash"/>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {storeToRefs} from 'pinia';

definePageMeta({
  middleware: 'auth',
  pageTransition: {
    name: 'slide'
  },
  breadcrumb: (route: any) => route.params.name
});

const route = useRoute();
const tenantName = computed(() => route.params.name as string);
const {fetchApi} = useApi();
const keycloakStore = useKeycloakStore();
const {isInitialized} = storeToRefs(keycloakStore);

const tenant = ref<Tenant | null>(null);
const loading = ref(true);
const saving = ref(false);

const formData = ref({
  name: '',
  status: 'ACTIVE',
  description: 'Retail store specializing in fashion and accessories.'
});

const hasUnsavedChanges = computed(() => {
  if (!tenant.value) return false;
  return formData.value.name !== tenant.value.name ||
      formData.value.status !== tenant.value.status;
});

const statusOptions = [
  {label: 'Active', value: 'ACTIVE'},
  {label: 'Inactive', value: 'INACTIVE'},
  {label: 'Suspended', value: 'SUSPENDED'},
  {label: 'Deleted', value: 'DELETED'},
];

const getTenantDetails = async () => {
  loading.value = true;
  try {
    const data = await fetchApi<Tenant>(`/tenants/${tenantName.value}`);
    tenant.value = data;
    resetForm();
  } catch (error) {
    console.error('Failed to fetch tenant details:', error);
    tenant.value = {
      name: tenantName.value,
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    resetForm();
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  if (tenant.value) {
    formData.value.name = tenant.value.name;
    formData.value.status = tenant.value.status;
  }
};

const saveTenant = async () => {
  saving.value = true;
  try {
    await fetchApi(`/tenants/${tenantName.value}`, {
      method: 'PUT',
      body: formData.value
    });
    if (tenant.value) {
      tenant.value.name = formData.value.name;
      tenant.value.status = formData.value.status;
      tenant.value.updatedAt = new Date().toISOString();
    }
    if (formData.value.name !== tenantName.value) {
      navigateTo(`/tenants/${formData.value.name}`);
    }
  } catch (error) {
    console.error('Failed to save tenant:', error);
  } finally {
    saving.value = false;
  }
};

const getStatusSeverity = (status?: string) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'success';
    case 'INACTIVE':
      return 'warn';
    case 'DELETED':
      return 'danger';
    case 'SUSPENDED':
      return 'danger';
    default:
      return 'info';
  }
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short'
  });
};

watch(isInitialized, (initialized) => {
  if (initialized) {
    getTenantDetails();
  }
}, {immediate: true});
</script>
