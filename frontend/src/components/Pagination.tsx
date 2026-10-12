type PaginationProps = {
  page: number // 0부터 시작
  totalPages: number
  onChange: (page: number) => void
}

export function Pagination({ page, totalPages, onChange }: PaginationProps) {
  if (totalPages <= 1) return null

  // 현재 페이지 주변으로 최대 5개 번호 표시
  const start = Math.max(0, Math.min(page - 2, totalPages - 5))
  const end = Math.min(totalPages, start + 5)
  const pages = Array.from({ length: end - start }, (_, i) => start + i)

  const buttonClass = 'min-w-9 rounded-md px-3 py-1.5 text-sm disabled:opacity-30'

  return (
    <nav className="mt-10 flex justify-center gap-1" aria-label="페이지">
      <button className={buttonClass} disabled={page === 0} onClick={() => onChange(page - 1)}>
        이전
      </button>
      {pages.map((p) => (
        <button
          key={p}
          onClick={() => onChange(p)}
          aria-current={p === page ? 'page' : undefined}
          className={`${buttonClass} ${p === page ? 'bg-gray-900 text-white' : 'hover:bg-gray-100'}`}
        >
          {p + 1}
        </button>
      ))}
      <button className={buttonClass} disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        다음
      </button>
    </nav>
  )
}