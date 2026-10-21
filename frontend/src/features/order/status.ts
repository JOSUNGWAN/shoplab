import type { OrderStatus } from './types'

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING_PAYMENT: '결제 대기',
  PAID: '결제 완료',
  PREPARING: '배송 준비',
  SHIPPING: '배송 중',
  DELIVERED: '배송 완료',
  CANCELED: '취소',
}

// 백엔드 OrderStatus.isCancelableByMember()와 같은 규칙
export function isCancelable(status: OrderStatus) {
  return status === 'PENDING_PAYMENT' || status === 'PAID'
}