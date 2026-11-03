import type { OrderStatus } from '../order/types'

export type AdminOrder = {
  orderNumber: string
  status: OrderStatus
  totalAmount: number
  orderedAt: string
  title: string
  itemCount: number
  memberEmail: string
  memberName: string
  recipient: string
  phone: string
  timeDeal: boolean
}

export type AdminOrderSearchParams = {
  status?: OrderStatus
  from: string
  to: string
  keyword?: string
  page: number
  size: number
}

// 관리자가 바꿀 수 있는 상태 (서버의 isAdminProgressTarget과 같은 목록)
export type AdminProgressStatus = 'PREPARING' | 'SHIPPING' | 'DELIVERED'

export type BulkStatusResult = {
  successCount: number
  failures: { orderNumber: string; reason: string }[]
}