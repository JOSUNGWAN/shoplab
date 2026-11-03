import { useState, type FormEvent } from 'react'
import { addDays, toDateInputValue } from '../../../lib/date'
import { ORDER_STATUS_OPTIONS } from '../constants'
import type { AdminOrderFilter } from '../searchParams'
import type { OrderStatus } from '../../order/types'

type Props = {
  initial: AdminOrderFilter
  onSearch: (filter: AdminOrderFilter) => void
  onReset: () => void
}

const QUICK_RANGES = [
  { label: '오늘', days: 0 },
  { label: '7일', days: 7 },
  { label: '30일', days: 30 },
]

export default function AdminOrderSearchForm({ initial, onSearch, onReset }: Props) {
  const [from, setFrom] = useState(initial.from)
  const [to, setTo] = useState(initial.to)
  const [status, setStatus] = useState(initial.status ?? '')
  const [keyword, setKeyword] = useState(initial.keyword ?? '')
  const [error, setError] = useState<string | null>(null)

  function setRange(days: number) {
    const today = new Date()
    setFrom(toDateInputValue(addDays(today, -days)))
    setTo(toDateInputValue(today))
  }

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    if (from > to) {
      setError('시작일이 종료일보다 늦을 수 없습니다.')
      return
    }
    setError(null)
    onSearch({
      from,
      to,
      status: status ? (status as OrderStatus) : undefined,
      keyword: keyword.trim() || undefined,
    })
  }

  const inputClass = 'rounded border border-gray-300 px-2 py-1.5 text-sm'

  return (
    <form onSubmit={handleSubmit} className="space-y-3 rounded-lg border border-gray-200 bg-white p-4">
      <div className="flex flex-wrap items-center gap-2">
        <span className="w-14 text-sm font-medium text-gray-600">기간</span>
        <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className={inputClass} />
        <span className="text-gray-400">~</span>
        <input type="date" value={to} onChange={(e) => setTo(e.target.value)} className={inputClass} />
        {QUICK_RANGES.map((range) => (
          <button
            key={range.label}
            type="button"
            onClick={() => setRange(range.days)}
            className="rounded border border-gray-300 px-2 py-1.5 text-xs hover:bg-gray-50"
          >
            {range.label}
          </button>
        ))}
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <span className="w-14 text-sm font-medium text-gray-600">조건</span>
        <select value={status} onChange={(e) => setStatus(e.target.value)} className={inputClass}>
          <option value="">전체 상태</option>
          {ORDER_STATUS_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          maxLength={50}
          placeholder="주문번호 · 받는 분 · 회원 이메일"
          className={`${inputClass} w-72`}
        />
        <button type="submit" className="rounded bg-gray-900 px-4 py-1.5 text-sm font-semibold text-white">
          검색
        </button>
        <button type="button" onClick={onReset} className="rounded border border-gray-300 px-4 py-1.5 text-sm">
          초기화
        </button>
      </div>

      {error && <p className="text-sm text-red-600">{error}</p>}
    </form>
  )
}
