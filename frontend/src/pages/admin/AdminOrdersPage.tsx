import { useSearchParams } from 'react-router'
import { getErrorMessage } from '../../api/client'
import AdminOrderSearchForm from '../../features/admin/components/AdminOrderSearchForm'
import AdminOrderTable from '../../features/admin/components/AdminOrderTable'
import { useAdminOrders } from '../../features/admin/hooks'
import {
  parseAdminOrderParams,
  toAdminOrderSearchParams,
  type AdminOrderFilter,
} from '../../features/admin/searchParams'

export default function AdminOrdersPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const params = parseAdminOrderParams(searchParams)
  const { data, isPending, isFetching, isError, error } = useAdminOrders(params)

  // URL이 바뀌면(검색·페이지 이동·뒤로 가기) 검색 폼과 그리드 선택 상태를 새로 시작
  const viewKey = searchParams.toString()

  function search(filter: AdminOrderFilter) {
    setSearchParams(toAdminOrderSearchParams(filter))
  }

  function movePage(page: number) {
    const next = new URLSearchParams(searchParams)
    next.set('page', String(page))
    setSearchParams(next)
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">주문 관리</h1>

      <AdminOrderSearchForm
        key={`form-${viewKey}`}
        initial={params}
        onSearch={search}
        onReset={() => setSearchParams({})}
      />

      {isError ? (
        <p className="rounded bg-red-50 px-4 py-3 text-sm text-red-700">{getErrorMessage(error)}</p>
      ) : (
        <AdminOrderTable
          key={`table-${viewKey}`}
          data={data}
          isLoading={isPending}
          isFetching={isFetching}
          onPageChange={movePage}
        />
      )}
    </div>
  )
}
