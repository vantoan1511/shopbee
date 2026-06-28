<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useOrders } from '~/composables/useOrders'
import type { Order } from '~/composables/useOrders'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { orders, getOrderByNumber, advanceOrderStatus } = useOrders()

if (orders.value.length === 0) {
  const { data: serverOrders } = await useFetch('/api/orders')
  if (serverOrders.value) {
    orders.value = serverOrders.value as Order[]
  }
}

const searchQuery = ref('')
const trackedOrder = ref<Order | null>(null)
const hasSearched = ref(false)
const isSubmitting = ref(false)

// Stepper steps config
const trackingSteps = [
  { status: 'placed', label: 'Ordered', icon: 'pi pi-shopping-bag' },
  { status: 'processing', label: 'Processing', icon: 'pi pi-cog' },
  { status: 'shipped', label: 'Shipped', icon: 'pi pi-send' },
  {
    status: 'out_for_delivery',
    label: 'Out for Delivery',
    icon: 'pi pi-truck'
  },
  { status: 'delivered', label: 'Delivered', icon: 'pi pi-home' }
]

const orderStatuses = [
  'placed',
  'processing',
  'shipped',
  'out_for_delivery',
  'delivered'
]

// Map the order status to a numeric step index (0 to 4)
const currentStepIndex = computed(() => {
  if (!trackedOrder.value) return -1
  return orderStatuses.indexOf(trackedOrder.value.status)
})

// Progress percentage for visual bar
const progressPercentage = computed(() => {
  const step = currentStepIndex.value
  if (step === -1) return 0
  return (step / 4) * 100
})

// Calculate package position on mock map (percentage along path)
const mapTruckPosition = computed(() => {
  const step = currentStepIndex.value
  if (step === -1) return 0
  switch (trackedOrder.value?.status) {
    case 'placed':
      return 5
    case 'processing':
      return 15
    case 'shipped':
      return 50
    case 'out_for_delivery':
      return 82
    case 'delivered':
      return 95
    default:
      return 0
  }
})

// Get status badge properties for detailed order view
const statusBadge = computed(() => {
  if (!trackedOrder.value) return { label: '', class: '' }
  return getStatusBadgeInfo(trackedOrder.value.status)
})

const getStatusBadgeInfo = (status: Order['status']) => {
  switch (status) {
    case 'placed':
      return {
        label: 'Order Placed',
        class: 'text-blue-400 bg-blue-500/10 border border-blue-500/20'
      }
    case 'processing':
      return {
        label: 'Processing',
        class: 'text-amber-400 bg-amber-500/10 border border-amber-500/20'
      }
    case 'shipped':
      return {
        label: 'Shipped (In Transit)',
        class: 'text-indigo-650 dark:text-indigo-400 bg-indigo-500/10 border border-indigo-500/20'
      }
    case 'out_for_delivery':
      return {
        label: 'Out for Delivery',
        class: 'text-pink-400 bg-pink-500/10 border border-pink-500/20'
      }
    case 'delivered':
      return {
        label: 'Delivered',
        class: 'text-emerald-400 bg-emerald-500/10 border border-emerald-500/20'
      }
    case 'cancelled':
      return {
        label: 'Cancelled',
        class: 'text-rose-400 bg-rose-500/10 border border-rose-500/20'
      }
    default:
      return {
        label: 'Unknown',
        class: 'text-slate-500 dark:text-slate-400 bg-slate-500/10 border border-slate-500/20'
      }
  }
}

const getStatusLabel = (status: Order['status']) => {
  return getStatusBadgeInfo(status).label
}

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(val)
}

// Active and Previous Orders Computed Properties
const activeOrders = computed(() => {
  return orders.value.filter(
    (o) => o.status !== 'delivered' && o.status !== 'cancelled'
  )
})

const previousOrders = computed(() => {
  return orders.value.filter(
    (o) => o.status === 'delivered' || o.status === 'cancelled'
  )
})

const clearTrackedOrder = () => {
  trackedOrder.value = null
  searchQuery.value = ''
  hasSearched.value = false
  // Clear URL query parameters
  router.replace({ query: {} })
}

const handleSearch = async (prefilled?: string) => {
  const query = prefilled || searchQuery.value.trim()
  if (!query) return

  isSubmitting.value = true
  hasSearched.value = true

  // Simulate short lookup latency for premium feel
  await new Promise((resolve) => setTimeout(resolve, 850))

  const found = getOrderByNumber(query)
  if (found) {
    trackedOrder.value = found
    // Sync URL query parameters
    router.replace({ query: { number: found.orderNumber } })
    toast.add({
      severity: 'success',
      summary: 'Order Found',
      detail: `Tracking information for ${found.orderNumber} loaded.`,
      life: 3000
    })
  } else {
    // Keep user on dashboard but show error toast
    toast.add({
      severity: 'error',
      summary: 'Not Found',
      detail: `No order records found matching "${query}". Please check the number and try again.`,
      life: 4000
    })
  }
  isSubmitting.value = false
}

