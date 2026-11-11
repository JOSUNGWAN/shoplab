import { Link } from 'react-router'
import { formatPrice } from '../../../../lib/format'
import type { TopProduct } from '../types'

export default function TopProductsTable({ products }: { products: TopProduct[] }) {
  return (
    <section className="rounded-lg border border-gray-200 bg-white p-5">
      <h2 className="font-semibold">판매 순위</h2>
      <p className="text-sm text-gray-500">판매 수량 기준 상위 5개</p>

      {products.length === 0 ? (
        <p className="py-16 text-center text-sm text-gray-400">기간 내 판매된 상품이 없습니다.</p>
      ) : (
        <ol className="mt-3 divide-y divide-gray-100">
          {products.map((product) => (
            <li key={product.productId} className="flex items-center gap-3 py-2.5 text-sm">
              <span className="w-5 text-center font-bold text-gray-400 tabular-nums">{product.rank}</span>
              <Link
                to={`/admin/products/${product.productId}/edit`}
                className="min-w-0 flex-1 truncate hover:underline"
              >
                {product.productName}
              </Link>
              <div className="text-right tabular-nums">
                <p className="font-semibold">{product.quantity.toLocaleString()}개</p>
                <p className="text-xs text-gray-500">{formatPrice(product.salesAmount)}</p>
              </div>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
