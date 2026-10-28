import { Link } from 'react-router'
import { useNow } from '../../../hooks/useNow'
import { formatDuration, formatPrice } from '../../../lib/format'
import { countdownTarget, currentStatus, TIME_DEAL_STATUS_LABEL } from '../status'
import type { TimeDeal } from '../types'

export function TimeDealCard({ deal }: { deal: TimeDeal }) {
  const now = useNow()
  const status = currentStatus(deal, now)
  const target = countdownTarget(deal, status)
  const soldCount = deal.totalQuantity - deal.remainingQuantity
  const soldPercent = Math.round((soldCount / deal.totalQuantity) * 100)
  const inactive = status === 'SOLD_OUT' || status === 'ENDED'

  return (
    <Link to={`/time-deals/${deal.id}`} className="group block">
      <div className="relative aspect-square overflow-hidden rounded-lg bg-gray-100">
        {deal.thumbnailUrl && (
          <img
            src={deal.thumbnailUrl}
            alt={deal.productName}
            loading="lazy"
            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
          />
        )}
        <span className="absolute left-2 top-2 rounded-md bg-red-600 px-2 py-0.5 text-xs font-bold text-white">
          {deal.discountRate}%
        </span>
        {inactive && (
          <div className="absolute inset-0 flex items-center justify-center bg-black/50 font-semibold text-white">
            {TIME_DEAL_STATUS_LABEL[status]}
          </div>
        )}
      </div>

      <p className="mt-3 text-sm font-medium">{deal.productName}</p>
      <p className="mt-1">
        <span className="font-bold text-red-600">{formatPrice(deal.dealPrice)}</span>
        <span className="ml-2 text-xs text-gray-400 line-through">{formatPrice(deal.originalPrice)}</span>
      </p>

      <div className="mt-2 h-1.5 overflow-hidden rounded-full bg-gray-100">
        <div className="h-full bg-red-500" style={{ width: `${soldPercent}%` }} />
      </div>
      <p className="mt-1 flex justify-between text-xs text-gray-500">
        <span>
          {soldCount}/{deal.totalQuantity}개 판매
        </span>
        {target && (
          <span className="tabular-nums">
            {status === 'UPCOMING' ? '오픈까지 ' : '종료까지 '}
            {formatDuration(target - now)}
          </span>
        )}
      </p>
    </Link>
  )
}
