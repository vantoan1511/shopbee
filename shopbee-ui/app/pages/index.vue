<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useProducts } from '~/composables/useProducts'
import type { Product, Voucher } from '~/composables/useProducts'

const router = useRouter()
const toast = useToast()
const {
  products,
  vouchersList,
  filteredProducts,
  sortBy,
  resetFilters,
  addToCart
} = useProducts()

const { data: serverProducts } = await useFetch('/api/products')
if (serverProducts.value) {
  products.value = serverProducts.value as Product[]
}

const { data: serverVouchers } = await useFetch('/api/vouchers')
if (serverVouchers.value) {
  vouchersList.value = serverVouchers.value as Voucher[]
}

const mobileFiltersVisible = ref(false)
const quickViewProduct = ref<Product | null>(null)
const quickViewVisible = ref(false)

const quickViewSelectedVariants = ref<Record<string, string>>({})

watch(
  quickViewProduct,
  (newProduct) => {
    quickViewSelectedVariants.value = {}
    if (newProduct?.variants) {
      newProduct.variants.forEach((v) => {
        if (v.options.length > 0) {
          const firstOpt = v.options[0]
          if (firstOpt) {
            quickViewSelectedVariants.value[v.name] = firstOpt
          }
        }
      })
    }
  },
  { immediate: true }
)

const sortOptions = [
  { label: 'Featured', value: 'featured' },
  { label: 'Price: Low to High', value: 'price-asc' },
  { label: 'Price: High to Low', value: 'price-desc' },
  { label: 'Highest Rated', value: 'rating' }
]

const handleQuickView = (product: Product) => {
  quickViewProduct.value = product
  quickViewVisible.value = true
}

const handleAddToCart = (product: Product) => {
  const success = addToCart(product, 1, quickViewSelectedVariants.value)
  if (success) {
    toast.add({
      severity: 'success',
      summary: 'Added to Cart',
      detail: `${product.name} has been added to your cart.`,
      life: 3000
    })
  } else {
    toast.add({
      severity: 'error',
      summary: 'Out of Stock',
      detail: `Sorry, ${product.name} is currently unavailable or has reached limit.`,
      life: 3000
    })
  }
}

const goToProductDetail = (id: number) => {
  quickViewVisible.value = false
  router.push(`/products/${id}`)
}
</script>

