<template>
  <div>
    <NuxtLoadingIndicator/>
    <NuxtLayout>
      <NuxtPage/>
    </NuxtLayout>
  </div>
</template>
<script setup lang="ts">
</script>
<style>
.page-enter-active,
.page-leave-active,
.slide-enter-active,
.slide-leave-active,
.rotate-enter-active,
.rotate-leave-active,
.layout-enter-active,
.layout-leave-active {
  transition: all 0.4s;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
  filter: blur(1rem);
}

.rotate-enter-from,
.rotate-leave-to {
  opacity: 0;
  transform: rotate3d(1, 1, 1, 15deg);
}

.layout-enter-from,
.layout-leave-to {
  filter: grayscale(1);
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  filter: blur(1rem);
  transform: translateX(100%);
}
</style>