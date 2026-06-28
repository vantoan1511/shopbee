<script setup lang="ts">
import { computed } from 'vue'
import { useProducts } from '~/composables/useProducts'

const {
  categories,
  brands,
  allFeatures,
  selectedBrands,
  selectedCategories,
  selectedFeatures,
  priceRange,
  onlyInStock,
  resetFilters
} = useProducts()

// Check if any filters are active
const hasActiveFilters = computed(() => {
  return (
    selectedBrands.value.length > 0 ||
    selectedCategories.value.length > 0 ||
    selectedFeatures.value.length > 0 ||
    priceRange.value[0] > 0 ||
    priceRange.value[1] < 1500 ||
    onlyInStock.value
  )
})

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0
  }).format(val)
}
</script>

<template>
  <div
    class="flex flex-col gap-6 bg-white dark:bg-slate-900/30 border border-slate-200 dark:border-white/5 rounded-2xl p-6 h-fit"
  >
    <!-- Header -->
    <div class="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-white/5">
      <div class="flex items-center gap-2">
        <i class="pi pi-filter text-indigo-500 dark:text-indigo-400"></i>
        <h2 class="text-sm font-extrabold text-slate-800 dark:text-white tracking-wide uppercase">
          Filters
        </h2>
      </div>
      <button
        v-if="hasActiveFilters"
        class="text-[11px] font-bold text-indigo-650 dark:text-indigo-400 hover:text-indigo-500 dark:hover:text-indigo-300 transition-colors focus:outline-none flex items-center gap-1 cursor-pointer"
        @click="resetFilters"
      >
        <i class="pi pi-refresh text-[9px]"></i>
        Reset All
      </button>
    </div>

    <!-- Category Filter -->
    <div class="flex flex-col gap-3">
      <h3 class="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-505">
        Categories
      </h3>
      <div class="flex flex-col gap-2.5">
        <div
          v-for="cat in categories"
          :key="cat"
          class="flex items-center gap-3 cursor-pointer group"
        >
          <Checkbox
            v-model="selectedCategories"
            :input-id="`cat-${cat}`"
            :name="`cat-${cat}`"
            :value="cat"
            class="w-5 h-5 rounded-md border border-slate-300 dark:border-white/10 checked:bg-indigo-500 checked:border-indigo-500 focus:outline-none"
          />
          <label
            :for="`cat-${cat}`"
            class="text-xs font-medium text-slate-600 dark:text-slate-300 group-hover:text-slate-900 dark:group-hover:text-white cursor-pointer select-none transition-colors"
          >
            {{ cat }}
          </label>
        </div>
      </div>
    </div>

    <div class="h-px bg-slate-100 dark:bg-white/5"></div>

    <!-- Brand Filter -->
    <div class="flex flex-col gap-3">
      <h3 class="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-505">
        Brands
      </h3>
      <div class="flex flex-col gap-2.5">
        <div
          v-for="brand in brands"
          :key="brand"
          class="flex items-center gap-3 cursor-pointer group"
        >
          <Checkbox
            v-model="selectedBrands"
            :input-id="`brand-${brand}`"
            :name="`brand-${brand}`"
            :value="brand"
            class="w-5 h-5 rounded-md border border-slate-300 dark:border-white/10 checked:bg-indigo-500 checked:border-indigo-500 focus:outline-none"
          />
          <label
            :for="`brand-${brand}`"
            class="text-xs font-medium text-slate-600 dark:text-slate-300 group-hover:text-slate-900 dark:group-hover:text-white cursor-pointer select-none transition-colors"
          >
            {{ brand }}
          </label>
        </div>
      </div>
    </div>

    <div class="h-px bg-slate-100 dark:bg-white/5"></div>

    <!-- Price Range Filter -->
    <div class="flex flex-col gap-4">
      <div class="flex justify-between items-center">
        <h3 class="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-505">
          Price Range
        </h3>
        <span
          class="text-[11px] font-bold text-slate-700 dark:text-slate-200 bg-slate-50 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5 px-2 py-0.5 rounded-md"
        >
          {{ formatCurrency(priceRange[0]) }} -
          {{ formatCurrency(priceRange[1]) }}
        </span>
      </div>
      <div class="px-2">
        <Slider
          v-model="priceRange"
          range
          :min="0"
          :max="1500"
          class="w-full h-1.5"
          :pt="{
            startHandler: { 'aria-label': 'Minimum price filter' },
            endHandler: { 'aria-label': 'Maximum price filter' }
          }"
        />
      </div>
    </div>

    <div class="h-px bg-slate-100 dark:bg-white/5"></div>

    <!-- Features Filter -->
    <div class="flex flex-col gap-3">
      <h3 class="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-505">
        Features
      </h3>
      <div class="flex flex-col gap-2.5">
        <div
          v-for="feat in allFeatures"
          :key="feat"
          class="flex items-center gap-3 cursor-pointer group"
        >
          <Checkbox
            v-model="selectedFeatures"
            :input-id="`feat-${feat}`"
            :name="`feat-${feat}`"
            :value="feat"
            class="w-5 h-5 rounded-md border border-slate-300 dark:border-white/10 checked:bg-indigo-500 checked:border-indigo-500 focus:outline-none"
          />
          <label
            :for="`feat-${feat}`"
            class="text-xs font-medium text-slate-600 dark:text-slate-300 group-hover:text-slate-900 dark:group-hover:text-white cursor-pointer select-none transition-colors"
          >
            {{ feat }}
          </label>
        </div>
      </div>
    </div>

    <div class="h-px bg-slate-100 dark:bg-white/5"></div>

    <!-- Stock Availability Filter -->
    <div class="flex items-center justify-between">
      <div class="flex flex-col gap-0.5">
        <h3 class="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-505">
          Availability
        </h3>
        <label
          for="in-stock-switch"
          class="text-[10px] text-slate-500 dark:text-slate-400 cursor-pointer select-none"
          >Only show items in stock</label
        >
      </div>
      <ToggleSwitch
        v-model="onlyInStock"
        input-id="in-stock-switch"
        class="scale-90"
      />
    </div>
  </div>
</template>

<style scoped>
/* Customizable PrimeVue styling overrides if needed */
:deep(.p-slider-handle) {
  background: rgb(99, 102, 241);
  border: 2px solid white;
}
:deep(.p-slider-range) {
  background: rgb(99, 102, 241);
}
:deep(.p-checkbox-box.p-highlight) {
  border-color: rgb(99, 102, 241);
  background: rgb(99, 102, 241);
}
:deep(.p-toggleswitch.p-toggleswitch-checked .p-toggleswitch-slider) {
  background: rgb(99, 102, 241);
}
</style>
