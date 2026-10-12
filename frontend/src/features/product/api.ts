import { api } from '../../api/client'
import type { ApiResponse, PageResponse } from '../../types/api'
import type { Category, ProductDetail, ProductListParams, ProductSummary } from './types'

export async function getCategories() {
  const { data } = await api.get<ApiResponse<Category[]>>('/categories')
  return data.data
}

export async function getProducts(params: ProductListParams) {
  const { data } = await api.get<ApiResponse<PageResponse<ProductSummary>>>('/products', { params })
  return data.data
}

export async function getProduct(productId: number) {
  const { data } = await api.get<ApiResponse<ProductDetail>>(`/products/${productId}`)
  return data.data
}