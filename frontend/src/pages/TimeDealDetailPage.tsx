import { useQuery } from '@tanstack/react-query'
import { Link, useLocation, useNavigate, useParams } from 'react-router'
import { getErrorMessage } from '../api/client'
import { useNow } from '../hooks/useNow'
import { getTimeDeal } from '../features/timedeal/api'
import { countdownTarget, currentStatus, TIME_DEAL_STATUS_LABEL } from '../features/timedeal/status'
import type { CheckoutItem } from '../features/order/types'
import { formatDateTime, formatDuration, formatPrice } from '../lib/format'
import { useAuthStore } from '../stores/authStore'

export default function TimeDealDetailPage() {
  const { timeDealId } = useParams()
  const id = Number(timeDealId)
  const navigate = useNavigate()
  const location = useLocation()
  const accessToken = useAuthStore((state) => state.accessToken)
  const now = useNow()

  const {
    data: deal,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['time-deal', id],
    queryFn: () => getTimeDeal(id),
    enabled: Number.isInteger(id),
    // 진행 중·오픈 예정일 때만 3초마다 남은 수량 갱신 (폴링)
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return status === 'ONGOING' || status === 'UPCOMING' ? 3_000 : false
    },
  })

  if (isLoading) return <p className="py-20 text-center text-gray-500">불러오는 중...</p>
  if (isError || !deal) {
    return (
      <div className="py-20 text-center">
        <p className="mb-4 text-gray-600">{getErrorMessage(error)}</p>
        <Link to="/time-deals" className="underline">
          타임딜 목록으로
        </Link>
      </div>
    )
  }

  const status = currentStatus(deal, now)
  const target = countdownTarget(deal, status)

  const handleBuy = () => {
    if (!accessToken) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }
    const item: CheckoutItem = {
      productId: deal.productId,
      productName: deal.productName,
      price: deal.dealPrice,
      quantity: 1,
      thumbnailUrl: deal.thumbnailUrl,
    }
    navigate('/checkout', { state: { items: [item], timeDealId: deal.id } })
  }

  return (
    <div className="py-10">
      <Link to="/time-deals" className="text-sm text-gray-500 hover:underline">
        ← 타임딜 목록
      </Link>

      <div className="mt-4 grid gap-10 md:grid-cols-2">
        <div className="relative aspect-square overflow-hidden rounded-xl bg-gray-100">
          {deal.thumbnailUrl && (
            <img src={deal.thumbnailUrl} alt={deal.productName} className="h-full w-full object-cover" />
          )}
          <span className="absolute left-3 top-3 rounded-md bg-red-600 px-3 py-1 font-bold text-white">
            {deal.discountRate}%
          </span>
        </div>

        <div>
          <span className="rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-600">
            타임딜 · {TIME_DEAL_STATUS_LABEL[status]}
          </span>
          <h1 className="mt-3 text-2xl font-bold">{deal.productName}</h1>

          <p className="mt-4">
            <span className="text-3xl font-bold text-red-600">{formatPrice(deal.dealPrice)}</span>
            <span className="ml-3 text-gray-400 line-through">{formatPrice(deal.originalPrice)}</span>
          </p>

          {target && (
            <div className="mt-6 rounded-lg bg-gray-900 p-4 text-center text-white">
              <p className="text-xs text-gray-400">{status === 'UPCOMING' ? '오픈까지' : '종료까지'}</p>
              <p className="mt-1 text-3xl font-bold tabular-nums">{formatDuration(target - now)}</p>
            </div>
          )}

          <dl className="mt-6 space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-gray-500">남은 수량</dt>
              <dd className="font-semibold">
                {deal.remainingQuantity} / {deal.totalQuantity}개
              </dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">진행 기간</dt>
              <dd>
                {formatDateTime(deal.startAt)} ~ {formatDateTime(deal.endAt)}
              </dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">구매 제한</dt>
              <dd>1인 1개</dd>
            </div>
          </dl>

          <button
            onClick={handleBuy}
            disabled={status !== 'ONGOING'}
            className="mt-8 w-full rounded-lg bg-red-600 py-3 font-bold text-white disabled:bg-gray-300"
          >
            {status === 'ONGOING' ? '지금 구매하기' : TIME_DEAL_STATUS_LABEL[status]}
          </button>
          <p className="mt-3 text-xs text-gray-400">
            결제창을 닫거나 15분 안에 결제하지 않으면 주문이 취소되고 수량이 반환됩니다.
          </p>
        </div>
      </div>
    </div>
  )
}
