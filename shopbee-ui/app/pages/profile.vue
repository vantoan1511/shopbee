<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useUserProfile } from '~/composables/useUserProfile'
import type { UserAddress } from '~/composables/useUserProfile'

const toast = useToast()
const router = useRouter()

const {
  userProfile,
  userAddresses,
  userPaymentMethods,
  userBalance,
  updateProfile,
  addAddress,
  updateAddress,
  deleteAddress,
  addPaymentMethod,
  deletePaymentMethod,
  setDefaultAddress,
  setDefaultPaymentMethod
} = useUserProfile()

// Tabs navigation state: 'info' | 'addresses' | 'payments'
const activeTab = ref<'info' | 'addresses' | 'payments'>('info')

// Personal Profile edit forms
const profileForm = ref({
  name: userProfile.value.name,
  email: userProfile.value.email,
  phone: userProfile.value.phone
})

const isSavingProfile = ref(false)

watch(
  () => userProfile.value,
  (newVal) => {
    profileForm.value = {
      name: newVal.name,
      email: newVal.email,
      phone: newVal.phone
    }
  },
  { deep: true }
)

const handleSaveProfile = async () => {
  if (!profileForm.value.name.trim()) {
    toast.add({
      severity: 'error',
      summary: 'Validation Error',
      detail: 'Name cannot be empty.',
      life: 3000
    })
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (
    !profileForm.value.email.trim() ||
    !emailRegex.test(profileForm.value.email)
  ) {
    toast.add({
      severity: 'error',
      summary: 'Validation Error',
      detail: 'Please enter a valid email address.',
      life: 3000
    })
    return
  }

  isSavingProfile.value = true
  // Mock saving delay for premium feel
  await new Promise((resolve) => setTimeout(resolve, 800))
  updateProfile(profileForm.value)
  isSavingProfile.value = false

  toast.add({
    severity: 'success',
    summary: 'Profile Updated',
    detail: 'Your personal information was updated successfully.',
    life: 3000
  })
}

// Address Book state & dialogues
const addressDialogVisible = ref(false)
const isEditingAddress = ref(false)
const addressFormId = ref('')
const addressForm = ref<Omit<UserAddress, 'id'>>({
  name: '',
  address: '',
  city: '',
  zip: '',
  type: 'home',
  isDefault: false
})

const openAddAddressDialog = () => {
  isEditingAddress.value = false
  addressFormId.value = ''
  addressForm.value = {
    name: userProfile.value.name,
    address: '',
    city: '',
    zip: '',
    type: 'home',
    isDefault: userAddresses.value.length === 0
  }
  addressDialogVisible.value = true
}

const openEditAddressDialog = (addr: UserAddress) => {
  isEditingAddress.value = true
  addressFormId.value = addr.id
  addressForm.value = {
    name: addr.name,
    address: addr.address,
    city: addr.city,
    zip: addr.zip,
    type: addr.type,
    isDefault: addr.isDefault
  }
  addressDialogVisible.value = true
}

const handleSaveAddress = () => {
  const f = addressForm.value
  if (!f.name.trim() || !f.address.trim() || !f.city.trim() || !f.zip.trim()) {
    toast.add({
      severity: 'error',
      summary: 'Form Incomplete',
      detail: 'Please fill in all address fields.',
      life: 3000
    })
    return
  }

  if (isEditingAddress.value) {
    updateAddress(addressFormId.value, f)
    toast.add({
      severity: 'success',
      summary: 'Address Updated',
      detail: 'Address entry was updated successfully.',
      life: 3000
    })
  } else {
    addAddress(f)
    toast.add({
      severity: 'success',
      summary: 'Address Added',
      detail: 'New address was added to your book.',
      life: 3000
    })
  }
  addressDialogVisible.value = false
}

const handleDeleteAddress = (id: string) => {
  deleteAddress(id)
  toast.add({
    severity: 'info',
    summary: 'Address Deleted',
    detail: 'The address was removed from your address book.',
    life: 3000
  })
}

const handleSetDefaultAddress = (id: string) => {
  setDefaultAddress(id)
  toast.add({
    severity: 'success',
    summary: 'Default Changed',
    detail: 'Primary shipping address has been updated.',
    life: 3000
  })
}

// Payment Methods state & dialogues
const paymentDialogVisible = ref(false)
const paymentForm = ref({
  cardNumber: '',
  cardName: '',
  expiry: '',
  cvc: '',
  isDefault: false
})

const openAddPaymentDialog = () => {
  paymentForm.value = {
    cardNumber: '',
    cardName: userProfile.value.name,
    expiry: '',
    cvc: '',
    isDefault: userPaymentMethods.value.length === 0
  }
  paymentDialogVisible.value = true
}

const handleSavePaymentMethod = () => {
  const f = paymentForm.value
  const rawCard = f.cardNumber.replace(/\s+/g, '')

  if (rawCard.length < 16) {
    toast.add({
      severity: 'error',
      summary: 'Validation Error',
      detail: 'Please enter a valid 16-digit card number.',
      life: 3000
    })
    return
  }
  if (!f.cardName.trim()) {
    toast.add({
      severity: 'error',
      summary: 'Validation Error',
      detail: 'Cardholder name is required.',
      life: 3000
    })
    return
  }
  if (!/^\d{2}\/\d{2}$/.test(f.expiry)) {
    toast.add({
      severity: 'error',
      summary: 'Validation Error',
      detail: 'Expiry must be in MM/YY format.',
      life: 3000
    })
    return
  }
  if (f.cvc.length < 3) {
    toast.add({
      severity: 'error',
      summary: 'Validation Error',
      detail: 'CVC code must be at least 3 digits.',
      life: 3000
    })
    return
  }

  addPaymentMethod({
    type: 'credit_card',
    cardDetails: {
      cardNumber: f.cardNumber,
      cardName: f.cardName,
      expiry: f.expiry,
      cvc: f.cvc
    },
    isDefault: f.isDefault
  })

  toast.add({
    severity: 'success',
    summary: 'Payment Saved',
    detail: 'Credit card was added to your wallet.',
    life: 3000
  })
  paymentDialogVisible.value = false
}

const handleDeletePayment = (id: string) => {
  deletePaymentMethod(id)
  toast.add({
    severity: 'info',
    summary: 'Card Removed',
    detail: 'The payment method was removed.',
    life: 3000
  })
}

const handleSetDefaultPayment = (id: string) => {
  setDefaultPaymentMethod(id)
  toast.add({
    severity: 'success',
    summary: 'Default Changed',
    detail: 'Primary payment method updated.',
    life: 3000
  })
}

const formatCardNumber = (num?: string) => {
  if (!num) return '•••• •••• •••• ••••'
  const clean = num.replace(/\s+/g, '')
  return `•••• •••• •••• ${clean.slice(-4)}`
}

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(val)
}

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

