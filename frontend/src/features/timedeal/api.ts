import { api } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type { OrderDetail } from '../order/types'
import type { TimeDeal, TimeDealOrderRequest } from './types'

export async function getTimeDeals() {
  const { data } = await api.get<ApiResponse<TimeDeal[]>>('/time-deals')
  return data.data
}

export async function getTimeDeal(timeDealId: number) {
  const { data } = await api.get<ApiResponse<TimeDeal>>(`/time-deals/${timeDealId}`)
  return data.data
}

export async function purchaseTimeDeal(timeDealId: number, body: TimeDealOrderRequest) {
  const { data } = await api.post<ApiResponse<OrderDetail>>(`/time-deals/${timeDealId}/orders`, body)
  return data.data
}