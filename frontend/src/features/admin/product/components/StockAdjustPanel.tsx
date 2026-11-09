import { useState } from 'react'
import { getErrorMessage } from '../../../../api/client'
import { useAdjustStock } from '../hooks'
import type { AdminProductDetail } from '../types'

export default function StockAdjustPanel({ product }: { product: AdminProductDetail }) {
  const [amount, setAmount] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const adjust = useAdjustStock()

  const quantity = Number(amount)
  const valid = Number.isInteger(quantity) && quantity >= 1 && quantity <= 99_999

  function submit(sign: 1 | -1) {
    if (!valid) return
    setMessage(null)
    adjust.mutate(
      { id: product.id, delta: sign * quantity },
      {
        onSuccess: (updated) => {
          setAmount('')
          setMessage(
            `${sign > 0 ? '입고' : '차감'} ${quantity.toLocaleString()}개 → 현재 ${updated.stockQuantity.toLocaleString()}개`,
          )
        },
      },
    )
  }

  return (
    <section className="space-y-3 rounded-lg border border-gray-200 bg-white p-5">
      <h2 className="font-semibold">재고 조정</h2>
      <p className="text-sm text-gray-600">
        현재 재고 <b className="text-lg text-gray-900">{product.stockQuantity.toLocaleString()}</b>개
      </p>
      <input
        type="number"
        min={1}
        max={99_999}
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        placeholder="수량"
        className="w-full rounded border border-gray-300 px-3 py-2 text-sm"
      />
      <div className="grid grid-cols-2 gap-2">
        <button
          type="button"
          onClick={() => submit(1)}
          disabled={!valid || adjust.isPending}
          className="rounded bg-blue-600 py-2 text-sm font-semibold text-white disabled:bg-gray-300"
        >
          + 입고
        </button>
        <button
          type="button"
          onClick={() => submit(-1)}
          disabled={!valid || adjust.isPending}
          className="rounded bg-gray-700 py-2 text-sm font-semibold text-white disabled:bg-gray-300"
        >
          − 차감
        </button>
      </div>
      {adjust.isError && <p className="text-sm text-red-600">{getErrorMessage(adjust.error)}</p>}
      {message && <p className="text-sm text-green-700">{message}</p>}
    </section>
  )
}
