import type { OrderStatus } from '../order/types'
import type { AdminProgressStatus } from './types'

export const ORDER_STATUS_OPTIONS: { value: OrderStatus; label: string }[] = [
  { value: 'PENDING_PAYMENT', label: '결제 대기' },
  { value: 'PAID', label: '결제 완료' },
  { value: 'PREPARING', label: '배송 준비' },
  { value: 'SHIPPING', label: '배송 중' },
  { value: 'DELIVERED', label: '배송 완료' },
  { value: 'CANCELED', label: '취소' },
]

export const PROGRESS_STATUS_OPTIONS: { value: AdminProgressStatus; label: string }[] = [
  { value: 'PREPARING', label: '배송 준비' },
  { value: 'SHIPPING', label: '배송 중' },
  { value: 'DELIVERED', label: '배송 완료' },
]

export const ADMIN_ORDER_PAGE_SIZE = 20