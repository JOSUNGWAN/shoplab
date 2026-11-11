import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatPrice } from '../../../../lib/format'
import { amountTickFormatter, formatLongDate, formatShortDate, niceTicks } from '../format'
import type { DailySales } from '../types'

type Metric = 'salesAmount' | 'orderCount'

const METRICS: Record<Metric, { label: string; format: (value: number) => string; integerOnly: boolean }> = {
  salesAmount: { label: '매출', format: formatPrice, integerOnly: false },
  orderCount: { label: '주문 수', format: (value) => `${value.toLocaleString()}건`, integerOnly: true },
}

// 차트 색: 막대는 단일 색상, 글씨·축·눈금선은 회색 계열
const BAR_COLOR = '#2a78d6'
const GRID_COLOR = '#e5e7eb'
const AXIS_COLOR = '#d1d5db'
const TICK_COLOR = '#6b7280'

export default function DailySalesChart({ data }: { data: DailySales[] }) {
  const [metric, setMetric] = useState<Metric>('salesAmount')
  const { label, format, integerOnly } = METRICS[metric]
  const total = data.reduce((sum, day) => sum + day[metric], 0)
  const ticks = niceTicks(Math.max(...data.map((day) => day[metric])), integerOnly)
  const tickFormatter =
    metric === 'salesAmount' ? amountTickFormatter(ticks) : (value: number) => value.toLocaleString()

  return (
    <section className="rounded-lg border border-gray-200 bg-white p-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="font-semibold">일별 {label}</h2>
          <p className="text-sm text-gray-500">
            기간 합계 <b className="text-gray-900 tabular-nums">{format(total)}</b>
          </p>
        </div>
        <div
          role="group"
          aria-label="차트 지표"
          className="inline-flex rounded-md border border-gray-300 p-0.5 text-sm"
        >
          {(Object.keys(METRICS) as Metric[]).map((key) => (
            <button
              key={key}
              type="button"
              onClick={() => setMetric(key)}
              aria-pressed={metric === key}
              className={`rounded px-3 py-1 ${metric === key ? 'bg-gray-900 text-white' : 'text-gray-600 hover:bg-gray-100'}`}
            >
              {METRICS[key].label}
            </button>
          ))}
        </div>
      </div>

      {total === 0 ? (
        <p className="flex h-72 items-center justify-center text-sm text-gray-400">기간 내 결제된 주문이 없습니다.</p>
      ) : (
        <div className="mt-4 h-72">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data} margin={{ top: 8, right: 8, bottom: 0, left: 0 }}>
              <CartesianGrid vertical={false} stroke={GRID_COLOR} />
              <XAxis
                dataKey="date"
                tickFormatter={formatShortDate}
                tickLine={false}
                axisLine={{ stroke: AXIS_COLOR }}
                tick={{ fontSize: 12, fill: TICK_COLOR }}
                minTickGap={8}
              />
              <YAxis
                ticks={ticks}
                domain={[0, ticks.at(-1) ?? 0]}
                tickFormatter={tickFormatter}
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 12, fill: TICK_COLOR }}
                width={52}
              />
              <Tooltip
                cursor={{ fill: '#f3f4f6' }}
                labelFormatter={(value) => formatLongDate(String(value))}
                formatter={(value) => [format(Number(value)), label]}
              />
              <Bar dataKey={metric} fill={BAR_COLOR} radius={[4, 4, 0, 0]} maxBarSize={24} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      <details className="mt-3 text-sm">
        <summary className="cursor-pointer text-gray-500 hover:text-gray-900">표로 보기</summary>
        <table className="mt-2 w-full">
          <thead className="text-left text-gray-500">
            <tr>
              <th className="py-1 font-medium">날짜</th>
              <th className="py-1 text-right font-medium">주문 수</th>
              <th className="py-1 text-right font-medium">매출</th>
            </tr>
          </thead>
          <tbody className="tabular-nums">
            {data.map((day) => (
              <tr key={day.date} className="border-t border-gray-100">
                <td className="py-1">{formatLongDate(day.date)}</td>
                <td className="py-1 text-right">{day.orderCount.toLocaleString()}건</td>
                <td className="py-1 text-right">{formatPrice(day.salesAmount)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </details>
    </section>
  )
}
