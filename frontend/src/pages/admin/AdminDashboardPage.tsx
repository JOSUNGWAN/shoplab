import { useSearchParams } from 'react-router'
import { getErrorMessage } from '../../api/client'
import { addDays, toDateInputValue } from '../../lib/date'
import DailySalesChart from '../../features/admin/dashboard/components/DailySalesChart'
import SummaryCards from '../../features/admin/dashboard/components/SummaryCards'
import TopProductsTable from '../../features/admin/dashboard/components/TopProductsTable'
import { useDailySales, useDashboardSummary, useTopProducts } from '../../features/admin/dashboard/hooks'

const PERIOD_OPTIONS = [7, 14, 30]
const DEFAULT_DAYS = 14

function Loading({ height }: { height: string }) {
  return <div className={`${height} animate-pulse rounded-lg border border-gray-200 bg-white`} />
}

function ErrorBox({ error }: { error: unknown }) {
  return <p className="rounded bg-red-50 px-4 py-3 text-sm text-red-700">{getErrorMessage(error)}</p>
}

export default function AdminDashboardPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const daysParam = Number(searchParams.get('days'))
  const days = PERIOD_OPTIONS.includes(daysParam) ? daysParam : DEFAULT_DAYS

  const today = new Date()
  const period = { from: toDateInputValue(addDays(today, -(days - 1))), to: toDateInputValue(today) }

  const summary = useDashboardSummary()
  const daily = useDailySales(period)
  const top = useTopProducts(period)

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold">대시보드</h1>
          <p className="text-sm text-gray-500">매출은 결제 완료 이후 주문 기준이며, 취소된 주문은 제외합니다.</p>
        </div>
        <div
          role="group"
          aria-label="조회 기간"
          className="inline-flex rounded-md border border-gray-300 bg-white p-0.5 text-sm"
        >
          {PERIOD_OPTIONS.map((option) => (
            <button
              key={option}
              type="button"
              onClick={() => setSearchParams({ days: String(option) })}
              aria-pressed={days === option}
              className={`rounded px-3 py-1 ${days === option ? 'bg-gray-900 text-white' : 'text-gray-600 hover:bg-gray-100'}`}
            >
              {option}일
            </button>
          ))}
        </div>
      </div>

      {summary.isError ? (
        <ErrorBox error={summary.error} />
      ) : summary.isPending ? (
        <Loading height="h-28" />
      ) : (
        <SummaryCards summary={summary.data} />
      )}

      <div className="grid items-start gap-5 lg:grid-cols-[1fr_340px]">
        {daily.isError ? (
          <ErrorBox error={daily.error} />
        ) : daily.isPending ? (
          <Loading height="h-96" />
        ) : (
          <DailySalesChart data={daily.data} />
        )}

        {top.isError ? (
          <ErrorBox error={top.error} />
        ) : top.isPending ? (
          <Loading height="h-96" />
        ) : (
          <TopProductsTable products={top.data} />
        )}
      </div>
    </div>
  )
}
