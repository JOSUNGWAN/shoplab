import { ORDER_STATUS_LABEL } from '../status'
import type { OrderStatus } from '../types'

const COLOR: Record<OrderStatus, string> = {
  PENDING_PAYMENT: 'bg-yellow-50 text-yellow-700',
  PAID: 'bg-blue-50 text-blue-700',
  PREPARING: 'bg-blue-50 text-blue-700',
  SHIPPING: 'bg-indigo-50 text-indigo-700',
  DELIVERED: 'bg-green-50 text-green-700',
  CANCELED: 'bg-gray-100 text-gray-500',
}

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${COLOR[status]}`}>
      {ORDER_STATUS_LABEL[status]}
    </span>
  )
}
