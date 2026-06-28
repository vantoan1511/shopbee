import { initialReviews, mockProducts } from '../utils/db'
import type { Review } from '../../app/composables/useProducts'

export default defineEventHandler(async (event) => {
  const body = await readBody(event)
  const { productId, author, rating, title, comment } = body || {}

  if (!productId || !author || rating === undefined || !title || !comment) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Missing required review fields'
    })
  }

  const parsedProductId = parseInt(productId, 10)
  const parsedRating = parseInt(rating, 10)

  const nextId =
    initialReviews.length > 0
      ? Math.max(...initialReviews.map((r) => r.id)) + 1
      : 1

  const newReview: Review = {
    id: nextId,
    productId: parsedProductId,
    author,
    rating: parsedRating,
    title,
    comment,
    date: new Date().toISOString().slice(0, 10),
    verified: false
  }

  initialReviews.push(newReview)

  // Recalculate product average rating and count
  const productReviews = initialReviews.filter(
    (r) => r.productId === parsedProductId
  )
  const total = productReviews.length
  const sum = productReviews.reduce((acc, r) => acc + r.rating, 0)
  const product = mockProducts.find((p) => p.id === parsedProductId)
  if (product) {
    product.rating = parseFloat((sum / total).toFixed(1))
    product.reviewsCount = total
  }

  return { success: true, review: newReview, product }
})
