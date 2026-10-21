import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { getErrorMessage } from '../api/client'
import { cancelOrder, getOrder } from '../features/order/api'
import { OrderStatusBadge } from '../features/order/components/OrderStatusBadge'
import { isCancelable } from '../features/order/status'
import { formatDateTime, formatPrice } from '../lib/format'

export default function OrderDetailPage() {
  const { orderNumber = '' } = useParams()
  const queryClient = useQueryClient()

  const {
    data: order,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['order', orderNumber],
    queryFn: () => getOrder(orderNumber),
    enabled: !!orderNumber,
  })

  const cancel = useMutation({
    mutationFn: () => cancelOrder(orderNumber),
    onSuccess: (updated) => {
      queryClient.setQueryData(['order', orderNumber], updated)
      queryClient.invalidateQueries({ queryKey: ['orders'] })
    },
  })

  if (isLoading) return <p className="py-20 text-center text-gray-500">불러오는 중...</p>
  if (isError || !order) {
    return (
      <div className="py-20 text-center">
        <p className="mb-4 text-gray-600">{getErrorMessage(error)}</p>
        <Link to="/mypage/orders" className="underline">
          주문 내역으로
        </Link>
      </div>
    )
  }

  const handleCancel = () => {
    const message = order.status === 'PAID' ? '주문을 취소하고 결제 금액을 환불할까요?' : '주문을 취소할까요?'
    if (window.confirm(message)) cancel.mutate()
  }

  return (
    <div className="mx-auto max-w-2xl py-10">
      <Link to="/mypage/orders" className="text-sm text-gray-500 hover:underline">
        ← 주문 내역
      </Link>

      <div className="mt-4 flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold">주문 상세</h1>
          <p className="mt-1 text-sm text-gray-500">
            {formatDateTime(order.orderedAt)} · {order.orderNumber}
          </p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <section className="mt-8">
        <h2 className="mb-3 font-bold">주문 상품</h2>
        <ul className="divide-y divide-gray-200 rounded-lg border border-gray-200">
          {order.items.map((item) => (
            <li key={item.productId} className="flex justify-between p-4 text-sm">
              <div>
                <Link to={`/products/${item.productId}`} className="font-medium hover:underline">
                  {item.productName}
                </Link>
                <p className="text-gray-500">
                  {formatPrice(item.orderPrice)} × {item.quantity}개
                </p>
              </div>
              <p className="font-semibold">{formatPrice(item.subtotal)}</p>
            </li>
          ))}
        </ul>
        <div className="mt-3 flex justify-between px-1 font-bold">
          <span>총 결제 금액</span>
          <span>{formatPrice(order.totalAmount)}</span>
        </div>
      </section>

      <section className="mt-8">
        <h2 className="mb-3 font-bold">배송지</h2>
        <div className="rounded-lg border border-gray-200 p-4 text-sm leading-6">
          <p>
            {order.recipient} · {order.phone}
          </p>
          <p>
            ({order.zipcode}) {order.address1} {order.address2}
          </p>
        </div>
      </section>

      {cancel.isError && <p className="mt-6 text-sm text-red-600">{getErrorMessage(cancel.error)}</p>}

      {isCancelable(order.status) && (
        <button
          onClick={handleCancel}
          disabled={cancel.isPending}
          className="mt-8 w-full rounded-lg border border-gray-300 py-3 text-sm disabled:opacity-40"
        >
          {cancel.isPending ? '취소 처리 중...' : order.status === 'PAID' ? '주문 취소 (환불)' : '주문 취소'}
        </button>
      )}
    </div>
  )
}
