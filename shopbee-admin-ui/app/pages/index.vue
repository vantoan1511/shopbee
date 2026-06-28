<template>
  <div class="flex items-center justify-center h-screen">
    <div class="text-center">
      <i class="pi pi-spin pi-spinner text-4xl mb-4 text-primary"></i>
      <p class="">Verifying session...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useAuthenticationStore} from '~/stores/useAuthenticationStore';
import {useKeycloakStore} from '~/stores/useKeycloakStore';

definePageMeta({
  layout: 'empty'
});

const authStore = useAuthenticationStore();
const keycloakStore = useKeycloakStore();
const router = useRouter();

// Watch for authentication state and redirect
watchEffect(() => {
  if (keycloakStore.isInitialized) {
    if (authStore.isAuthenticated) {
      router.push('/tenants');
    } else {
      router.push('/login');
    }
  }
});
</script>
