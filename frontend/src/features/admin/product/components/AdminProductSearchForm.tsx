import { useState, type FormEvent } from 'react'
import { PRODUCT_STATUS_OPTIONS } from '../constants'
import type { AdminProductFilter } from '../searchParams'
import type { AdminProductStatus, CategoryOption } from '../types'

type Props = {
  initial: AdminProductFilter
  categories: CategoryOption[]
  onSearch: (filter: AdminProductFilter) => void
  onReset: () => void
}

export default function AdminProductSearchForm({ initial, categories, onSearch, onReset }: Props) {
  const [keyword, setKeyword] = useState(initial.keyword ?? '')
  const [categoryId, setCategoryId] = useState(initial.categoryId ? String(initial.categoryId) : '')
  const [status, setStatus] = useState(initial.status ?? '')

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    onSearch({
      keyword: keyword.trim() || undefined,
      categoryId: categoryId ? Number(categoryId) : undefined,
      status: status ? (status as AdminProductStatus) : undefined,
    })
  }

  const inputClass = 'rounded border border-gray-300 px-2 py-1.5 text-sm'

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-wrap items-center gap-2 rounded-lg border border-gray-200 bg-white p-4"
    >
      <input
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        maxLength={50}
        placeholder="상품명"
        className={`${inputClass} w-60`}
      />
      <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} className={inputClass}>
        <option value="">전체 카테고리</option>
        {categories.map((category) => (
          <option key={category.id} value={category.id}>
            {category.label}
          </option>
        ))}
      </select>
      <select value={status} onChange={(e) => setStatus(e.target.value)} className={inputClass}>
        <option value="">전체 상태</option>
        {PRODUCT_STATUS_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      <button type="submit" className="rounded bg-gray-900 px-4 py-1.5 text-sm font-semibold text-white">
        검색
      </button>
      <button type="button" onClick={onReset} className="rounded border border-gray-300 px-4 py-1.5 text-sm">
        초기화
      </button>
    </form>
  )
}