// Trigger simulated progression step
const handleDemoAdvance = async () => {
  if (!trackedOrder.value) return
  const orderNum = trackedOrder.value.orderNumber
  const success = await advanceOrderStatus(orderNum)
  if (success) {
    // Re-fetch object reference to trigger reactivity
    const updated = getOrderByNumber(orderNum)
    if (updated) {
      trackedOrder.value = { ...updated }
      toast.add({
        severity: 'info',
        summary: 'Demo Mode: Status Advanced',
        detail: `Order status is now: ${trackedOrder.value.status.toUpperCase()}`,
        life: 3000
      })
    }
  } else {
    toast.add({
      severity: 'warn',
      summary: 'Demo Mode Alert',
      detail: 'This order is already fully delivered and completed.',
      life: 3000
    })
  }
}

// Check query params on mount
onMounted(() => {
  const queryNum = route.query.number
  if (queryNum && typeof queryNum === 'string') {
    searchQuery.value = queryNum
    handleSearch(queryNum)
  }
})

const getPaymentMethodLabel = (method?: string) => {
  switch (method) {
    case 'credit_card':
      return 'Credit / Debit Card'
    case 'shopbee_pay':
      return 'Shopbee Pay (E-Wallet)'
    case 'bank_transfer':
      return 'Direct Bank Transfer'
    case 'cod':
      return 'Cash on Delivery (COD)'
    default:
      return 'Credit / Debit Card'
  }
}

const getPaymentMethodIcon = (method?: string) => {
  switch (method) {
    case 'credit_card':
      return 'pi pi-credit-card'
    case 'shopbee_pay':
      return 'pi pi-bolt'
    case 'bank_transfer':
      return 'pi pi-building'
    case 'cod':
      return 'pi pi-wallet'
    default:
      return 'pi pi-credit-card'
  }
}
</script>

