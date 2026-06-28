import { ref, computed, watch } from 'vue'

export interface UserProfile {
  name: string
  email: string
  phone: string
}

export interface UserAddress {
  id: string
  name: string
  address: string
  city: string
  zip: string
  type: 'home' | 'office'
  isDefault: boolean
}

export interface UserPaymentMethod {
  id: string
  type: 'credit_card' | 'shopbee_pay' | 'bank_transfer' | 'cod'
  cardDetails?: {
    cardNumber: string
    cardName: string
    expiry: string
    cvc: string
  }
  isDefault: boolean
}

export interface UserAccount {
  id: string
  balance: number
  profile: UserProfile
  addresses: UserAddress[]
  paymentMethods: UserPaymentMethod[]
}

// Global ref singleton state
const accounts = ref<UserAccount[]>([])
const activeAccountId = ref<string>('user-1')
let hasHydrated = false

interface SimpleStorage {
  getItem(key: string): string | null
  setItem(key: string, value: string): void
}

const getLocalStorage = (): SimpleStorage | undefined => {
  if (typeof globalThis !== 'undefined' && 'localStorage' in globalThis) {
    return (globalThis as unknown as { localStorage: SimpleStorage })
      .localStorage
  }
  return undefined
}

const hasWindow = (): boolean => {
  return typeof globalThis !== 'undefined' && 'window' in globalThis
}

// Sync with localStorage
const loadState = async () => {
  const storage = getLocalStorage()
  if (!storage || hasHydrated) return

  const savedAccounts = storage.getItem('shopbee_accounts')
  if (savedAccounts) {
    try {
      accounts.value = JSON.parse(savedAccounts)
    } catch (e) {
      console.error('Failed to parse saved accounts from local storage', e)
    }
  } else {
    try {
      const serverAccounts = await $fetch<UserAccount[]>('/api/accounts')
      if (serverAccounts) {
        accounts.value = serverAccounts
        storage.setItem('shopbee_accounts', JSON.stringify(serverAccounts))
      }
    } catch (e) {
      console.error('Failed to fetch fallback accounts from server', e)
    }
  }

  const savedActiveId = storage.getItem('shopbee_active_account_id')
  if (savedActiveId) {
    activeAccountId.value = savedActiveId
  } else if (accounts.value.length > 0 && accounts.value[0]) {
    activeAccountId.value = accounts.value[0].id
  }

  hasHydrated = true
}

const saveState = () => {
  const storage = getLocalStorage()
  if (!storage) return
  storage.setItem('shopbee_accounts', JSON.stringify(accounts.value))
  storage.setItem('shopbee_active_account_id', activeAccountId.value)
}

// Set up single listeners for changes
if (hasWindow()) {
  watch(accounts, saveState, { deep: true })
  watch(activeAccountId, saveState)
}

