import type { OrderStatus } from '../order/types'
import { addDays, toDateInputValue } from '../../lib/date'
import { ADMIN_ORDER_PAGE_SIZE, ORDER_STATUS_OPTIONS } from './constants'
import type { AdminOrderSearchParams } from './types'

export type AdminOrderFilter = Pick<AdminOrderSearchParams, 'status' | 'from' | 'to' | 'keyword'>

const STATUS_VALUES = ORDER_STATUS_OPTIONS.map((option) => option.value)

function isOrderStatus(value: string | null): value is OrderStatus {
  return value !== null && STATUS_VALUES.includes(value as OrderStatus)
}

/** URL 쿼리 → 검색 조건 (값이 없거나 이상하면 기본값: 최근 30일, 전체 상태, 첫 페이지) */
export function parseAdminOrderParams(searchParams: URLSearchParams): AdminOrderSearchParams {
  const today = new Date()
  const status = searchParams.get('status')
  const page = Number(searchParams.get('page'))

  return {
    status: isOrderStatus(status) ? status : undefined,
    from: searchParams.get('from') || toDateInputValue(addDays(today, -30)),
    to: searchParams.get('to') || toDateInputValue(today),
    keyword: searchParams.get('keyword')?.trim() || undefined,
    page: Number.isInteger(page) && page > 0 ? page : 0,
    size: ADMIN_ORDER_PAGE_SIZE,
  }
}

/** 검색 조건 → URL 쿼리 (새로 검색하면 페이지는 처음부터) */
export function toAdminOrderSearchParams(filter: AdminOrderFilter): URLSearchParams {
  const next = new URLSearchParams()
  if (filter.status) next.set('status', filter.status)
  next.set('from', filter.from)
  next.set('to', filter.to)
  if (filter.keyword) next.set('keyword', filter.keyword)
  return next
}