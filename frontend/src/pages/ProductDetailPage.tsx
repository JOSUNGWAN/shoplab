import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { getErrorMessage } from '../api/client'
import { getProduct } from '../features/product/api'
import { formatPrice } from '../lib/format'

export default function ProductDetailPage() {
  const { productId } = useParams()
  const id = Number(productId)
  const [quantity, setQuantity] = useState(1)

  const { data: product, isLoading, isError, error } = useQuery({
    queryKey: ['product', id],
    queryFn: () => getProduct(id),
    enabled: Number.isInteger(id),
  })

  if (isLoading) return <p className="py-20 text-center text-gray-500">불러오는 중...</p>

  if (isError || !product) {
    return (
      <div className="py-20 text-center">
        <p className="mb-4 text-gray-600">{getErrorMessage(error)}</p>
        <Link to="/products" className="underline">상품 목록으로</Link>
      </div>
    )
  }

  const soldOut = product.status === 'SOLD_OUT' || product.stockQuantity === 0
  const maxQuantity = Math.min(product.stockQuantity, 99)

  return (
    <div className="py-10">
      <div className="grid gap-10 md:grid-cols-2">
        <div className="aspect-square overflow-hidden rounded-xl bg-gray-100">
          {product.thumbnailUrl && (
            <img src={product.thumbnailUrl} alt={product.name} className="h-full w-full object-cover" />
          )}
        </div>

        <div>
          <Link to={`/products?categoryId=${product.categoryId}`} className="text-sm text-gray-500 hover:underline">
            {product.categoryName}
          </Link>
          <h1 className="mt-2 text-2xl font-bold">{product.name}</h1>
          <p className="mt-4 text-2xl font-semibold">{formatPrice(product.price)}</p>
          <p className={`mt-2 text-sm ${soldOut ? 'text-red-600' : 'text-gray-500'}`}>
            {soldOut ? '품절된 상품입니다.' : `재고 ${product.stockQuantity}개`}
          </p>

          {!soldOut && (
            <div className="mt-8 flex items-center gap-4">
              <span className="text-sm text-gray-700">수량</span>
              <div className="flex items-center rounded-md border border-gray-300">
                <button
                  className="px-3 py-1.5 disabled:opacity-30"
                  disabled={quantity <= 1}
                  onClick={() => setQuantity((q) => q - 1)}
                  aria-label="수량 줄이기"
                >
                  −
                </button>
                <span className="w-10 text-center text-sm">{quantity}</span>
                <button
                  className="px-3 py-1.5 disabled:opacity-30"
                  disabled={quantity >= maxQuantity}
                  onClick={() => setQuantity((q) => q + 1)}
                  aria-label="수량 늘리기"
                >
                  +
                </button>
              </div>
            </div>
          )}

          {!soldOut && (
            <p className="mt-6 text-sm text-gray-500">
              총 금액 <span className="ml-2 text-lg font-bold text-gray-900">{formatPrice(product.price * quantity)}</span>
            </p>
          )}

          <button
            disabled
            className="mt-6 w-full rounded-lg bg-gray-900 py-3 font-medium text-white disabled:opacity-40"
            title="장바구니 기능은 다음 단계에서 추가됩니다"
          >
            {soldOut ? '품절' : '장바구니 담기'}
          </button>
        </div>
      </div>

      <section className="mt-16 border-t border-gray-200 pt-8">
        <h2 className="mb-4 text-lg font-bold">상품 설명</h2>
        <p className="whitespace-pre-line text-gray-700">{product.description}</p>
      </section>
    </div>
  )
}