export function useUserProfile() {
  // Trigger hydration client-side
  if (hasWindow() && !hasHydrated) {
    loadState()
  }

  const activeAccount = computed(() => {
    return (accounts.value.find((acc) => acc.id === activeAccountId.value) ||
      accounts.value[0] || {
        id: 'user-1',
        balance: 1500.0,
        profile: {
          name: 'Hannah Becker',
          email: 'hannah@example.com',
          phone: '+1 (555) 019-2834'
        },
        addresses: [
          {
            id: 'addr-1-1',
            name: 'Hannah Becker',
            address: '128 Magnolia Dr',
            city: 'Cupertino',
            zip: '95014',
            type: 'home',
            isDefault: true
          }
        ],
        paymentMethods: [
          {
            id: 'pay-1-1',
            type: 'credit_card',
            cardDetails: {
              cardNumber: '4111 2222 3333 4444',
              cardName: 'Hannah Becker',
              expiry: '12/29',
              cvc: '123'
            },
            isDefault: true
          }
        ]
      }) as UserAccount
  })

  const userProfile = computed(() => activeAccount.value.profile)
  const userAddresses = computed(() => activeAccount.value.addresses)
  const userPaymentMethods = computed(() => activeAccount.value.paymentMethods)
  const userBalance = computed(() => activeAccount.value.balance)

  const switchAccount = (id: string) => {
    const exists = accounts.value.some((acc) => acc.id === id)
    if (exists) {
      activeAccountId.value = id
    }
  }

  const updateProfile = (updated: Partial<UserProfile>) => {
    if (activeAccount.value) {
      activeAccount.value.profile = {
        ...activeAccount.value.profile,
        ...updated
      }
    }
  }

  const addAddress = (address: Omit<UserAddress, 'id'>) => {
    const newAddress: UserAddress = {
      ...address,
      id: `addr-${Date.now()}-${Math.floor(Math.random() * 1000)}`
    }

    if (newAddress.isDefault) {
      activeAccount.value.addresses.forEach((addr) => {
        addr.isDefault = false
      })
    }

    activeAccount.value.addresses.push(newAddress)
  }

  const updateAddress = (id: string, updated: Partial<UserAddress>) => {
    const address = activeAccount.value.addresses.find((addr) => addr.id === id)
    if (address) {
      if (updated.isDefault) {
        activeAccount.value.addresses.forEach((addr) => {
          if (addr.id !== id) addr.isDefault = false
        })
      }
      Object.assign(address, updated)
    }
  }

  const deleteAddress = (id: string) => {
    const index = activeAccount.value.addresses.findIndex(
      (addr) => addr.id === id
    )
    if (index !== -1) {
      const wasDefault = activeAccount.value.addresses[index]?.isDefault
      activeAccount.value.addresses.splice(index, 1)

      // If we deleted the default address and have other addresses left, make the first one default
      if (wasDefault && activeAccount.value.addresses.length > 0) {
        const first = activeAccount.value.addresses[0]
        if (first) {
          first.isDefault = true
        }
      }
    }
  }

  const setDefaultAddress = (id: string) => {
    activeAccount.value.addresses.forEach((addr) => {
      addr.isDefault = addr.id === id
    })
  }

  const addPaymentMethod = (method: Omit<UserPaymentMethod, 'id'>) => {
    const newMethod: UserPaymentMethod = {
      ...method,
      id: `pay-${Date.now()}-${Math.floor(Math.random() * 1000)}`
    }

    if (newMethod.isDefault) {
      activeAccount.value.paymentMethods.forEach((pm) => {
        pm.isDefault = false
      })
    }

    activeAccount.value.paymentMethods.push(newMethod)
  }

  const updatePaymentMethod = (
    id: string,
    updated: Partial<UserPaymentMethod>
  ) => {
    const method = activeAccount.value.paymentMethods.find((pm) => pm.id === id)
    if (method) {
      if (updated.isDefault) {
        activeAccount.value.paymentMethods.forEach((pm) => {
          if (pm.id !== id) pm.isDefault = false
        })
      }
      Object.assign(method, updated)
    }
  }

  const deletePaymentMethod = (id: string) => {
    const index = activeAccount.value.paymentMethods.findIndex(
      (pm) => pm.id === id
    )
    if (index !== -1) {
      const wasDefault = activeAccount.value.paymentMethods[index]?.isDefault
      activeAccount.value.paymentMethods.splice(index, 1)

      // Auto promote first remaining method if we deleted the default one
      if (wasDefault && activeAccount.value.paymentMethods.length > 0) {
        const first = activeAccount.value.paymentMethods[0]
        if (first) {
          first.isDefault = true
        }
      }
    }
  }

  const setDefaultPaymentMethod = (id: string) => {
    activeAccount.value.paymentMethods.forEach((pm) => {
      pm.isDefault = pm.id === id
    })
  }

  const deductBalance = (amount: number) => {
    if (activeAccount.value) {
      activeAccount.value.balance = Math.max(
        0,
        activeAccount.value.balance - amount
      )
    }
  }

  return {
    accounts,
    activeAccountId,
    activeAccount,
    userProfile,
    userAddresses,
    userPaymentMethods,
    userBalance,
    switchAccount,
    updateProfile,
    addAddress,
    updateAddress,
    deleteAddress,
    setDefaultAddress,
    addPaymentMethod,
    updatePaymentMethod,
    deletePaymentMethod,
    setDefaultPaymentMethod,
    deductBalance
  }
}
