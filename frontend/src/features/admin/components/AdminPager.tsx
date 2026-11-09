type Props = {
  page: number
  totalPages: number
  hasNext: boolean
  onChange: (page: number) => void
}

export default function AdminPager({ page, totalPages, hasNext, onChange }: Props) {
  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-center gap-3 text-sm">
      <button
        type="button"
        onClick={() => onChange(page - 1)}
        disabled={page === 0}
        className="rounded border border-gray-300 px-3 py-1 disabled:opacity-40"
      >
        이전
      </button>
      <span>
        {page + 1} / {totalPages}
      </span>
      <button
        type="button"
        onClick={() => onChange(page + 1)}
        disabled={!hasNext}
        className="rounded border border-gray-300 px-3 py-1 disabled:opacity-40"
      >
        다음
      </button>
    </div>
  )
}
