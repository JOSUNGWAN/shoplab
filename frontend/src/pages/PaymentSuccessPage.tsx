import { useEffect, useRef } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router'
import { getErrorMessage } from '../api/client'
import { cartKeys } from '../features/cart/hooks'
import { confirmPayment } from '../features/order/api'
import { formatPrice } from '../lib/format'

export default function PaymentSuccessPage() {
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()

  const paymentKey = searchParams.get('paymentKey')
  const orderId = searchParams.get('orderId')
  const amount = Number(searchParams.get('amount'))

  const confirm = useMutation({
    mutationFn: confirmPayment,
    onSuccess: () => {
      // 결제가 끝나면 서버가 장바구니를 비웠으니 화면 캐시도 갱신
      queryClient.invalidateQueries({ queryKey: cartKeys.all })
      queryClient.invalidateQueries({ queryKey: ['orders'] })
    },
  })

  // 승인 요청은 딱 한 번만 (개발 모드의 effect 두 번 실행 방지)
  const requestedRef = useRef(false)
  useEffect(() => {
    if (requestedRef.current || !paymentKey || !orderId || !amount) return
    requestedRef.current = true
    confirm.mutate({ paymentKey, orderId, amount })
  }, [paymentKey, orderId, amount, confirm])

  if (!paymentKey || !orderId || !amount) {
    return <p className="py-20 text-center text-gray-500">잘못된 접근입니다.</p>
  }

  if (confirm.isError) {
    return (
      <div className="py-20 text-center">
        <h1 className="mb-3 text-xl font-bold">결제를 완료하지 못했습니다</h1>
        <p className="mb-6 text-red-600">{getErrorMessage(confirm.error)}</p>
        <div className="flex justify-center gap-4 text-sm">
          <Link to="/mypage/orders" className="underline">
            주문 내역
          </Link>
          <Link to="/cart" className="underline">
            장바구니
          </Link>
        </div>
      </div>
    )
  }

  if (!confirm.isSuccess) {
    return <p className="py-20 text-center text-gray-500">결제를 확인하고 있습니다...</p>
  }

  const order = confirm.data
  return (
    <div className="mx-auto max-w-md py-20 text-center">
      <h1 className="mb-2 text-2xl font-bold">결제가 완료되었습니다</h1>
      <p className="mb-8 text-gray-500">주문번호 {order.orderNumber}</p>
      <div className="mb-8 rounded-xl border border-gray-200 p-6 text-left text-sm">
        {order.items.map((item) => (
          <div key={item.productId} className="flex justify-between py-1">
            <span>
              {item.productName} × {item.quantity}
            </span>
            <span>{formatPrice(item.subtotal)}</span>
          </div>
        ))}
        <div className="mt-3 flex justify-between border-t border-gray-200 pt-3 font-bold">
          <span>결제 금액</span>
          <span>{formatPrice(order.totalAmount)}</span>
        </div>
      </div>
      <div className="flex justify-center gap-3">
        <Link
          to={`/mypage/orders/${order.orderNumber}`}
          className="rounded-lg border border-gray-300 px-5 py-2.5 text-sm"
        >
          주문 상세 보기
        </Link>
        <Link to="/products" className="rounded-lg bg-gray-900 px-5 py-2.5 text-sm text-white">
          쇼핑 계속하기
        </Link>
      </div>
    </div>
  )
}
