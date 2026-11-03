import { api } from '../../api/client'
import type { ApiResponse, PageResponse } from '../../types/api'
import type { AdminOrder, AdminOrderSearchParams, AdminProgressStatus, BulkStatusResult } from './types'

export async function searchAdminOrders(params: AdminOrderSearchParams) {
  const { data } = await api.get<ApiResponse<PageResponse<AdminOrder>>>('/admin/orders', { params })
  return data.data
}

export async function changeOrderStatus(request: { orderNumbers: string[]; status: AdminProgressStatus }) {
  const { data } = await api.patch<ApiResponse<BulkStatusResult>>('/admin/orders/status', request)
  return data.data
}