import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { getErrorMessage } from '../api/client'
import { useCart, useRemoveCartItems, useUpdateCartItem } from '../features/cart/hooks'
import type { CartItem } from '../features/cart/types'
import { formatPrice } from '../lib/format'

export default function CartPage() {
  const { data: cart, isLoading, isError, error } = useCart()
  const updateItem = useUpdateCartItem()
  const removeItems = useRemoveCartItems()

  // "선택 해제한 항목"을 기억 → 새로 담긴 상품은 자동으로 선택된 상태
  const [unchecked, setUnchecked] = useState<Set<number>>(new Set())
  const navigate = useNavigate()

  if (isLoading) return <p className="py-20 text-center text-gray-500">불러오는 중...</p>
  if (isError) return <p className="py-20 text-center text-red-600">{getErrorMessage(error)}</p>

  const items = cart?.items ?? []

  if (items.length === 0) {
    return (
      <div className="py-20 text-center">
        <p className="mb-4 text-gray-500">장바구니가 비어 있습니다.</p>
        <Link to="/products" className="underline">
          상품 보러 가기
        </Link>
      </div>
    )
  }

  const availableItems = items.filter((item) => item.available)
  const selectedItems = availableItems.filter((item) => !unchecked.has(item.id))
  const allSelected = availableItems.length > 0 && selectedItems.length === availableItems.length
  const selectedQuantity = selectedItems.reduce((sum, item) => sum + item.quantity, 0)
  const selectedPrice = selectedItems.reduce((sum, item) => sum + item.subtotal, 0)
  const isBusy = updateItem.isPending || removeItems.isPending
  const mutationError = updateItem.error ?? removeItems.error

  const toggle = (id: number) =>
    setUnchecked((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })

  const toggleAll = () => setUnchecked(allSelected ? new Set(availableItems.map((item) => item.id)) : new Set())

  const changeQuantity = (item: CartItem, quantity: number) => updateItem.mutate({ cartItemId: item.id, quantity })

  const removeOne = (item: CartItem) => removeItems.mutate([item.id])

  const removeSelected = () => {
    if (selectedItems.length === 0) return
    if (!window.confirm(`선택한 상품 ${selectedItems.length}개를 삭제할까요?`)) return
    removeItems.mutate(selectedItems.map((item) => item.id))
  }

  const goCheckout = () => {
    navigate('/checkout', {
      state: {
        items: selectedItems.map((item) => ({
          productId: item.productId,
          productName: item.productName,
          price: item.price,
          quantity: item.quantity,
          thumbnailUrl: item.thumbnailUrl,
        })),
      },
    })
  }

  return (
    <div className="py-10">
      <h1 className="mb-6 text-2xl font-bold">장바구니</h1>

      {mutationError && (
        <p className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{getErrorMessage(mutationError)}</p>
      )}

      <div className="flex flex-col gap-8 md:flex-row">
        <section className="flex-1">
          <div className="flex items-center justify-between border-b border-gray-200 pb-3 text-sm">
            <label className="flex items-center gap-2">
              <input type="checkbox" checked={allSelected} onChange={toggleAll} className="size-4 accent-gray-900" />
              전체 선택 ({selectedItems.length}/{availableItems.length})
            </label>
            <button
              onClick={removeSelected}
              disabled={selectedItems.length === 0 || isBusy}
              className="text-gray-500 underline disabled:opacity-40"
            >
              선택 삭제
            </button>
          </div>

          <ul className="divide-y divide-gray-200">
            {items.map((item) => (
              <CartRow
                key={item.id}
                item={item}
                checked={item.available && !unchecked.has(item.id)}
                busy={isBusy}
                onToggle={() => toggle(item.id)}
                onChangeQuantity={(q) => changeQuantity(item, q)}
                onRemove={() => removeOne(item)}
              />
            ))}
          </ul>
        </section>

        <aside className="h-fit rounded-xl border border-gray-200 p-6 md:w-72">
          <h2 className="mb-4 font-bold">주문 예상 금액</h2>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-gray-500">선택 상품</dt>
              <dd>
                {selectedItems.length}종 / {selectedQuantity}개
              </dd>
            </div>
            <div className="flex justify-between border-t border-gray-200 pt-3 text-base font-bold">
              <dt>합계</dt>
              <dd>{formatPrice(selectedPrice)}</dd>
            </div>
          </dl>
          <button
            onClick={goCheckout}
            disabled={selectedItems.length === 0 || isBusy}
            className="mt-6 w-full rounded-lg bg-gray-900 py-3 font-medium text-white disabled:opacity-40"
          >
            {selectedItems.length}종 주문하기
          </button>
        </aside>
      </div>
    </div>
  )
}

type CartRowProps = {
  item: CartItem
  checked: boolean
  busy: boolean
  onToggle: () => void
  onChangeQuantity: (quantity: number) => void
  onRemove: () => void
}

function CartRow({ item, checked, busy, onToggle, onChangeQuantity, onRemove }: CartRowProps) {
  const maxQuantity = Math.min(item.stockQuantity, 99)
  const reason =
    item.stockQuantity === 0
      ? '품절'
      : item.quantity > item.stockQuantity
        ? `재고 부족 (남은 수량 ${item.stockQuantity}개)`
        : '구매 불가'

  return (
    <li className={`flex gap-4 py-5 ${item.available ? '' : 'opacity-60'}`}>
      <input
        type="checkbox"
        checked={checked}
        disabled={!item.available}
        onChange={onToggle}
        className="mt-1 size-4 accent-gray-900"
        aria-label={`${item.productName} 선택`}
      />

      <Link to={`/products/${item.productId}`} className="size-20 shrink-0 overflow-hidden rounded-lg bg-gray-100">
        {item.thumbnailUrl && (
          <img src={item.thumbnailUrl} alt={item.productName} className="h-full w-full object-cover" />
        )}
      </Link>

      <div className="flex-1">
        <div className="flex justify-between gap-2">
          <Link to={`/products/${item.productId}`} className="text-sm font-medium hover:underline">
            {item.productName}
          </Link>
          <button onClick={onRemove} disabled={busy} className="text-gray-400 hover:text-gray-900" aria-label="삭제">
            ✕
          </button>
        </div>
        <p className="mt-1 text-sm text-gray-500">{formatPrice(item.price)}</p>
        {!item.available && <p className="mt-1 text-sm text-red-600">{reason}</p>}

        <div className="mt-3 flex items-center justify-between">
          <div className="flex items-center rounded-md border border-gray-300">
            <button
              className="px-3 py-1 disabled:opacity-30"
              disabled={busy || item.quantity <= 1}
              onClick={() => onChangeQuantity(item.quantity - 1)}
              aria-label="수량 줄이기"
            >
              −
            </button>
            <span className="w-10 text-center text-sm">{item.quantity}</span>
            <button
              className="px-3 py-1 disabled:opacity-30"
              disabled={busy || item.quantity >= maxQuantity}
              onClick={() => onChangeQuantity(item.quantity + 1)}
              aria-label="수량 늘리기"
            >
              +
            </button>
          </div>
          <p className="font-semibold">{formatPrice(item.subtotal)}</p>
        </div>
      </div>
    </li>
  )
}
