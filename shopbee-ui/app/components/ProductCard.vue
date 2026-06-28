<script setup lang="ts">
import { useToast } from 'primevue/usetoast'
import { computed } from 'vue'
import type { Product } from '~/composables/useProducts'
import { useProducts } from '~/composables/useProducts'

const props = defineProps<{
  product: Product
  isLcp?: boolean
}>()

const emit = defineEmits<{
  (e: 'quick-view', product: Product): void
}>()

const { addToCart } = useProducts()
const toast = useToast()

// Format price with 2 decimals
const formattedPrice = computed(() => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(props.product.price)
})

// Stock status configuration
const stockStatus = computed(() => {
  if (props.product.stock === 0) {
    return {
      label: 'Out of Stock',
      severity: 'danger' as const,
      icon: 'pi pi-times-circle'
    }
  } else if (props.product.stock <= 5) {
    return {
      label: `Low Stock (${props.product.stock} left)`,
      severity: 'warn' as const,
      icon: 'pi pi-exclamation-triangle'
    }
  } else {
    return {
      label: 'In Stock',
      severity: 'success' as const,
      icon: 'pi pi-check-circle'
    }
  }
})

const handleAddToCart = () => {
  const success = addToCart(props.product)
  if (success) {
    toast.add({
      severity: 'success',
      summary: 'Added to Cart',
      detail: `${props.product.name} has been added to your cart.`,
      life: 3000
    })
  } else {
    toast.add({
      severity: 'error',
      summary: 'Out of Stock',
      detail: `Sorry, ${props.product.name} is currently unavailable or has reached limit.`,
      life: 3000
    })
  }
}
</script>

<template>
  <Card
    class="h-full bg-white dark:bg-slate-900/40 backdrop-blur-md border border-slate-200 dark:border-white/5 hover:border-indigo-500/20 hover:bg-slate-50 dark:hover:bg-slate-900/70 hover:-translate-y-1 transition-all duration-300 rounded-[20px] overflow-hidden flex flex-col justify-between cursor-pointer"
  >
    <template #header>
      <div class="relative w-full aspect-video overflow-hidden bg-slate-100 dark:bg-slate-950">
        <!-- Blur placeholder behind image for clean premium loading -->
        <div
          class="absolute inset-0 bg-slate-200 dark:bg-slate-900 flex items-center justify-center"
        >
          <i class="pi pi-image text-slate-400 dark:text-slate-700 text-3xl animate-pulse"></i>
        </div>
        <!-- Product Image -->
        <img
          :src="product.imageUrl"
          :alt="product.name"
          width="600"
          height="400"
          class="relative z-10 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          :loading="isLcp ? 'eager' : 'lazy'"
          :fetchpriority="isLcp ? 'high' : 'auto'"
          decoding="async"
        />
        <!-- Stock status Badge overlay -->
        <div class="absolute top-3 right-3 z-20">
          <Tag
            :severity="stockStatus.severity"
            :value="stockStatus.label"
            rounded
            class="px-2.5 py-0.5 font-bold text-[10px] uppercase shadow-md flex items-center gap-1"
          >
            <template #icon>
              <i :class="stockStatus.icon" class="text-[9px]"></i>
            </template>
          </Tag>
        </div>
        <!-- Brand overlay -->
        <div class="absolute bottom-3 left-3 z-20">
          <span
            class="px-2 py-0.5 rounded bg-white/70 dark:bg-slate-950/70 backdrop-blur-md text-[10px] font-semibold uppercase tracking-wider text-slate-650 dark:text-slate-300 border border-slate-200 dark:border-white/5"
          >
            {{ product.brand }}
          </span>
        </div>
      </div>
    </template>

    <template #title>
      <div class="px-5 pt-4">
        <h3
          class="text-base font-bold text-slate-800 dark:text-white group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors line-clamp-1"
        >
          {{ product.name }}
        </h3>
      </div>
    </template>

    <template #content>
      <div class="px-5 pb-4 flex-1 flex flex-col justify-between">
        <!-- Description -->
        <p class="text-xs text-slate-500 dark:text-slate-400 line-clamp-2 leading-relaxed mb-3">
          {{ product.description }}
        </p>

        <!-- Rating & Reviews -->
        <div class="flex items-center gap-2 mb-4">
          <Rating
            :model-value="product.rating"
            readonly
            :cancel="false"
            class="text-amber-500"
            size="small"
          >
            <template #onicon>
              <i class="pi pi-star-fill text-[11px] text-amber-500"></i>
            </template>
            <template #officon>
              <i class="pi pi-star text-[11px] text-slate-300 dark:text-slate-600"></i>
            </template>
          </Rating>
          <span class="text-[10px] font-semibold text-slate-550 dark:text-slate-400">
            ({{ product.reviewsCount }})
          </span>
        </div>

        <!-- Price and Actions -->
        <div
          class="flex items-center justify-between mt-auto pt-3 border-t border-slate-100 dark:border-white/5"
        >
          <div class="flex flex-col">
            <span
              class="text-[9px] uppercase tracking-wider text-slate-400 dark:text-slate-500 font-bold"
              >Price</span
            >
            <span class="text-lg font-extrabold text-slate-900 dark:text-white">{{
              formattedPrice
            }}</span>
          </div>
          <div class="flex gap-2">
            <!-- Quick View button -->
            <Button
              v-tooltip.bottom="'Quick View'"
              size="small"
              icon="pi pi-eye"
              aria-label="Quick View"
              severity="secondary"
              variant="text"
              class="w-9 h-9 rounded-xl flex items-center justify-center p-0 border border-slate-200 dark:border-white/10 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white transition-colors"
              @click.stop.prevent="emit('quick-view', product)"
            />
            <!-- Add to Cart button -->
            <Button
              icon="pi pi-cart-plus"
              label="Add to Cart"
              aria-label="Add to Cart"
              :disabled="product.stock === 0"
              class="h-9 px-3.5 rounded-xl text-xs font-bold bg-linear-to-r from-indigo-500 to-blue-600 text-white border-0 hover:shadow-[0_0_15px_rgba(99,102,241,0.4)] disabled:opacity-50 disabled:pointer-events-none transition-all flex items-center gap-1.5"
              @click.stop.prevent="handleAddToCart"
            >
            </Button>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
/* Ensure custom cards fit full height flex correctly */
:deep(.p-card) {
  display: flex;
  flex-direction: column;
}
:deep(.p-card-body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  flex: 1;
}
</style>
