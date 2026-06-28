import { initialReviews } from '../utils/db'

export default defineEventHandler((event) => {
  const query = getQuery(event)
  const productIdStr = query.productId
  if (productIdStr) {
    const productId = parseInt(productIdStr as string, 10)
    return initialReviews.filter((r) => r.productId === productId)
  }
  return initialReviews
})
