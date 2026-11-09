import { Link } from 'react-router'
import { getErrorMessage } from '../../../../api/client'
import { formatPrice } from '../../../../lib/format'
import type { PageResponse } from '../../../../types/api'
import AdminPager from '../../components/AdminPager'
import { useChangeVisibility } from '../hooks'
import type { AdminProduct } from '../types'
import ProductStatusBadge from './ProductStatusBadge'

type Props = {
  data: PageResponse<AdminProduct> | undefined
  isLoading: boolean
  isFetching: boolean
  onPageChange: (page: number) => void
}

export default function AdminProductTable({ data, isLoading, isFetching, onPageChange }: Props) {
  const visibility = useChangeVisibility()
  const products = data?.content ?? []

  function toggleVisibility(product: AdminProduct) {
    const hidden = product.status === 'HIDDEN'
    if (!hidden && !window.confirm(`'${product.name}'을(를) 숨길까요?\n쇼핑몰에서 더 이상 보이지 않습니다.`)) return
    visibility.mutate({ id: product.id, visible: hidden })
  }

  return (
    <div className="space-y-3">
      <p className="text-sm text-gray-600">
        총 <b>{data?.totalElements ?? 0}</b>개{isFetching && <span className="ml-2 text-gray-400">불러오는 중…</span>}
      </p>

      {visibility.isError && (
        <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{getErrorMessage(visibility.error)}</p>
      )}

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="w-full text-sm">
          <thead className="border-b border-gray-200 bg-gray-50 text-left text-gray-600">
            <tr>
              <th className="px-3 py-2 font-medium">이미지</th>
              <th className="px-3 py-2 font-medium">상품명</th>
              <th className="px-3 py-2 font-medium">카테고리</th>
              <th className="px-3 py-2 text-right font-medium">가격</th>
              <th className="px-3 py-2 text-right font-medium">재고</th>
              <th className="px-3 py-2 font-medium">상태</th>
              <th className="px-3 py-2 font-medium">관리</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={7} className="py-16 text-center text-gray-400">
                  불러오는 중…
                </td>
              </tr>
            ) : products.length === 0 ? (
              <tr>
                <td colSpan={7} className="py-16 text-center text-gray-400">
                  검색 조건에 맞는 상품이 없습니다.
                </td>
              </tr>
            ) : (
              products.map((product) => {
                const hidden = product.status === 'HIDDEN'
                const pending = visibility.isPending && visibility.variables?.id === product.id
                return (
                  <tr
                    key={product.id}
                    className={`border-b border-gray-100 last:border-0 ${hidden ? 'bg-gray-50 text-gray-400' : ''}`}
                  >
                    <td className="px-3 py-2">
                      {product.thumbnailUrl ? (
                        <img
                          src={product.thumbnailUrl}
                          alt=""
                          loading="lazy"
                          className="size-10 rounded object-cover"
                        />
                      ) : (
                        <div className="size-10 rounded bg-gray-100" />
                      )}
                    </td>
                    <td className="px-3 py-2">
                      <p className="font-medium">{product.name}</p>
                      <p className="text-xs text-gray-400">#{product.id}</p>
                    </td>
                    <td className="px-3 py-2 whitespace-nowrap">{product.categoryName}</td>
                    <td className="px-3 py-2 text-right whitespace-nowrap">{formatPrice(product.price)}</td>
                    <td
                      className={`px-3 py-2 text-right ${product.stockQuantity === 0 ? 'font-semibold text-red-600' : ''}`}
                    >
                      {product.stockQuantity.toLocaleString()}
                    </td>
                    <td className="px-3 py-2">
                      <ProductStatusBadge status={product.status} />
                    </td>
                    <td className="px-3 py-2">
                      <div className="flex gap-2 whitespace-nowrap">
                        <Link
                          to={`/admin/products/${product.id}/edit`}
                          className="rounded border border-gray-300 px-2 py-1 text-xs text-gray-700 hover:bg-gray-50"
                        >
                          수정
                        </Link>
                        <button
                          type="button"
                          onClick={() => toggleVisibility(product)}
                          disabled={pending}
                          className="rounded border border-gray-300 px-2 py-1 text-xs text-gray-700 hover:bg-gray-50 disabled:opacity-40"
                        >
                          {hidden ? '보이기' : '숨기기'}
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      <AdminPager
        page={data?.page ?? 0}
        totalPages={data?.totalPages ?? 0}
        hasNext={!!data?.hasNext}
        onChange={onPageChange}
      />
    </div>
  )
}
