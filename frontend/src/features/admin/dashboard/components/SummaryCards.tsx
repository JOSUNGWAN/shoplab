import { Link } from 'react-router'
import { formatPrice } from '../../../../lib/format'
import { toDateInputValue } from '../../../../lib/date'
import type { DashboardSummary } from '../types'

// 요약 건수는 전체 기간 기준 → 주문 관리로 이동할 때도 전체 기간으로 검색해야 숫자가 같음
const ALL_TIME_FROM = '2000-01-01'

type Card = {
  label: string
  value: string
  sub?: string
  to?: string
}

function compareWithYesterday(today: number, yesterday: number): string {
  if (yesterday === 0) return today === 0 ? '어제와 같음' : '어제 매출 없음'
  const rate = Math.round(((today - yesterday) / yesterday) * 100)
  if (rate === 0) return '어제와 같음'
  return `어제보다 ${rate > 0 ? '▲' : '▼'} ${Math.abs(rate)}%`
}

function ordersLink(status: string) {
  const params = new URLSearchParams({ status, from: ALL_TIME_FROM, to: toDateInputValue(new Date()) })
  return `/admin/orders?${params}`
}

export default function SummaryCards({ summary }: { summary: DashboardSummary }) {
  const cards: Card[] = [
    {
      label: '오늘 매출',
      value: formatPrice(summary.todaySalesAmount),
      sub: compareWithYesterday(summary.todaySalesAmount, summary.yesterdaySalesAmount),
    },
    { label: '오늘 주문', value: `${summary.todayOrderCount.toLocaleString()}건`, sub: '결제 완료 기준' },
    {
      label: '배송 준비 대기',
      value: `${summary.paidCount.toLocaleString()}건`,
      sub: '결제 완료',
      to: ordersLink('PAID'),
    },
    { label: '배송 준비 중', value: `${summary.preparingCount.toLocaleString()}건`, to: ordersLink('PREPARING') },
    { label: '배송 중', value: `${summary.shippingCount.toLocaleString()}건`, to: ordersLink('SHIPPING') },
    {
      label: '품절 상품',
      value: `${summary.soldOutProductCount.toLocaleString()}개`,
      to: '/admin/products?status=SOLD_OUT',
    },
    {
      label: `재고 ${summary.lowStockThreshold}개 이하`,
      value: `${summary.lowStockProductCount.toLocaleString()}개`,
      sub: '판매 중 상품',
    },
  ]

  return (
    <div className="grid grid-cols-2 gap-3 md:grid-cols-4 xl:grid-cols-7">
      {cards.map((card) => {
        const body = (
          <>
            <p className="text-sm text-gray-500">{card.label}</p>
            <p className="mt-1 text-2xl font-bold text-gray-900 tabular-nums">{card.value}</p>
            {card.sub && <p className="mt-1 text-xs text-gray-500">{card.sub}</p>}
          </>
        )
        return card.to ? (
          <Link
            key={card.label}
            to={card.to}
            className="group rounded-lg border border-gray-200 bg-white p-4 hover:border-gray-400"
          >
            {body}
            <p className="mt-2 text-xs text-gray-400 group-hover:text-gray-700">목록 보기 →</p>
          </Link>
        ) : (
          <div key={card.label} className="rounded-lg border border-gray-200 bg-white p-4">
            {body}
          </div>
        )
      })}
    </div>
  )
}
