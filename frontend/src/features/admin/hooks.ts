import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { changeOrderStatus, searchAdminOrders } from './api'
import type { AdminOrderSearchParams } from './types'

export const adminOrderKeys = {
  all: ['admin', 'orders'] as const,
  list: (params: AdminOrderSearchParams) => [...adminOrderKeys.all, 'list', params] as const,
}

export function useAdminOrders(params: AdminOrderSearchParams) {
  return useQuery({
    queryKey: adminOrderKeys.list(params),
    queryFn: () => searchAdminOrders(params),
    placeholderData: keepPreviousData, // 페이지를 넘길 때 이전 목록을 유지 (깜빡임 방지)
  })
}

export function useChangeOrderStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: changeOrderStatus,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: adminOrderKeys.all }),
  })
}