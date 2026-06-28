import { ref, computed } from 'vue'

export interface Review {
  id: number
  productId: number
  author: string
  rating: number
  title: string
  comment: string
  date: string
  verified: boolean
}

export interface ProductVariant {
  name: string
  options: string[]
}

export interface Product {
  id: number
  name: string
  description: string
  price: number
  sku: string
  active: boolean
  brand: string
  category: string
  stock: number
  imageUrl: string
  images: string[]
  rating: number
  reviewsCount: number
  features: string[]
  specs: Record<string, string>
  variants?: ProductVariant[]
}

export interface Voucher {
  id: string
  code: string
  title: string
  description: string
  type: 'platform' | 'merchant' | 'event'
  discountType: 'percentage' | 'fixed'
  discountValue: number
  minSubtotal: number
  merchantBrand?: string
  collected: boolean
  colorClass: string
  expiryDate: string
}

export interface CartItem {
  id: string
  product: Product
  quantity: number
  selectedVariants: Record<string, string>
  selectedForCheckout: boolean
}

// Module-level refs for shared singleton state matching original design
const products = ref<Product[]>([])
const vouchersList = ref<Voucher[]>([])
const reviewsList = ref<Review[]>([])
const appliedVoucher = ref<Voucher | null>(null)
const cart = ref<CartItem[]>([])

const searchQuery = ref('')
const selectedBrands = ref<string[]>([])
const selectedCategories = ref<string[]>([])
const selectedFeatures = ref<string[]>([])
const priceRange = ref<[number, number]>([0, 1500])
const onlyInStock = ref(false)
const sortBy = ref('featured')

