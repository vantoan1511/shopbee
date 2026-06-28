import { ref } from 'vue'

export interface OrderItem {
  productId: number
  name: string
  price: number
  quantity: number
  selectedVariants: Record<string, string>
  imageUrl: string
}

export interface ShippingDetails {
  name: string
  email: string
  address: string
  city: string
  zip: string
}

export interface OrderTimelineEvent {
  status:
    | 'placed'
    | 'processing'
    | 'shipped'
    | 'out_for_delivery'
    | 'delivered'
    | 'cancelled'
  title: string
  description: string
  timestamp: string
}

export interface Order {
  orderNumber: string
  datePlaced: string
  status:
    | 'placed'
    | 'processing'
    | 'shipped'
    | 'out_for_delivery'
    | 'delivered'
    | 'cancelled'
  shippingAddress: ShippingDetails
  items: OrderItem[]
  subtotal: number
  shippingFee: number
  tax: number
  discount: number
  totalPaid: number
  paymentMethod?: 'credit_card' | 'shopbee_pay' | 'bank_transfer' | 'cod'
  carrier?: string
  trackingNumber?: string
  timeline: OrderTimelineEvent[]
}

// Module-level persistent state (persists across pages in current tab session)
const orders = ref<Order[]>([])

export function useOrders() {
  const addOrder = async (order: Order) => {
    try {
      const response = await $fetch<{ success: boolean; order: Order }>(
        '/api/orders',
        {
          method: 'POST',
          body: order
        }
      )
      if (response && response.success) {
        const exists = orders.value.some(
          (o: Order) => o.orderNumber === order.orderNumber
        )
        if (!exists) {
          orders.value.push(response.order)
        }
        return true
      }
    } catch (e) {
      console.error('Failed to add order', e)
    }
    return false
  }

  const getOrderByNumber = (orderNumber: string) => {
    const cleanNumber = orderNumber.trim().toUpperCase()
    return orders.value.find(
      (o: Order) => o.orderNumber.toUpperCase() === cleanNumber
    )
  }

  const advanceOrderStatus = async (orderNumber: string) => {
    try {
      const response = await $fetch<{ success: boolean; order: Order }>(
        '/api/orders/advance',
        {
          method: 'POST',
          body: { orderNumber }
        }
      )
      if (response && response.success) {
        const index = orders.value.findIndex(
          (o: Order) => o.orderNumber === orderNumber
        )
        if (index !== -1) {
          orders.value[index] = response.order
        }
        return true
      }
    } catch (e) {
      console.error('Failed to advance order status', e)
    }
    return false
  }

  return {
    orders,
    addOrder,
    getOrderByNumber,
    advanceOrderStatus
  }
}