<template>
  <div class="flex-1 flex flex-col gap-6 animate-slide-up">
    <!-- Breadcrumb back -->
    <div>
      <NuxtLink
        v-if="!trackedOrder"
        to="/"
        class="group inline-flex items-center gap-2.5 text-xs font-bold text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors focus:outline-none cursor-pointer"
      >
        <i
          class="pi pi-arrow-left group-hover:-translate-x-1 transition-transform"
        />
        Return to Catalog
      </NuxtLink>
      <button
        v-else
        class="group inline-flex items-center gap-2.5 text-xs font-bold text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors focus:outline-none cursor-pointer bg-transparent border-0"
        @click="clearTrackedOrder"
      >
        <i
          class="pi pi-arrow-left group-hover:-translate-x-1 transition-transform"
        />
        Back to My Orders
      </button>
    </div>

    <!-- Page Title -->
    <div>
      <h1 class="text-2xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
        {{ trackedOrder ? 'Track Your Order' : 'My Orders' }}
      </h1>
      <p class="text-xs text-slate-500 mt-1">
        {{
          trackedOrder
            ? 'Monitor shipment progress, estimated arrival details, and courier activities.'
            : 'View, track, and manage your recent purchases and active deliveries.'
        }}
      </p>
    </div>

    <!-- 1. DETAILED TRACKING VIEW -->
    <div
      v-if="trackedOrder"
      class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start mt-2"
    >
      <!-- Left Column: Status Stepper, Timeline, Map -->
      <div class="lg:col-span-8 flex flex-col gap-6">
        <!-- Main Tracking Details Card -->
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col gap-8 relative overflow-hidden"
        >
          <!-- Order Metadata header -->
          <div
            class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200 dark:border-white/5"
          >
            <div class="flex flex-col">
              <span
                class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                >Tracking Order</span
              >
              <h2
                class="text-lg sm:text-xl font-black text-slate-900 dark:text-white font-mono mt-0.5"
              >
                {{ trackedOrder.orderNumber }}
              </h2>
            </div>
            <div class="flex flex-wrap items-center gap-3">
              <span class="text-xs text-slate-500 dark:text-slate-400 font-medium">
                Placed:
                <span class="text-slate-750 dark:text-slate-200 font-bold">{{
                  trackedOrder.datePlaced
                }}</span>
              </span>
              <span
                class="px-2.5 py-0.5 rounded-full border text-[10px] font-bold uppercase tracking-wider"
                :class="statusBadge.class"
              >
                {{ statusBadge.label }}
              </span>
            </div>
          </div>

          <!-- Stepper progress timeline -->
          <div class="relative py-4">
            <!-- Background connecting line -->
            <div
              class="absolute left-6 right-6 top-1/2 -translate-y-1/2 h-1 bg-slate-100 dark:bg-slate-950 rounded-full"
            >
              <div
                class="h-full rounded-full bg-linear-to-r from-emerald-500 to-indigo-500 transition-all duration-700"
                :style="{ width: `${progressPercentage}%` }"
              />
            </div>

            <!-- Steps -->
            <div class="relative z-10 flex justify-between items-center w-full">
              <div
                v-for="(step, idx) in trackingSteps"
                :key="step.status"
                class="flex flex-col items-center gap-2.5 relative"
              >
                <!-- Step Indicator Circle -->
                <div
                  class="w-12 h-12 rounded-full flex items-center justify-center border transition-all duration-500"
                  :class="
                    idx <= currentStepIndex
                      ? 'bg-slate-100 dark:bg-slate-950 border-emerald-500 text-emerald-400 shadow-[0_0_15px_rgba(16,185,129,0.3)]'
                      : 'bg-slate-100 dark:bg-slate-950 border-slate-200 dark:border-white/5 text-slate-600'
                  "
                >
                  <i :class="step.icon" class="text-sm font-bold" />
                </div>

                <!-- Step label -->
                <span
                  class="text-[9px] sm:text-[10px] font-bold uppercase tracking-wider text-center"
                  :class="
                    idx <= currentStepIndex ? 'text-slate-900 dark:text-white' : 'text-slate-600'
                  "
                >
                  {{ step.label }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Premium SVG Map simulation -->
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col gap-4 relative overflow-hidden"
        >
          <div class="flex justify-between items-center pb-2">
            <h3
              class="text-xs font-bold uppercase tracking-wider text-indigo-650 dark:text-indigo-400"
            >
              Transit Live Map
            </h3>
            <span
              class="text-[10px] text-slate-500 font-bold uppercase tracking-wider"
              >Standard Delivery (Simulated)</span
            >
          </div>

          <!-- Map simulation screen container -->
          <div
            class="relative w-full h-44 rounded-2xl bg-linear-to-b from-slate-950 to-slate-900 border border-slate-200 dark:border-white/5 overflow-hidden shadow-inner flex items-center"
          >
            <!-- Map grid background design grid lines -->
            <div
              class="absolute inset-0 z-0 opacity-10 pointer-events-none map-grid"
            ></div>

            <!-- Abstract Route Line -->
            <svg
              class="absolute inset-0 w-full h-full z-10 pointer-events-none"
            >
              <!-- Background dashed track -->
              <path
                id="route-path"
                d="M 50 110 C 150 40, 250 140, 350 70 C 450 20, 550 160, 650 90"
                fill="none"
                stroke="#1e293b"
                stroke-width="4"
                stroke-linecap="round"
                class="w-full"
              />
              <!-- Active highlight path -->
              <path
                d="M 50 110 C 150 40, 250 140, 350 70 C 450 20, 550 160, 650 90"
                fill="none"
                stroke="url(#gradient-active)"
                stroke-width="4"
                stroke-linecap="round"
                stroke-dasharray="1000"
                :stroke-dashoffset="1000 - (1000 * mapTruckPosition) / 100"
                class="transition-all duration-1000 ease-out"
              />

              <!-- SVG Gradient definitions -->
              <defs>
                <linearGradient
                  id="gradient-active"
                  x1="0%"
                  y1="0%"
                  x2="100%"
                  y2="0%"
                >
                  <stop offset="0%" stop-color="#10b981" />
                  <stop offset="100%" stop-color="#6366f1" />
                </linearGradient>
              </defs>
            </svg>

            <!-- Map pins -->
            <!-- Start Warehouse pin -->
            <div
              class="absolute left-6 bottom-14 z-20 flex flex-col items-center gap-1"
            >
              <div
                class="w-6 h-6 rounded-full bg-emerald-500/10 border border-emerald-500 flex items-center justify-center"
              >
                <i class="pi pi-building text-[10px] text-emerald-400" />
              </div>
              <span
                class="text-[8px] font-bold text-slate-500 uppercase tracking-widest"
                >Warehouse</span
              >
            </div>

            <!-- Recipient House pin -->
            <div
              class="absolute right-6 top-16 z-20 flex flex-col items-center gap-1"
            >
              <div
                class="w-6 h-6 rounded-full bg-indigo-500/10 border border-indigo-500 flex items-center justify-center"
                :class="{
                  'animate-pulse ring-4 ring-indigo-500/20':
                    trackedOrder.status !== 'delivered'
                }"
              >
                <i class="pi pi-home text-[10px] text-indigo-650 dark:text-indigo-400" />
              </div>
              <span
                class="text-[8px] font-bold text-slate-500 uppercase tracking-widest"
                >Home</span
              >
            </div>

            <!-- Transit vehicle visual tracker (Moving depending on progress) -->
            <div
              class="absolute z-30 flex flex-col items-center gap-1.5 pointer-events-none transition-all duration-1000 ease-out"
              :style="{
                left: `${mapTruckPosition}%`,
                transform: 'translateX(-50%)',
                top: '35%'
              }"
            >
              <div
                class="w-8 h-8 rounded-full bg-indigo-500 text-slate-900 dark:text-white flex items-center justify-center shadow-[0_0_15px_rgba(99,102,241,0.6)]"
                :class="{
                  'animate-bounce': trackedOrder.status !== 'delivered'
                }"
              >
                <i
                  :class="
                    trackedOrder.status === 'delivered'
                      ? 'pi pi-check'
                      : 'pi pi-truck'
                  "
                  class="text-xs"
                />
              </div>
              <span
                class="text-[9px] bg-white dark:bg-slate-100 dark:bg-slate-950/90 border border-slate-200 dark:border-white/10 text-indigo-300 font-extrabold uppercase px-1.5 py-0.5 rounded-md tracking-wider shadow-md whitespace-nowrap"
              >
                {{
                  trackedOrder.status === 'delivered'
                    ? 'Delivered'
                    : 'Package Transit'
                }}
              </span>
            </div>
          </div>
        </div>

        <!-- Activity Timeline Tree logs -->
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col gap-5"
        >
          <h3
            class="text-xs font-bold uppercase tracking-wider text-indigo-650 dark:text-indigo-400"
          >
            Activity Log
          </h3>

          <div
            class="flex flex-col relative pl-6 border-l border-slate-200 dark:border-white/5 ml-3 gap-6"
          >
            <div
              v-for="(evt, idx) in [...trackedOrder.timeline].reverse()"
              :key="idx"
              class="relative flex flex-col gap-1"
            >
              <!-- Timeline Circular Node Icon -->
              <span
                class="absolute left-[-31px] top-0.5 w-4.5 h-4.5 rounded-full flex items-center justify-center border"
                :class="
                  idx === 0
                    ? 'bg-slate-100 dark:bg-slate-950 border-indigo-500 text-indigo-650 dark:text-indigo-400'
                    : 'bg-slate-100 dark:bg-slate-950 border-slate-200 dark:border-white/10 text-slate-600'
                "
              >
                <i class="pi pi-circle-fill text-[6px]" />
              </span>

              <!-- Log metadata -->
              <div class="flex items-center gap-3">
                <h4
                  class="text-xs font-extrabold"
                  :class="idx === 0 ? 'text-slate-900 dark:text-white' : 'text-slate-600 dark:text-slate-300'"
                >
                  {{ evt.title }}
                </h4>
                <span class="text-[9px] font-mono text-slate-500">{{
                  evt.timestamp
                }}</span>
              </div>
              <p class="text-xs text-slate-500 dark:text-slate-400 leading-normal max-w-lg">
                {{ evt.description }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Shipping address, billing breakdown -->
      <div class="lg:col-span-4 flex flex-col gap-6">
        <!-- Recipient and Shipping Carrier details -->
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col gap-5"
        >
          <h3
            class="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white pb-3 border-b border-slate-200 dark:border-white/5"
          >
            Delivery Details
          </h3>

          <!-- Shipping Recipient address -->
          <div class="flex flex-col gap-1.5 text-xs font-medium">
            <span
              class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
              >Recipient Details</span
            >
            <span class="text-slate-900 dark:text-white font-extrabold text-sm">{{
              trackedOrder.shippingAddress.name
            }}</span>
            <span class="text-slate-500 dark:text-slate-400 leading-normal">
              {{ trackedOrder.shippingAddress.address }}, <br />
              {{ trackedOrder.shippingAddress.city }}
              {{ trackedOrder.shippingAddress.zip }}
            </span>
            <span class="text-slate-500 mt-0.5 font-semibold">{{
              trackedOrder.shippingAddress.email
            }}</span>
          </div>

          <div class="h-px bg-slate-100 dark:bg-white/5" />

          <!-- Carrier Details -->
          <div class="flex flex-col gap-1.5 text-xs font-medium">
            <span
              class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
              >Shipping Carrier</span
            >
            <div class="flex justify-between items-center mt-0.5">
              <span class="text-slate-600 dark:text-slate-300 font-bold">{{
                trackedOrder.carrier || 'Pending'
              }}</span>
              <span
                v-if="trackedOrder.trackingNumber"
                class="px-2 py-0.5 bg-slate-100 dark:bg-slate-950 border border-slate-200 dark:border-white/15 rounded text-[10px] font-mono font-bold text-indigo-650 dark:text-indigo-400 select-all"
                >{{ trackedOrder.trackingNumber }}</span
              >
              <span v-else class="text-slate-600 text-xs italic"
                >Awaiting dispatch</span
              >
            </div>
            <span class="text-[9px] text-slate-500 mt-1.5 font-semibold"
              >Est. Delivery: 3-5 Working Days</span
            >
          </div>

          <div class="h-px bg-slate-100 dark:bg-white/5" />

          <!-- Payment Details -->
          <div class="flex flex-col gap-1.5 text-xs font-medium">
            <span
              class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
              >Payment Method</span
            >
            <div class="flex items-center gap-2 mt-0.5 text-slate-600 dark:text-slate-300">
              <i
                :class="getPaymentMethodIcon(trackedOrder.paymentMethod)"
                class="text-indigo-650 dark:text-indigo-400 text-xs"
              />
              <span class="font-bold">
                {{ getPaymentMethodLabel(trackedOrder.paymentMethod) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Order Items Breakdown -->
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col gap-5"
        >
          <h3
            class="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white pb-3 border-b border-slate-200 dark:border-white/5"
          >
            Purchase Details
          </h3>

          <div class="flex flex-col gap-4">
            <div
              v-for="(item, idx) in trackedOrder.items"
              :key="idx"
              class="flex items-center gap-3"
            >
              <!-- Item Image -->
              <div
                class="w-12 h-12 rounded-xl overflow-hidden bg-slate-100 dark:bg-slate-950 border border-slate-200 dark:border-white/5 shrink-0"
              >
                <img
                  :src="item.imageUrl"
                  :alt="item.name"
                  class="w-full h-full object-cover"
                />
              </div>
              <div class="flex flex-col min-w-0 flex-1">
                <span class="text-xs font-bold text-slate-900 dark:text-white truncate">{{
                  item.name
                }}</span>
                <span class="text-[9px] text-slate-500 font-bold mt-0.5">
                  Qty: {{ item.quantity }} &bull; Price:
                  {{ formatCurrency(item.price) }}
                </span>
                <div
                  v-if="Object.keys(item.selectedVariants).length > 0"
                  class="flex flex-wrap gap-1 mt-1"
                >
                  <span
                    v-for="(val, name) in item.selectedVariants"
                    :key="name"
                    class="px-1.5 py-0.5 bg-slate-100 dark:bg-slate-950 rounded text-[8px] text-slate-500 dark:text-slate-400"
                  >
                    {{ name }}: {{ val }}
                  </span>
                </div>
              </div>
              <span class="text-xs font-extrabold text-slate-750 dark:text-slate-200 shrink-0">
                {{ formatCurrency(item.price * item.quantity) }}
              </span>
            </div>
          </div>

          <div class="h-px bg-slate-100 dark:bg-white/5" />

          <!-- Invoice pricing rows -->
          <div class="flex flex-col gap-2.5 text-xs font-semibold">
            <div class="flex justify-between items-center">
              <span class="text-slate-500">Subtotal</span>
              <span class="text-slate-600 dark:text-slate-300">{{
                formatCurrency(trackedOrder.subtotal)
              }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-slate-500">Shipping</span>
              <span class="text-slate-600 dark:text-slate-300">
                {{
                  trackedOrder.shippingFee === 0
                    ? 'FREE'
                    : formatCurrency(trackedOrder.shippingFee)
                }}
              </span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-slate-500">Tax (8%)</span>
              <span class="text-slate-600 dark:text-slate-300">{{
                formatCurrency(trackedOrder.tax)
              }}</span>
            </div>
            <div
              v-if="trackedOrder.discount > 0"
              class="flex justify-between items-center text-emerald-400 font-bold"
            >
              <span>Discount</span>
              <span>-{{ formatCurrency(trackedOrder.discount) }}</span>
            </div>
            <div class="h-px bg-slate-100 dark:bg-white/5 my-0.5" />
            <div
              class="flex justify-between items-center text-slate-900 dark:text-white font-bold text-sm"
            >
              <span>Total Paid</span>
              <span class="text-indigo-650 dark:text-indigo-400 font-black">{{
                formatCurrency(trackedOrder.totalPaid)
              }}</span>
            </div>
          </div>
        </div>

        <!-- Interactive Developer Sandbox Panel -->
        <div
          class="bg-indigo-950/20 border border-indigo-500/20 rounded-3xl p-5 shadow-2xl flex flex-col gap-3.5 relative overflow-hidden"
        >
          <div
            class="absolute inset-0 z-0 bg-[radial-gradient(circle_at_top_right,rgba(99,102,241,0.08),transparent_50%)] pointer-events-none"
          ></div>

          <div class="flex items-center gap-2 relative z-10">
            <i
              class="pi pi-cog text-indigo-650 dark:text-indigo-400 text-sm animate-spin"
              style="animation-duration: 8s"
            />
            <h4
              class="text-xs font-black uppercase tracking-wider text-indigo-300"
            >
              Demo Control Sandbox
            </h4>
          </div>
          <p
            class="text-[10px] text-slate-500 dark:text-slate-400 leading-relaxed relative z-10 font-medium"
          >
            Simulate the shipping cycle in real time. Click the action button
            below to advance this order to the next delivery milestone step.
          </p>
          <Button
            label="Advance Order Status"
            icon="pi pi-arrow-circle-right"
            :disabled="trackedOrder.status === 'delivered'"
            class="w-full py-2.5 bg-indigo-600/30 hover:bg-indigo-600 border border-indigo-500/30 hover:border-indigo-500 hover:shadow-[0_0_12px_rgba(99,102,241,0.3)] disabled:opacity-40 disabled:pointer-events-none rounded-xl font-bold text-xs text-indigo-300 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-all cursor-pointer flex items-center justify-center gap-1.5 mt-1 relative z-10"
            @click="handleDemoAdvance"
          />
        </div>
      </div>
    </div>

    <!-- 2. ORDERS DASHBOARD VIEW (LIST OF ALL ORDERS) -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start mt-2">
      <!-- Left Main Column: Active orders list, past purchases -->
      <div class="lg:col-span-8 flex flex-col gap-6">
        <!-- 2A. ACTIVE (IN PROGRESS) ORDERS -->
        <div class="flex flex-col gap-4">
          <h2
            class="text-xs font-black uppercase tracking-wider text-slate-900 dark:text-white flex items-center gap-2"
          >
            <i class="pi pi-truck text-indigo-650 dark:text-indigo-400" />
            In Progress Orders
            <span
              v-if="activeOrders.length > 0"
              class="bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 text-[10px] font-bold px-2 py-0.5 rounded-full"
            >
              {{ activeOrders.length }} Active
            </span>
          </h2>

          <div
            v-if="activeOrders.length === 0"
            class="flex flex-col items-center justify-center text-center p-10 bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/10 border border-dashed border-slate-200 dark:border-white/5 rounded-3xl min-h-[220px] shadow-sm"
          >
            <div
              class="w-14 h-14 rounded-2xl bg-indigo-500/10 flex items-center justify-center mb-4 border border-indigo-500/20"
            >
              <i class="pi pi-shopping-bag text-indigo-650 dark:text-indigo-400 text-lg" />
            </div>
            <h4 class="text-sm font-extrabold text-slate-750 dark:text-slate-200 mb-1">
              No active shipments
            </h4>
            <p
              class="text-xs text-slate-500 dark:text-slate-400 max-w-xs leading-relaxed font-semibold"
            >
              You don't have any orders in transit right now. Visit our catalog
              to make a purchase!
            </p>
            <NuxtLink
              to="/"
              class="mt-4 px-4 py-2 bg-slate-100 dark:bg-slate-900 hover:bg-slate-800 border border-slate-200 dark:border-white/10 text-xs font-bold text-slate-900 dark:text-white rounded-xl transition-all"
            >
              Explore Products
            </NuxtLink>
          </div>

          <!-- Active orders cards -->
          <div v-else class="flex flex-col gap-4">
            <div
              v-for="order in activeOrders"
              :key="order.orderNumber"
              class="group bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 hover:border-indigo-500/20 p-5 rounded-3xl shadow-xl flex flex-col gap-4 transition-all duration-300 hover:shadow-[0_0_20px_rgba(99,102,241,0.05)]"
            >
              <!-- Card Header -->
              <div
                class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-200 dark:border-white/5"
              >
                <div class="flex flex-col">
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                    >Order ID</span
                  >
                  <span class="text-sm font-bold text-slate-900 dark:text-white font-mono mt-0.5">{{
                    order.orderNumber
                  }}</span>
                </div>
                <div class="flex items-center gap-3">
                  <span class="text-[11px] text-slate-500 dark:text-slate-400 font-medium">
                    Placed:
                    <span class="text-slate-750 dark:text-slate-200 font-bold">{{
                      order.datePlaced
                    }}</span>
                  </span>
                  <span
                    class="px-2 py-0.5 rounded-full border text-[9px] font-bold uppercase tracking-wider"
                    :class="getStatusBadgeInfo(order.status).class"
                  >
                    {{ getStatusBadgeInfo(order.status).label }}
                  </span>
                </div>
              </div>

              <!-- Card Body: Item thumbnail images and info -->
              <div class="flex items-start gap-4">
                <div class="flex shrink-0 -space-x-2.5">
                  <div
                    v-for="(item, idx) in order.items.slice(0, 3)"
                    :key="idx"
                    class="w-12 h-12 rounded-xl overflow-hidden bg-slate-100 dark:bg-slate-950 border border-slate-900 shadow-lg shrink-0"
                  >
                    <img
                      :src="item.imageUrl"
                      :alt="item.name"
                      class="w-full h-full object-cover"
                    />
                  </div>
                  <div
                    v-if="order.items.length > 3"
                    class="w-12 h-12 rounded-xl bg-slate-100 dark:bg-slate-900 border border-slate-800 flex items-center justify-center text-[10px] font-extrabold text-slate-500 dark:text-slate-400 shrink-0 shadow-lg"
                  >
                    +{{ order.items.length - 3 }}
                  </div>
                </div>

                <div class="flex-1 min-w-0 flex flex-col justify-center py-1">
                  <p class="text-xs font-bold text-slate-900 dark:text-white truncate max-w-md">
                    {{ order.items[0]?.name }}
                    <span
                      v-if="order.items.length > 1"
                      class="text-slate-500 dark:text-slate-400 font-normal"
                    >
                      and {{ order.items.length - 1 }} other
                      {{ order.items.length - 1 === 1 ? 'item' : 'items' }}
                    </span>
                  </p>
                  <p class="text-[10px] text-slate-500 font-bold mt-1">
                    Total Paid:
                    <span class="text-indigo-300 font-black">{{
                      formatCurrency(order.totalPaid)
                    }}</span>
                  </p>
                </div>
              </div>

              <!-- Sleek active progress tracker -->
              <div
                class="flex flex-col gap-2 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/20 border border-slate-200 dark:border-white/5 p-3.5 rounded-2xl"
              >
                <div
                  class="flex justify-between items-center text-[9px] text-slate-500 font-extrabold uppercase tracking-widest"
                >
                  <span
                    >Current Milestone:
                    <span class="text-slate-600 dark:text-slate-300 font-black">{{
                      getStatusLabel(order.status)
                    }}</span></span
                  >
                  <span>Est. Delivery: 3-5 Working Days</span>
                </div>
                <!-- Progress bar -->
                <div
                  class="relative w-full h-1 bg-slate-100 dark:bg-slate-950 rounded-full overflow-hidden"
                >
                  <div
                    class="h-full bg-linear-to-r from-emerald-500 to-indigo-500 transition-all duration-700"
                    :style="{
                      width: `${(orderStatuses.indexOf(order.status) / 4) * 100}%`
                    }"
                  />
                </div>
              </div>

              <!-- Tracking CTA -->
              <div class="flex justify-end mt-1">
                <Button
                  label="Track Delivery Details"
                  icon="pi pi-compass"
                  class="px-4 py-2 bg-linear-to-r from-indigo-500 to-blue-600 border-0 hover:shadow-[0_0_12px_rgba(99,102,241,0.35)] rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer flex items-center justify-center gap-1.5"
                  @click="handleSearch(order.orderNumber)"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 2B. PREVIOUS (COMPLETED) ORDERS - SECONDARY STYLING -->
        <div class="flex flex-col gap-4 mt-4">
          <h2
            class="text-xs font-black uppercase tracking-wider text-slate-500 dark:text-slate-400 flex items-center gap-2"
          >
            <i class="pi pi-history text-slate-500" />
            Previous Orders
            <span
              v-if="previousOrders.length > 0"
              class="bg-slate-500/10 border border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 text-[9px] font-bold px-1.5 py-0.5 rounded-full"
            >
              {{ previousOrders.length }} History
            </span>
          </h2>

          <div
            v-if="previousOrders.length === 0"
            class="text-center p-8 bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/10 border border-dashed border-slate-200 dark:border-white/5 rounded-3xl min-h-[140px] flex flex-col justify-center items-center"
          >
            <p class="text-xs text-slate-500 font-semibold">
              No order history available.
            </p>
          </div>

          <!-- Secondary-styled list of previous orders -->
          <div
            v-else
            class="bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/10 border border-slate-200 dark:border-white/5 rounded-3xl p-5 flex flex-col gap-3"
          >
            <div
              v-for="order in previousOrders"
              :key="order.orderNumber"
              class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/20 border border-slate-200 dark:border-white/5 rounded-2xl hover:border-slate-200 dark:border-white/10 transition-colors"
            >
              <!-- Left side: stacking preview images + basic ID detail -->
              <div class="flex items-center gap-3">
                <div class="flex shrink-0 -space-x-2">
                  <div
                    v-for="(item, idx) in order.items.slice(0, 3)"
                    :key="idx"
                    class="w-8 h-8 rounded-lg overflow-hidden bg-slate-100 dark:bg-slate-950 border border-slate-900 shadow-md shrink-0"
                  >
                    <img
                      :src="item.imageUrl"
                      :alt="item.name"
                      class="w-full h-full object-cover"
                    />
                  </div>
                  <div
                    v-if="order.items.length > 3"
                    class="w-8 h-8 rounded-lg bg-slate-100 dark:bg-slate-900 border border-slate-800 flex items-center justify-center text-[9px] font-extrabold text-slate-500 shrink-0"
                  >
                    +{{ order.items.length - 3 }}
                  </div>
                </div>

                <div class="flex flex-col min-w-0">
                  <span
                    class="text-xs font-mono font-bold text-slate-500 dark:text-slate-400 hover:text-indigo-650 dark:text-indigo-400 transition-colors cursor-pointer"
                    @click="handleSearch(order.orderNumber)"
                  >
                    {{ order.orderNumber }}
                  </span>
                  <span class="text-[9px] text-slate-500 font-bold">
                    Placed {{ order.datePlaced }} &bull;
                    {{ order.items.length }}
                    {{ order.items.length === 1 ? 'item' : 'items' }}
                  </span>
                </div>
              </div>

              <!-- Right side: total price + secondary status tag + simple text detail link -->
              <div
                class="flex items-center justify-between sm:justify-end gap-6"
              >
                <div class="flex flex-col sm:text-right">
                  <span class="text-xs font-bold text-slate-600 dark:text-slate-300">{{
                    formatCurrency(order.totalPaid)
                  }}</span>
                  <span class="text-[9px] text-slate-500 font-semibold">{{
                    getPaymentMethodLabel(order.paymentMethod)
                  }}</span>
                </div>
                <div class="flex items-center gap-3.5">
                  <span
                    class="px-2 py-0.5 rounded-full border text-[8px] font-extrabold uppercase tracking-widest"
                    :class="
                      order.status === 'delivered'
                        ? 'text-slate-500 bg-slate-500/5 border-slate-200 dark:border-white/5'
                        : 'text-rose-400/80 bg-rose-500/5 border-rose-500/10'
                    "
                  >
                    {{ order.status }}
                  </span>
                  <button
                    class="text-xs font-bold text-indigo-650 dark:text-indigo-400 hover:text-indigo-600 dark:hover:text-indigo-300 transition-colors cursor-pointer bg-transparent border-0 focus:outline-none"
                    @click="handleSearch(order.orderNumber)"
                  >
                    Details
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Sidebar (Quick Track Input & Account stats) -->
      <div class="lg:col-span-4 flex flex-col gap-6">
        <!-- Sidebar Widget: Quick Search -->
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 p-5 sm:p-6 rounded-3xl shadow-xl flex flex-col gap-4 relative overflow-hidden"
        >
          <div class="flex flex-col gap-1">
            <h3
              class="text-xs font-black uppercase tracking-wider text-slate-600 dark:text-slate-300"
            >
              Track Guest Order
            </h3>
            <p class="text-[10px] text-slate-500 leading-normal font-semibold">
              Enter an order code (e.g. from an invoice) to track it directly.
            </p>
          </div>
          <div class="flex flex-col gap-2">
            <div class="relative w-full">
              <span
                class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
              >
                <i class="pi pi-compass text-slate-500 text-xs"></i>
              </span>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="e.g. SB-2026-104928"
                class="w-full bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/10 hover:border-slate-300 dark:hover:border-white/20 focus:border-indigo-500 text-slate-750 dark:text-slate-200 placeholder-slate-600 py-2.5 pl-8 pr-3 rounded-xl font-mono text-xs transition-all focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                @keyup.enter="handleSearch()"
              />
            </div>
            <Button
              :label="isSubmitting ? 'Searching...' : 'Track'"
              icon="pi pi-search"
              :loading="isSubmitting"
              class="w-full py-2.5 bg-linear-to-r from-indigo-500 to-blue-600 border-0 hover:shadow-[0_0_10px_rgba(99,102,241,0.3)] rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer flex items-center justify-center gap-1.5"
              @click="handleSearch()"
            />
          </div>
        </div>

        <!-- Sidebar Widget: Stats Overview -->
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 p-5 sm:p-6 rounded-3xl shadow-xl flex flex-col gap-4 relative overflow-hidden"
        >
          <div
            class="absolute inset-0 z-0 bg-[radial-gradient(circle_at_top_right,rgba(99,102,241,0.05),transparent_50%)] pointer-events-none"
          ></div>
          <div class="relative z-10 flex flex-col gap-3">
            <h3
              class="text-xs font-black uppercase tracking-wider text-slate-900 dark:text-white pb-2.5 border-b border-slate-200 dark:border-white/5"
            >
              Account Summary
            </h3>
            <div class="flex flex-col gap-2.5 text-xs font-semibold">
              <div class="flex justify-between items-center">
                <span class="text-slate-500">Customer Profile</span>
                <span class="text-slate-600 dark:text-slate-300 font-bold">Guest Customer</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-slate-500">In Transit</span>
                <span class="text-slate-900 dark:text-white font-bold">{{
                  activeOrders.length
                }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-slate-500">Delivered</span>
                <span class="text-slate-900 dark:text-white font-bold">{{
                  previousOrders.length
                }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Abstract Grid map backdrop lines styling */
.map-grid {
  background-size: 20px 20px;
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
}
</style>
