<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useProducts } from '~/composables/useProducts'
import type { CartItem, Voucher } from '~/composables/useProducts'
import { useOrders } from '~/composables/useOrders'
import { useUserProfile } from '~/composables/useUserProfile'

const toast = useToast()
const router = useRouter()

const {
  userProfile,
  userAddresses,
  userPaymentMethods,
  userBalance,
  deductBalance
} = useUserProfile()

const {
  cart,
  removeFromCart,
  updateCartItemQuantity,
  updateCartItemVariant,
  toggleCartItemSelection,
  selectAllCartItems,
  checkoutItems,
  cartSubtotal,
  cartTaxes,
  cartShipping,
  cartTotal,
  vouchersList,
  appliedVoucher,
  cartDiscount,
  applyVoucher,
  removeVoucher
} = useProducts()

const { addOrder } = useOrders()

// --- Voucher Promo Code State ---
const promoCodeInput = ref('')
const voucherModalVisible = ref(false)

// Saved invoice state post-payment success
const orderDiscount = ref(0)
const orderVoucherCode = ref('')
const orderTotalPaid = ref(0)

const handleApplyPromo = () => {
  const code = promoCodeInput.value.trim()
  if (!code) return
  try {
    applyVoucher(code)
    toast.add({
      severity: 'success',
      summary: 'Promo Applied',
      detail: `Promo code "${appliedVoucher.value?.code}" applied successfully!`,
      life: 3000
    })
    promoCodeInput.value = ''
  } catch (err) {
    const error = err as Error
    toast.add({
      severity: 'error',
      summary: 'Promo Error',
      detail: error.message || 'Could not apply promo code.',
      life: 4000
    })
  }
}

const handleRemovePromo = () => {
  if (appliedVoucher.value) {
    const code = appliedVoucher.value.code
    removeVoucher()
    toast.add({
      severity: 'info',
      summary: 'Promo Removed',
      detail: `Promo code "${code}" has been removed from order.`,
      life: 3000
    })
  }
}

const selectVoucherFromModal = (voucher: Voucher) => {
  try {
    applyVoucher(voucher.code)
    toast.add({
      severity: 'success',
      summary: 'Voucher Applied',
      detail: `Voucher "${voucher.code}" applied to order successfully!`,
      life: 3000
    })
    voucherModalVisible.value = false
  } catch (err) {
    const error = err as Error
    toast.add({
      severity: 'error',
      summary: 'Application Error',
      detail: error.message || 'Could not apply voucher.',
      life: 4000
    })
  }
}

const handleVisitVoucherHub = () => {
  voucherModalVisible.value = false
  router.push('/vouchers')
}

const collectedVouchers = computed(() => {
  return vouchersList.value.filter((v) => v.collected)
})

const eligibleVouchers = computed(() => {
  return collectedVouchers.value.filter((v) => {
    if (cartSubtotal.value < v.minSubtotal) return false
    if (v.type === 'merchant' && v.merchantBrand) {
      return checkoutItems.value.some(
        (item) => item.product.brand === v.merchantBrand
      )
    }
    return true
  })
})

const ineligibleVouchers = computed(() => {
  return collectedVouchers.value.filter((v) => {
    if (cartSubtotal.value < v.minSubtotal) return true
    if (v.type === 'merchant' && v.merchantBrand) {
      return !checkoutItems.value.some(
        (item) => item.product.brand === v.merchantBrand
      )
    }
    return false
  })
})

const getIneligibleReason = (v: Voucher) => {
  if (cartSubtotal.value < v.minSubtotal) {
    return `Min spend of ${formatCurrency(v.minSubtotal)} required.`
  }
  if (v.type === 'merchant' && v.merchantBrand) {
    return `Requires a ${v.merchantBrand} brand item in cart.`
  }
  return 'Cart requirements not met.'
}

// --- Checkout Modal Wizard ---
const checkoutVisible = ref(false)
const activeStep = ref(1) // 1: Shipping, 2: Payment, 3: Success
const isProcessingPayment = ref(false)
const orderNumber = ref('')

// Step 1: Shipping Address Form
const shippingForm = ref({
  name: '',
  email: '',
  address: '',
  city: '',
  zip: ''
})
const shippingErrors = ref({
  name: '',
  email: '',
  address: '',
  city: '',
  zip: ''
})

// Step 2: Payment Form
const paymentForm = ref({
  cardNumber: '',
  cardName: '',
  expiry: '',
  cvc: ''
})
const paymentErrors = ref({
  cardNumber: '',
  cardName: '',
  expiry: '',
  cvc: ''
})

const selectedPaymentMethod = ref<
  'credit_card' | 'shopbee_pay' | 'bank_transfer' | 'cod'
>('credit_card')

const selectedAddressId = ref('')
const selectedCardId = ref('')

const selectAddress = (addrId: string) => {
  selectedAddressId.value = addrId
  handleAddressChange(addrId)
}

const selectCard = (cardId: string) => {
  selectedCardId.value = cardId
  handleCardChange(cardId)
}

const handleAddressChange = (addressId: string) => {
  const addr = userAddresses.value.find((a) => a.id === addressId)
  if (addr) {
    shippingForm.value.name = addr.name
    shippingForm.value.email = userProfile.value.email
    shippingForm.value.address = addr.address
    shippingForm.value.city = addr.city
    shippingForm.value.zip = addr.zip
  }
}

const handleCardChange = (cardId: string) => {
  const pm = userPaymentMethods.value.find((p) => p.id === cardId)
  if (pm && pm.type === 'credit_card' && pm.cardDetails) {
    paymentForm.value.cardName = pm.cardDetails.cardName
    paymentForm.value.cardNumber = pm.cardDetails.cardNumber
    paymentForm.value.expiry = pm.cardDetails.expiry
    paymentForm.value.cvc = pm.cardDetails.cvc
  }
}

const isPayButtonDisabled = computed(() => {
  if (isProcessingPayment.value) return true
  if (
    selectedPaymentMethod.value === 'shopbee_pay' &&
    cartTotal.value > userBalance.value
  ) {
    return true
  }
  return false
})

const processingLabel = computed(() => {
  switch (selectedPaymentMethod.value) {
    case 'credit_card':
      return 'Authorizing Card...'
    case 'shopbee_pay':
      return 'Deducting Balance...'
    case 'bank_transfer':
      return 'Generating Reference...'
    case 'cod':
      return 'Confirming Order...'
    default:
      return 'Processing...'
  }
})

const payButtonLabel = computed(() => {
  if (isProcessingPayment.value) return processingLabel.value
  switch (selectedPaymentMethod.value) {
    case 'credit_card':
      return `Pay ${formatCurrency(cartTotal.value)}`
    case 'shopbee_pay':
      if (cartTotal.value > userBalance.value) {
        return 'Insufficient Shopbee Pay Balance'
      }
      return `Pay with Shopbee Pay`
    case 'bank_transfer':
      return 'Confirm Bank Transfer'
    case 'cod':
      return 'Place Order (COD)'
    default:
      return `Pay ${formatCurrency(cartTotal.value)}`
  }
})

// --- Computed Properties ---
const isAllSelected = computed({
  get: () =>
    cart.value.length > 0 && cart.value.every((i) => i.selectedForCheckout),
  set: (val: boolean) => selectAllCartItems(val)
})

const itemsSelectedCount = computed(() => {
  return cart.value.filter((i) => i.selectedForCheckout).length
})

// Progress for free shipping bar
const shippingProgress = computed(() => {
  return Math.min(100, (cartSubtotal.value / 200) * 100)
})

const shippingRemaining = computed(() => {
  return Math.max(0, 200 - cartSubtotal.value)
})

// --- Form Formatting Handlers ---
const formatCardNumberInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  let value = target.value.replace(/\D/g, '')
  if (value.length > 16) {
    value = value.substring(0, 16)
  }
  const parts = []
  for (let i = 0; i < value.length; i += 4) {
    parts.push(value.substring(i, i + 4))
  }
  paymentForm.value.cardNumber = parts.join(' ')
}

const formatExpiryInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  let value = target.value.replace(/\D/g, '')
  if (value.length > 4) {
    value = value.substring(0, 4)
  }
  if (value.length > 2) {
    paymentForm.value.expiry = `${value.slice(0, 2)}/${value.slice(2)}`
  } else {
    paymentForm.value.expiry = value
  }
}

const formatCvcInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  let value = target.value.replace(/\D/g, '')
  if (value.length > 3) {
    value = value.substring(0, 3)
  }
  paymentForm.value.cvc = value
}

// --- Cart Adjustments ---
const incrementQty = (item: CartItem) => {
  const success = updateCartItemQuantity(item.id, item.quantity + 1)
  if (!success) {
    toast.add({
      severity: 'warn',
      summary: 'Stock Limit Reached',
      detail: `Cannot add more. We only have ${item.product.stock} items of ${item.product.name} in stock.`,
      life: 3000
    })
  }
}

const decrementQty = (item: CartItem) => {
  if (item.quantity > 1) {
    updateCartItemQuantity(item.id, item.quantity - 1)
  }
}