<template>
  <div class="flex-1 flex flex-col gap-6 animate-slide-up">
    <!-- Header Hero Banner -->
    <section
      class="relative rounded-3xl overflow-hidden bg-gradient-to-r from-slate-50 via-indigo-50/50 to-slate-50 dark:from-slate-900 dark:via-indigo-950/80 dark:to-slate-900 border border-slate-200 dark:border-white/5 py-10 px-8 sm:px-12 mb-2 shadow-2xl"
    >
      <div class="absolute inset-0 pointer-events-none overflow-hidden">
        <div
          class="absolute top-0 right-0 w-80 h-80 bg-indigo-500/10 rounded-full blur-3xl"
        ></div>
      </div>
      <div class="relative z-10 max-w-2xl">
        <span
          class="text-xs font-bold uppercase tracking-widest text-indigo-650 dark:text-indigo-400"
          >Shopbee Storefront</span
        >
        <h1
          class="text-3xl sm:text-5xl font-black tracking-tight text-slate-900 dark:text-white mt-2 mb-4"
        >
          Discover Premium <br class="hidden sm:inline" />
          <span
            class="bg-linear-to-r from-indigo-600 via-purple-600 to-pink-600 dark:from-indigo-400 dark:via-purple-400 dark:to-pink-400 bg-clip-text text-transparent"
            >Smart Devices & Gear</span
          >
        </h1>
        <p class="text-sm sm:text-base text-slate-655 dark:text-slate-400 leading-relaxed max-w-lg">
          Explore our next-generation catalog with real-time reactive filters,
          premium UI components, and rich animations.
        </p>
      </div>
    </section>

    <!-- Toolbar / Sort Panel -->
    <div
      class="flex flex-col sm:flex-row gap-4 items-stretch sm:items-center justify-between bg-white dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 p-4 rounded-2xl"
    >
      <div class="flex items-center gap-3 justify-between sm:justify-start">
        <span class="text-xs font-bold text-slate-500 dark:text-slate-400">
          Showing
          <span class="text-slate-800 dark:text-white">{{ filteredProducts.length }}</span> Products
        </span>
        <!-- Mobile Filters Button -->
        <Button
          icon="pi pi-filter"
          label="Filters"
          severity="secondary"
          outlined
          class="lg:hidden text-xs py-1.5 px-3 rounded-lg border border-slate-200 dark:border-white/10 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white transition-colors"
          @click="mobileFiltersVisible = true"
        />
      </div>

      <div class="flex items-center gap-2 justify-end">
        <label
          for="sort-select"
          class="text-xs font-bold text-slate-550 dark:text-slate-400 whitespace-nowrap"
          >Sort By:</label
        >
        <Select
          id="sort-select"
          v-model="sortBy"
          :options="sortOptions"
          option-label="label"
          option-value="value"
          class="bg-slate-50 dark:bg-slate-950/60 border border-slate-200 dark:border-white/10 text-slate-700 dark:text-slate-200 text-xs font-semibold rounded-xl w-44 hover:border-slate-350 dark:hover:border-white/20 transition-all focus:outline-none"
        />
      </div>
    </div>

    <!-- Main Workspace -->
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6 items-start">
      <!-- Desktop Sidebar Filters -->
      <aside class="hidden lg:block lg:col-span-1">
        <ProductFilters />
      </aside>

      <!-- Products Grid -->
      <div class="lg:col-span-3">
        <!-- Loaded Products Grid -->
        <div
          v-if="filteredProducts.length > 0"
          class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6"
        >
          <ProductCard
            v-for="(product, index) in filteredProducts"
            :key="product.id"
            :product="product"
            :is-lcp="index === 0"
            @click="goToProductDetail(product.id)"
            @quick-view="handleQuickView"
          />
        </div>

        <!-- Empty Filter State -->
        <div
          v-else
          class="flex flex-col items-center justify-center text-center p-12 bg-slate-50 dark:bg-slate-900/20 border border-dashed border-slate-200 dark:border-white/10 rounded-3xl min-h-[350px]"
        >
          <div
            class="w-16 h-16 rounded-2xl bg-indigo-500/10 flex items-center justify-center mb-4 border border-indigo-500/20"
          >
            <i class="pi pi-search-minus text-2xl text-indigo-500 dark:text-indigo-400"></i>
          </div>
          <h3 class="text-lg font-bold text-slate-800 dark:text-white mb-2">
            No products found matching filters
          </h3>
          <p class="text-sm text-slate-500 dark:text-slate-400 max-w-sm mb-6 leading-relaxed">
            Try adjusting your search criteria, price range slider, or selection
            filters to find what you are looking for.
          </p>
          <Button
            label="Clear All Filters"
            icon="pi pi-filter-slash"
            class="px-4 py-2 text-xs font-bold bg-indigo-600 hover:bg-indigo-500 border-0 rounded-xl transition-colors text-white"
            @click="resetFilters"
          />
        </div>
      </div>
    </div>

    <!-- Mobile Drawer Filters -->
    <Drawer
      v-model:visible="mobileFiltersVisible"
      header="Filter Options"
      position="left"
      class="bg-white dark:bg-slate-950 text-slate-800 dark:text-slate-100 border-r border-slate-200 dark:border-white/5 w-80 max-w-[90vw]"
    >
      <div class="p-2">
        <ProductFilters />
      </div>
    </Drawer>

    <!-- Premium Quick-View Dialog Modal -->
    <Dialog
      v-model:visible="quickViewVisible"
      modal
      dismissable-mask
      class="bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 border border-slate-200 dark:border-white/10 max-w-4xl w-full mx-4 rounded-3xl overflow-hidden shadow-2xl"
      content-class="p-0"
      header-class="p-0 border-0 bg-transparent absolute right-4 top-4 z-50 text-slate-800 dark:text-white"
    >
      <div v-if="quickViewProduct" class="grid grid-cols-1 md:grid-cols-2">
        <!-- Product Image -->
        <div
          class="relative bg-slate-100 dark:bg-slate-950 flex items-center justify-center min-h-65 md:min-h-full"
        >
          <img
            :src="quickViewProduct.imageUrl"
            :alt="quickViewProduct.name"
            class="w-full h-full object-cover"
          />
          <div class="absolute top-4 left-4">
            <Tag
              :severity="quickViewProduct.stock > 0 ? 'success' : 'danger'"
              :value="quickViewProduct.stock > 0 ? 'In Stock' : 'Out of Stock'"
              rounded
              class="px-2 py-0.5 text-[10px] font-bold"
            ></Tag>
          </div>
        </div>

        <!-- Product Specs Details -->
        <div class="p-6 md:p-8 flex flex-col justify-between h-full">
          <div>
            <!-- Breadcrumbs / tags -->
            <div class="flex items-center gap-2 mb-3">
              <span
                class="text-[10px] font-bold uppercase tracking-wider text-indigo-600 dark:text-indigo-400"
              >
                {{ quickViewProduct.category }}
              </span>
              <span class="text-slate-400 dark:text-slate-600 text-[10px]">&bull;</span>
              <span
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >
                {{ quickViewProduct.brand }}
              </span>
            </div>

            <!-- Title & SKU -->
            <h2 class="text-xl font-extrabold text-slate-900 dark:text-white mb-1 leading-tight">
              {{ quickViewProduct.name }}
            </h2>
            <span
              class="text-[10px] font-mono text-slate-400 dark:text-slate-500 uppercase tracking-widest block mb-4"
            >
              SKU: {{ quickViewProduct.sku }}
            </span>

            <!-- Rating -->
            <div class="flex items-center gap-2 mb-4">
              <Rating
                v-model="quickViewProduct.rating"
                readonly
                :cancel="false"
                class="text-amber-500"
              >
                <template #onicon>
                  <i class="pi pi-star-fill text-[12px] text-amber-500"></i>
                </template>
                <template #officon>
                  <i class="pi pi-star text-[12px] text-slate-300 dark:text-slate-700"></i>
                </template>
              </Rating>
              <span class="text-[11px] font-semibold text-slate-500 dark:text-slate-400">
                {{ quickViewProduct.rating }} ({{
                  quickViewProduct.reviewsCount
                }}
                reviews)
              </span>
            </div>

            <!-- Description -->
            <p class="text-xs text-slate-600 dark:text-slate-300 leading-relaxed mb-6">
              {{ quickViewProduct.description }}
            </p>

            <!-- Key Features Specs -->
            <div v-if="quickViewProduct.features.length > 0" class="mb-6">
              <h4
                class="text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-2.5"
              >
                Key Features
              </h4>
              <ul class="grid grid-cols-2 gap-2 p-0 m-0 list-none">
                <li
                  v-for="feat in quickViewProduct.features"
                  :key="feat"
                  class="flex items-center gap-2 text-xs text-slate-650 dark:text-slate-300"
                >
                  <i
                    class="pi pi-check text-[10px] text-green-500 dark:text-green-400 bg-green-500/10 dark:bg-green-500/20 p-1 rounded-full"
                  ></i>
                  <span>{{ feat }}</span>
                </li>
              </ul>
            </div>

            <!-- Variant Variant Selectors -->
            <div
              v-if="
                quickViewProduct.variants &&
                quickViewProduct.variants.length > 0
              "
              class="mb-6"
            >
              <div
                v-for="variant in quickViewProduct.variants"
                :key="variant.name"
                class="flex flex-col gap-2 mb-3"
              >
                <span
                  class="text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                  >Select {{ variant.name }}</span
                >
                <div class="flex flex-wrap gap-1.5">
                  <button
                    v-for="opt in variant.options"
                    :key="opt"
                    class="px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all duration-200 focus:outline-none cursor-pointer"
                    :class="
                      quickViewSelectedVariants[variant.name] === opt
                        ? 'bg-indigo-50 dark:bg-indigo-500/20 border-indigo-500 text-indigo-700 dark:text-white font-bold shadow-xs'
                        : 'bg-slate-50 dark:bg-slate-950/40 border-slate-200 dark:border-white/10 text-slate-600 dark:text-slate-300 hover:border-slate-350 dark:hover:border-white/20 hover:bg-slate-100 dark:hover:bg-slate-900'
                    "
                    @click="quickViewSelectedVariants[variant.name] = opt"
                  >
                    {{ opt }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer Action and Pricing -->
          <div
            class="pt-4 border-t border-slate-100 dark:border-white/5 flex items-center justify-between gap-4 mt-auto"
          >
            <div class="flex flex-col">
              <span
                class="text-[9px] uppercase tracking-wider text-slate-400 dark:text-slate-505 font-bold"
                >Price</span
              >
              <span class="text-xl font-extrabold text-slate-800 dark:text-white">
                {{
                  new Intl.NumberFormat('en-US', {
                    style: 'currency',
                    currency: 'USD'
                  }).format(quickViewProduct.price)
                }}
              </span>
            </div>
            <div class="flex gap-2">
              <Button
                label="Full Details"
                icon="pi pi-info-circle"
                severity="secondary"
                outlined
                class="text-xs py-2 px-3 border border-slate-200 dark:border-white/10 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white rounded-xl font-bold transition-all whitespace-nowrap"
                @click="goToProductDetail(quickViewProduct.id)"
              />
              <Button
                label="Add To Cart"
                icon="pi pi-shopping-cart"
                :disabled="quickViewProduct.stock === 0"
                class="text-xs py-2 px-4 bg-linear-to-r from-indigo-500 to-blue-600 border-0 hover:shadow-[0_0_15px_rgba(99,102,241,0.4)] disabled:opacity-50 disabled:pointer-events-none rounded-xl font-bold text-white transition-all whitespace-nowrap"
                @click="handleAddToCart(quickViewProduct)"
              />
            </div>
          </div>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
/* PrimeVue Select component custom style tweaks */
.dark :deep(.p-select) {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
:deep(.p-select-label) {
  padding: 0.5rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 600;
}
.dark :deep(.p-select-label) {
  color: rgb(226, 232, 240);
}
.dark :deep(.p-select-overlay) {
  background: rgb(15, 23, 42);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
:deep(.p-select-option) {
  font-size: 0.75rem;
  font-weight: 500;
}
.dark :deep(.p-select-option) {
  color: rgb(203, 213, 225);
}
:deep(.p-select-option:hover) {
  background: rgba(99, 102, 241, 0.15);
}
.dark :deep(.p-select-option:hover) {
  color: white;
}
:deep(.p-select-option.p-select-option-selected) {
  background: rgba(99, 102, 241, 0.3);
}
.dark :deep(.p-select-option.p-select-option-selected) {
  color: white;
}

/* Modal PrimeVue overlays custom styling */
.dark :deep(.p-dialog-header-actions) {
  color: rgb(226, 232, 240);
}
:deep(.p-dialog-close-button) {
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.08);
}
.dark :deep(.p-dialog-close-button) {
  color: rgb(226, 232, 240) !important;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
:deep(.p-dialog-close-button:hover) {
  background: rgba(0, 0, 0, 0.1) !important;
}
.dark :deep(.p-dialog-close-button:hover) {
  background: rgba(255, 255, 255, 0.15) !important;
}
</style>
