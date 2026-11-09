import type { AdminProductStatus } from './types'

export const PRODUCT_STATUS_LABEL: Record<AdminProductStatus, string> = {
  ON_SALE: '판매 중',
  SOLD_OUT: '품절',
  HIDDEN: '숨김',
}

export const PRODUCT_STATUS_OPTIONS = (Object.keys(PRODUCT_STATUS_LABEL) as AdminProductStatus[]).map((value) => ({
  value,
  label: PRODUCT_STATUS_LABEL[value],
}))

export const ADMIN_PRODUCT_PAGE_SIZE = 20