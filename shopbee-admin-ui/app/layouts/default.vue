<template>
  <div class="flex h-screen overflow-hidden">
    <!-- Sidebar with dynamic visibility and transitions -->
    <ClientOnly>
      <!-- Transition wrapper for overlay -->
      <Transition name="fade">
        <div v-if="sidebarVisible" class="fixed inset-0 backdrop-blur-sm z-40 lg:hidden"
             @click="closeSidebar"></div>
      </Transition>

      <div :class="[
            sidebarVisible ? 'translate-x-0 w-64 opacity-100' : '-translate-x-full lg:translate-x-0 lg:w-0 lg:opacity-0 lg:invisible overflow-hidden'
        ]"
           class="fixed inset-y-0 left-0 z-50 transform lg:relative lg:translate-x-0 transition-all duration-300 ease-in-out shrink-0 ">
        <LayoutAppSidebar/>
      </div>
    </ClientOnly>

    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <ClientOnly>
        <LayoutAppHeader/>
      </ClientOnly>

      <main class="flex-1 overflow-x-hidden overflow-y-auto bg-surface-100 dark:bg-surface-900 custom-scrollbar ">
        <div class="container mx-auto">
          <slot/>
        </div>
      </main>

      <LayoutAppFooter/>
    </div>
  </div>
</template>

<script setup lang="ts">
const {sidebarVisible, closeSidebar} = useLayout();

// Handle responsive sidebar on mount
onMounted(() => {
  if (window.innerWidth < 1024) {
    closeSidebar();
  }
});

// Close sidebar on route change for mobile
const route = useRoute();
watch(() => route.path, () => {
  if (window.innerWidth < 1024) {
    closeSidebar();
  }
});
</script>

<style>
/* Smooth transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--p-surface-200);
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--p-surface-300);
}
</style>
