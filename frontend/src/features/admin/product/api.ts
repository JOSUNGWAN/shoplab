import { api } from '../../../api/client'
import type { ApiResponse, PageResponse } from '../../../types/api'
import type {
  AdminProduct,
  AdminProductDetail,
  AdminProductPayload,
  AdminProductSearchParams,
  CategoryNode,
} from './types'

const BASE = '/admin/products'

export async function searchAdminProducts(params: AdminProductSearchParams) {
  const { data } = await api.get<ApiResponse<PageResponse<AdminProduct>>>(BASE, { params })
  return data.data
}

export async function getAdminProduct(id: number) {
  const { data } = await api.get<ApiResponse<AdminProductDetail>>(`${BASE}/${id}`)
  return data.data
}

export async function createProduct(payload: AdminProductPayload) {
  const { data } = await api.post<ApiResponse<AdminProductDetail>>(BASE, payload)
  return data.data
}

export async function updateProduct({ id, ...payload }: AdminProductPayload & { id: number }) {
  const { data } = await api.put<ApiResponse<AdminProductDetail>>(`${BASE}/${id}`, payload)
  return data.data
}

export async function adjustStock({ id, delta }: { id: number; delta: number }) {
  const { data } = await api.patch<ApiResponse<AdminProductDetail>>(`${BASE}/${id}/stock`, { delta })
  return data.data
}

export async function changeVisibility({ id, visible }: { id: number; visible: boolean }) {
  const { data } = await api.patch<ApiResponse<AdminProductDetail>>(`${BASE}/${id}/visibility`, { visible })
  return data.data
}

export async function getCategories() {
  const { data } = await api.get<ApiResponse<CategoryNode[]>>('/categories')
  return data.data
}