<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useProducts } from '~/composables/useProducts'
import type { Product, Review } from '~/composables/useProducts'
import { useOrders } from '~/composables/useOrders'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { products, getProductById, addToCart, reviewsList, addReview } =
  useProducts()
const { addOrder } = useOrders()

if (products.value.length === 0) {
  const { data: serverProducts } = await useFetch('/api/products')
  if (serverProducts.value) {
    products.value = serverProducts.value as Product[]
  }
}

const productId = route.params.id
const activeProductId = Array.isArray(productId) ? productId[0] : productId
if (activeProductId) {
  const { data: serverReviews } = await useFetch('/api/reviews', {
    query: { productId: activeProductId }
  })
  if (serverReviews.value) {
    const numericProductId = parseInt(activeProductId, 10)
    const filteredExisting = reviewsList.value.filter(
      (r: Review) => r.productId !== numericProductId
    )
    reviewsList.value = [
      ...filteredExisting,
      ...(serverReviews.value as Review[])
    ]
  }
}

// ─── Product ───────────────────────────────────────────────────────────────
const product = computed(() => {
  const id = route.params.id
  const activeId = Array.isArray(id) ? id[0] : id
  return activeId ? getProductById(activeId) : undefined
})

const formattedPrice = computed(() => {
  if (!product.value) return '$0.00'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(product.value.price)
})

const stockStatus = computed(() => {
  if (!product.value) return null
  if (product.value.stock === 0)
    return {
      label: 'Out of Stock',
      colorClass: 'text-rose-400 bg-rose-500/10 border-rose-500/20'
    }
  if (product.value.stock <= 5)
    return {
      label: `Low Stock (${product.value.stock} left)`,
      colorClass: 'text-amber-400 bg-amber-500/10 border-amber-500/20'
    }
  return {
    label: 'In Stock',
    colorClass: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20'
  }
})

const selectedVariants = ref<Record<string, string>>({})

watch(
  product,
  (newProduct) => {
    selectedVariants.value = {}
    if (newProduct?.variants) {
      newProduct.variants.forEach((v) => {
        if (v.options.length > 0) {
          const firstOpt = v.options[0]
          if (firstOpt) {
            selectedVariants.value[v.name] = firstOpt
          }
        }
      })
    }
  },
  { immediate: true }
)

const handleAddToCart = () => {
  if (!product.value) return
  const success = addToCart(product.value, 1, selectedVariants.value)
  if (success) {
    toast.add({
      severity: 'success',
      summary: 'Added to Cart',
      detail: `${product.value.name} has been added to your cart.`,
      life: 3000
    })
  } else {
    toast.add({
      severity: 'error',
      summary: 'Out of Stock',
      detail: `Sorry, ${product.value.name} is currently unavailable or quantity exceeds stock.`,
      life: 3000
    })
  }
}

const goBack = () => router.push('/')

// ─── Direct Purchase Checkout State ─────────────────────────────────────────
const directPurchaseVisible = ref(false)
const purchaseActiveStep = ref(1) // 1: Options, 2: Shipping, 3: Payment, 4: Success
const purchaseVariants = ref<Record<string, string>>({})

const directShippingForm = ref({
  name: '',
  email: '',
  address: '',
  city: '',
  zip: ''
})
const directShippingErrors = ref({
  name: '',
  email: '',
  address: '',
  city: '',
  zip: ''
})

const directPaymentForm = ref({
  cardNumber: '',
  cardName: '',
  expiry: '',
  cvc: ''
})
const directPaymentErrors = ref({
  cardNumber: '',
  cardName: '',
  expiry: '',
  cvc: ''
})
const directPaymentMethod = ref<
  'credit_card' | 'shopbee_pay' | 'bank_transfer' | 'cod'
>('credit_card')

const isProcessingDirectPayment = ref(false)
const directOrderNumber = ref('')
const directOrderTotalPaid = ref(0)

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(val)
}

const directSubtotal = computed(() => product.value?.price ?? 0)
const directShippingFee = computed(() => {
  if (directSubtotal.value === 0) return 0
  return directSubtotal.value > 200 ? 0 : 15.0
})
const directTax = computed(() => directSubtotal.value * 0.08)
const directTotal = computed(
  () => directSubtotal.value + directShippingFee.value + directTax.value
)

const formatDirectCardNumberInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  let value = target.value.replace(/\D/g, '')
  if (value.length > 16) {
    value = value.substring(0, 16)
  }
  const parts = []
  for (let i = 0; i < value.length; i += 4) {
    parts.push(value.substring(i, i + 4))
  }
  directPaymentForm.value.cardNumber = parts.join(' ')
}

const formatDirectExpiryInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  let value = target.value.replace(/\D/g, '')
  if (value.length > 4) {
    value = value.substring(0, 4)
  }
  if (value.length > 2) {
    directPaymentForm.value.expiry = `${value.slice(0, 2)}/${value.slice(2)}`
  } else {
    directPaymentForm.value.expiry = value
  }
}

const formatDirectCvcInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  let value = target.value.replace(/\D/g, '')
  if (value.length > 3) {
    value = value.substring(0, 3)
  }
  directPaymentForm.value.cvc = value
}

const validateDirectShipping = () => {
  let valid = true
  directShippingErrors.value = {
    name: '',
    email: '',
    address: '',
    city: '',
    zip: ''
  }

  if (!directShippingForm.value.name.trim()) {
    directShippingErrors.value.name = 'Full name is required.'
    valid = false
  } else if (directShippingForm.value.name.trim().length < 2) {
    directShippingErrors.value.name = 'Name must be at least 2 characters.'
    valid = false
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!directShippingForm.value.email.trim()) {
    directShippingErrors.value.email = 'Email address is required.'
    valid = false
  } else if (!emailRegex.test(directShippingForm.value.email.trim())) {
    directShippingErrors.value.email = 'Please enter a valid email address.'
    valid = false
  }

  if (!directShippingForm.value.address.trim()) {
    directShippingErrors.value.address = 'Shipping address is required.'
    valid = false
  }

  if (!directShippingForm.value.city.trim()) {
    directShippingErrors.value.city = 'City is required.'
    valid = false
  }

  if (!directShippingForm.value.zip.trim()) {
    directShippingErrors.value.zip = 'Postal code is required.'
    valid = false
  }

  return valid
}

const validateDirectPayment = () => {
  if (directPaymentMethod.value !== 'credit_card') {
    return true
  }
  let valid = true
  directPaymentErrors.value = {
    cardNumber: '',
    cardName: '',
    expiry: '',
    cvc: ''
  }

  const rawCard = directPaymentForm.value.cardNumber.replace(/\s+/g, '')
  if (rawCard.length < 16) {
    directPaymentErrors.value.cardNumber =
      'Please enter a valid 16-digit card number.'
    valid = false
  }

  if (!directPaymentForm.value.cardName.trim()) {
    directPaymentErrors.value.cardName = 'Cardholder name is required.'
    valid = false
  }

  if (
    !directPaymentForm.value.expiry ||
    !/^\d{2}\/\d{2}$/.test(directPaymentForm.value.expiry)
  ) {
    directPaymentErrors.value.expiry = 'Expiry must be in MM/YY format.'
    valid = false
  } else {
    const [monthStr, yearStr] = directPaymentForm.value.expiry.split('/')
    if (monthStr && yearStr) {
      const month = parseInt(monthStr, 10)
      if (month < 1 || month > 12) {
        directPaymentErrors.value.expiry =
          'Expiration month must be between 01 and 12.'
        valid = false
      }
    }
  }

  if (directPaymentForm.value.cvc.length < 3) {
    directPaymentErrors.value.cvc = 'CVC must be 3 digits.'
    valid = false
  }

  return valid
}