const goBack = () => router.push('/')
</script>

<template>
  <div class="flex-1 flex flex-col gap-6 animate-slide-up">
    <!-- Back button -->
    <div>
      <button
        class="group flex items-center gap-2.5 text-xs font-bold text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white transition-colors focus:outline-none cursor-pointer border-0 bg-transparent"
        @click="{ goBack }"
      >
        <i
          class="pi pi-arrow-left group-hover:-translate-x-1 transition-transform"
        />
        Return to Catalog
      </button>
    </div>

    <!-- Page Title -->
    <div>
      <h1 class="text-2xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
        My Profile
      </h1>
      <p class="text-xs text-slate-500 mt-1">
        Configure shipping destinations, default payments, and account
        properties.
      </p>
    </div>

    <!-- Dashboard Layout Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start mt-2">
      <!-- Left sidebar selector tabs -->
      <div
        class="lg:col-span-3 flex flex-col gap-2 bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 p-4 rounded-3xl"
      >
        <button
          class="flex items-center gap-3 w-full px-4 py-3 rounded-2xl text-xs font-bold transition-all text-left cursor-pointer focus:outline-none"
          :class="
            activeTab === 'info'
              ? 'bg-indigo-500/10 border border-indigo-500/30 text-slate-900 dark:text-white shadow-md'
              : 'text-slate-500 dark:text-slate-400 border border-transparent hover:text-slate-750 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-100 dark:bg-white/5'
          "
          @click="activeTab = 'info'"
        >
          <i class="pi pi-user text-sm" />
          <span>Personal Info</span>
        </button>

        <button
          class="flex items-center gap-3 w-full px-4 py-3 rounded-2xl text-xs font-bold transition-all text-left cursor-pointer focus:outline-none"
          :class="
            activeTab === 'addresses'
              ? 'bg-indigo-500/10 border border-indigo-500/30 text-slate-900 dark:text-white shadow-md'
              : 'text-slate-500 dark:text-slate-400 border border-transparent hover:text-slate-750 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-100 dark:bg-white/5'
          "
          @click="activeTab = 'addresses'"
        >
          <i class="pi pi-map-marker text-sm" />
          <span>Address Book</span>
          <span
            class="ml-auto bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/10 px-2 py-0.5 rounded-md text-[9px] font-mono text-slate-500"
          >
            {{ userAddresses.length }}
          </span>
        </button>

        <button
          class="flex items-center gap-3 w-full px-4 py-3 rounded-2xl text-xs font-bold transition-all text-left cursor-pointer focus:outline-none"
          :class="
            activeTab === 'payments'
              ? 'bg-indigo-500/10 border border-indigo-500/30 text-slate-900 dark:text-white shadow-md'
              : 'text-slate-500 dark:text-slate-400 border border-transparent hover:text-slate-750 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-100 dark:bg-white/5'
          "
          @click="activeTab = 'payments'"
        >
          <i class="pi pi-credit-card text-sm" />
          <span>Payment Methods</span>
          <span
            class="ml-auto bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-white/10 px-2 py-0.5 rounded-md text-[9px] font-mono text-slate-500"
          >
            {{ userPaymentMethods.length }}
          </span>
        </button>
      </div>

      <!-- Right Tab Content Panel -->
      <div class="lg:col-span-9">
        <!-- 1. PERSONAL INFORMATION TAB -->
        <div
          v-if="activeTab === 'info'"
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 p-6 sm:p-8 rounded-3xl shadow-2xl flex flex-col gap-6 animate-slide-up"
        >
          <div
            class="pb-4 border-b border-slate-200 dark:border-white/5 flex justify-between items-center"
          >
            <div>
              <h2 class="text-lg font-extrabold text-slate-900 dark:text-white">
                Personal Profile
              </h2>
              <p class="text-xs text-slate-500 mt-0.5">
                Manage your name, contact fields, and wallet balance.
              </p>
            </div>
            <!-- Dynamic E-Wallet Balance display badge -->
            <div class="flex flex-col items-end shrink-0">
              <span
                class="text-[8px] uppercase tracking-wider text-slate-500 font-bold"
                >E-Wallet Balance</span
              >
              <span
                class="text-sm font-black text-emerald-400 font-mono mt-0.5 bg-emerald-500/10 border border-emerald-500/20 px-2.5 py-1 rounded-xl"
              >
                {{ formatCurrency(userBalance) }}
              </span>
            </div>
          </div>

          <!-- Edit Profile Form Fields -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <div class="flex flex-col gap-1.5">
              <label
                for="profile-name"
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >Full Name</label
              >
              <InputText
                id="profile-name"
                v-model="profileForm.name"
                class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
                placeholder="Name"
              />
            </div>
            <div class="flex flex-col gap-1.5">
              <label
                for="profile-email"
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >Email Address</label
              >
              <InputText
                id="profile-email"
                v-model="profileForm.email"
                class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
                placeholder="Email address"
              />
            </div>
            <div class="flex flex-col gap-1.5 sm:col-span-2">
              <label
                for="profile-phone"
                class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >Phone Number</label
              >
              <InputText
                id="profile-phone"
                v-model="profileForm.phone"
                class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
                placeholder="Phone number"
              />
            </div>
          </div>

          <!-- Save Trigger button -->
          <div class="flex justify-end mt-4">
            <Button
              label="Save Changes"
              icon="pi pi-check"
              :loading="isSavingProfile"
              class="px-5 py-3 font-bold bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl hover:shadow-[0_0_20px_rgba(99,102,241,0.4)] text-slate-900 dark:text-white transition-all cursor-pointer"
              @click="handleSaveProfile"
            />
          </div>
        </div>

        <!-- 2. ADDRESS BOOK TAB -->
        <div
          v-if="activeTab === 'addresses'"
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 p-6 sm:p-8 rounded-3xl shadow-2xl flex flex-col gap-6 animate-slide-up"
        >
          <div
            class="pb-4 border-b border-slate-200 dark:border-white/5 flex justify-between items-center"
          >
            <div>
              <h2 class="text-lg font-extrabold text-slate-900 dark:text-white">Address Book</h2>
              <p class="text-xs text-slate-500 mt-0.5">
                Manage destinations for faster, automated shipping entries.
              </p>
            </div>
            <Button
              label="Add Address"
              icon="pi pi-plus"
              class="px-4 py-2 font-bold text-xs bg-indigo-500/10 hover:bg-indigo-500 border border-indigo-500/20 text-indigo-300 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white rounded-xl transition-all cursor-pointer"
              @click="openAddAddressDialog"
            />
          </div>

          <!-- Empty address state -->
          <div
            v-if="userAddresses.length === 0"
            class="flex flex-col items-center justify-center p-8 bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/30 border border-dashed border-slate-200 dark:border-white/5 rounded-2xl text-center"
          >
            <i class="pi pi-map-marker text-slate-600 text-2xl mb-2" />
            <span class="text-sm font-semibold text-slate-500 dark:text-slate-400"
              >No saved addresses found.</span
            >
            <p class="text-xs text-slate-600 mt-0.5">
              Add a delivery point to auto-fulfill checkouts.
            </p>
          </div>

          <!-- Addresses list -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="addr in userAddresses"
              :key="addr.id"
              class="bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex flex-col justify-between hover:border-indigo-500/20 transition-all duration-300 relative group"
            >
              <!-- Tags/Header row -->
              <div class="flex items-center gap-2 mb-3">
                <!-- Type Tag -->
                <span
                  class="px-2 py-0.5 text-[9px] font-bold uppercase rounded-md flex items-center gap-1"
                  :class="
                    addr.type === 'home'
                      ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
                      : 'bg-orange-500/10 text-orange-400 border border-orange-500/20'
                  "
                >
                  <i
                    :class="
                      addr.type === 'home' ? 'pi pi-home' : 'pi pi-briefcase'
                    "
                    class="text-[8px]"
                  />
                  {{ addr.type }}
                </span>
                <!-- Default Tag -->
                <span
                  v-if="addr.isDefault"
                  class="px-2 py-0.5 text-[9px] font-extrabold uppercase bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-md"
                >
                  Default
                </span>
              </div>

              <!-- Address summary -->
              <div
                class="flex flex-col text-xs font-semibold leading-relaxed mb-6"
              >
                <span class="text-slate-750 dark:text-slate-200 text-sm font-extrabold">{{
                  addr.name
                }}</span>
                <span class="text-slate-500 dark:text-slate-400 mt-1.5">{{ addr.address }}</span>
                <span class="text-slate-500 dark:text-slate-400"
                  >{{ addr.city }}, {{ addr.zip }}</span
                >
              </div>

              <!-- Action button bar -->
              <div
                class="flex items-center justify-between border-t border-slate-200 dark:border-white/5 pt-3 mt-auto"
              >
                <button
                  v-if="!addr.isDefault"
                  class="text-[10px] font-bold text-slate-500 hover:text-indigo-650 dark:text-indigo-400 transition-colors bg-transparent border-0 cursor-pointer"
                  @click="handleSetDefaultAddress(addr.id)"
                >
                  Set as Default
                </button>
                <span
                  v-else
                  class="text-[10px] font-bold text-emerald-500 flex items-center gap-1"
                >
                  <i class="pi pi-check-circle" /> Primary Address
                </span>

                <div class="flex items-center gap-3">
                  <button
                    class="text-slate-500 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white p-1 rounded transition-colors bg-transparent border-0 cursor-pointer"
                    title="Edit address"
                    @click="openEditAddressDialog(addr)"
                  >
                    <i class="pi pi-pencil text-xs" />
                  </button>
                  <button
                    class="text-slate-500 hover:text-rose-400 p-1 rounded transition-colors bg-transparent border-0 cursor-pointer"
                    title="Delete address"
                    @click="handleDeleteAddress(addr.id)"
                  >
                    <i class="pi pi-trash text-xs" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 3. PAYMENT METHODS TAB -->
        <div
          v-if="activeTab === 'payments'"
          class="bg-white dark:bg-slate-100 dark:bg-slate-900/30 backdrop-blur-md border border-slate-200 dark:border-white/5 p-6 sm:p-8 rounded-3xl shadow-2xl flex flex-col gap-6 animate-slide-up"
        >
          <div
            class="pb-4 border-b border-slate-200 dark:border-white/5 flex justify-between items-center"
          >
            <div>
              <h2 class="text-lg font-extrabold text-slate-900 dark:text-white">Payment Options</h2>
              <p class="text-xs text-slate-500 mt-0.5">
                Setup saved credit cards and active payment preferences.
              </p>
            </div>
            <Button
              label="Add Credit Card"
              icon="pi pi-plus"
              class="px-4 py-2 font-bold text-xs bg-indigo-500/10 hover:bg-indigo-500 border border-indigo-500/20 text-indigo-300 hover:text-slate-900 dark:hover:text-slate-900 dark:text-white rounded-xl transition-all cursor-pointer"
              @click="openAddPaymentDialog"
            />
          </div>

          <!-- Saved Payments list -->
          <div class="flex flex-col gap-4">
            <div
              v-for="pm in userPaymentMethods"
              :key="pm.id"
              class="bg-slate-50 dark:bg-slate-100 dark:bg-slate-950/40 border border-slate-200 dark:border-white/5 rounded-2xl p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:border-indigo-500/20 transition-all duration-300"
            >
              <!-- Card / Wallet metadata -->
              <div class="flex items-center gap-4">
                <!-- Method Logo Icon -->
                <div
                  class="w-12 h-12 rounded-xl bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-white/5 flex items-center justify-center text-indigo-650 dark:text-indigo-400"
                >
                  <i
                    :class="
                      pm.type === 'credit_card'
                        ? 'pi pi-credit-card'
                        : pm.type === 'shopbee_pay'
                          ? 'pi pi-bolt'
                          : pm.type === 'bank_transfer'
                            ? 'pi pi-building'
                            : 'pi pi-wallet'
                    "
                    class="text-lg"
                  />
                </div>

                <div class="flex flex-col">
                  <div class="flex items-center gap-2">
                    <span class="text-sm font-extrabold text-slate-900 dark:text-white">
                      {{
                        pm.type === 'credit_card'
                          ? 'Credit Card'
                          : pm.type === 'shopbee_pay'
                            ? 'Shopbee Pay (E-Wallet)'
                            : pm.type === 'bank_transfer'
                              ? 'Direct Bank Deposit'
                              : 'Cash on Delivery (COD)'
                      }}
                    </span>
                    <span
                      v-if="pm.isDefault"
                      class="px-2 py-0.5 text-[8px] font-extrabold uppercase bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-md"
                    >
                      Default
                    </span>
                  </div>
                  <!-- Subdetails -->
                  <span
                    class="text-[11px] font-semibold text-slate-500 font-mono mt-1"
                  >
                    {{
                      pm.type === 'credit_card'
                        ? `${formatCardNumber(pm.cardDetails?.cardNumber)} (${pm.cardDetails?.expiry})`
                        : pm.type === 'shopbee_pay'
                          ? `E-Wallet Balance: ${formatCurrency(userBalance)}`
                          : 'Direct integration details setup'
                    }}
                  </span>
                </div>
              </div>

              <!-- Controls -->
              <div
                class="flex items-center gap-4 pt-3 sm:pt-0 border-t sm:border-t-0 border-slate-200 dark:border-white/5 justify-between sm:justify-start"
              >
                <button
                  v-if="!pm.isDefault"
                  class="text-[10px] font-bold text-slate-500 hover:text-indigo-650 dark:text-indigo-400 transition-colors bg-transparent border-0 cursor-pointer"
                  @click="handleSetDefaultPayment(pm.id)"
                >
                  Set as Default
                </button>
                <span
                  v-else
                  class="text-[10px] font-bold text-emerald-500 flex items-center gap-1"
                >
                  <i class="pi pi-check-circle" /> Default Method
                </span>

                <!-- Delete card if card method -->
                <button
                  v-if="pm.type === 'credit_card'"
                  class="text-slate-500 hover:text-rose-400 p-2 rounded-xl transition-colors hover:bg-rose-500/10 bg-transparent border-0 cursor-pointer"
                  title="Remove payment option"
                  @click="handleDeletePayment(pm.id)"
                >
                  <i class="pi pi-trash text-xs" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- DIALOG: ADD/EDIT ADDRESS -->
    <Dialog
      v-model:visible="addressDialogVisible"
      modal
      dismissable-mask
      class="bg-slate-100 dark:bg-slate-950 text-slate-100 border border-slate-200 dark:border-white/10 max-w-md w-full mx-4 rounded-3xl overflow-hidden shadow-2xl"
      content-class="p-6"
      header-class="p-6 border-b border-slate-200 dark:border-white/5 bg-transparent text-slate-900 dark:text-white font-extrabold text-lg"
    >
      <template #header>
        <div class="flex items-center gap-2.5">
          <i class="pi pi-map-marker text-indigo-650 dark:text-indigo-400" />
          <span>{{
            isEditingAddress ? 'Edit Address' : 'Add New Address'
          }}</span>
        </div>
      </template>

      <div class="flex flex-col gap-4">
        <!-- Recipient name -->
        <div class="flex flex-col gap-1.5">
          <label
            for="addr-recipient"
            class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
            >Recipient Name</label
          >
          <InputText
            id="addr-recipient"
            v-model="addressForm.name"
            placeholder="e.g. Hannah Becker"
            class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
          />
        </div>

        <!-- Address -->
        <div class="flex flex-col gap-1.5">
          <label
            for="addr-street"
            class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
            >Street Address</label
          >
          <InputText
            id="addr-street"
            v-model="addressForm.address"
            placeholder="e.g. 128 Magnolia Dr"
            class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
          />
        </div>

        <!-- Grid: City + Postal Code -->
        <div class="grid grid-cols-2 gap-4">
          <div class="flex flex-col gap-1.5">
            <label
              for="addr-city"
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >City</label
            >
            <InputText
              id="addr-city"
              v-model="addressForm.city"
              placeholder="e.g. Cupertino"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
            />
          </div>
          <div class="flex flex-col gap-1.5">
            <label
              for="addr-zip"
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Postal Code</label
            >
            <InputText
              id="addr-zip"
              v-model="addressForm.zip"
              placeholder="e.g. 95014"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
            />
          </div>
        </div>

        <!-- Address Type selectors (Home or Office) -->
        <div class="flex flex-col gap-2">
          <label
            class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
            >Address Type</label
          >
          <div class="grid grid-cols-2 gap-3">
            <button
              type="button"
              class="flex items-center justify-center gap-2 p-3 rounded-2xl border transition-all duration-300 cursor-pointer focus:outline-none text-xs font-bold"
              :class="
                addressForm.type === 'home'
                  ? 'bg-indigo-500/10 border-indigo-500 text-slate-900 dark:text-white shadow-md'
                  : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
              "
              @click="addressForm.type = 'home'"
            >
              <i class="pi pi-home" /> Home
            </button>
            <button
              type="button"
              class="flex items-center justify-center gap-2 p-3 rounded-2xl border transition-all duration-300 cursor-pointer focus:outline-none text-xs font-bold"
              :class="
                addressForm.type === 'office'
                  ? 'bg-indigo-500/10 border-indigo-500 text-slate-900 dark:text-white shadow-md'
                  : 'bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-200 dark:border-white/10 hover:text-slate-750 dark:text-slate-200'
              "
              @click="addressForm.type = 'office'"
            >
              <i class="pi pi-briefcase" /> Office
            </button>
          </div>
        </div>

        <!-- Checkbox Default -->
        <div class="flex items-center gap-2.5 mt-2">
          <Checkbox
            v-model="addressForm.isDefault"
            binary
            input-id="addr-default"
            class="w-5 h-5 rounded-md border border-slate-200 dark:border-white/10 checked:bg-indigo-500 checked:border-indigo-500 focus:outline-none"
          />
          <label
            for="addr-default"
            class="text-xs font-bold text-slate-600 dark:text-slate-300 select-none cursor-pointer"
          >
            Set as default shipping address
          </label>
        </div>

        <!-- Save Button -->
        <Button
          label="Save Address"
          icon="pi pi-check"
          class="w-full py-3 mt-4 font-bold bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl hover:shadow-[0_0_15px_rgba(99,102,241,0.4)] text-slate-900 dark:text-white transition-all cursor-pointer"
          @click="handleSaveAddress"
        />
      </div>
    </Dialog>

    <!-- DIALOG: ADD CREDIT CARD -->
    <Dialog
      v-model:visible="paymentDialogVisible"
      modal
      dismissable-mask
      class="bg-slate-100 dark:bg-slate-950 text-slate-100 border border-slate-200 dark:border-white/10 max-w-md w-full mx-4 rounded-3xl overflow-hidden shadow-2xl"
      content-class="p-6"
      header-class="p-6 border-b border-slate-200 dark:border-white/5 bg-transparent text-slate-900 dark:text-white font-extrabold text-lg"
    >
      <template #header>
        <div class="flex items-center gap-2.5">
          <i class="pi pi-credit-card text-indigo-650 dark:text-indigo-400" />
          <span>Add Credit Card</span>
        </div>
      </template>

      <div class="flex flex-col gap-4">
        <!-- Visual Glassmorphic credit card preview -->
        <div
          class="w-full aspect-video rounded-2xl p-5 shadow-lg relative overflow-hidden flex flex-col justify-between text-slate-900 dark:text-white font-mono tracking-widest bg-linear-to-br from-indigo-600/70 via-indigo-750/70 to-purple-750/70 backdrop-blur-md border border-slate-200 dark:border-white/15 select-none mb-2"
        >
          <div class="relative z-10 flex justify-between items-center">
            <div class="flex items-center gap-1">
              <i class="pi pi-bolt text-indigo-650 dark:text-indigo-400 text-sm" />
              <span
                class="text-[9px] font-black tracking-normal uppercase text-slate-100"
                >Shopbee Pay</span
              >
            </div>
            <i class="pi pi-wifi text-slate-600 dark:text-slate-300 text-xs rotate-90" />
          </div>

          <div
            class="relative z-10 w-9 h-7 rounded-md bg-linear-to-r from-yellow-300 via-amber-400 to-yellow-600 border border-amber-300/30 opacity-80"
          />

          <div
            class="relative z-10 text-sm text-slate-100 font-bold text-center tracking-widest my-1"
          >
            {{ paymentForm.cardNumber || '•••• •••• •••• ••••' }}
          </div>

          <div class="relative z-10 flex justify-between items-end">
            <div class="flex flex-col min-w-0">
              <span
                class="text-[7px] text-indigo-300 font-bold uppercase tracking-wide"
                >Card Holder</span
              >
              <span
                class="text-[10px] font-black text-slate-100 truncate mt-0.5"
              >
                {{ paymentForm.cardName.toUpperCase() || 'YOUR NAME' }}
              </span>
            </div>
            <div class="flex flex-col shrink-0 text-right">
              <span
                class="text-[7px] text-indigo-300 font-bold uppercase tracking-wide"
                >Expires</span
              >
              <span class="text-[10px] font-black text-slate-100 mt-0.5">{{
                paymentForm.expiry || 'MM/YY'
              }}</span>
            </div>
          </div>
        </div>

        <!-- Input cardholder -->
        <div class="flex flex-col gap-1.5">
          <label
            for="card-holder-name"
            class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
            >Cardholder Name</label
          >
          <InputText
            id="card-holder-name"
            v-model="paymentForm.cardName"
            placeholder="e.g. Hannah Becker"
            class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
          />
        </div>

        <!-- Input Card Number -->
        <div class="flex flex-col gap-1.5">
          <label
            for="card-number"
            class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
            >Card Number</label
          >
          <InputText
            id="card-number"
            v-model="paymentForm.cardNumber"
            placeholder="4111 2222 3333 4444"
            class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
            @input="formatCardNumberInput"
          />
        </div>

        <!-- Expiry & CVC Grid -->
        <div class="grid grid-cols-2 gap-4">
          <div class="flex flex-col gap-1.5">
            <label
              for="card-expiry"
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >Expiration Date</label
            >
            <InputText
              id="card-expiry"
              v-model="paymentForm.expiry"
              placeholder="MM/YY"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
              @input="formatExpiryInput"
            />
          </div>
          <div class="flex flex-col gap-1.5">
            <label
              for="card-cvc"
              class="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >CVC Code</label
            >
            <InputText
              id="card-cvc"
              v-model="paymentForm.cvc"
              placeholder="123"
              class="w-full text-sm bg-slate-50 dark:bg-slate-100 dark:bg-slate-900/60 border-slate-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-slate-750 dark:text-slate-200 focus:border-indigo-500/50"
              @input="formatCvcInput"
            />
          </div>
        </div>

        <!-- Checkbox Default -->
        <div class="flex items-center gap-2.5 mt-2">
          <Checkbox
            v-model="paymentForm.isDefault"
            binary
            input-id="payment-default"
            class="w-5 h-5 rounded-md border border-slate-200 dark:border-white/10 checked:bg-indigo-500 checked:border-indigo-500 focus:outline-none"
          />
          <label
            for="payment-default"
            class="text-xs font-bold text-slate-600 dark:text-slate-300 select-none cursor-pointer"
          >
            Set as default payment method
          </label>
        </div>

        <!-- Save Button -->
        <Button
          label="Save Credit Card"
          icon="pi pi-check"
          class="w-full py-3 mt-4 font-bold bg-linear-to-r from-indigo-500 to-blue-600 border-0 rounded-xl hover:shadow-[0_0_15px_rgba(99,102,241,0.4)] text-slate-900 dark:text-white transition-all cursor-pointer"
          @click="handleSavePaymentMethod"
        />
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
:deep(.switcher-select.p-select) {
  background: rgba(255, 255, 255, 0.03) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 12px !important;
  padding: 6px 12px !important;
  height: 36px !important;
}

:deep(.switcher-select .p-select-label) {
  padding: 0 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  color: #cbd5e1 !important; /* slate-300 */
}

:deep(.switcher-select .p-select-dropdown) {
  color: #818cf8 !important; /* Indigo 400 */
}
</style>
