import { vouchersList } from '../../utils/db'

export default defineEventHandler(async (event) => {
  const body = await readBody(event)
  const id = body?.id
  if (!id) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Voucher ID is required'
    })
  }
  const voucher = vouchersList.find((v) => v.id === id)
  if (!voucher) {
    throw createError({
      statusCode: 404,
      statusMessage: `Voucher with ID ${id} not found`
    })
  }
  voucher.collected = true
  return { success: true, voucher }
})