const handleRemove = (item: CartItem) => {
  removeFromCart(item.id)
  toast.add({
    severity: 'info',
    summary: 'Removed from Cart',
    detail: `${item.product.name} has been removed.`,
    life: 3000
  })
}

const handleVariantChange = (
  item: CartItem,
  variantName: string,
  newValue: string
) => {
  const newVariants = { ...item.selectedVariants, [variantName]: newValue }
  updateCartItemVariant(item.id, newVariants)
}

// --- Checkout Wizards Logic ---
const openCheckout = () => {
  if (checkoutItems.value.length === 0) return
  activeStep.value = 1
  checkoutVisible.value = true

  // Auto-fill from active profile default shipping address
  const defaultAddr =
    userAddresses.value.find((a) => a.isDefault) || userAddresses.value[0]
  if (defaultAddr) {
    selectedAddressId.value = defaultAddr.id
    shippingForm.value.name = defaultAddr.name
    shippingForm.value.email = userProfile.value.email
    shippingForm.value.address = defaultAddr.address
    shippingForm.value.city = defaultAddr.city
    shippingForm.value.zip = defaultAddr.zip
  } else {
    selectedAddressId.value = ''
    shippingForm.value = {
      name: '',
      email: userProfile.value.email || '',
      address: '',
      city: '',
      zip: ''
    }
  }

  // Auto-fill preferred payment method
  const defaultPay =
    userPaymentMethods.value.find((p) => p.isDefault) ||
    userPaymentMethods.value[0]
  if (defaultPay) {
    selectedPaymentMethod.value = defaultPay.type
    if (defaultPay.type === 'credit_card' && defaultPay.cardDetails) {
      selectedCardId.value = defaultPay.id
      paymentForm.value.cardName = defaultPay.cardDetails.cardName
      paymentForm.value.cardNumber = defaultPay.cardDetails.cardNumber
      paymentForm.value.expiry = defaultPay.cardDetails.expiry
      paymentForm.value.cvc = defaultPay.cardDetails.cvc
    } else {
      selectedCardId.value = ''
      paymentForm.value = { cardNumber: '', cardName: '', expiry: '', cvc: '' }
    }
  } else {
    selectedPaymentMethod.value = 'credit_card'
    selectedCardId.value = ''
    paymentForm.value = { cardNumber: '', cardName: '', expiry: '', cvc: '' }
  }
}

const validateShipping = () => {
  let valid = true
  shippingErrors.value = { name: '', email: '', address: '', city: '', zip: '' }

  if (!shippingForm.value.name.trim()) {
    shippingErrors.value.name = 'Full name is required.'
    valid = false
  } else if (shippingForm.value.name.trim().length < 2) {
    shippingErrors.value.name = 'Name must be at least 2 characters.'
    valid = false
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!shippingForm.value.email.trim()) {
    shippingErrors.value.email = 'Email address is required.'
    valid = false
  } else if (!emailRegex.test(shippingForm.value.email.trim())) {
    shippingErrors.value.email = 'Please enter a valid email address.'
    valid = false
  }

  if (!shippingForm.value.address.trim()) {
    shippingErrors.value.address = 'Shipping address is required.'
    valid = false
  }

  if (!shippingForm.value.city.trim()) {
    shippingErrors.value.city = 'City is required.'
    valid = false
  }

  if (!shippingForm.value.zip.trim()) {
    shippingErrors.value.zip = 'Postal code is required.'
    valid = false
  }

  return valid
}

const validatePayment = () => {
  if (selectedPaymentMethod.value !== 'credit_card') {
    return true
  }
  let valid = true
  paymentErrors.value = { cardNumber: '', cardName: '', expiry: '', cvc: '' }

  const rawCard = paymentForm.value.cardNumber.replace(/\s+/g, '')
  if (rawCard.length < 16) {
    paymentErrors.value.cardNumber =
      'Please enter a valid 16-digit card number.'
    valid = false
  }

  if (!paymentForm.value.cardName.trim()) {
    paymentErrors.value.cardName = 'Cardholder name is required.'
    valid = false
  }

  if (
    !paymentForm.value.expiry ||
    !/^\d{2}\/\d{2}$/.test(paymentForm.value.expiry)
  ) {
    paymentErrors.value.expiry = 'Expiry must be in MM/YY format.'
    valid = false
  } else {
    const [monthStr, yearStr] = paymentForm.value.expiry.split('/')
    if (monthStr && yearStr) {
      const month = parseInt(monthStr, 10)
      if (month < 1 || month > 12) {
        paymentErrors.value.expiry =
          'Expiration month must be between 01 and 12.'
        valid = false
      }
    }
  }

  if (paymentForm.value.cvc.length < 3) {
    paymentErrors.value.cvc = 'CVC must be 3 digits.'
    valid = false
  }

  return valid
}

const nextStep = () => {
  if (activeStep.value === 1 && validateShipping()) {
    activeStep.value = 2
  }
}

const prevStep = () => {
  if (activeStep.value === 2) {
    activeStep.value = 1
  }
}

const submitPayment = async () => {
  if (!validatePayment()) return
  isProcessingPayment.value = true

  // Simulate network transaction latency
  await new Promise((resolve) => setTimeout(resolve, 2200))

  isProcessingPayment.value = false

  // Deduct dynamic e-wallet balance if Shopbee Pay was used
  if (selectedPaymentMethod.value === 'shopbee_pay') {
    deductBalance(cartTotal.value)
  }

  orderNumber.value = `SB-2026-${Math.floor(100000 + Math.random() * 900000)}`
  activeStep.value = 3

  // Save current invoice parameters for Success page before clearing cart
  orderDiscount.value = cartDiscount.value
  orderVoucherCode.value = appliedVoucher.value?.code || ''
  orderTotalPaid.value = cartTotal.value

  // Clear checkout items from shared store cart
  const checkedOutIds = checkoutItems.value.map((i) => i.id)

  // Build and save order to composable database
  const orderItems = checkoutItems.value.map((item) => ({
    productId: item.product.id,
    name: item.product.name,
    price: item.product.price,
    quantity: item.quantity,
    selectedVariants: { ...item.selectedVariants },
    imageUrl: item.product.imageUrl
  }))

  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10)
  const timeStr = `${dateStr} ${now.toTimeString().slice(0, 5)}`

  addOrder({
    orderNumber: orderNumber.value,
    datePlaced: dateStr,
    status: 'placed',
    shippingAddress: {
      name: shippingForm.value.name,
      email: shippingForm.value.email,
      address: shippingForm.value.address,
      city: shippingForm.value.city,
      zip: shippingForm.value.zip
    },
    items: orderItems,
    subtotal: cartSubtotal.value,
    shippingFee: cartShipping.value,
    tax: cartTaxes.value,
    discount: cartDiscount.value,
    totalPaid: cartTotal.value,
    paymentMethod: selectedPaymentMethod.value,
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

  checkedOutIds.forEach((id) => {
    removeFromCart(id)
  })

  // Clear voucher state
  removeVoucher()

  toast.add({
    severity: 'success',
    summary: 'Payment Successful',
    detail: 'Your payment was approved and order has been created.',
    life: 5000
  })
}

const handleTrackOrder = () => {
  const orderNum = orderNumber.value
  closeCheckout()
  router.push(`/orders?number=${orderNum}`)
}

const handleCopyAccount = () => {
  navigator.clipboard.writeText('1029-4820-1928')
  toast.add({
    severity: 'success',
    summary: 'Copied to Clipboard',
    detail: 'Bank account number copied successfully.',
    life: 2500
  })
}

const closeCheckout = () => {
  checkoutVisible.value = false
  if (activeStep.value === 3) {
    activeStep.value = 1
    shippingForm.value = { name: '', email: '', address: '', city: '', zip: '' }
    paymentForm.value = { cardNumber: '', cardName: '', expiry: '', cvc: '' }
    selectedPaymentMethod.value = 'credit_card'
    orderDiscount.value = 0
    orderVoucherCode.value = ''
    orderTotalPaid.value = 0
  }
}

// --- Helpers ---
const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(val)
}

const goBack = () => router.push('/')
</script>

