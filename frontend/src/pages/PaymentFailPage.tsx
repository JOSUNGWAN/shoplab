import { useEffect, useRef } from 'react'
import { Link, useSearchParams } from 'react-router'
import { cancelOrder } from '../features/order/api'

export default function PaymentFailPage() {
  const [searchParams] = useSearchParams()
  const code = searchParams.get('code')
  const message = searchParams.get('message')
  const orderId = searchParams.get('orderId')

  // 결제가 안 된 주문은 바로 취소해서 재고 반환 (한 번만)
  const canceledRef = useRef(false)
  useEffect(() => {
    if (!orderId || canceledRef.current) return
    canceledRef.current = true
    cancelOrder(orderId).catch(() => {})
  }, [orderId])

  const userCanceled = code === 'PAY_PROCESS_CANCELED'

  return (
    <div className="py-20 text-center">
      <h1 className="mb-3 text-xl font-bold">{userCanceled ? '결제를 취소했습니다' : '결제에 실패했습니다'}</h1>
      {!userCanceled && message && <p className="mb-2 text-gray-600">{message}</p>}
      {code && <p className="mb-8 text-xs text-gray-400">오류 코드: {code}</p>}
      <Link to="/cart" className="rounded-lg bg-gray-900 px-5 py-2.5 text-sm text-white">
        장바구니로 돌아가기
      </Link>
    </div>
  )
}
