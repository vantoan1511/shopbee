<template>
  <div class="p-6 ">
    <h2 class="text-2xl font-bold">Product id: {{ id }}</h2>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  validate (route) {
    // Check if the id is made up of digits
    return typeof route.params.id === 'string' && /^\d+$/.test(route.params.id)
  },
  breadcrumb: (route: any) => `Product #${route.params.id}`
})

const route = useRoute()
const id = computed(() => route.params.id)
</script>
