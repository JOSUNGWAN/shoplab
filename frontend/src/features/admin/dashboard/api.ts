import { api } from '../../../api/client'
import type { ApiResponse } from '../../../types/api'
import type { DailySales, DashboardPeriod, DashboardSummary, TopProduct } from './types'

const BASE = '/admin/dashboard'

export async function getDashboardSummary() {
  const { data } = await api.get<ApiResponse<DashboardSummary>>(`${BASE}/summary`)
  return data.data
}

export async function getDailySales(period: DashboardPeriod) {
  const { data } = await api.get<ApiResponse<DailySales[]>>(`${BASE}/daily-sales`, { params: period })
  return data.data
}

export async function getTopProducts(period: DashboardPeriod) {
  const { data } = await api.get<ApiResponse<TopProduct[]>>(`${BASE}/top-products`, {
    params: { ...period, limit: 5 },
  })
  return data.data
}