<template>
  <div class="flex-1 flex flex-col gap-6 animate-slide-up">
    <!-- Back Button -->
    <div>
      <button
        class="group flex items-center gap-2.5 text-xs font-bold text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors focus:outline-none cursor-pointer"
        @click="goBack"
      >
        <i
          class="pi pi-arrow-left group-hover:-translate-x-1 transition-transform"
        />
        Continue Shopping
      </button>
    </div>

    <!-- Page Title -->
    <div>
      <h1 class="text-2xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
        Your Shopping Cart
      </h1>
      <p class="text-xs text-slate-500 mt-1">
        Manage items, configure options, and review your purchase.
      </p>
    </div>

    <!-- Empty State -->
    <div
      v-if="cart.length === 0"
      class="flex flex-col items-center justify-center text-center p-12 bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/20 border border-dashed border-slate-200 dark:border-white/10 rounded-3xl min-h-[400px] mt-2 shadow-2xl"
    >
      <div
        class="w-20 h-20 rounded-2xl bg-indigo-500/10 flex items-center justify-center mb-5 border border-indigo-500/20 animate-pulse"
      >
        <i class="pi pi-shopping-bag text-3xl text-indigo-650 dark:text-indigo-400" />
      </div>
      <h3 class="text-xl font-extrabold text-slate-900 dark:text-white mb-2">
        Your shopping cart is empty
      </h3>
      <p
        class="text-sm text-slate-500 dark:text-slate-400 max-w-sm mb-8 leading-relaxed font-medium"
      >
        Looks like you haven't added anything to your cart yet. Head back to our
        catalog to discover premium devices.
      </p>
      <Button
        label="Explore Catalog"
        icon="pi pi-arrow-right"
        icon-pos="right"
        class="px-5 py-3 font-bold bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl hover:shadow-[0_0_20px_rgba(99,102,241,0.4)] text-slate-900 dark:text-white transition-all cursor-pointer"
        @click="goBack"
      />
    </div>

    <!-- Content Workspace -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start mt-2">
      <!-- Left Column: Items List -->
      <div class="lg:col-span-8 flex flex-col gap-4">
        <!-- Select All Header -->
        <div
          class="flex items-center justify-between bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-2xl px-5 py-3.5 backdrop-blur-md"
        >
          <div class="flex items-center gap-3">
            <Checkbox
              v-model="isAllSelected"
              binary
              input-id="select-all"
              class="w-5 h-5 rounded-md border border-slate-200 dark:border-white/10 checked:bg-indigo-500 checked:border-indigo-500 focus:outline-none"
            />
            <label
              for="select-all"
              class="text-xs font-bold text-slate-600 dark:text-slate-300 select-none cursor-pointer"
            >
              Select All ({{ cart.length }} items)
            </label>
          </div>
          <span
            v-if="itemsSelectedCount > 0"
            class="text-[10px] font-extrabold uppercase tracking-wider text-indigo-650 dark:text-indigo-400 bg-indigo-500/10 border border-indigo-500/25 px-2.5 py-0.5 rounded-full"
          >
            {{ itemsSelectedCount }} Selected for Checkout
          </span>
        </div>

        <!-- Cart Items List -->
        <div class="flex flex-col gap-4">
          <div
            v-for="item in cart"
            :key="item.id"
            class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/30 hover:bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/50 hover:border-indigo-500/10 border border-slate-200 dark:border-white/5 p-4 sm:p-5 rounded-3xl backdrop-blur-md transition-all duration-300"
          >
            <!-- Checkbox + Info Column -->
            <div class="flex items-center gap-4 w-full sm:w-auto">
              <!-- Item Checkbox -->
              <Checkbox
                :model-value="item.selectedForCheckout"
                binary
                class="w-5 h-5 rounded-md border border-slate-200 dark:border-white/10 checked:bg-indigo-500 checked:border-indigo-500 focus:outline-none"
                @update:model-value="toggleCartItemSelection(item.id)"
              />

              <!-- Item Image -->
              <div
                class="w-20 h-20 sm:w-24 sm:h-24 rounded-2xl overflow-hidden bg-slate-100 dark:bg-slate-950 border border-slate-200 dark:border-white/5 shrink-0"
              >
                <img
                  :src="item.product.imageUrl"
                  :alt="item.product.name"
                  class="w-full h-full object-cover"
                />
              </div>

              <!-- Product Info -->
              <div class="flex flex-col min-w-0">
                <span
                  class="text-[9px] uppercase tracking-widest text-slate-500 font-bold"
                >
                  {{ item.product.brand }}
                </span>
                <NuxtLink
                  :to="`/products/${item.product.id}`"
                  class="text-sm sm:text-base font-extrabold text-slate-900 dark:text-white hover:text-indigo-650 dark:text-indigo-400 transition-colors truncate mt-0.5"
                >
                  {{ item.product.name }}
                </NuxtLink>
                <span class="text-[10px] font-mono text-slate-600 mt-0.5">
                  SKU: {{ item.product.sku }}
                </span>

                <!-- Variant Selection / Specs Display -->
                <div
                  v-if="
                    item.selectedVariants &&
                    Object.keys(item.selectedVariants).length > 0
                  "
                  class="flex flex-wrap gap-2 mt-2"
                >
                  <div
                    v-for="(val, name) in item.selectedVariants"
                    :key="name"
                    class="flex items-center gap-1.5 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/50 border border-slate-200 dark:border-white/5 px-2.5 py-1 rounded-xl text-[10px] font-semibold text-slate-600 dark:text-slate-300"
                  >
                    <span class="text-slate-500">{{ name }}:</span>
                    <Select
                      :model-value="val"
                      :options="
                        item.product.variants?.find((v) => v.name === name)
                          ?.options || []
                      "
                      class="variant-select"
                      @change="
                        handleVariantChange(item, String(name), $event.value)
                      "
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Qty controls & Pricing Column -->
            <div
              class="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center gap-4 w-full sm:w-auto pt-3 sm:pt-0 border-t sm:border-t-0 border-slate-200 dark:border-white/5"
            >
              <!-- Qty Adjustment -->
              <div
                class="flex items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/10 rounded-xl overflow-hidden"
              >
                <button
                  class="px-2.5 py-1.5 text-xs text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors hover:bg-slate-100 dark:bg-slate-900 border-r border-slate-200 dark:border-white/10 focus:outline-none cursor-pointer"
                  @click="decrementQty(item)"
                >
                  <i class="pi pi-minus text-[8px]" />
                </button>
                <span class="w-8 text-center text-xs font-bold text-slate-750 dark:text-slate-200">
                  {{ item.quantity }}
                </span>
                <button
                  class="px-2.5 py-1.5 text-xs text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors hover:bg-slate-100 dark:bg-slate-900 border-l border-slate-200 dark:border-white/10 focus:outline-none cursor-pointer"
                  @click="incrementQty(item)"
                >
                  <i class="pi pi-plus text-[8px]" />
                </button>
              </div>

              <!-- Pricing and Delete Row -->
              <div class="flex items-center gap-4">
                <div class="flex flex-col text-right">
                  <span class="text-xs font-black text-slate-900 dark:text-white">
                    {{ formatCurrency(item.product.price * item.quantity) }}
                  </span>
                  <span
                    v-if="item.quantity > 1"
                    class="text-[9px] text-slate-500 font-semibold mt-0.5"
                  >
                    {{ formatCurrency(item.product.price) }} each
                  </span>
                </div>

                <!-- Delete button -->
                <button
                  class="text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 p-2 rounded-xl transition-all cursor-pointer"
                  title="Remove item"
                  @click="handleRemove(item)"
                >
                  <i class="pi pi-trash text-sm" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Order Summary Card -->
      <div class="lg:col-span-4 sticky top-24">
        <div
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 rounded-3xl p-6 sm:p-8 flex flex-col gap-6 shadow-2xl"
        >
          <h2
            class="text-lg font-extrabold text-slate-900 dark:text-white tracking-wide uppercase pb-3 border-b border-slate-200 dark:border-white/5"
          >
            Order Summary
          </h2>

          <!-- Shipping threshold progress bar -->
          <div v-if="cartSubtotal > 0" class="flex flex-col gap-2.5">
            <div
              class="flex justify-between items-center text-[10px] font-bold tracking-wide"
            >
              <span
                :class="
                  cartSubtotal >= 200 ? 'text-emerald-400' : 'text-slate-500 dark:text-slate-400'
                "
              >
                {{
                  cartSubtotal >= 200
                    ? 'FREE SHIPPING UNLOCKED'
                    : 'FREE SHIPPING THRESHOLD'
                }}
              </span>
              <span class="text-slate-500 dark:text-slate-400">
                {{
                  cartSubtotal >= 200
                    ? ''
                    : `${formatCurrency(shippingRemaining)} left`
                }}
              </span>
            </div>
            <div
              class="w-full h-1.5 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 rounded-full overflow-hidden border border-slate-200 dark:border-white/5"
            >
              <div
                class="h-full rounded-full transition-all duration-500 bg-linear-to-r"
                :class="
                  cartSubtotal >= 200
                    ? 'from-emerald-500 to-teal-500'
                    : 'from-indigo-500 to-blue-500'
                "
                :style="{ width: `${shippingProgress}%` }"
              />
            </div>
            <span
              class="text-[9px] text-slate-500 leading-normal font-semibold"
            >
              {{
                cartSubtotal >= 200
                  ? 'Your order qualifies for free standard shipping!'
                  : 'Add premium products to unlock free shipping ($200 minimum).'
              }}
            </span>
          </div>

          <!-- Vouchers & Promos Section -->
          <div
            v-if="cartSubtotal > 0"
            class="flex flex-col gap-3 pb-4 border-b border-slate-200 dark:border-white/5 my-1"
          >
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Vouchers & Promos</label
            >

            <!-- Applied Voucher State -->
            <div
              v-if="appliedVoucher"
              class="flex items-center justify-between bg-emerald-500/10 border border-emerald-500/25 p-3 rounded-xl"
            >
              <div class="flex flex-col min-w-0">
                <div class="flex items-center gap-1.5">
                  <span
                    class="px-1.5 py-0.5 rounded bg-emerald-500/20 text-[9px] font-bold text-emerald-400 font-mono tracking-wider"
                  >
                    {{ appliedVoucher.code }}
                  </span>
                  <span
                    v-if="cartDiscount === 0"
                    class="text-[9px] font-bold text-rose-400"
                  >
                    Inactive
                  </span>
                </div>
                <span
                  class="text-[10px] text-slate-600 dark:text-slate-300 font-semibold truncate mt-1"
                >
                  {{ appliedVoucher.title }}
                </span>
                <span
                  v-if="cartDiscount === 0"
                  class="text-[9px] text-rose-400/90 font-medium leading-normal mt-0.5"
                >
                  {{ getIneligibleReason(appliedVoucher) }}
                </span>
              </div>
              <button
                class="text-slate-500 dark:text-slate-400 hover:text-rose-400 hover:bg-rose-500/15 p-1.5 rounded-lg transition-colors cursor-pointer"
                title="Remove voucher"
                @click="handleRemovePromo"
              >
                <i class="pi pi-times text-xs" />
              </button>
            </div>

            <!-- Apply Voucher Input Form -->
            <div v-else class="flex gap-2">
              <input
                v-model="promoCodeInput"
                type="text"
                placeholder="Enter promo code"
                class="flex-1 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/10 hover:border-slate-300 dark:hover:border-white/20 focus:border-indigo-500 text-slate-750 dark:text-slate-200 placeholder-slate-600 px-3.5 py-2 rounded-xl font-medium text-xs transition-all focus:outline-none"
                @keyup.enter="handleApplyPromo"
              />
              <Button
                label="Apply"
                class="px-3.5 bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl font-bold text-[10px] uppercase tracking-wider text-slate-900 dark:text-white hover:shadow-[0_0_10px_rgba(99,102,241,0.3)] transition-all cursor-pointer"
                @click="handleApplyPromo"
              />
            </div>

            <!-- Browse collected vouchers link -->
            <button
              class="w-full flex items-center justify-center gap-2 py-2 border border-dashed border-slate-200 dark:border-white/10 hover:border-slate-300 dark:hover:border-white/20 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/20 hover:bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 rounded-xl text-[10px] font-bold uppercase tracking-wider text-indigo-650 dark:text-indigo-400 hover:text-indigo-600 dark:hover:text-indigo-300 transition-all cursor-pointer"
              @click="voucherModalVisible = true"
            >
              <i class="pi pi-ticket text-xs" />
              Browse Collected Vouchers ({{ collectedVouchers.length }})
            </button>
          </div>

          <!-- Invoice pricing rows -->
          <div class="flex flex-col gap-3 text-xs font-semibold">
            <div class="flex justify-between items-center">
              <span class="text-slate-500 dark:text-slate-400">Subtotal</span>
              <span class="text-slate-750 dark:text-slate-200">{{
                formatCurrency(cartSubtotal)
              }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-slate-500 dark:text-slate-400">Estimated Shipping</span>
              <span class="text-slate-750 dark:text-slate-200">
                {{ cartShipping === 0 ? 'FREE' : formatCurrency(cartShipping) }}
              </span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-slate-500 dark:text-slate-400">Estimated Tax (8%)</span>
              <span class="text-slate-750 dark:text-slate-200">{{
                formatCurrency(cartTaxes)
              }}</span>
            </div>
            <div
              v-if="cartDiscount > 0"
              class="flex justify-between items-center text-emerald-400 font-bold"
            >
              <span class="flex items-center gap-1.5">
                <i class="pi pi-tag text-xs" />
                Discount ({{ appliedVoucher?.code }})
              </span>
              <span>-{{ formatCurrency(cartDiscount) }}</span>
            </div>
            <div class="h-px bg-slate-100 dark:bg-white/5 my-1" />
            <div class="flex justify-between items-center text-sm font-bold">
              <span class="text-slate-900 dark:text-white">Order Total</span>
              <span class="text-indigo-650 dark:text-indigo-400 text-base font-black">
                {{ formatCurrency(cartTotal) }}
              </span>
            </div>
          </div>

          <!-- Checkout CTA -->
          <div>
            <Button
              label="Proceed to Checkout"
              icon="pi pi-arrow-right"
              icon-pos="right"
              :disabled="itemsSelectedCount === 0"
              class="w-full py-3 bg-linear-to-r from-indigo-500 to-blue-600 border-0 hover:shadow-[0_0_20px_rgba(99,102,241,0.4)] disabled:opacity-50 disabled:pointer-events-none rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer flex items-center justify-center gap-1.5"
              @click="openCheckout"
            />
            <span
              v-if="itemsSelectedCount === 0"
              class="block text-center text-[10px] text-rose-400/90 font-bold mt-2"
            >
              Please select at least one item to proceed.
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Visual Payment & Checkout Wizard Overlay Dialog -->
    <Dialog
      v-model:visible="checkoutVisible"
      modal
      dismissable-mask
      class="bg-slate-100 dark:bg-slate-950 text-slate-100 border border-slate-200 dark:border-white/10 max-w-lg w-full mx-4 rounded-3xl overflow-hidden shadow-2xl"
      content-class="p-6"
      header-class="p-6 border-b border-slate-200 dark:border-white/5 bg-transparent text-slate-900 dark:text-white font-extrabold text-lg"
      @hide="closeCheckout"
    >
      <template #header>
        <div class="flex items-center gap-2.5">
          <i class="pi pi-lock text-indigo-650 dark:text-indigo-400" />
          <span>Secure Checkout</span>
        </div>
      </template>

      <div class="flex flex-col gap-6">
        <!-- Step Indicators (Progress wizard) -->
        <div
          class="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-white/5"
        >
          <div
            v-for="step in [
              { num: 1, label: 'Shipping' },
              { num: 2, label: 'Payment' },
              { num: 3, label: 'Success' }
            ]"
            :key="step.num"
            class="flex items-center gap-2"
          >
            <span
              class="w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold"
              :class="
                activeStep === step.num
                  ? 'bg-indigo-500 text-slate-900 dark:text-white font-black ring-4 ring-indigo-500/25'
                  : activeStep > step.num
                    ? 'bg-emerald-500 text-slate-900 dark:text-white'
                    : 'bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-white/10 text-slate-500'
              "
            >
              <i
                v-if="activeStep > step.num"
                class="pi pi-check text-[8px] font-bold"
              />
              <span v-else>{{ step.num }}</span>
            </span>
            <span
              class="text-[11px] font-bold tracking-wide"
              :class="activeStep === step.num ? 'text-slate-900 dark:text-white' : 'text-slate-500'"
            >
              {{ step.label }}
            </span>
            <i
              v-if="step.num < 3"
              class="pi pi-angle-right text-[10px] text-slate-700 ml-1 hidden sm:inline"
            />
          </div>
        </div>

        <!-- STEP 1: SHIPPING DETAILS -->
        <div v-if="activeStep === 1" class="flex flex-col gap-4">
          <h3
            class="text-xs font-bold uppercase tracking-wider text-indigo-650 dark:text-indigo-400"
          >
            Shipping Information
          </h3>

          <!-- Saved Address Picker -->
          <div v-if="userAddresses.length > 0" class="flex flex-col gap-2">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Saved Addresses</label
            >
            <div class="flex flex-col gap-2 max-h-36 overflow-y-auto pr-0.5">
              <button
                v-for="addr in userAddresses"
                :key="addr.id"
                type="button"
                class="flex items-start gap-3 p-3 rounded-2xl border transition-all duration-200 cursor-pointer text-left focus:outline-none w-full"
                :class="
                  selectedAddressId === addr.id
                    ? 'bg-indigo-500/10 border-indigo-500 shadow-[0_0_10px_rgba(99,102,241,0.2)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 hover:border-slate-200 dark:border-white/15'
                "
                @click="selectAddress(addr.id)"
              >
                <div
                  class="w-7 h-7 rounded-xl flex items-center justify-center shrink-0 mt-0.5"
                  :class="
                    selectedAddressId === addr.id
                      ? 'bg-indigo-500/20 text-indigo-650 dark:text-indigo-400'
                      : 'bg-slate-800 text-slate-500'
                  "
                >
                  <i
                    :class="
                      addr.type === 'office' ? 'pi pi-building' : 'pi pi-home'
                    "
                    class="text-[10px]"
                  />
                </div>
                <div class="flex flex-col min-w-0 flex-1">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-bold text-slate-750 dark:text-slate-200 truncate">{{
                      addr.name
                    }}</span>
                    <span
                      class="text-[8px] uppercase tracking-wider font-black px-1.5 py-0.5 rounded-full shrink-0"
                      :class="
                        addr.type === 'office'
                          ? 'bg-sky-500/15 text-sky-400'
                          : 'bg-emerald-500/15 text-emerald-400'
                      "
                      >{{ addr.type }}</span
                    >
                    <span
                      v-if="addr.isDefault"
                      class="text-[8px] uppercase tracking-wider font-black px-1.5 py-0.5 rounded-full bg-indigo-500/15 text-indigo-650 dark:text-indigo-400 shrink-0"
                      >Default</span
                    >
                  </div>
                  <span class="text-[10px] text-slate-500 truncate mt-0.5"
                    >{{ addr.address }}, {{ addr.city }} {{ addr.zip }}</span
                  >
                </div>
                <i
                  v-if="selectedAddressId === addr.id"
                  class="pi pi-check-circle text-indigo-650 dark:text-indigo-400 text-sm shrink-0 mt-0.5"
                />
              </button>
            </div>
            <div class="flex items-center gap-2 my-1">
              <div class="flex-1 h-px bg-slate-100 dark:bg-white/5" />
              <span
                class="text-[9px] uppercase tracking-wider text-slate-600 font-bold"
                >or fill manually</span
              >
              <div class="flex-1 h-px bg-slate-100 dark:bg-white/5" />
            </div>
          </div>

          <!-- Full Name -->
          <div class="flex flex-col gap-1.5">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Full Name</label
            >
            <InputText
              v-model="shippingForm.name"
              placeholder="e.g. Liam Thompson"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
              :class="{ 'border-rose-500/50': shippingErrors.name }"
            />
            <span
              v-if="shippingErrors.name"
              class="text-[10px] text-rose-400 font-semibold"
            >
              {{ shippingErrors.name }}
            </span>
          </div>

          <!-- Email -->
          <div class="flex flex-col gap-1.5">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Email Address</label
            >
            <InputText
              v-model="shippingForm.email"
              placeholder="e.g. liam@example.com"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
              :class="{ 'border-rose-500/50': shippingErrors.email }"
            />
            <span
              v-if="shippingErrors.email"
              class="text-[10px] text-rose-400 font-semibold"
            >
              {{ shippingErrors.email }}
            </span>
          </div>

          <!-- Address -->
          <div class="flex flex-col gap-1.5">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Street Address</label
            >
            <InputText
              v-model="shippingForm.address"
              placeholder="e.g. 104 Applewood Dr"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
              :class="{ 'border-rose-500/50': shippingErrors.address }"
            />
            <span
              v-if="shippingErrors.address"
              class="text-[10px] text-rose-400 font-semibold"
            >
              {{ shippingErrors.address }}
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
                v-model="shippingForm.city"
                placeholder="e.g. Cupertino"
                class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                :class="{ 'border-rose-500/50': shippingErrors.city }"
              />
              <span
                v-if="shippingErrors.city"
                class="text-[10px] text-rose-400 font-semibold"
              >
                {{ shippingErrors.city }}
              </span>
            </div>
            <div class="flex flex-col gap-1.5">
              <label
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >Postal Code</label
              >
              <InputText
                v-model="shippingForm.zip"
                placeholder="e.g. 95014"
                class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                :class="{ 'border-rose-500/50': shippingErrors.zip }"
              />
              <span
                v-if="shippingErrors.zip"
                class="text-[10px] text-rose-400 font-semibold"
              >
                {{ shippingErrors.zip }}
              </span>
            </div>
          </div>

          <!-- Invoice Summary block -->
          <div
            class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border border-slate-200 dark:border-white/5 rounded-2xl p-4 mt-2"
          >
            <div class="flex flex-col">
              <span
                class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                >Total Due</span
              >
              <span class="text-lg font-black text-slate-900 dark:text-white mt-0.5">{{
                formatCurrency(cartTotal)
              }}</span>
            </div>
            <Button
              label="Next: Payment"
              icon="pi pi-credit-card"
              icon-pos="right"
              class="px-5 py-2.5 bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(99,102,241,0.4)] transition-all cursor-pointer"
              @click="nextStep"
            />
          </div>
        </div>

        <!-- STEP 2: PAYMENT & GLASS CARD PREVIEW -->
        <div v-if="activeStep === 2" class="flex flex-col gap-5">
          <!-- Payment Method Selector Grid -->
          <div class="flex flex-col gap-2">
            <label
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
            >
              Payment Method
            </label>
            <div class="grid grid-cols-2 gap-3">
              <!-- Credit Card -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  selectedPaymentMethod === 'credit_card'
                    ? 'bg-indigo-500/10 border-indigo-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(99,102,241,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="selectedPaymentMethod = 'credit_card'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    selectedPaymentMethod === 'credit_card'
                      ? 'text-indigo-650 dark:text-indigo-400 border-indigo-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-credit-card text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">Credit Card</span>
                  <span class="text-[8px] text-slate-500 font-semibold truncate"
                    >Visa, Mastercard</span
                  >
                </div>
              </button>

              <!-- Shopbee Pay -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  selectedPaymentMethod === 'shopbee_pay'
                    ? 'bg-indigo-500/10 border-indigo-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(99,102,241,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="selectedPaymentMethod = 'shopbee_pay'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    selectedPaymentMethod === 'shopbee_pay'
                      ? 'text-indigo-650 dark:text-indigo-400 border-indigo-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-bolt text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">Shopbee Pay</span>
                  <span class="text-[8px] text-slate-500 font-semibold truncate"
                    >E-Wallet Balance</span
                  >
                </div>
              </button>

              <!-- Bank Transfer -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  selectedPaymentMethod === 'bank_transfer'
                    ? 'bg-indigo-500/10 border-indigo-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(99,102,241,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="selectedPaymentMethod = 'bank_transfer'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    selectedPaymentMethod === 'bank_transfer'
                      ? 'text-indigo-650 dark:text-indigo-400 border-indigo-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-building text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">Bank Transfer</span>
                  <span class="text-[8px] text-slate-500 font-semibold truncate"
                    >Direct Deposit</span
                  >
                </div>
              </button>

              <!-- Cash on Delivery -->
              <button
                type="button"
                class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-300 cursor-pointer text-left focus:outline-none"
                :class="
                  selectedPaymentMethod === 'cod'
                    ? 'bg-indigo-500/10 border-indigo-500 text-slate-900 dark:text-white shadow-[0_0_12px_rgba(99,102,241,0.25)]'
                    : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
                "
                @click="selectedPaymentMethod = 'cod'"
              >
                <div
                  class="w-8 h-8 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5"
                  :class="
                    selectedPaymentMethod === 'cod'
                      ? 'text-indigo-650 dark:text-indigo-400 border-indigo-500/30'
                      : 'text-slate-500 dark:text-slate-400'
                  "
                >
                  <i class="pi pi-wallet text-xs" />
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="text-xs font-bold truncate">COD</span>
                  <span class="text-[8px] text-slate-500 font-semibold truncate"
                    >Pay on Delivery</span
                  >
                </div>
              </button>
            </div>
          </div>

          <!-- CREDIT CARD CONTENT VIEW -->
          <div
            v-if="selectedPaymentMethod === 'credit_card'"
            class="flex flex-col gap-5"
          >
            <!-- Saved Card Picker -->
            <div
              v-if="
                userPaymentMethods.filter((p) => p.type === 'credit_card')
                  .length > 0
              "
              class="flex flex-col gap-2"
            >
              <label
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >Saved Cards</label
              >
              <div class="flex flex-col gap-2">
                <button
                  v-for="pm in userPaymentMethods.filter(
                    (p) => p.type === 'credit_card'
                  )"
                  :key="pm.id"
                  type="button"
                  class="flex items-center gap-3 p-3 rounded-2xl border transition-all duration-200 cursor-pointer text-left focus:outline-none w-full"
                  :class="
                    selectedCardId === pm.id
                      ? 'bg-indigo-500/10 border-indigo-500 shadow-[0_0_10px_rgba(99,102,241,0.2)]'
                      : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 hover:border-slate-200 dark:border-white/15'
                  "
                  @click="selectCard(pm.id)"
                >
                  <div
                    class="w-8 h-8 rounded-xl flex items-center justify-center shrink-0"
                    :class="
                      selectedCardId === pm.id
                        ? 'bg-indigo-500/20 text-indigo-650 dark:text-indigo-400'
                        : 'bg-slate-800 text-slate-500'
                    "
                  >
                    <i class="pi pi-credit-card text-xs" />
                  </div>
                  <div class="flex flex-col min-w-0 flex-1">
                    <div class="flex items-center gap-2">
                      <span class="text-xs font-bold text-slate-750 dark:text-slate-200 truncate">
                        {{ pm.cardDetails?.cardName || 'Saved Card' }}
                      </span>
                      <span
                        v-if="pm.isDefault"
                        class="text-[8px] uppercase tracking-wider font-black px-1.5 py-0.5 rounded-full bg-indigo-500/15 text-indigo-650 dark:text-indigo-400 shrink-0"
                        >Default</span
                      >
                    </div>
                    <span class="text-[10px] text-slate-500 font-mono mt-0.5">
                      •••• •••• ••••
                      {{ pm.cardDetails?.cardNumber?.slice(-4) || '????' }}
                      &nbsp;·&nbsp; Exp {{ pm.cardDetails?.expiry }}
                    </span>
                  </div>
                  <i
                    v-if="selectedCardId === pm.id"
                    class="pi pi-check-circle text-indigo-650 dark:text-indigo-400 text-sm shrink-0"
                  />
                </button>
              </div>
              <div class="flex items-center gap-2 my-1">
                <div class="flex-1 h-px bg-slate-100 dark:bg-white/5" />
                <span
                  class="text-[9px] uppercase tracking-wider text-slate-600 font-bold"
                  >or enter new card</span
                >
                <div class="flex-1 h-px bg-slate-100 dark:bg-white/5" />
              </div>
            </div>

            <!-- Glassmorphic Card Visual Live Preview -->
            <div
              class="w-full aspect-video rounded-3xl p-6 shadow-2xl relative overflow-hidden flex flex-col justify-between text-slate-900 dark:text-white font-mono tracking-widest bg-linear-to-br from-indigo-600/70 via-indigo-750/70 to-purple-750/70 backdrop-blur-md border border-slate-200 dark:border-white/15 select-none"
            >
              <!-- Card Ambient Glow circles -->
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

              <!-- Logo & Contactless Row -->
              <div class="relative z-10 flex justify-between items-center">
                <div class="flex items-center gap-1.5">
                  <i
                    class="pi pi-bolt text-indigo-650 dark:text-indigo-400 text-lg drop-shadow-[0_0_6px_rgba(129,140,248,0.7)]"
                  />
                  <span
                    class="text-xs font-black tracking-normal uppercase text-slate-100"
                    >Shopbee Pay</span
                  >
                </div>
                <i class="pi pi-wifi text-slate-600 dark:text-slate-300 text-sm rotate-90" />
              </div>

              <!-- Card Chip gold Visual -->
              <div
                class="relative z-10 w-11 h-8 rounded-lg bg-linear-to-r from-yellow-300 via-amber-400 to-yellow-600 border border-amber-300/30 opacity-80"
              />

              <!-- Card Number -->
              <div
                class="relative z-10 text-base sm:text-lg text-slate-100 font-bold text-center tracking-widest my-2 select-all"
              >
                {{ paymentForm.cardNumber || '•••• •••• •••• ••••' }}
              </div>

              <!-- Cardholder Name & Expiry Row -->
              <div class="relative z-10 flex justify-between items-end">
                <div class="flex flex-col min-w-0">
                  <span
                    class="text-[8px] text-indigo-300 font-bold uppercase tracking-wide"
                    >Card Holder</span
                  >
                  <span
                    class="text-xs font-black text-slate-100 truncate tracking-wide mt-0.5"
                  >
                    {{ paymentForm.cardName.toUpperCase() || 'YOUR NAME' }}
                  </span>
                </div>
                <div class="flex flex-col shrink-0 text-right">
                  <span
                    class="text-[8px] text-indigo-300 font-bold uppercase tracking-wide"
                    >Expires</span
                  >
                  <span class="text-xs font-black text-slate-100 mt-0.5">{{
                    paymentForm.expiry || 'MM/YY'
                  }}</span>
                </div>
              </div>
            </div>

            <!-- Payment Form Fields -->
            <div class="flex flex-col gap-4">
              <!-- Card Name -->
              <div class="flex flex-col gap-1.5">
                <label
                  class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                  >Cardholder Name</label
                >
                <InputText
                  v-model="paymentForm.cardName"
                  placeholder="e.g. Liam Thompson"
                  class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                  :class="{ 'border-rose-500/50': paymentErrors.cardName }"
                />
                <span
                  v-if="paymentErrors.cardName"
                  class="text-[10px] text-rose-400 font-semibold"
                >
                  {{ paymentErrors.cardName }}
                </span>
              </div>

              <!-- Card Number -->
              <div class="flex flex-col gap-1.5">
                <label
                  class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                  >Card Number</label
                >
                <InputText
                  v-model="paymentForm.cardNumber"
                  placeholder="1234 5678 1234 5678"
                  class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                  :class="{ 'border-rose-500/50': paymentErrors.cardNumber }"
                  @input="formatCardNumberInput"
                />
                <span
                  v-if="paymentErrors.cardNumber"
                  class="text-[10px] text-rose-400 font-semibold"
                >
                  {{ paymentErrors.cardNumber }}
                </span>
              </div>

              <!-- Grid: Expiry + CVC -->
              <div class="grid grid-cols-2 gap-4">
                <div class="flex flex-col gap-1.5">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >Expiration Date</label
                  >
                  <InputText
                    v-model="paymentForm.expiry"
                    placeholder="MM/YY"
                    class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                    :class="{ 'border-rose-500/50': paymentErrors.expiry }"
                    @input="formatExpiryInput"
                  />
                  <span
                    v-if="paymentErrors.expiry"
                    class="text-[10px] text-rose-400 font-semibold"
                  >
                    {{ paymentErrors.expiry }}
                  </span>
                </div>
                <div class="flex flex-col gap-1.5">
                  <label
                    class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                    >CVC Code</label
                  >
                  <InputText
                    v-model="paymentForm.cvc"
                    placeholder="123"
                    type="password"
                    class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 placeholder:text-slate-600 focus:border-indigo-500/50"
                    :class="{ 'border-rose-500/50': paymentErrors.cvc }"
                    @input="formatCvcInput"
                  />
                  <span
                    v-if="paymentErrors.cvc"
                    class="text-[10px] text-rose-400 font-semibold"
                  >
                    {{ paymentErrors.cvc }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- SHOPBEE PAY CONTENT VIEW -->
          <div
            v-else-if="selectedPaymentMethod === 'shopbee_pay'"
            class="flex flex-col gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-3xl p-5 relative overflow-hidden"
          >
            <div
              class="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(99,102,241,0.08),transparent_50%)] pointer-events-none"
            ></div>
            <div
              class="flex items-center justify-between pb-3 border-b border-slate-200 dark:border-white/5"
            >
              <div class="flex items-center gap-2">
                <i class="pi pi-bolt text-indigo-650 dark:text-indigo-400 text-lg" />
                <span class="text-xs font-black uppercase text-slate-750 dark:text-slate-200"
                  >Shopbee Pay Wallet</span
                >
              </div>
              <span
                class="px-2.5 py-0.5 rounded-full text-[9px] font-extrabold uppercase tracking-wider border"
                :class="
                  cartTotal <= 1200
                    ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20'
                    : 'text-rose-400 bg-rose-500/10 border-rose-500/20'
                "
              >
                {{
                  cartTotal <= 1200
                    ? 'Sufficient Balance'
                    : 'Insufficient Balance'
                }}
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
                >Order Total: {{ formatCurrency(cartTotal) }}</span
              >
            </div>

            <div
              v-if="cartTotal > 1200"
              class="bg-rose-500/10 border border-rose-500/20 rounded-2xl p-3 flex items-start gap-2.5"
            >
              <i
                class="pi pi-exclamation-triangle text-rose-400 text-xs shrink-0 mt-0.5"
              />
              <div class="flex flex-col">
                <span
                  class="text-[10px] font-bold text-rose-400 uppercase tracking-wide"
                  >Insufficient Wallet Funds</span
                >
                <span
                  class="text-[9px] text-slate-500 dark:text-slate-400 font-medium leading-relaxed mt-0.5"
                  >Please choose another payment method or proceed with a
                  different cart selection.</span
                >
              </div>
            </div>

            <div
              v-else
              class="bg-indigo-500/5 border border-indigo-500/15 rounded-2xl p-3 flex items-start gap-2.5"
            >
              <i
                class="pi pi-info-circle text-indigo-650 dark:text-indigo-400 text-xs shrink-0 mt-0.5"
              />
              <div class="flex flex-col">
                <span
                  class="text-[10px] font-bold text-indigo-300 uppercase tracking-wide"
                  >Fast, Secure & Automated</span
                >
                <span
                  class="text-[9px] text-slate-500 dark:text-slate-400 font-medium leading-relaxed mt-0.5"
                  >Your order amount will be deducted instantly from your secure
                  e-wallet. No passwords or card digits required.</span
                >
              </div>
            </div>
          </div>

          <!-- BANK TRANSFER CONTENT VIEW -->
          <div
            v-else-if="selectedPaymentMethod === 'bank_transfer'"
            class="flex flex-col gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-3xl p-5 relative overflow-hidden"
          >
            <div class="flex items-center gap-2 pb-3 border-b border-slate-200 dark:border-white/5">
              <i class="pi pi-building text-indigo-650 dark:text-indigo-400 text-lg" />
              <span class="text-xs font-black uppercase text-slate-750 dark:text-slate-200"
                >Direct Bank Transfer</span
              >
            </div>

            <div class="flex flex-col gap-3 text-xs font-semibold">
              <div
                class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 px-3 py-2.5 rounded-xl"
              >
                <div class="flex flex-col">
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                    >Bank Name</span
                  >
                  <span class="text-slate-900 dark:text-white font-bold mt-0.5"
                    >Shopbee Partner Bank</span
                  >
                </div>
                <i class="pi pi-university text-slate-600 text-sm" />
              </div>

              <div
                class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 px-3 py-2.5 rounded-xl"
              >
                <div class="flex flex-col">
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                    >Account Name</span
                  >
                  <span class="text-slate-900 dark:text-white font-bold mt-0.5"
                    >Shopbee Commerce Inc.</span
                  >
                </div>
              </div>

              <div
                class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 px-3 py-2.5 rounded-xl"
              >
                <div class="flex flex-col min-w-0">
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                    >Account Number</span
                  >
                  <span class="text-slate-900 dark:text-white font-bold font-mono mt-0.5"
                    >1029-4820-1928</span
                  >
                </div>
                <button
                  type="button"
                  class="px-2.5 py-1.5 rounded-lg bg-indigo-600/20 hover:bg-indigo-600 border border-indigo-500/20 hover:border-indigo-500 text-[9px] font-bold uppercase tracking-wider text-indigo-300 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-all cursor-pointer flex items-center gap-1.5 focus:outline-none"
                  @click="handleCopyAccount"
                >
                  <i class="pi pi-copy" />
                  Copy
                </button>
              </div>

              <div
                class="flex justify-between items-center bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 px-3 py-2.5 rounded-xl"
              >
                <div class="flex flex-col">
                  <span
                    class="text-[9px] uppercase tracking-wider text-slate-500 font-bold"
                    >Transfer Reference</span
                  >
                  <span class="text-slate-900 dark:text-white font-mono font-bold mt-0.5">{{
                    `REF-${shippingForm.name.replace(/\s+/g, '').slice(0, 5).toUpperCase() || 'ORDER'}-${shippingForm.zip || 'ZIP'}`
                  }}</span>
                </div>
                <span class="text-[8px] text-slate-500 italic"
                  >Include in payment info</span
                >
              </div>
            </div>

            <div
              class="bg-indigo-500/5 border border-indigo-500/15 rounded-2xl p-3 flex items-start gap-2.5"
            >
              <i
                class="pi pi-info-circle text-indigo-650 dark:text-indigo-400 text-xs shrink-0 mt-0.5"
              />
              <div class="flex flex-col">
                <span
                  class="text-[10px] font-bold text-indigo-300 uppercase tracking-wide"
                  >Verification Notice</span
                >
                <span
                  class="text-[9px] text-slate-500 dark:text-slate-400 font-medium leading-relaxed mt-0.5"
                  >Please transfer exactly
                  <strong>{{ formatCurrency(cartTotal) }}</strong> using the
                  reference code above. Your order will be placed in processing
                  after verification.</span
                >
              </div>
            </div>
          </div>

          <!-- CASH ON DELIVERY CONTENT VIEW -->
          <div
            v-else-if="selectedPaymentMethod === 'cod'"
            class="flex flex-col gap-4 bg-white dark:bg-slate-100 dark:bg-slate-900/40 border border-slate-200 dark:border-white/5 rounded-3xl p-5 relative overflow-hidden"
          >
            <div class="flex items-center gap-2 pb-3 border-b border-slate-200 dark:border-white/5">
              <i class="pi pi-wallet text-indigo-650 dark:text-indigo-400 text-lg" />
              <span class="text-xs font-black uppercase text-slate-750 dark:text-slate-200"
                >Cash on Delivery (COD)</span
              >
            </div>

            <div
              class="flex flex-col py-2 justify-center items-center gap-2 text-center"
            >
              <div
                class="w-12 h-12 rounded-full bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center"
              >
                <i class="pi pi-truck text-lg text-indigo-650 dark:text-indigo-400" />
              </div>
              <span class="text-xs font-bold text-slate-750 dark:text-slate-200"
                >Pay when your package arrives</span
              >
              <p
                class="text-[10px] text-slate-500 dark:text-slate-400 font-medium max-w-[320px] leading-relaxed"
              >
                No advance online payment is required. You will pay the courier
                driver directly in cash upon receiving your order.
              </p>
            </div>

            <div
              class="bg-indigo-500/5 border border-indigo-500/15 rounded-2xl p-3 flex items-start gap-2.5"
            >
              <i
                class="pi pi-info-circle text-indigo-650 dark:text-indigo-400 text-xs shrink-0 mt-0.5"
              />
              <div class="flex flex-col">
                <span
                  class="text-[10px] font-bold text-indigo-300 uppercase tracking-wide"
                  >Preparation Reassurance</span
                >
                <span
                  class="text-[9px] text-slate-500 dark:text-slate-400 font-medium leading-relaxed mt-0.5"
                  >Please prepare the exact sum of
                  <strong>{{ formatCurrency(cartTotal) }}</strong> to ensure
                  smooth handover with the transit courier agent.</span
                >
              </div>
            </div>
          </div>

          <!-- Wizard Navigation Bottom Row -->
          <div
            class="flex justify-between items-center border-t border-slate-200 dark:border-white/5 pt-4 mt-2"
          >
            <button
              type="button"
              class="flex items-center gap-2 px-4 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 text-xs font-bold text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white hover:bg-white dark:bg-slate-100 dark:bg-slate-900/40 transition-all cursor-pointer"
              :disabled="isProcessingPayment"
              @click="prevStep"
            >
              <i class="pi pi-arrow-left" />
              Back
            </button>

            <Button
              :label="payButtonLabel"
              :icon="
                isProcessingPayment ? 'pi pi-spin pi-spinner' : 'pi pi-check'
              "
              :loading="isProcessingPayment"
              class="px-6 py-2.5 bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white hover:shadow-[0_0_15px_rgba(99,102,241,0.45)] transition-all disabled:opacity-50 disabled:pointer-events-none cursor-pointer flex items-center justify-center gap-1.5"
              :disabled="isPayButtonDisabled"
              @click="submitPayment"
            />
          </div>
        </div>

        <!-- STEP 3: SUCCESS CONFIRMATION -->
        <div
          v-if="activeStep === 3"
          class="flex flex-col items-center text-center py-6 gap-5 animate-slide-up"
        >
          <!-- Animated success circles -->
          <div class="relative w-20 h-20 flex items-center justify-center">
            <div
              class="absolute inset-0 bg-emerald-500/10 rounded-full border border-emerald-500/20 animate-ping"
            />
            <div
              class="w-16 h-16 rounded-full bg-emerald-500/20 flex items-center justify-center border border-emerald-500/40"
            >
              <i class="pi pi-check text-emerald-400 text-2xl font-black" />
            </div>
          </div>

          <div class="flex flex-col gap-2">
            <h2 class="text-xl font-extrabold text-slate-900 dark:text-white">Order Confirmed!</h2>
            <p
              class="text-xs text-slate-500 dark:text-slate-400 max-w-xs leading-relaxed font-semibold"
            >
              Thank you for your purchase. We've received your shipping details
              and processed the payment successfully.
            </p>
          </div>

          <!-- Order details meta -->
          <div
            class="flex flex-col gap-3 w-full bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border border-slate-200 dark:border-white/5 rounded-2xl p-5 text-xs font-semibold"
          >
            <div class="flex justify-between items-center">
              <span class="text-slate-500">Order Number</span>
              <span class="text-slate-900 dark:text-white font-mono font-bold">{{
                orderNumber
              }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-slate-500">Receipt Email</span>
              <span class="text-slate-900 dark:text-white truncate max-w-[180px]">{{
                shippingForm.email
              }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-slate-500">Delivery Method</span>
              <span class="text-slate-900 dark:text-white">Standard Delivery (3-5 Days)</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-slate-500">Payment Method</span>
              <span class="text-slate-900 dark:text-white">
                {{
                  selectedPaymentMethod === 'credit_card'
                    ? 'Credit / Debit Card'
                    : selectedPaymentMethod === 'shopbee_pay'
                      ? 'Shopbee Pay (E-Wallet)'
                      : selectedPaymentMethod === 'bank_transfer'
                        ? 'Direct Bank Transfer'
                        : 'Cash on Delivery (COD)'
                }}
              </span>
            </div>
            <div
              v-if="orderDiscount > 0"
              class="flex justify-between items-center text-emerald-400 font-bold"
            >
              <span>Discount Applied</span>
              <span
                >-{{ formatCurrency(orderDiscount) }} ({{
                  orderVoucherCode
                }})</span
              >
            </div>
            <div class="h-px bg-slate-100 dark:bg-white/5 my-0.5" />
            <div
              class="flex justify-between items-center text-slate-900 dark:text-white font-bold text-sm"
            >
              <span>Total Paid</span>
              <span class="text-indigo-650 dark:text-indigo-400 font-black">{{
                formatCurrency(orderTotalPaid)
              }}</span>
            </div>
          </div>

          <!-- Return Catalog CTA -->
          <div class="flex flex-col sm:flex-row gap-3 w-full mt-2">
            <Button
              label="Track Order"
              icon="pi pi-compass"
              class="flex-grow py-3 bg-indigo-600 hover:bg-indigo-500 border border-indigo-500/20 rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-all cursor-pointer flex items-center justify-center gap-1.5"
              @click="handleTrackOrder"
            />
            <Button
              label="Continue Shopping"
              icon="pi pi-shopping-bag"
              class="flex-grow py-3 border border-slate-200 dark:border-white/10 hover:bg-white dark:bg-slate-100 dark:bg-slate-900/40 text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white rounded-xl font-bold text-xs transition-all cursor-pointer flex items-center justify-center gap-1.5"
              @click="closeCheckout"
            />
          </div>
        </div>
      </div>
    </Dialog>

    <!-- Browse Collected Vouchers Modal Dialog -->
    <Dialog
      v-model:visible="voucherModalVisible"
      modal
      dismissable-mask
      class="bg-slate-100 dark:bg-slate-950 text-slate-100 border border-slate-200 dark:border-white/10 max-w-md w-full mx-4 rounded-3xl overflow-hidden shadow-2xl"
      content-class="p-6"
      header-class="p-6 border-b border-slate-200 dark:border-white/5 bg-transparent text-slate-900 dark:text-white font-extrabold text-lg"
    >
      <template #header>
        <div class="flex items-center gap-2.5">
          <i class="pi pi-ticket text-indigo-650 dark:text-indigo-400" />
          <span>My Collected Vouchers</span>
        </div>
      </template>

      <div class="flex flex-col gap-5 max-h-[450px] overflow-y-auto pr-1">
        <!-- If no collected vouchers -->
        <div
          v-if="collectedVouchers.length === 0"
          class="flex flex-col items-center text-center py-8 gap-4"
        >
          <div
            class="w-14 h-14 rounded-2xl bg-slate-100 dark:bg-slate-900 flex items-center justify-center border border-slate-200 dark:border-white/5"
          >
            <i class="pi pi-ticket text-slate-600 text-xl" />
          </div>
          <div class="flex flex-col gap-1.5">
            <h4 class="text-sm font-bold text-slate-900 dark:text-white">
              No vouchers collected yet
            </h4>
            <p class="text-xs text-slate-500 max-w-[240px] leading-relaxed">
              Visit our Voucher Hub to claim coupons, event discounts, and store
              rewards!
            </p>
          </div>
          <Button
            label="Visit Voucher Hub"
            size="small"
            class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 border-0 rounded-xl font-bold text-xs text-slate-900 dark:text-white transition-colors cursor-pointer"
            @click="handleVisitVoucherHub"
          />
        </div>

        <div v-else class="flex flex-col gap-6">
          <!-- Section: Eligible Vouchers -->
          <div class="flex flex-col gap-3">
            <h4
              class="text-[10px] font-bold uppercase tracking-wider text-emerald-400"
            >
              Eligible Vouchers ({{ eligibleVouchers.length }})
            </h4>
            <div
              v-if="eligibleVouchers.length === 0"
              class="text-xs text-slate-600 italic"
            >
              No collected vouchers are eligible for the current cart
              items/subtotal.
            </div>
            <div v-else class="flex flex-col gap-3">
              <!-- Render Voucher Tickets -->
              <div
                v-for="voucher in eligibleVouchers"
                :key="voucher.id"
                class="flex items-stretch rounded-xl border border-emerald-500/20 bg-emerald-500/5 hover:bg-emerald-500/10 transition-colors p-3.5 relative overflow-hidden"
              >
                <!-- Color bar -->
                <div
                  class="w-1.5 shrink-0 bg-linear-to-b rounded-l-lg"
                  :class="voucher.colorClass"
                ></div>
                <!-- Details -->
                <div
                  class="flex-1 flex flex-col justify-between pl-3 min-w-0 pr-2"
                >
                  <div class="flex flex-col">
                    <span class="text-[9px] uppercase font-bold text-slate-500 dark:text-slate-400">
                      {{
                        voucher.type === 'merchant'
                          ? `${voucher.merchantBrand} Store`
                          : 'Platform Code'
                      }}
                    </span>
                    <span
                      class="text-xs font-bold text-slate-900 dark:text-white mt-0.5 truncate"
                      >{{ voucher.title }}</span
                    >
                    <span
                      class="text-[10px] text-slate-500 mt-0.5 font-medium leading-relaxed"
                      >{{ voucher.description }}</span
                    >
                  </div>
                  <span
                    class="text-[9px] font-mono text-indigo-650 dark:text-indigo-400 font-bold mt-2 bg-indigo-500/10 border border-indigo-500/20 px-1.5 py-0.5 rounded w-max"
                  >
                    Code: {{ voucher.code }}
                  </span>
                </div>
                <!-- Action / Value -->
                <div
                  class="shrink-0 flex flex-col justify-between items-end min-w-[70px]"
                >
                  <span class="text-sm font-black text-slate-900 dark:text-white">
                    {{
                      voucher.discountType === 'percentage'
                        ? `${voucher.discountValue}%`
                        : `$${voucher.discountValue}`
                    }}
                    OFF
                  </span>
                  <Button
                    label="Apply"
                    size="small"
                    class="py-1 px-3 bg-indigo-600 hover:bg-indigo-500 border-0 rounded-lg text-[10px] font-extrabold uppercase text-slate-900 dark:text-white cursor-pointer"
                    @click="selectVoucherFromModal(voucher)"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Section: Ineligible Vouchers -->
          <div class="flex flex-col gap-3">
            <h4
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500"
            >
              Collected but Ineligible ({{ ineligibleVouchers.length }})
            </h4>
            <div
              v-if="ineligibleVouchers.length === 0"
              class="text-xs text-slate-700 italic"
            >
              All collected vouchers are eligible.
            </div>
            <div v-else class="flex flex-col gap-3">
              <div
                v-for="voucher in ineligibleVouchers"
                :key="voucher.id"
                class="flex items-stretch rounded-xl border border-slate-200 dark:border-white/5 bg-white dark:bg-slate-100 dark:bg-slate-900/40 opacity-60 p-3.5 relative overflow-hidden"
              >
                <!-- Color bar -->
                <div class="w-1.5 shrink-0 bg-slate-800 rounded-l-lg"></div>
                <!-- Details -->
                <div
                  class="flex-1 flex flex-col justify-between pl-3 min-w-0 pr-2"
                >
                  <div class="flex flex-col">
                    <span class="text-[9px] uppercase font-bold text-slate-500">
                      {{
                        voucher.type === 'merchant'
                          ? `${voucher.merchantBrand} Store`
                          : 'Platform Code'
                      }}
                    </span>
                    <span
                      class="text-xs font-bold text-slate-500 dark:text-slate-400 mt-0.5 truncate"
                      >{{ voucher.title }}</span
                    >
                    <span class="text-[9px] text-rose-400 font-semibold mt-1">
                      Reason: {{ getIneligibleReason(voucher) }}
                    </span>
                  </div>
                  <span
                    class="text-[9px] font-mono text-slate-600 font-bold mt-2 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/5 px-1.5 py-0.5 rounded w-max"
                  >
                    Code: {{ voucher.code }}
                  </span>
                </div>
                <!-- Value -->
                <div
                  class="shrink-0 flex flex-col justify-between items-end min-w-[70px]"
                >
                  <span class="text-sm font-black text-slate-500">
                    {{
                      voucher.discountType === 'percentage'
                        ? `${voucher.discountValue}%`
                        : `$${voucher.discountValue}`
                    }}
                    OFF
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
/* Customizable PrimeVue styling overrides */
:deep(.p-checkbox-box.p-highlight) {
  border-color: rgb(99, 102, 241) !important;
  background: rgb(99, 102, 241) !important;
}

.dark :deep(.p-dialog-header-actions) {
  color: rgb(226, 232, 240);
}

.dark :deep(.p-dialog-close-button) {
  color: rgb(226, 232, 240) !important;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
}

:deep(.p-dialog-close-button:hover) {
  background: rgba(255, 255, 255, 0.15) !important;
}

/* Custom styling to make PrimeVue Select component look like a small inline text dropdown */
:deep(.variant-select.p-select) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
  height: auto !important;
  display: inline-flex !important;
  align-items: center !important;
  gap: 2px !important;
}

:deep(.variant-select .p-select-label) {
  padding: 0 !important;
  font-size: 10px !important;
  font-weight: 800 !important;
  color: white !important;
  line-height: 1 !important;
}

:deep(.variant-select .p-select-dropdown) {
  width: auto !important;
  height: auto !important;
  color: rgb(129, 140, 248) !important; /* Indigo 400 */
}

/* Style the popup overlay option list */
.dark :deep(.p-select-overlay) {
  background: rgb(15, 23, 42) !important; /* slate-900 */
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 12px !important;
  box-shadow:
    0 10px 15px -3px rgba(0, 0, 0, 0.3),
    0 4px 6px -4px rgba(0, 0, 0, 0.3) !important;
}

.dark :deep(.p-select-option) {
  font-size: 10px !important;
  font-weight: 600 !important;
  color: rgb(203, 213, 225) !important; /* slate-300 */
  padding: 6px 12px !important;
  border-radius: 6px !important;
}

:deep(.p-select-option:hover) {
  background: rgba(99, 102, 241, 0.15) !important; /* Indigo 500 opacity */
  color: white !important;
}

:deep(.p-select-option.p-select-option-selected) {
  background: rgba(99, 102, 241, 0.3) !important;
  color: white !important;
}
</style>
