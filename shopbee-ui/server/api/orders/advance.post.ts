import { initialOrders } from '../../utils/db'
import type { Order } from '../../../app/composables/useOrders'

export default defineEventHandler(async (event) => {
  const body = await readBody(event)
  const orderNumber = body?.orderNumber

  if (!orderNumber) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Order number is required'
    })
  }

  const cleanNumber = orderNumber.trim().toUpperCase()
  const order = initialOrders.find(
    (o) => o.orderNumber.toUpperCase() === cleanNumber
  )

  if (!order) {
    throw createError({
      statusCode: 404,
      statusMessage: `Order ${orderNumber} not found`
    })
  }

  const orderStatuses: Order['status'][] = [
    'placed',
    'processing',
    'shipped',
    'out_for_delivery',
    'delivered'
  ]
  const currentIndex = orderStatuses.indexOf(order.status)
  if (currentIndex === -1 || currentIndex === orderStatuses.length - 1) {
    return {
      success: false,
      message: 'Cannot advance order status any further'
    }
  }

  const nextStatus = orderStatuses[currentIndex + 1]
  if (!nextStatus) {
    return { success: false, message: 'Invalid next status' }
  }

  order.status = nextStatus

  // Set mock tracking numbers if shipped
  if (nextStatus === 'shipped' && !order.trackingNumber) {
    order.carrier = 'FedEx'
    order.trackingNumber = `FX-${Math.floor(100000000 + Math.random() * 900000000)}`
  }

  // Add a status change event to timeline
  const now = new Date()
  const timeStr = `${now.toISOString().slice(0, 10)} ${now.toTimeString().slice(0, 5)}`

  let title = ''
  let description = ''

  switch (nextStatus) {
    case 'processing':
      title = 'Payment Confirmed & Processing'
      description =
        'Warehouse is packaging items and scheduling courier dispatch.'
      break
    case 'shipped':
      title = 'Dispatched from Warehouse'
      description = `Package shipped via ${order.carrier}. Tracking number: ${order.trackingNumber}.`
      break
    case 'out_for_delivery':
      title = 'Out for Delivery'
      description = 'Local courier driver has loaded package for dispatch.'
      break
    case 'delivered':
      title = 'Delivered'
      description = 'Package delivered successfully to recipient address.'
      break
  }

  order.timeline.push({
    status: nextStatus,
    title,
    description,
    timestamp: timeStr
  })

  return { success: true, order }
})
