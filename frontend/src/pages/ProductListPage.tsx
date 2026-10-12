import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router'
import { Pagination } from '../components/Pagination'
import { getCategories, getProducts } from '../features/product/api'
import { ProductCard } from '../features/product/components/ProductCard'

const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: '최신순' },
  { value: 'price,asc', label: '낮은 가격순' },
  { value: 'price,desc', label: '높은 가격순' },
]

const PAGE_SIZE = 12

export default function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams()

  // URL에서 현재 조건 읽기
  const categoryIdParam = searchParams.get('categoryId')
  const categoryId = categoryIdParam ? Number(categoryIdParam) : undefined
  const page = Number(searchParams.get('page') ?? 0)
  const sort = searchParams.get('sort') ?? 'createdAt,desc'

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
    staleTime: 5 * 60 * 1000, // 카테고리는 잘 안 바뀌니 5분간 재사용
  })

  const { data, isLoading, isError } = useQuery({
    queryKey: ['products', { categoryId, page, sort }],
    queryFn: () => getProducts({ categoryId, page, size: PAGE_SIZE, sort }),
    placeholderData: keepPreviousData, // 다음 페이지 로딩 중에도 이전 목록 유지
  })

  // URL 조건 바꾸기 (undefined면 해당 조건 삭제)
  const updateParams = (next: Record<string, string | undefined>) => {
    const params = new URLSearchParams(searchParams)
    Object.entries(next).forEach(([key, value]) => {
      if (value === undefined) params.delete(key)
      else params.set(key, value)
    })
    setSearchParams(params)
  }

  // 카테고리나 정렬을 바꾸면 1페이지로
  const selectCategory = (id?: number) => updateParams({ categoryId: id?.toString(), page: undefined })
  const changeSort = (value: string) => updateParams({ sort: value, page: undefined })
  const changePage = (p: number) => {
    updateParams({ page: p === 0 ? undefined : String(p) })
    window.scrollTo({ top: 0 })
  }

  const categoryClass = (id?: number) =>
    `text-left text-sm ${categoryId === id ? 'font-semibold text-gray-900' : 'text-gray-500 hover:text-gray-900'}`

  return (
    <div className="flex flex-col gap-8 py-10 md:flex-row">
      <aside className="shrink-0 md:w-44">
        <h2 className="mb-4 font-bold">카테고리</h2>
        <button className={categoryClass(undefined)} onClick={() => selectCategory(undefined)}>
          전체
        </button>
        <div className="mt-3 space-y-4">
          {categories?.map((root) => (
            <div key={root.id}>
              <button className={categoryClass(root.id)} onClick={() => selectCategory(root.id)}>
                {root.name}
              </button>
              <ul className="mt-2 space-y-1.5 pl-3">
                {root.children.map((child) => (
                  <li key={child.id}>
                    <button className={categoryClass(child.id)} onClick={() => selectCategory(child.id)}>
                      {child.name}
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </aside>

      <section className="flex-1">
        <div className="mb-6 flex items-center justify-between">
          <p className="text-sm text-gray-500">총 {data?.totalElements ?? 0}개</p>
          <select
            value={sort}
            onChange={(e) => changeSort(e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm"
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {isLoading && <p className="py-20 text-center text-gray-500">불러오는 중...</p>}
        {isError && <p className="py-20 text-center text-red-600">상품을 불러오지 못했습니다.</p>}
        {data && data.content.length === 0 && (
          <p className="py-20 text-center text-gray-500">상품이 없습니다.</p>
        )}

        {data && data.content.length > 0 && (
          <>
            <div className="grid grid-cols-2 gap-x-5 gap-y-8 md:grid-cols-3">
              {data.content.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
            <Pagination page={page} totalPages={data.totalPages} onChange={changePage} />
          </>
        )}
      </section>
    </div>
  )
}