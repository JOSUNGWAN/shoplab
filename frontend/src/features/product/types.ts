export type ProductStatus = 'ON_SALE' | 'SOLD_OUT' | 'HIDDEN'

export type ProductSummary = {
  id: number
  name: string
  price: number
  status: ProductStatus
  thumbnailUrl: string | null
  categoryName: string
}

export type ProductDetail = {
  id: number
  name: string
  description: string
  price: number
  stockQuantity: number
  status: ProductStatus
  thumbnailUrl: string | null
  categoryId: number
  categoryName: string
}

export type Category = {
  id: number
  name: string
  children: Category[]
}

export type ProductListParams = {
  categoryId?: number
  page: number
  size: number
  sort: string
}