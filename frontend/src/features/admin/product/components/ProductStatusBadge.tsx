import { PRODUCT_STATUS_LABEL } from '../constants'
import type { AdminProductStatus } from '../types'

const STYLE: Record<AdminProductStatus, string> = {
  ON_SALE: 'bg-green-100 text-green-700',
  SOLD_OUT: 'bg-orange-100 text-orange-700',
  HIDDEN: 'bg-gray-200 text-gray-600',
}

export default function ProductStatusBadge({ status }: { status: AdminProductStatus }) {
  return (
    <span className={`rounded px-2 py-0.5 text-xs font-semibold whitespace-nowrap ${STYLE[status]}`}>
      {PRODUCT_STATUS_LABEL[status]}
    </span>
  )
}
