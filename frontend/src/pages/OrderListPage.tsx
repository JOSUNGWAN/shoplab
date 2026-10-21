import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router'
import { Pagination } from '../components/Pagination'
import { getErrorMessage } from '../api/client'
import { getOrders } from '../features/order/api'
import { OrderStatusBadge } from '../features/order/components/OrderStatusBadge'
import { formatDateTime, formatPrice } from '../lib/format'

export default function OrderListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Number(searchParams.get('page') ?? 0)

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['orders', page],
    queryFn: () => getOrders(page),
    placeholderData: keepPreviousData,
  })

  if (isLoading) return <p className="py-20 text-center text-gray-500">불러오는 중...</p>
  if (isError) return <p className="py-20 text-center text-red-600">{getErrorMessage(error)}</p>

  return (
    <div className="py-10">
      <h1 className="mb-6 text-2xl font-bold">주문 내역</h1>

      {data && data.content.length === 0 ? (
        <div className="py-20 text-center">
          <p className="mb-4 text-gray-500">주문 내역이 없습니다.</p>
          <Link to="/products" className="underline">
            상품 보러 가기
          </Link>
        </div>
      ) : (
        <>
          <ul className="divide-y divide-gray-200 border-y border-gray-200">
            {data?.content.map((order) => (
              <li key={order.orderNumber}>
                <Link
                  to={`/mypage/orders/${order.orderNumber}`}
                  className="flex items-center gap-4 py-4 hover:bg-gray-50"
                >
                  <div className="flex-1">
                    <p className="text-xs text-gray-500">
                      {formatDateTime(order.orderedAt)} · {order.orderNumber}
                    </p>
                    <p className="mt-1 font-medium">{order.title}</p>
                  </div>
                  <OrderStatusBadge status={order.status} />
                  <p className="w-28 text-right font-semibold">{formatPrice(order.totalAmount)}</p>
                </Link>
              </li>
            ))}
          </ul>
          {data && (
            <Pagination
              page={page}
              totalPages={data.totalPages}
              onChange={(p) => setSearchParams(p === 0 ? {} : { page: String(p) })}
            />
          )}
        </>
      )}
    </div>
  )
}
