import { Link } from 'react-router'
import { formatPrice } from '../../../lib/format'
import type { ProductSummary } from '../types'

export function ProductCard({ product }: { product: ProductSummary }) {
  const soldOut = product.status === 'SOLD_OUT'

  return (
    <Link to={`/products/${product.id}`} className="group block">
      <div className="relative aspect-square overflow-hidden rounded-lg bg-gray-100">
        {product.thumbnailUrl && (
          <img
            src={product.thumbnailUrl}
            alt={product.name}
            loading="lazy"
            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
          />
        )}
        {soldOut && (
          <div className="absolute inset-0 flex items-center justify-center bg-black/40 text-sm font-semibold text-white">
            품절
          </div>
        )}
      </div>
      <p className="mt-3 text-xs text-gray-500">{product.categoryName}</p>
      <p className="mt-1 text-sm font-medium">{product.name}</p>
      <p className="mt-1 font-semibold">{formatPrice(product.price)}</p>
    </Link>
  )
}