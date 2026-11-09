import { Link, useSearchParams } from 'react-router'
import { getErrorMessage } from '../../api/client'
import AdminProductSearchForm from '../../features/admin/product/components/AdminProductSearchForm'
import AdminProductTable from '../../features/admin/product/components/AdminProductTable'
import { useAdminProducts, useCategoryOptions } from '../../features/admin/product/hooks'
import {
  parseAdminProductParams,
  toAdminProductSearchParams,
  type AdminProductFilter,
} from '../../features/admin/product/searchParams'

export default function AdminProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const params = parseAdminProductParams(searchParams)
  const products = useAdminProducts(params)
  const categories = useCategoryOptions()

  function search(filter: AdminProductFilter) {
    setSearchParams(toAdminProductSearchParams(filter))
  }

  function movePage(page: number) {
    const next = new URLSearchParams(searchParams)
    next.set('page', String(page))
    setSearchParams(next)
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">상품 관리</h1>
        <Link
          to="/admin/products/new"
          className="rounded bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
        >
          + 상품 등록
        </Link>
      </div>

      <AdminProductSearchForm
        key={`form-${searchParams.toString()}`}
        initial={params}
        categories={categories.data ?? []}
        onSearch={search}
        onReset={() => setSearchParams({})}
      />

      {products.isError ? (
        <p className="rounded bg-red-50 px-4 py-3 text-sm text-red-700">{getErrorMessage(products.error)}</p>
      ) : (
        <AdminProductTable
          data={products.data}
          isLoading={products.isPending}
          isFetching={products.isFetching}
          onPageChange={movePage}
        />
      )}
    </div>
  )
}
