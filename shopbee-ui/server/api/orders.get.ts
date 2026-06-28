import { initialOrders } from '../utils/db'

export default defineEventHandler(() => {
  return initialOrders
})
