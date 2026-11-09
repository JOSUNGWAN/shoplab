export type AdminProductStatus = 'ON_SALE' | 'SOLD_OUT' | 'HIDDEN'

export type AdminProduct = {
  id: number
  name: string
  categoryId: number
  categoryName: string
  price: number
  stockQuantity: number
  status: AdminProductStatus
  thumbnailUrl: string | null
}

export type AdminProductDetail = AdminProduct & {
  description: string | null
}

export type AdminProductSearchParams = {
  keyword?: string
  categoryId?: number
  status?: AdminProductStatus
  page: number
  size: number
}

export type AdminProductPayload = {
  categoryId: number
  name: string
  description: string
  price: number
  stockQuantity?: number // 등록할 때만 보냄
  thumbnailUrl?: string
}

// /api/categories 응답 (트리면 children이 있고, 평평한 목록이면 없음 → 둘 다 처리)
export type CategoryNode = {
  id: number
  name: string
  children?: CategoryNode[]
}

export type CategoryOption = {
  id: number
  label: string
}