import { initialOrders } from '../utils/db'
import type { Order } from '../../app/composables/useOrders'

export default defineEventHandler(async (event) => {
  const order = await readBody<Order>(event)
  if (!order || !order.orderNumber) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid order data'
    })
  }

  const exists = initialOrders.some((o) => o.orderNumber === order.orderNumber)
  if (!exists) {
    initialOrders.push(order)
  }

  return { success: true, order }
})
