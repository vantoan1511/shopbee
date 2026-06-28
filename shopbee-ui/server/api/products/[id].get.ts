import { mockProducts } from '../../utils/db'

export default defineEventHandler((event) => {
  const idStr = event.context.params?.id
  if (!idStr) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Product ID is required'
    })
  }
  const id = parseInt(idStr, 10)
  const product = mockProducts.find((p) => p.id === id)
  if (!product) {
    throw createError({
      statusCode: 404,
      statusMessage: `Product with ID ${id} not found`
    })
  }
  return product
})
