import { ADMIN_PRODUCT_PAGE_SIZE, PRODUCT_STATUS_LABEL } from './constants'
import type { AdminProductSearchParams, AdminProductStatus } from './types'

export type AdminProductFilter = Pick<AdminProductSearchParams, 'keyword' | 'categoryId' | 'status'>

function isProductStatus(value: string | null): value is AdminProductStatus {
  return value !== null && value in PRODUCT_STATUS_LABEL
}

export function parseAdminProductParams(searchParams: URLSearchParams): AdminProductSearchParams {
  const categoryId = Number(searchParams.get('categoryId'))
  const status = searchParams.get('status')
  const page = Number(searchParams.get('page'))

  return {
    keyword: searchParams.get('keyword')?.trim() || undefined,
    categoryId: Number.isInteger(categoryId) && categoryId > 0 ? categoryId : undefined,
    status: isProductStatus(status) ? status : undefined,
    page: Number.isInteger(page) && page > 0 ? page : 0,
    size: ADMIN_PRODUCT_PAGE_SIZE,
  }
}

export function toAdminProductSearchParams(filter: AdminProductFilter): URLSearchParams {
  const next = new URLSearchParams()
  if (filter.keyword) next.set('keyword', filter.keyword)
  if (filter.categoryId) next.set('categoryId', String(filter.categoryId))
  if (filter.status) next.set('status', filter.status)
  return next
}