export function useProducts() {
  const cartCount = computed(() => {
    return cart.value.reduce(
      (sum: number, item: CartItem) => sum + item.quantity,
      0
    )
  })

  const getDefaultVariants = (product: Product): Record<string, string> => {
    const defaults: Record<string, string> = {}
    if (product.variants) {
      for (const v of product.variants) {
        if (v.options && v.options.length > 0) {
          const firstOpt = v.options[0]
          if (firstOpt) {
            defaults[v.name] = firstOpt
          }
        }
      }
    }
    return defaults
  }

  const addToCart = (
    product: Product,
    quantity = 1,
    selectedVariants?: Record<string, string>
  ) => {
    if (product.stock === 0) return false

    const variants = selectedVariants || getDefaultVariants(product)
    const cartItemId = `${product.id}-${Object.entries(variants)
      .map(([k, v]) => `${k}:${v}`)
      .sort()
      .join(',')}`

    const totalQtyInCart = cart.value
      .filter((item: CartItem) => item.product.id === product.id)
      .reduce((sum: number, item: CartItem) => sum + item.quantity, 0)

    if (totalQtyInCart + quantity > product.stock) {
      return false
    }

    const existing = cart.value.find((item: CartItem) => item.id === cartItemId)
    if (existing) {
      existing.quantity += quantity
      return true
    } else {
      cart.value.push({
        id: cartItemId,
        product,
        quantity,
        selectedVariants: variants,
        selectedForCheckout: true
      })
      return true
    }
  }

  const removeFromCart = (cartItemId: string) => {
    const index = cart.value.findIndex(
      (item: CartItem) => item.id === cartItemId
    )
    if (index !== -1) {
      cart.value.splice(index, 1)
      return true
    }
    return false
  }

  const updateCartItemQuantity = (cartItemId: string, quantity: number) => {
    const item = cart.value.find((i: CartItem) => i.id === cartItemId)
    if (!item) return false

    const otherQtyInCart = cart.value
      .filter(
        (i: CartItem) => i.product.id === item.product.id && i.id !== cartItemId
      )
      .reduce((sum: number, i: CartItem) => sum + i.quantity, 0)

    if (otherQtyInCart + quantity > item.product.stock) {
      return false
    }

    item.quantity = Math.max(1, quantity)
    return true
  }

  const updateCartItemVariant = (
    cartItemId: string,
    newVariants: Record<string, string>
  ) => {
    const index = cart.value.findIndex(
      (item: CartItem) => item.id === cartItemId
    )
    if (index === -1) return false

    const item = cart.value[index]
    if (!item) return false
    const product = item.product

    const newCartItemId = `${product.id}-${Object.entries(newVariants)
      .map(([k, v]) => `${k}:${v}`)
      .sort()
      .join(',')}`

    if (newCartItemId === cartItemId) {
      item.selectedVariants = newVariants
      return true
    }

    const existingIndex = cart.value.findIndex(
      (i: CartItem) => i.id === newCartItemId
    )
    if (existingIndex !== -1) {
      const existingItem = cart.value[existingIndex]
      if (!existingItem) return false
      const totalQty = existingItem.quantity + item.quantity
      existingItem.quantity = Math.min(totalQty, product.stock)
      cart.value.splice(index, 1)
    } else {
      item.id = newCartItemId
      item.selectedVariants = newVariants
    }
    return true
  }

  const toggleCartItemSelection = (cartItemId: string) => {
    const item = cart.value.find((i: CartItem) => i.id === cartItemId)
    if (item) {
      item.selectedForCheckout = !item.selectedForCheckout
    }
  }

  const selectAllCartItems = (selected: boolean) => {
    cart.value.forEach((item: CartItem) => {
      item.selectedForCheckout = selected
    })
  }

  const checkoutItems = computed(() => {
    return cart.value.filter((item: CartItem) => item.selectedForCheckout)
  })

  const cartSubtotal = computed(() => {
    return checkoutItems.value.reduce(
      (sum: number, item: CartItem) => sum + item.product.price * item.quantity,
      0
    )
  })

  const cartDiscount = computed(() => {
    if (!appliedVoucher.value) return 0
    const voucher = appliedVoucher.value

    if (cartSubtotal.value < voucher.minSubtotal) {
      return 0
    }

    if (voucher.type === 'platform' || voucher.type === 'event') {
      if (voucher.discountType === 'percentage') {
        const discount = cartSubtotal.value * (voucher.discountValue / 100)
        if (voucher.code === 'LUCKYDRAW99') {
          return Math.min(discount, 100)
        }
        return discount
      } else {
        return Math.min(voucher.discountValue, cartSubtotal.value)
      }
    } else if (voucher.type === 'merchant' && voucher.merchantBrand) {
      const merchantItemsSubtotal = checkoutItems.value
        .filter(
          (item: CartItem) => item.product.brand === voucher.merchantBrand
        )
        .reduce(
          (sum: number, item: CartItem) =>
            sum + item.product.price * item.quantity,
          0
        )

      if (voucher.discountType === 'percentage') {
        return merchantItemsSubtotal * (voucher.discountValue / 100)
      } else {
        return Math.min(voucher.discountValue, merchantItemsSubtotal)
      }
    }
    return 0
  })

  const cartTaxes = computed(() => {
    const taxableAmount = Math.max(0, cartSubtotal.value - cartDiscount.value)
    return taxableAmount * 0.08
  })

  const cartShipping = computed(() => {
    if (cartSubtotal.value === 0) return 0
    return cartSubtotal.value > 200 ? 0 : 15.0
  })

  const cartTotal = computed(() => {
    return Math.max(
      0,
      cartSubtotal.value -
        cartDiscount.value +
        cartTaxes.value +
        cartShipping.value
    )
  })

  const categories = computed(() => {
    return Array.from(new Set(products.value.map((p: Product) => p.category)))
  })

  const brands = computed(() => {
    return Array.from(new Set(products.value.map((p: Product) => p.brand)))
  })

  const allFeatures = [
    '5G',
    'ANC',
    'Waterproof',
    'Wireless',
    'Touch Control',
    '4K Display'
  ]

  const filteredProducts = computed(() => {
    let result = [...products.value]

    if (searchQuery.value.trim()) {
      const query = searchQuery.value.toLowerCase().trim()
      result = result.filter(
        (p) =>
          p.name.toLowerCase().includes(query) ||
          p.description.toLowerCase().includes(query)
      )
    }

    if (selectedCategories.value.length > 0) {
      result = result.filter((p) =>
        selectedCategories.value.includes(p.category)
      )
    }

    if (selectedBrands.value.length > 0) {
      result = result.filter((p) => selectedBrands.value.includes(p.brand))
    }

    if (selectedFeatures.value.length > 0) {
      result = result.filter((p) =>
        selectedFeatures.value.every((f: string) => p.features.includes(f))
      )
    }

    result = result.filter(
      (p) => p.price >= priceRange.value[0] && p.price <= priceRange.value[1]
    )

    if (onlyInStock.value) {
      result = result.filter((p) => p.stock > 0)
    }

    if (sortBy.value === 'price-asc') {
      result.sort((a, b) => a.price - b.price)
    } else if (sortBy.value === 'price-desc') {
      result.sort((a, b) => b.price - a.price)
    } else if (sortBy.value === 'rating') {
      result.sort((a, b) => b.rating - a.rating)
    }

    return result
  })

  const resetFilters = () => {
    searchQuery.value = ''
    selectedBrands.value = []
    selectedCategories.value = []
    selectedFeatures.value = []
    priceRange.value = [0, 1500]
    onlyInStock.value = false
    sortBy.value = 'featured'
  }

  const getProductById = (id: number | string) => {
    const numericId = typeof id === 'string' ? parseInt(id, 10) : id
    return products.value.find((p: Product) => p.id === numericId)
  }

  const collectVoucher = async (id: string) => {
    try {
      const response = await $fetch<{ success: boolean; voucher: Voucher }>(
        '/api/vouchers/collect',
        {
          method: 'POST',
          body: { id }
        }
      )
      if (response && response.success) {
        const v = vouchersList.value.find((item: Voucher) => item.id === id)
        if (v) {
          v.collected = true
        }
        return true
      }
    } catch (e) {
      console.error('Failed to collect voucher', e)
    }
    return false
  }

  const applyVoucher = (code: string) => {
    const cleanCode = code.toUpperCase().trim()
    const v = vouchersList.value.find(
      (item: Voucher) => item.code.toUpperCase() === cleanCode
    )
    if (!v) {
      throw new Error('Voucher code does not exist.')
    }
    if (!v.collected) {
      throw new Error(
        'You have not collected this voucher yet. Please collect it first!'
      )
    }
    if (cartSubtotal.value < v.minSubtotal) {
      throw new Error(
        `Minimum purchase of $${v.minSubtotal} is required to apply this voucher.`
      )
    }
    if (v.type === 'merchant' && v.merchantBrand) {
      const brandInCart = checkoutItems.value.some(
        (item: CartItem) => item.product.brand === v.merchantBrand
      )
      if (!brandInCart) {
        throw new Error(
          `This voucher only applies to products from ${v.merchantBrand}.`
        )
      }
    }
    appliedVoucher.value = v
    return true
  }

  const removeVoucher = () => {
    appliedVoucher.value = null
  }

  const addReview = async (
    productId: number,
    data: { author: string; rating: number; title: string; comment: string }
  ) => {
    try {
      const response = await $fetch<{
        success: boolean
        review: Review
        product: Product
      }>('/api/reviews', {
        method: 'POST',
        body: { productId, ...data }
      })
      if (response && response.success) {
        reviewsList.value.push(response.review)
        const product = products.value.find((p: Product) => p.id === productId)
        if (product && response.product) {
          product.rating = response.product.rating
          product.reviewsCount = response.product.reviewsCount
        }
      }
    } catch (e) {
      console.error('Failed to add review', e)
    }
  }

  return {
    products,
    categories,
    brands,
    allFeatures,
    searchQuery,
    selectedBrands,
    selectedCategories,
    selectedFeatures,
    priceRange,
    onlyInStock,
    sortBy,
    filteredProducts,
    cart,
    cartCount,
    addToCart,
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
    resetFilters,
    getProductById,
    reviewsList,
    addReview,
    vouchersList,
    appliedVoucher,
    cartDiscount,
    collectVoucher,
    applyVoucher,
    removeVoucher
  }
}
