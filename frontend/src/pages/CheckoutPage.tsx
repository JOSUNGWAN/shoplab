import { useEffect, useRef, useState } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ANONYMOUS, loadTossPayments } from '@tosspayments/tosspayments-sdk'
import { FormField } from '../components/FormField'
import { getErrorMessage } from '../api/client'
import { cancelOrder, createOrder } from '../features/order/api'
import { shippingSchema, type ShippingForm } from '../features/order/schemas'
import type { CheckoutItem } from '../features/order/types'
import { formatPrice } from '../lib/format'

type TossWidgets = ReturnType<Awaited<ReturnType<typeof loadTossPayments>>['widgets']>

const CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY as string | undefined

function makeOrderName(items: CheckoutItem[]) {
  const first = items[0].productName
  const name = items.length > 1 ? `${first} 외 ${items.length - 1}건` : first
  return name.slice(0, 100) // 토스 orderName 최대 100자
}

export default function CheckoutPage() {
  const location = useLocation()
  const items = (location.state as { items?: CheckoutItem[] } | null)?.items ?? []
  const expectedAmount = items.reduce((sum, item) => sum + item.price * item.quantity, 0)

  const widgetsRef = useRef<TossWidgets | null>(null)
  const startedRef = useRef(false)
  const [widgetReady, setWidgetReady] = useState(false)
  const [widgetError, setWidgetError] = useState<string | null>(null)
  const [payError, setPayError] = useState<string | null>(null)
  const [paying, setPaying] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ShippingForm>({ resolver: zodResolver(shippingSchema), defaultValues: { address2: '' } })

  // 키가 없으면 effect 없이 바로 계산 (상태로 둘 필요 없음)
  const configError = CLIENT_KEY ? null : '결제 클라이언트 키가 설정되지 않았습니다. (.env.local 확인)'
  // 결제위젯 초기화 + 렌더링 (개발 모드에서 두 번 실행되지 않게 ref로 막기)
  useEffect(() => {
    if (items.length === 0 || startedRef.current || !CLIENT_KEY) return
    startedRef.current = true

    const init = async () => {
      const tossPayments = await loadTossPayments(CLIENT_KEY)
      const widgets = tossPayments.widgets({ customerKey: ANONYMOUS })
      await widgets.setAmount({ currency: 'KRW', value: expectedAmount })
      await Promise.all([
        widgets.renderPaymentMethods({ selector: '#payment-method', variantKey: 'DEFAULT' }),
        widgets.renderAgreement({ selector: '#agreement', variantKey: 'AGREEMENT' }),
      ])
      widgetsRef.current = widgets
      setWidgetReady(true)
    }
    init().catch((e) => setWidgetError(getErrorMessage(e)))
  }, [items.length, expectedAmount])

  if (items.length === 0) {
    return <Navigate to="/cart" replace />
  }

  const onPay = async (form: ShippingForm) => {
    const widgets = widgetsRef.current
    if (!widgets) return

    setPaying(true)
    setPayError(null)
    let orderNumber: string | null = null

    try {
      // ① 주문 생성 (재고 차감, 금액은 서버가 계산)
      const order = await createOrder({
        items: items.map(({ productId, quantity }) => ({ productId, quantity })),
        recipient: form.recipient,
        phone: form.phone,
        zipcode: form.zipcode,
        address1: form.address1,
        address2: form.address2 || undefined,
      })
      orderNumber = order.orderNumber

      // ② 서버가 계산한 금액을 기준으로 결제
      await widgets.setAmount({ currency: 'KRW', value: order.totalAmount })
      await widgets.requestPayment({
        orderId: order.orderNumber,
        orderName: makeOrderName(items),
        successUrl: `${window.location.origin}/payments/success`,
        failUrl: `${window.location.origin}/payments/fail`,
        customerName: form.recipient,
        customerMobilePhone: form.phone.replace(/-/g, ''),
      })
    } catch (e) {
      // 결제창을 닫았거나 실패 → 방금 만든 주문을 취소해서 재고 바로 반환
      if (orderNumber) {
        cancelOrder(orderNumber).catch(() => {})
      }
      setPayError(getErrorMessage(e))
      setPaying(false)
    }
  }

  return (
    <form onSubmit={(e) => handleSubmit(onPay)(e)} className="py-10" noValidate>
      <h1 className="mb-6 text-2xl font-bold">주문서</h1>

      <div className="flex flex-col gap-8 md:flex-row">
        <div className="flex-1 space-y-10">
          <section>
            <h2 className="mb-3 font-bold">주문 상품 ({items.length})</h2>
            <ul className="divide-y divide-gray-200 rounded-lg border border-gray-200">
              {items.map((item) => (
                <li key={item.productId} className="flex items-center gap-4 p-4">
                  <div className="size-14 shrink-0 overflow-hidden rounded-md bg-gray-100">
                    {item.thumbnailUrl && (
                      <img src={item.thumbnailUrl} alt={item.productName} className="h-full w-full object-cover" />
                    )}
                  </div>
                  <div className="flex-1 text-sm">
                    <p className="font-medium">{item.productName}</p>
                    <p className="text-gray-500">
                      {formatPrice(item.price)} × {item.quantity}개
                    </p>
                  </div>
                  <p className="font-semibold">{formatPrice(item.price * item.quantity)}</p>
                </li>
              ))}
            </ul>
          </section>

          <section className="space-y-4">
            <h2 className="font-bold">배송지</h2>
            <FormField id="recipient" label="받는 분" error={errors.recipient?.message} {...register('recipient')} />
            <FormField
              id="phone"
              label="연락처"
              placeholder="010-1234-5678"
              error={errors.phone?.message}
              {...register('phone')}
            />
            <FormField
              id="zipcode"
              label="우편번호"
              placeholder="06236"
              error={errors.zipcode?.message}
              {...register('zipcode')}
            />
            <FormField id="address1" label="주소" error={errors.address1?.message} {...register('address1')} />
            <FormField
              id="address2"
              label="상세 주소 (선택)"
              error={errors.address2?.message}
              {...register('address2')}
            />
          </section>

          <section>
            <h2 className="mb-3 font-bold">결제 수단</h2>
            {(configError ?? widgetError) && <p className="mb-3 text-sm text-red-600">{configError ?? widgetError}</p>}
            <div id="payment-method" />
            <div id="agreement" />
          </section>
        </div>

        <aside className="h-fit rounded-xl border border-gray-200 p-6 md:sticky md:top-6 md:w-72">
          <h2 className="mb-4 font-bold">결제 금액</h2>
          <div className="flex justify-between text-base font-bold">
            <span>합계</span>
            <span>{formatPrice(expectedAmount)}</span>
          </div>
          {payError && <p className="mt-4 text-sm text-red-600">{payError}</p>}
          <button
            type="submit"
            disabled={!widgetReady || paying}
            className="mt-6 w-full rounded-lg bg-gray-900 py-3 font-medium text-white disabled:opacity-40"
          >
            {paying ? '결제 진행 중...' : !widgetReady ? '결제 준비 중...' : `${formatPrice(expectedAmount)} 결제하기`}
          </button>
        </aside>
      </div>
    </form>
  )
}