const directPurchaseSteps = computed(() => {
  const list = []
  let count = 1
  if (product.value?.variants && product.value.variants.length > 0) {
    list.push({ id: 1, displayNum: count++, label: 'Options' })
  }
  list.push({ id: 2, displayNum: count++, label: 'Shipping' })
  list.push({ id: 3, displayNum: count++, label: 'Payment' })
  list.push({ id: 4, displayNum: count++, label: 'Success' })
  return list
})

const nextDirectStep = () => {
  if (purchaseActiveStep.value === 1) {
    purchaseActiveStep.value = 2
  } else if (purchaseActiveStep.value === 2 && validateDirectShipping()) {
    purchaseActiveStep.value = 3
  }
}

const prevDirectStep = () => {
  if (purchaseActiveStep.value === 3) {
    purchaseActiveStep.value = 2
  } else if (purchaseActiveStep.value === 2) {
    if (product.value?.variants && product.value.variants.length > 0) {
      purchaseActiveStep.value = 1
    }
  }
}

const closeDirectPurchase = () => {
  directPurchaseVisible.value = false
  if (purchaseActiveStep.value === 4) {
    purchaseActiveStep.value = 1
    directShippingForm.value = {
      name: '',
      email: '',
      address: '',
      city: '',
      zip: ''
    }
    directPaymentForm.value = {
      cardNumber: '',
      cardName: '',
      expiry: '',
      cvc: ''
    }
    directPaymentMethod.value = 'credit_card'
    directOrderTotalPaid.value = 0
  }
}

const openDirectPurchase = () => {
  if (!product.value) return

  purchaseVariants.value = { ...selectedVariants.value }

  if (product.value.variants && product.value.variants.length > 0) {
    purchaseActiveStep.value = 1
  } else {
    purchaseActiveStep.value = 2
  }

  directPurchaseVisible.value = true
}

const submitDirectPayment = async () => {
  if (!validateDirectPayment()) return
  isProcessingDirectPayment.value = true

  // Simulate network transaction latency
  await new Promise((resolve) => setTimeout(resolve, 2200))

  isProcessingDirectPayment.value = false
  directOrderNumber.value = `SB-2026-${Math.floor(100000 + Math.random() * 900000)}`
  purchaseActiveStep.value = 4 // Success step

  directOrderTotalPaid.value = directTotal.value

  // Create single item order
  const orderItem = {
    productId: product.value!.id,
    name: product.value!.name,
    price: product.value!.price,
    quantity: 1,
    selectedVariants: { ...purchaseVariants.value },
    imageUrl: product.value!.imageUrl
  }

  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10)
  const timeStr = `${dateStr} ${now.toTimeString().slice(0, 5)}`

  addOrder({
    orderNumber: directOrderNumber.value,
    datePlaced: dateStr,
    status: 'placed',
    shippingAddress: {
      name: directShippingForm.value.name,
      email: directShippingForm.value.email,
      address: directShippingForm.value.address,
      city: directShippingForm.value.city,
      zip: directShippingForm.value.zip
    },
    items: [orderItem],
    subtotal: directSubtotal.value,
    shippingFee: directShippingFee.value,
    tax: directTax.value,
    discount: 0,
    totalPaid: directTotal.value,
    paymentMethod: directPaymentMethod.value,
    timeline: [
      {
        status: 'placed',
        title: 'Order Placed',
        description:
          'Thank you for your purchase. We have received your order details.',
        timestamp: timeStr
      }
    ]
  })

  toast.add({
    severity: 'success',
    summary: 'Payment Successful',
    detail: 'Your payment was approved and order has been created.',
    life: 5000
  })
}

// ─── Gallery ──────────────────────────────────────────────────────────────
const activeImageIndex = ref(0)

const galleryImages = computed(() => {
  if (!product.value) return []
  return product.value.images?.length
    ? product.value.images
    : [product.value.imageUrl]
})

const activeImage = computed(
  () => galleryImages.value[activeImageIndex.value] ?? galleryImages.value[0]
)

// Reset gallery index on route/product change
watch(
  () => route.params.id,
  () => {
    activeImageIndex.value = 0
  }
)

// ─── Reviews ──────────────────────────────────────────────────────────────
const productReviews = computed(() =>
  reviewsList.value.filter((r) => r.productId === product.value?.id)
)

const ratingFilter = ref<'all' | '5' | '4' | '3' | '2' | '1'>('all')
const reviewsSort = ref<'recent' | 'highest' | 'lowest'>('recent')

const sortOptions = [
  { label: 'Most Recent', value: 'recent' },
  { label: 'Highest Rating', value: 'highest' },
  { label: 'Lowest Rating', value: 'lowest' }
]

const sortedAndFilteredReviews = computed(() => {
  let list = [...productReviews.value]
  if (ratingFilter.value !== 'all') {
    const target = parseInt(ratingFilter.value, 10)
    list = list.filter((r) => r.rating === target)
  }
  if (reviewsSort.value === 'recent') {
    list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
  } else if (reviewsSort.value === 'highest') {
    list.sort((a, b) => b.rating - a.rating)
  } else {
    list.sort((a, b) => a.rating - b.rating)
  }
  return list
})

// ─── Rating Histogram ─────────────────────────────────────────────────────
const ratingStats = computed(() => {
  const total = productReviews.value.length
  const counts: Record<number, number> = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  productReviews.value.forEach((r) => {
    counts[r.rating] = (counts[r.rating] ?? 0) + 1
  })
  return [5, 4, 3, 2, 1].map((stars) => ({
    stars,
    count: counts[stars] ?? 0,
    percentage: total ? Math.round(((counts[stars] ?? 0) / total) * 100) : 0
  }))
})

const averageRating = computed(() => {
  const total = productReviews.value.length
  if (!total) return product.value?.rating ?? 0
  const sum = productReviews.value.reduce((acc, r) => acc + r.rating, 0)
  return parseFloat((sum / total).toFixed(1))
})

// ─── Write a Review Form ──────────────────────────────────────────────────
const showReviewForm = ref(false)
const isSubmitting = ref(false)
const hoverRating = ref(0)

const form = ref({ author: '', rating: 5, title: '', comment: '' })
const errors = ref({ author: '', comment: '' })

const toggleReviewForm = () => {
  showReviewForm.value = !showReviewForm.value
  if (!showReviewForm.value) resetForm()
}

const resetForm = () => {
  form.value = { author: '', rating: 5, title: '', comment: '' }
  errors.value = { author: '', comment: '' }
  hoverRating.value = 0
}

const setRating = (val: number) => {
  form.value.rating = val
}

const validateForm = () => {
  let valid = true
  errors.value = { author: '', comment: '' }
  if (form.value.author.trim().length < 2) {
    errors.value.author = 'Name must be at least 2 characters.'
    valid = false
  }
  if (form.value.comment.trim().length < 10) {
    errors.value.comment = 'Review must be at least 10 characters.'
    valid = false
  }
  return valid
}

const submitReview = async () => {
  if (!validateForm() || !product.value) return
  isSubmitting.value = true
  await new Promise((resolve) => setTimeout(resolve, 600))
  addReview(product.value.id, { ...form.value })
  isSubmitting.value = false
  showReviewForm.value = false
  resetForm()
  toast.add({
    severity: 'success',
    summary: 'Review Submitted',
    detail: 'Thank you! Your review has been published.',
    life: 4000
  })
}

