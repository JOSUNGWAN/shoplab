import { api } from '../../api/client'
import type { ApiResponse, PageResponse } from '../../types/api'
import type { CreateOrderRequest, OrderDetail, OrderSummary } from './types'

export async function createOrder(body: CreateOrderRequest) {
  const { data } = await api.post<ApiResponse<OrderDetail>>('/orders', body)
  return data.data
}

export async function getOrders(page: number) {
  const { data } = await api.get<ApiResponse<PageResponse<OrderSummary>>>('/orders', { params: { page } })
  return data.data
}

export async function getOrder(orderNumber: string) {
  const { data } = await api.get<ApiResponse<OrderDetail>>(`/orders/${orderNumber}`)
  return data.data
}

export async function cancelOrder(orderNumber: string) {
  const { data } = await api.post<ApiResponse<OrderDetail>>(`/orders/${orderNumber}/cancel`)
  return data.data
}

export async function confirmPayment(body: { paymentKey: string; orderId: string; amount: number }) {
  const { data } = await api.post<ApiResponse<OrderDetail>>('/payments/confirm', body)
  return data.data
}