<template>
  <div class="h-screen w-screen flex items-center justify-center">
    <div
        class="py-16 px-8 sm:px-16 shadow flex flex-col w-11/12 sm:w-[30rem] text-center border-none rounded-3xl">
      <h1 class="font-bold text-4xl mt-0 mb-4 text-primary">Shopbee</h1>
      <p class=" mb-8">Management Console</p>

      <ClientOnly>
        <div v-if="!isAuthenticated" class="flex flex-col gap-4">
          <Button label="Sign In to Admin Portal"
                  icon="pi pi-sign-in"
                  class="py-4 text-lg"
                  @click="() => login()"
                  fluid/>
          <p class="text-sm ">
            Secure authentication powered by Keycloak
          </p>
        </div>

        <div v-else class="flex flex-col gap-4">
          <i class="pi pi-check-circle text-green-500 text-5xl mb-2"></i>
          <p class="font-medium ">You are already signed in.</p>
          <Button label="Go to Tenants" @click="navigateTo('/tenants')" fluid/>
        </div>
        <template #fallback>
          <div class="flex justify-center py-8">
            <i class="pi pi-spin pi-spinner text-2xl text-primary"></i>
          </div>
        </template>
      </ClientOnly>

      <div class="mt-8 pt-6">
        <span class=" text-sm">Supported Login Methods</span>
        <div class="flex gap-4 items-center justify-center mt-4">
          <i class="pi pi-google text-xl "></i>
          <i class="pi pi-github text-xl "></i>
          <i class="pi pi-microsoft text-xl "></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'empty'
});

const {login, isAuthenticated} = useAuthentication();
</script>