// ─── Helpers ──────────────────────────────────────────────────────────────
const displayedStarRating = computed(
  () => hoverRating.value || form.value.rating
)

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const authorInitials = (name: string) => {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

const avatarColor = (name: string) => {
  const colors = [
    'from-indigo-500 to-blue-600',
    'from-violet-500 to-purple-600',
    'from-emerald-500 to-teal-600',
    'from-amber-500 to-orange-600',
    'from-rose-500 to-pink-600',
    'from-cyan-500 to-sky-600'
  ]
  let hash = 0
  for (const ch of name) hash = ch.charCodeAt(0) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}
</script>

<template>
  <div class="flex-1 flex flex-col gap-6 animate-slide-up">
    <!-- Back to Catalog -->
    <div>
      <button
        class="group flex items-center gap-2.5 text-xs font-bold text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors focus:outline-none"
        @click="goBack"
      >
        <i
          class="pi pi-arrow-left group-hover:-translate-x-1 transition-transform"
        />
        Back to Catalog
      </button>
    </div>

    <!-- Main Content -->
    <div v-if="product" class="flex flex-col gap-8">
      <!-- Top Row: Gallery + Purchase Panel -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <!-- LEFT: Gallery -->
        <div class="lg:col-span-5 flex flex-col gap-4">
          <!-- Main Image Viewport -->
          <div
            class="bg-white dark:bg-slate-100 dark:bg-slate-900/40 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl overflow-hidden shadow-2xl p-3"
          >
            <div
              class="relative w-full aspect-square rounded-2xl overflow-hidden bg-slate-100 dark:bg-slate-950"
            >
              <Transition name="gallery-fade" mode="out-in">
                <img
                  :key="activeImage"
                  :src="activeImage"
                  :alt="product.name"
                  class="w-full h-full object-cover"
                />
              </Transition>
            </div>
          </div>

          <!-- Thumbnail Strip -->
          <div
            v-if="galleryImages.length > 1"
            class="flex gap-3 overflow-x-auto pb-1"
          >
            <button
              v-for="(img, idx) in galleryImages"
              :key="idx"
              class="shrink-0 w-16 h-16 rounded-xl overflow-hidden border-2 transition-all duration-200 focus:outline-none"
              :class="
                activeImageIndex === idx
                  ? 'border-indigo-500 ring-2 ring-indigo-500/40 scale-105'
                  : 'border-slate-200 dark:border-white/10 hover:border-white/30 opacity-60 hover:opacity-100'
              "
              @click="activeImageIndex = idx"
            >
              <img
                :src="img"
                :alt="`${product.name} view ${idx + 1}`"
                class="w-full h-full object-cover"
              />
            </button>
          </div>

          <!-- Technical Specifications -->
          <div
            class="bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/20 border border-slate-200 dark:border-white/5 rounded-3xl p-5 flex flex-col gap-3"
          >
            <h3
              class="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
            >
              Specifications
            </h3>
            <!-- Quick SKU / Brand / Category meta -->
            <div class="grid grid-cols-2 gap-3">
              <div
                class="flex flex-col bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-xl p-3"
              >
                <span
                  class="text-[10px] text-slate-500 font-bold uppercase tracking-wider"
                  >SKU</span
                >
                <span
                  class="text-xs font-semibold text-slate-750 dark:text-slate-200 mt-1 font-mono break-all"
                  >{{ product.sku }}</span
                >
              </div>
              <div
                class="flex flex-col bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-xl p-3"
              >
                <span
                  class="text-[10px] text-slate-500 font-bold uppercase tracking-wider"
                  >Brand</span
                >
                <span class="text-xs font-semibold text-slate-750 dark:text-slate-200 mt-1">{{
                  product.brand
                }}</span>
              </div>
              <div
                class="flex flex-col bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-xl p-3"
              >
                <span
                  class="text-[10px] text-slate-500 font-bold uppercase tracking-wider"
                  >Category</span
                >
                <span class="text-xs font-semibold text-slate-750 dark:text-slate-200 mt-1">{{
                  product.category
                }}</span>
              </div>
              <div
                class="flex flex-col bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-xl p-3"
              >
                <span
                  class="text-[10px] text-slate-500 font-bold uppercase tracking-wider"
                  >Availability</span
                >
                <span
                  v-if="stockStatus"
                  class="text-xs font-bold mt-1"
                  :class="
                    product.stock === 0
                      ? 'text-rose-400'
                      : product.stock <= 5
                        ? 'text-amber-400'
                        : 'text-emerald-400'
                  "
                  >{{ stockStatus.label }}</span
                >
              </div>
            </div>
            <!-- Full Specs Table -->
            <div
              v-if="product.specs && Object.keys(product.specs).length > 0"
              class="flex flex-col divide-y divide-slate-100 dark:divide-white/5 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/30 rounded-2xl border border-slate-200 dark:border-white/5 overflow-hidden"
            >
              <div
                v-for="(val, key) in product.specs"
                :key="key"
                class="grid grid-cols-5 gap-2 px-4 py-2.5 hover:bg-white dark:bg-slate-100 dark:bg-slate-900/30 transition-colors"
              >
                <span
                  class="text-[10px] font-semibold text-slate-500 uppercase tracking-wide col-span-2 flex items-center"
                  >{{ key }}</span
                >
                <span
                  class="text-[11px] font-semibold text-slate-750 dark:text-slate-200 col-span-3 text-right flex items-center justify-end"
                  >{{ val }}</span
                >
              </div>
            </div>
          </div>
        </div>

        <!-- RIGHT: Purchase Panel -->
        <div class="lg:col-span-7 flex flex-col gap-6">
          <div
            class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 sm:p-8 flex flex-col gap-6"
          >
            <!-- Category Tag -->
            <div class="flex items-center gap-2">
              <span
                class="px-2.5 py-0.5 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-[10px] font-bold uppercase tracking-wider text-indigo-650 dark:text-indigo-400"
              >
                {{ product.category }}
              </span>
            </div>

            <!-- Title -->
            <h1
              class="text-2xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight -mt-2"
            >
              {{ product.name }}
            </h1>

            <!-- Rating Summary -->
            <div class="flex items-center gap-3 -mt-2">
              <Rating :model-value="averageRating" readonly :cancel="false">
                <template #onicon
                  ><i class="pi pi-star-fill text-sm text-amber-500"
                /></template>
                <template #officon
                  ><i class="pi pi-star text-sm text-slate-700"
                /></template>
              </Rating>
              <span class="text-xs font-bold text-slate-500 dark:text-slate-400">
                {{ averageRating }} &bull; ({{ productReviews.length }} reviews)
              </span>
            </div>

            <!-- Variant Selectors -->
            <div
              v-if="product.variants && product.variants.length > 0"
              class="flex flex-col gap-5 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-2xl p-5"
            >
              <div
                v-for="variant in product.variants"
                :key="variant.name"
                class="flex flex-col gap-2.5"
              >
                <span
                  class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                  >Select {{ variant.name }}</span
                >
                <div class="flex flex-wrap gap-2.5">
                  <button
                    v-for="opt in variant.options"
                    :key="opt"
                    class="px-4 py-2 text-xs font-semibold rounded-xl border transition-all duration-200 focus:outline-none"
                    :class="
                      selectedVariants[variant.name] === opt
                        ? 'bg-indigo-500/20 border-indigo-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(99,102,241,0.3)]'
                        : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border-slate-200 dark:border-white/10 text-slate-600 dark:text-slate-300 hover:border-slate-300 dark:hover:border-white/20'
                    "
                    @click="selectedVariants[variant.name] = opt"
                  >
                    {{ opt }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Price + CTA -->
            <div
              class="bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5 rounded-2xl p-4 sm:p-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4"
            >
              <div class="flex flex-col">
                <span
                  class="text-[10px] uppercase tracking-wider text-slate-500 font-bold"
                  >Standard Price</span
                >
                <span class="text-3xl font-black text-slate-900 dark:text-white mt-1">{{
                  formattedPrice
                }}</span>
              </div>
              <div class="flex flex-col sm:flex-row gap-3 w-full sm:w-auto">
                <Button
                  label="Add to Shopping Cart"
                  icon="pi pi-shopping-cart"
                  :disabled="product.stock === 0"
                  class="w-full sm:w-auto px-6 py-3 bg-slate-800 hover:bg-slate-750 border border-slate-200 dark:border-white/10 rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer"
                  @click="handleAddToCart"
                />
                <Button
                  label="Purchase"
                  icon="pi pi-bolt"
                  :disabled="product.stock === 0"
                  class="w-full sm:w-auto px-8 py-3 bg-linear-to-r from-emerald-500 to-teal-600 border-0 hover:shadow-[0_0_20px_rgba(16,185,129,0.4)] disabled:opacity-50 disabled:pointer-events-none rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer"
                  @click="openDirectPurchase"
                />
              </div>
            </div>

            <!-- Description -->
            <div>
              <h3
                class="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-2"
              >
                Overview
              </h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 leading-relaxed font-medium">
                {{ product.description }}
              </p>
            </div>

            <!-- Features -->
            <div v-if="product.features.length > 0">
              <h3
                class="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-3"
              >
                Key Features
              </h3>
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div
                  v-for="feat in product.features"
                  :key="feat"
                  class="flex items-center gap-3 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/30 border border-slate-200 dark:border-white/5 p-3 rounded-xl text-xs font-semibold text-slate-600 dark:text-slate-300"
                >
                  <i
                    class="pi pi-check text-[10px] text-green-400 bg-green-500/10 p-1.5 rounded-full shrink-0"
                  />
                  <span>{{ feat }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- ─── REVIEWS SECTION ─────────────────────────────────────────── -->
          <div
            class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 sm:p-8 flex flex-col gap-6"
          >
            <!-- Reviews Header -->
            <div
              class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-5 border-b border-slate-200 dark:border-white/5"
            >
              <div>
                <h2
                  class="text-sm font-extrabold uppercase tracking-wider text-slate-900 dark:text-white"
                >
                  Customer Reviews
                </h2>
                <p class="text-[11px] text-slate-500 mt-0.5">
                  {{ productReviews.length }} verified reviews
                </p>
              </div>
              <button
                class="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all focus:outline-none"
                :class="
                  showReviewForm
                    ? 'bg-slate-700/60 text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-white/10 hover:bg-slate-700'
                    : 'bg-linear-to-r from-indigo-500 to-blue-600 text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(99,102,241,0.35)]'
                "
                @click="toggleReviewForm"
              >
                <i :class="showReviewForm ? 'pi pi-times' : 'pi pi-pencil'" />
                {{ showReviewForm ? 'Cancel' : 'Write a Review' }}
              </button>
            </div>

            <!-- Rating Histogram Dashboard -->
            <div
              class="flex flex-col sm:flex-row gap-6 items-start sm:items-center"
            >
              <!-- Big Score -->
              <div
                class="flex flex-col items-center shrink-0 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-2xl px-6 py-5 min-w-[120px]"
              >
                <span class="text-5xl font-black text-slate-900 dark:text-white leading-none">{{
                  averageRating
                }}</span>
                <div class="flex gap-0.5 mt-2">
                  <i
                    v-for="n in 5"
                    :key="n"
                    class="pi text-sm"
                    :class="
                      n <= Math.round(averageRating)
                        ? 'pi-star-fill text-amber-500'
                        : 'pi-star text-slate-700'
                    "
                  />
                </div>
                <span class="text-[10px] text-slate-500 font-semibold mt-1.5"
                  >out of 5</span
                >
              </div>
              <!-- Histogram Bars -->
              <div class="flex flex-col gap-2 flex-1 w-full">
                <div
                  v-for="row in ratingStats"
                  :key="row.stars"
                  class="flex items-center gap-3"
                >
                  <button
                    class="text-[10px] font-bold text-slate-500 dark:text-slate-400 shrink-0 w-12 text-right hover:text-amber-400 transition-colors"
                    :class="
                      ratingFilter === String(row.stars) ? 'text-amber-400' : ''
                    "
                    @click="
                      ratingFilter =
                        ratingFilter === String(row.stars)
                          ? 'all'
                          : (String(row.stars) as '5' | '4' | '3' | '2' | '1')
                    "
                  >
                    {{ row.stars }} ★
                  </button>
                  <div
                    class="flex-1 h-2 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/50 rounded-full overflow-hidden"
                  >
                    <div
                      class="h-full rounded-full bg-gradient-to-r from-amber-500 to-yellow-400 transition-all duration-500"
                      :style="{ width: `${row.percentage}%` }"
                    />
                  </div>
                  <span
                    class="text-[10px] font-semibold text-slate-500 w-9 text-right shrink-0"
                    >{{ row.percentage }}%</span
                  >
                </div>
              </div>
            </div>

            <!-- Write a Review Form (collapsible) -->
            <Transition name="form-slide">
              <div
                v-if="showReviewForm"
                class="flex flex-col gap-4 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-indigo-500/20 rounded-2xl p-5 sm:p-6"
              >
                <h3
                  class="text-xs font-bold uppercase tracking-wider text-indigo-650 dark:text-indigo-400"
                >
                  Write Your Review
                </h3>

                <!-- Star Selector -->
                <div class="flex flex-col gap-2">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >Your Rating</label
                  >
                  <div class="flex gap-1.5">
                    <button
                      v-for="n in 5"
                      :key="n"
                      class="focus:outline-none transition-transform hover:scale-125"
                      @mouseenter="hoverRating = n"
                      @mouseleave="hoverRating = 0"
                      @click="setRating(n)"
                    >
                      <i
                        class="pi text-2xl transition-colors"
                        :class="
                          n <= displayedStarRating
                            ? 'pi-star-fill text-amber-500'
                            : 'pi-star text-slate-600 hover:text-amber-400'
                        "
                      />
                    </button>
                    <span
                      class="ml-2 text-xs font-bold text-slate-500 dark:text-slate-400 self-center"
                    >
                      {{ form.rating }} star{{ form.rating !== 1 ? 's' : '' }}
                    </span>
                  </div>
                </div>

                <!-- Name -->
                <div class="flex flex-col gap-1.5">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >Your Name</label
                  >
                  <InputText
                    v-model="form.author"
                    placeholder="e.g. Alex Martinez"
                    class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                    :class="{ 'border-rose-500/50': errors.author }"
                    @blur="validateForm"
                  />
                  <span
                    v-if="errors.author"
                    class="text-[10px] text-rose-400 font-semibold"
                    >{{ errors.author }}</span
                  >
                </div>

                <!-- Title -->
                <div class="flex flex-col gap-1.5">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >Review Title
                    <span class="text-slate-600 normal-case font-normal"
                      >(optional)</span
                    ></label
                  >
                  <InputText
                    v-model="form.title"
                    placeholder="Summarize your experience"
                    class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                  />
                </div>

                <!-- Comment -->
                <div class="flex flex-col gap-1.5">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >Your Review</label
                  >
                  <Textarea
                    v-model="form.comment"
                    placeholder="Share what you liked, disliked, or found useful..."
                    :rows="4"
                    class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50 resize-none"
                    :class="{ 'border-rose-500/50': errors.comment }"
                    @blur="validateForm"
                  />
                  <div class="flex justify-between items-center">
                    <span
                      v-if="errors.comment"
                      class="text-[10px] text-rose-400 font-semibold"
                      >{{ errors.comment }}</span
                    >
                    <span
                      class="ml-auto text-[10px] text-slate-600 font-semibold"
                      >{{ form.comment.length }} chars</span
                    >
                  </div>
                </div>

                <!-- Submit -->
                <div class="flex justify-end">
                  <Button
                    :label="isSubmitting ? 'Submitting...' : 'Publish Review'"
                    :icon="
                      isSubmitting ? 'pi pi-spin pi-spinner' : 'pi pi-send'
                    "
                    :loading="isSubmitting"
                    class="px-6 py-2.5 bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(99,102,241,0.4)] transition-all disabled:opacity-60 disabled:pointer-events-none"
                    :disabled="isSubmitting"
                    @click="submitReview"
                  />
                </div>
              </div>
            </Transition>

            <!-- Review Controls Toolbar -->
            <div
              class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 border-t border-slate-200 dark:border-white/5 pt-5"
            >
              <!-- Star filter pills -->
              <div class="flex flex-wrap items-center gap-2">
                <span
                  class="text-[10px] font-bold uppercase tracking-wider text-slate-500 mr-1"
                  >Filter:</span
                >
                <button
                  v-for="pill in [
                    { label: 'All', value: 'all' },
                    { label: '5★', value: '5' },
                    { label: '4★', value: '4' },
                    { label: '3★', value: '3' },
                    { label: '2★', value: '2' },
                    { label: '1★', value: '1' }
                  ]"
                  :key="pill.value"
                  class="px-2.5 py-1 rounded-full text-[10px] font-bold border transition-colors focus:outline-none"
                  :class="
                    ratingFilter === pill.value
                      ? 'bg-amber-500/20 border-amber-500/40 text-amber-400'
                      : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border-slate-200 dark:border-white/10 text-slate-500 dark:text-slate-400 hover:border-slate-300 dark:hover:border-white/20 hover:text-slate-600 dark:text-slate-300'
                  "
                  @click="
                    ratingFilter = pill.value as
                      | 'all'
                      | '5'
                      | '4'
                      | '3'
                      | '2'
                      | '1'
                  "
                >
                  {{ pill.label }}
                </button>
              </div>
              <!-- Sort -->
              <div class="flex items-center gap-2">
                <span
                  class="text-[10px] font-bold uppercase tracking-wider text-slate-500"
                  >Sort:</span
                >
                <Select
                  v-model="reviewsSort"
                  :options="sortOptions"
                  option-label="label"
                  option-value="value"
                  class="text-xs bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border-slate-200 dark:border-white/10 rounded-xl min-w-[140px]"
                />
              </div>
            </div>

            <!-- Reviews List -->
            <div class="flex flex-col gap-4">
              <!-- Empty state -->
              <div
                v-if="sortedAndFilteredReviews.length === 0"
                class="flex flex-col items-center justify-center py-10 text-center"
              >
                <div
                  class="w-12 h-12 rounded-2xl bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/50 border border-slate-200 dark:border-white/5 flex items-center justify-center mb-3"
                >
                  <i class="pi pi-comment text-xl text-slate-600" />
                </div>
                <p class="text-sm font-semibold text-slate-500 dark:text-slate-400">
                  {{
                    ratingFilter === 'all'
                      ? 'Be the first to review this product.'
                      : `No ${ratingFilter}-star reviews yet.`
                  }}
                </p>
                <button
                  v-if="ratingFilter !== 'all'"
                  class="mt-3 text-[11px] font-bold text-indigo-650 dark:text-indigo-400 hover:text-indigo-600 dark:hover:text-indigo-300 transition-colors"
                  @click="ratingFilter = 'all'"
                >
                  Show all reviews
                </button>
              </div>

              <!-- Review Cards -->
              <div
                v-for="review in sortedAndFilteredReviews"
                :key="review.id"
                class="flex flex-col gap-3 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/30 border border-slate-200 dark:border-white/5 p-4 sm:p-5 rounded-2xl hover:border-slate-200 dark:border-white/10 transition-colors"
              >
                <!-- Author row -->
                <div class="flex items-center gap-3">
                  <!-- Avatar -->
                  <div
                    class="w-9 h-9 rounded-xl bg-gradient-to-br flex items-center justify-center shrink-0 text-slate-900 dark:text-white text-[11px] font-extrabold"
                    :class="avatarColor(review.author)"
                  >
                    {{ authorInitials(review.author) }}
                  </div>
                  <div class="flex flex-col flex-1 min-w-0">
                    <div class="flex items-center gap-2">
                      <span class="text-xs font-bold text-slate-900 dark:text-white truncate">{{
                        review.author
                      }}</span>
                      <span
                        v-if="review.verified"
                        class="px-1.5 py-px rounded bg-emerald-500/10 border border-emerald-500/20 text-[9px] font-bold text-emerald-400 uppercase tracking-wider shrink-0"
                        >Verified</span
                      >
                    </div>
                    <span class="text-[10px] text-slate-500">{{
                      formatDate(review.date)
                    }}</span>
                  </div>
                  <!-- Rating -->
                  <div class="flex gap-0.5 shrink-0">
                    <i
                      v-for="n in 5"
                      :key="n"
                      class="pi text-[11px]"
                      :class="
                        n <= review.rating
                          ? 'pi-star-fill text-amber-500'
                          : 'pi-star text-slate-700'
                      "
                    />
                  </div>
                </div>
                <!-- Title -->
                <p
                  v-if="review.title"
                  class="text-xs font-bold text-slate-750 dark:text-slate-200 -mb-1"
                >
                  "{{ review.title }}"
                </p>
                <!-- Comment -->
                <p class="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                  {{ review.comment }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Product Not Found -->
    <div
      v-else
      class="flex flex-col items-center justify-center text-center p-12 bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/20 border border-dashed border-slate-200 dark:border-white/10 rounded-3xl min-h-[400px] my-auto"
    >
      <div
        class="w-16 h-16 rounded-2xl bg-rose-500/10 flex items-center justify-center mb-4 border border-rose-500/20"
      >
        <i class="pi pi-info-circle text-2xl text-rose-400" />
      </div>
      <h3 class="text-lg font-bold text-slate-900 dark:text-white mb-2">Product Not Found</h3>
      <p class="text-sm text-slate-500 dark:text-slate-400 max-w-sm mb-6 leading-relaxed">
        The requested product identifier does not exist or has been removed from
        our active catalog.
      </p>
      <Button
        label="Return to Storefront Catalog"
        icon="pi pi-home"
        class="px-5 py-2.5 text-xs font-bold bg-indigo-600 hover:bg-indigo-500 border-0 rounded-xl transition-colors text-slate-900 dark:text-white"
        @click="goBack"
      />
    </div>

    <!-- Visual Direct Purchase Checkout Wizard Overlay Dialog -->
    <Dialog
      v-model:visible="directPurchaseVisible"
      modal
      dismissable-mask
      class="bg-slate-100 dark:bg-slate-950 text-slate-100 border border-slate-200 dark:border-white/10 max-w-lg w-full mx-4 rounded-3xl overflow-hidden shadow-2xl"
      content-class="p-6"
      header-class="p-6 border-b border-slate-200 dark:border-white/5 bg-transparent text-slate-900 dark:text-white font-extrabold text-lg"
      @hide="closeDirectPurchase"
    >
      <template #header>
        <div class="flex items-center gap-2.5">
          <i class="pi pi-lock text-emerald-400" />
          <span>Secure Direct Purchase</span>
        </div>
      </template>

      <div class="flex flex-col gap-6">
        <!-- Step Indicators (Progress wizard) -->
        <div
          class="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-white/5"
        >
          <div
            v-for="step in directPurchaseSteps"
            :key="step.id"
            class="flex items-center gap-2"
          >
            <span
              class="w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold"
              :class="
                purchaseActiveStep === step.id
                  ? 'bg-emerald-500 text-slate-900 dark:text-white font-black ring-4 ring-emerald-500/25'
                  : purchaseActiveStep > step.id
                    ? 'bg-emerald-600 text-slate-900 dark:text-white'
                    : 'bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-white/10 text-slate-500'
              "
            >
              <i
                v-if="purchaseActiveStep > step.id"
                class="pi pi-check text-[8px] font-bold"
              />
              <span v-else>{{ step.displayNum }}</span>
            </span>
            <span
              class="text-[11px] font-bold tracking-wide"
              :class="
                purchaseActiveStep === step.id ? 'text-slate-900 dark:text-white' : 'text-slate-500'
              "
            >
              {{ step.label }}
            </span>
            <i
              v-if="step.id < 4"
              class="pi pi-angle-right text-[10px] text-slate-700 ml-1 hidden sm:inline"
            />
          </div>
        </div>

        <!-- STEP 1: OPTIONS SELECTION (if variants exist) -->
        <div
          v-if="
            purchaseActiveStep === 1 &&
            product?.variants &&
            product.variants.length > 0
          "
          class="flex flex-col gap-4"
        >
          <h3
            class="text-xs font-bold uppercase tracking-wider text-emerald-400"
          >
            Confirm Product Options
          </h3>
          <div
            class="flex flex-col gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-2xl p-4"
          >
            <div
              v-for="variant in product?.variants"
              :key="variant.name"
              class="flex flex-col gap-2"
            >
              <span
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >Select {{ variant.name }}</span
              >
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="opt in variant.options"
                  :key="opt"
                  class="px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all duration-205 focus:outline-none cursor-pointer"
                  :class="
                    purchaseVariants[variant.name] === opt
                      ? 'bg-emerald-500/20 border-emerald-500 text-slate-900 dark:text-white shadow-[0_0_8px_rgba(16,185,129,0.25)]'
                      : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border-slate-200 dark:border-white/10 text-slate-600 dark:text-slate-300 hover:border-slate-300 dark:hover:border-white/20'
                  "
                  @click="purchaseVariants[variant.name] = opt"
                >
                  {{ opt }}
                </button>
              </div>
            </div>
          </div>

          <div
            class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border border-slate-200 dark:border-white/5 rounded-2xl p-4 mt-2"
          >
            <div class="flex flex-col">
              <span
                class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                >Price</span
              >
              <span class="text-lg font-black text-slate-900 dark:text-white mt-0.5">{{
                formatCurrency(directSubtotal)
              }}</span>
            </div>
            <Button
              label="Next: Shipping"
              icon="pi pi-truck"
              icon-pos="right"
              class="px-5 py-2.5 bg-linear-to-r from-emerald-500 to-teal-650 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(16,185,129,0.4)] transition-all cursor-pointer"
              @click="nextDirectStep"
            />
          </div>
        </div>

        <!-- STEP 2: SHIPPING DETAILS -->
        <div v-if="purchaseActiveStep === 2" class="flex flex-col gap-4">
          <h3
            class="text-xs font-bold uppercase tracking-wider text-emerald-400"
          >
            Shipping Information
          </h3>

          <!-- Full Name -->
          <div class="flex flex-col gap-1.5">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Full Name</label
            >
            <InputText
              v-model="directShippingForm.name"
              placeholder="e.g. Liam Thompson"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
              :class="{ 'border-rose-500/50': directShippingErrors.name }"
            />
            <span
              v-if="directShippingErrors.name"
              class="text-[10px] text-rose-400 font-semibold"
            >
              {{ directShippingErrors.name }}
            </span>
          </div>

          <!-- Email -->
          <div class="flex flex-col gap-1.5">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Email Address</label
            >
            <InputText
              v-model="directShippingForm.email"
              placeholder="e.g. liam@example.com"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
              :class="{ 'border-rose-500/50': directShippingErrors.email }"
            />
            <span
              v-if="directShippingErrors.email"
              class="text-[10px] text-rose-400 font-semibold"
            >
              {{ directShippingErrors.email }}
            </span>
          </div>

          <!-- Address -->
          <div class="flex flex-col gap-1.5">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Street Address</label
            >
            <InputText
              v-model="directShippingForm.address"
              placeholder="e.g. 104 Applewood Dr"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
              :class="{ 'border-rose-500/50': directShippingErrors.address }"
            />
            <span
              v-if="directShippingErrors.address"
              class="text-[10px] text-rose-400 font-semibold"
            >
              {{ directShippingErrors.address }}
            </span>
          </div>

          <!-- Grid: City + Postal Code -->
          <div class="grid grid-cols-2 gap-4">
            <div class="flex flex-col gap-1.5">
              <label
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >City</label
              >
              <InputText
                v-model="directShippingForm.city"
                placeholder="e.g. Cupertino"
                class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
                :class="{ 'border-rose-500/50': directShippingErrors.city }"
              />
              <span
                v-if="directShippingErrors.city"
                class="text-[10px] text-rose-400 font-semibold"
              >
                {{ directShippingErrors.city }}
              </span>
            </div>
            <div class="flex flex-col gap-1.5">
              <label
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >Postal Code</label
              >
              <InputText
                v-model="directShippingForm.zip"
                placeholder="e.g. 95014"
                class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
                :class="{ 'border-rose-500/50': directShippingErrors.zip }"
              />
              <span
                v-if="directShippingErrors.zip"
                class="text-[10px] text-rose-400 font-semibold"
              >
                {{ directShippingErrors.zip }}
              </span>
            </div>
          </div>

          <!-- Action buttons block -->
          <div
            class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border border-slate-200 dark:border-white/5 rounded-2xl p-4 mt-2"
          >
            <div class="flex flex-col">
              <span
                class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                >Total Due</span
              >
              <span class="text-lg font-black text-slate-900 dark:text-white mt-0.5">{{
                formatCurrency(directTotal)
              }}</span>
            </div>
            <div class="flex gap-2">
              <Button
                v-if="product?.variants && product.variants.length > 0"
                label="Back"
                icon="pi pi-angle-left"
                class="px-4 py-2.5 bg-slate-800 hover:bg-slate-700 border border-slate-200 dark:border-white/5 rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer"
                @click="prevDirectStep"
              />
              <Button
                label="Next: Payment"
                icon="pi pi-credit-card"
                icon-pos="right"
                class="px-5 py-2.5 bg-linear-to-r from-emerald-500 to-teal-650 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(16,185,129,0.4)] transition-all cursor-pointer"
                @click="nextDirectStep"
              />
            </div>
          </div>
        </div>

        <!-- STEP 3: PAYMENT METHOD -->
        <div v-if="purchaseActiveStep === 3" class="flex flex-col gap-5">
          <div class="flex flex-col gap-2">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Payment Method</label
            >
            <div class="grid grid-cols-2 gap-3">
              <!-- Credit Card -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  directPaymentMethod === 'credit_card'
                    ? 'bg-emerald-500/10 border-emerald-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(16,185,129,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="directPaymentMethod = 'credit_card'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    directPaymentMethod === 'credit_card'
                      ? 'text-emerald-400 border-emerald-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-credit-card text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">Credit Card</span>
                  <span
                    class="text-[8px] text-slate-500 font-semibold truncate font-medium"
                    >Visa, Mastercard</span
                  >
                </div>
              </button>

              <!-- Shopbee Pay -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  directPaymentMethod === 'shopbee_pay'
                    ? 'bg-emerald-500/10 border-emerald-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(16,185,129,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="directPaymentMethod = 'shopbee_pay'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    directPaymentMethod === 'shopbee_pay'
                      ? 'text-emerald-400 border-emerald-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-bolt text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">Shopbee Pay</span>
                  <span
                    class="text-[8px] text-slate-500 font-semibold truncate font-medium"
                    >E-Wallet Balance</span
                  >
                </div>
              </button>

              <!-- Bank Transfer -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  directPaymentMethod === 'bank_transfer'
                    ? 'bg-emerald-500/10 border-emerald-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(16,185,129,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="directPaymentMethod = 'bank_transfer'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    directPaymentMethod === 'bank_transfer'
                      ? 'text-emerald-400 border-emerald-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-building text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">Bank Transfer</span>
                  <span
                    class="text-[8px] text-slate-500 font-semibold truncate font-medium"
                    >Direct Deposit</span
                  >
                </div>
              </button>

              <!-- Cash on Delivery -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  directPaymentMethod === 'cod'
                    ? 'bg-emerald-500/10 border-emerald-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(16,185,129,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="directPaymentMethod = 'cod'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    directPaymentMethod === 'cod'
                      ? 'text-emerald-400 border-emerald-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-wallet text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">COD</span>
                  <span
                    class="text-[8px] text-slate-500 font-semibold truncate font-medium"
                    >Pay on Delivery</span
                  >
                </div>
              </button>
            </div>
          </div>

          <!-- CREDIT CARD FORM -->
          <div
            v-if="directPaymentMethod === 'credit_card'"
            class="flex flex-col gap-4"
          >
            <!-- Glass Card Visual Preview -->
            <div
              class="w-full aspect-video rounded-3xl p-6 shadow-2xl relative overflow-hidden flex flex-col justify-between text-slate-900 dark:text-white font-mono tracking-widest bg-linear-to-br from-emerald-600/70 via-emerald-750/70 to-teal-750/70 backdrop-blur-md border border-slate-200 dark:border-white/15 select-none"
            >
              <div
                class="absolute inset-0 z-0 overflow-hidden pointer-events-none opacity-40"
              >
                <div
                  class="absolute rounded-full blur-2xl top-[-20%] left-[-20%] w-[120px] h-[120px] bg-pink-500"
                />
                <div
                  class="absolute rounded-full blur-2xl bottom-[-20%] right-[-10%] w-[150px] h-[150px] bg-cyan-400"
                />
              </div>
              <div class="relative z-10 flex justify-between items-center">
                <div class="flex items-center gap-1.5">
                  <i
                    class="pi pi-bolt text-emerald-400 text-lg drop-shadow-[0_0_6px_rgba(52,211,153,0.7)]"
                  />
                  <span
                    class="text-xs font-black tracking-normal uppercase text-slate-100"
                    >Shopbee Direct</span
                  >
                </div>
                <i class="pi pi-wifi text-slate-600 dark:text-slate-300 text-sm rotate-90" />
              </div>
              <div
                class="relative z-10 w-11 h-8 rounded-lg bg-linear-to-r from-yellow-300 via-amber-400 to-yellow-600 border border-amber-300/30 opacity-80"
              />
              <div
                class="relative z-10 text-base sm:text-lg text-slate-100 font-bold text-center tracking-widest my-2"
              >
                {{ directPaymentForm.cardNumber || '•••• •••• •••• ••••' }}
              </div>
              <div class="relative z-10 flex justify-between items-end">
                <div class="flex flex-col min-w-0">
                  <span
                    class="text-[8px] text-emerald-200 font-bold uppercase tracking-wide"
                    >Card Holder</span
                  >
                  <span
                    class="text-xs font-black text-slate-100 truncate tracking-wide mt-0.5"
                  >
                    {{
                      directPaymentForm.cardName.toUpperCase() || 'YOUR NAME'
                    }}
                  </span>
                </div>
                <div class="flex flex-col shrink-0 text-right">
                  <span
                    class="text-[8px] text-emerald-200 font-bold uppercase tracking-wide"
                    >Expires</span
                  >
                  <span class="text-xs font-black text-slate-100 mt-0.5">{{
                    directPaymentForm.expiry || 'MM/YY'
                  }}</span>
                </div>
              </div>
            </div>

            <!-- Fields -->
            <div class="flex flex-col gap-3.5">
              <div class="flex flex-col gap-1">
                <label
                  class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                  >Cardholder Name</label
                >
                <InputText
                  v-model="directPaymentForm.cardName"
                  placeholder="e.g. Liam Thompson"
                  class="w-full text-xs bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
                  :class="{
                    'border-rose-500/50': directPaymentErrors.cardName
                  }"
                />
                <span
                  v-if="directPaymentErrors.cardName"
                  class="text-[10px] text-rose-400 font-semibold"
                  >{{ directPaymentErrors.cardName }}</span
                >
              </div>

              <div class="flex flex-col gap-1">
                <label
                  class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                  >Card Number</label
                >
                <InputText
                  v-model="directPaymentForm.cardNumber"
                  placeholder="1234 5678 1234 5678"
                  class="w-full text-xs bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
                  :class="{
                    'border-rose-500/50': directPaymentErrors.cardNumber
                  }"
                  @input="formatDirectCardNumberInput"
                />
                <span
                  v-if="directPaymentErrors.cardNumber"
                  class="text-[10px] text-rose-400 font-semibold"
                  >{{ directPaymentErrors.cardNumber }}</span
                >
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="flex flex-col gap-1">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >Expiration Date</label
                  >
                  <InputText
                    v-model="directPaymentForm.expiry"
                    placeholder="MM/YY"
                    class="w-full text-xs bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
                    :class="{
                      'border-rose-500/50': directPaymentErrors.expiry
                    }"
                    @input="formatDirectExpiryInput"
                  />
                  <span
                    v-if="directPaymentErrors.expiry"
                    class="text-[10px] text-rose-400 font-semibold"
                    >{{ directPaymentErrors.expiry }}</span
                  >
                </div>
                <div class="flex flex-col gap-1">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >CVC Code</label
                  >
                  <InputText
                    v-model="directPaymentForm.cvc"
                    placeholder="123"
                    type="password"
                    class="w-full text-xs bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-emerald-500/50"
                    :class="{ 'border-rose-500/50': directPaymentErrors.cvc }"
                    @input="formatDirectCvcInput"
                  />
                  <span
                    v-if="directPaymentErrors.cvc"
                    class="text-[10px] text-rose-400 font-semibold"
                    >{{ directPaymentErrors.cvc }}</span
                  >
                </div>
              </div>
            </div>
          </div>

          <!-- SHOPBEE PAY -->
          <div
            v-else-if="directPaymentMethod === 'shopbee_pay'"
            class="flex flex-col gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-3xl p-5 relative overflow-hidden"
          >
            <div
              class="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(16,185,129,0.08),transparent_50%)] pointer-events-none"
            ></div>
            <div
              class="flex items-center justify-between pb-3 border-b border-slate-200 dark:border-white/5"
            >
              <div class="flex items-center gap-2">
                <i class="pi pi-bolt text-emerald-400 text-lg" />
                <span class="text-xs font-black uppercase text-slate-750 dark:text-slate-200"
                  >Shopbee Pay Wallet</span
                >
              </div>
              <span
                class="px-2.5 py-0.5 rounded-full text-[9px] font-extrabold uppercase tracking-wider border text-emerald-400 bg-emerald-500/10 border-emerald-500/20"
              >
                Sufficient Balance
              </span>
            </div>
            <div
              class="flex flex-col py-3 text-center justify-center items-center gap-1.5"
            >
              <span
                class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                >Available Balance</span
              >
              <span class="text-2xl font-black text-slate-900 dark:text-white font-mono"
                >$1,200.00</span
              >
              <span class="text-[10px] text-slate-500 dark:text-slate-400 font-medium"
                >Order Total: {{ formatCurrency(directTotal) }}</span
              >
            </div>
            <div
              class="bg-emerald-500/5 border border-emerald-500/15 rounded-2xl p-3 flex items-start gap-2.5"
            >
              <i
                class="pi pi-info-circle text-emerald-400 text-xs shrink-0 mt-0.5"
              />
              <div class="flex flex-col">
                <span
                  class="text-[10px] font-bold text-emerald-300 uppercase tracking-wide"
                  >Fast, Secure & Automated</span
                >
                <span
                  class="text-[9px] text-slate-500 dark:text-slate-400 font-medium leading-normal mt-0.5"
                  >Funds will be deducted immediately from your Shopbee Pay
                  balance once confirmed.</span
                >
              </div>
            </div>
          </div>

          <!-- BANK TRANSFER -->
          <div
            v-else-if="directPaymentMethod === 'bank_transfer'"
            class="flex flex-col gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-3xl p-5"
          >
            <div class="flex flex-col py-2 border-b border-slate-200 dark:border-white/5 gap-0.5">
              <span
                class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                >Send exact payment amount to:</span
              >
              <span class="text-base font-extrabold text-slate-900 dark:text-white"
                >Shopbee Global Bank Ltd.</span
              >
            </div>
            <div class="grid grid-cols-2 gap-4 py-2">
              <div
                class="flex flex-col bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-xl p-3"
              >
                <span
                  class="text-[9px] text-slate-500 font-bold uppercase tracking-wider"
                  >Account Number</span
                >
                <div class="flex items-center justify-between mt-1">
                  <span class="text-xs font-bold text-slate-750 dark:text-slate-200 font-mono"
                    >1029-4820-1928</span
                  >
                </div>
              </div>
              <div
                class="flex flex-col bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-xl p-3"
              >
                <span
                  class="text-[9px] text-slate-500 font-bold uppercase tracking-wider"
                  >Transfer Amount</span
                >
                <span
                  class="text-xs font-black text-emerald-400 mt-1 font-mono"
                  >{{ formatCurrency(directTotal) }}</span
                >
              </div>
            </div>
            <div
              class="bg-indigo-500/5 border border-indigo-500/15 rounded-2xl p-3 flex items-start gap-2.5"
            >
              <i
                class="pi pi-info-circle text-indigo-650 dark:text-indigo-400 text-xs shrink-0 mt-0.5"
              />
              <span
                class="text-[9px] text-slate-500 dark:text-slate-400 font-medium leading-relaxed"
                >Please state the generated Order Reference in your bank
                transfer description fields. Orders are processed after deposit
                clearance.</span
              >
            </div>
          </div>

          <!-- CASH ON DELIVERY -->
          <div
            v-else-if="directPaymentMethod === 'cod'"
            class="flex flex-col gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-3xl p-5"
          >
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-2xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center shrink-0"
              >
                <i class="pi pi-wallet text-amber-400 text-sm" />
              </div>
              <div class="flex flex-col">
                <span class="text-xs font-extrabold text-slate-900 dark:text-white"
                  >Cash on Delivery Confirmation</span
                >
                <span class="text-[9px] text-slate-500 font-semibold mt-0.5"
                  >Pay with cash upon package courier arrival.</span
                >
              </div>
            </div>
            <div
              class="bg-amber-500/5 border border-amber-500/15 rounded-2xl p-3 flex items-start gap-2.5"
            >
              <i
                class="pi pi-exclamation-triangle text-amber-400 text-xs shrink-0 mt-0.5"
              />
              <span
                class="text-[9px] text-slate-500 dark:text-slate-400 font-medium leading-relaxed font-medium"
                >Ensure a representative is available at the shipping address to
                inspect and complete the cash hand-over upon delivery
                arrival.</span
              >
            </div>
          </div>

          <!-- Footer Summary -->
          <div
            class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border border-slate-200 dark:border-white/5 rounded-2xl p-4 mt-2"
          >
            <div class="flex flex-col">
              <span
                class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                >Total Due</span
              >
              <span class="text-lg font-black text-slate-900 dark:text-white mt-0.5">{{
                formatCurrency(directTotal)
              }}</span>
            </div>
            <div class="flex gap-2">
              <Button
                label="Back"
                icon="pi pi-angle-left"
                class="px-4 py-2.5 bg-slate-800 hover:bg-slate-700 border border-slate-200 dark:border-white/5 rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer"
                @click="prevDirectStep"
              />
              <Button
                :disabled="isProcessingDirectPayment"
                :label="
                  isProcessingDirectPayment
                    ? 'Processing...'
                    : `Pay ${formatCurrency(directTotal)}`
                "
                :icon="
                  isProcessingDirectPayment
                    ? 'pi pi-spin pi-spinner'
                    : 'pi pi-check-circle'
                "
                class="px-5 py-2.5 bg-linear-to-r from-emerald-500 to-teal-650 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(16,185,129,0.4)] transition-all cursor-pointer"
                @click="submitDirectPayment"
              />
            </div>
          </div>
        </div>

        <!-- STEP 4: SUCCESS CONFIRMATION -->
        <div
          v-if="purchaseActiveStep === 4"
          class="flex flex-col items-center text-center py-6 gap-5"
        >
          <div
            class="w-16 h-16 rounded-full bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shadow-[0_0_25px_rgba(16,185,129,0.2)]"
          >
            <i class="pi pi-check-circle text-3xl" />
          </div>

          <div class="flex flex-col gap-1">
            <h2 class="text-xl font-extrabold text-slate-900 dark:text-white">
              Purchase Complete!
            </h2>
            <p class="text-xs text-slate-500 dark:text-slate-400 leading-normal max-w-xs mx-auto">
              Your order has been placed successfully and payment was approved.
            </p>
          </div>

          <!-- Order details box -->
          <div
            class="w-full flex flex-col divide-y divide-slate-100 dark:divide-white/5 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-2xl overflow-hidden p-1.5 text-left"
          >
            <div class="grid grid-cols-3 gap-2 px-3 py-2 text-[10px]">
              <span class="text-slate-500 font-bold uppercase tracking-wider"
                >Order No.</span
              >
              <span
                class="text-slate-750 dark:text-slate-200 font-bold font-mono col-span-2 text-right"
                >{{ directOrderNumber }}</span
              >
            </div>
            <div class="grid grid-cols-3 gap-2 px-3 py-2 text-[10px]">
              <span class="text-slate-500 font-bold uppercase tracking-wider"
                >Product</span
              >
              <span
                class="text-slate-750 dark:text-slate-200 font-bold col-span-2 text-right truncate"
                >{{ product?.name }}</span
              >
            </div>
            <div
              v-if="Object.keys(purchaseVariants).length > 0"
              class="grid grid-cols-3 gap-2 px-3 py-2 text-[10px]"
            >
              <span class="text-slate-500 font-bold uppercase tracking-wider"
                >Variants</span
              >
              <span
                class="text-slate-750 dark:text-slate-200 font-bold col-span-2 text-right truncate"
              >
                {{
                  Object.entries(purchaseVariants)
                    .map(([k, v]) => `${k}: ${v}`)
                    .join(', ')
                }}
              </span>
            </div>
            <div class="grid grid-cols-3 gap-2 px-3 py-2 text-[10px]">
              <span class="text-slate-500 font-bold uppercase tracking-wider"
                >Amount Paid</span
              >
              <span
                class="text-emerald-400 font-black col-span-2 text-right font-mono"
                >{{ formatCurrency(directOrderTotalPaid) }}</span
              >
            </div>
            <div class="grid grid-cols-3 gap-2 px-3 py-2 text-[10px]">
              <span class="text-slate-500 font-bold uppercase tracking-wider"
                >Ship To</span
              >
              <span
                class="text-slate-600 dark:text-slate-300 font-semibold col-span-2 text-right truncate"
              >
                {{ directShippingForm.name }} ({{ directShippingForm.city }})
              </span>
            </div>
          </div>

          <div class="flex gap-3 w-full mt-2">
            <Button
              label="Track Order"
              icon="pi pi-compass"
              class="flex-1 py-3 bg-linear-to-r from-emerald-500 to-teal-650 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(16,185,129,0.35)] transition-all cursor-pointer"
              @click="router.push(`/orders?number=${directOrderNumber}`)"
            />
            <Button
              label="Close"
              class="px-5 py-3 bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-white/10 rounded-xl font-bold text-xs text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-all cursor-pointer"
              @click="closeDirectPurchase"
            />
          </div>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
/* Gallery fade transition */
.gallery-fade-enter-active,
.gallery-fade-leave-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}
.gallery-fade-enter-from {
  opacity: 0;
  transform: scale(1.03);
}
.gallery-fade-leave-to {
  opacity: 0;
  transform: scale(0.97);
}

/* Review form slide transition */
.form-slide-enter-active,
.form-slide-leave-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease,
    max-height 0.35s ease;
  max-height: 800px;
  overflow: hidden;
}
.form-slide-enter-from,
.form-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  max-height: 0;
}
</style>
