import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { getDailySales, getDashboardSummary, getTopProducts } from './api'
import type { DashboardPeriod } from './types'

export const dashboardKeys = {
  all: ['admin', 'dashboard'] as const,
  summary: () => [...dashboardKeys.all, 'summary'] as const,
  daily: (period: DashboardPeriod) => [...dashboardKeys.all, 'daily', period] as const,
  top: (period: DashboardPeriod) => [...dashboardKeys.all, 'top', period] as const,
}

export function useDashboardSummary() {
  return useQuery({
    queryKey: dashboardKeys.summary(),
    queryFn: getDashboardSummary,
    refetchInterval: 60_000, // 화면을 켜 두면 1분마다 갱신
  })
}

export function useDailySales(period: DashboardPeriod) {
  return useQuery({
    queryKey: dashboardKeys.daily(period),
    queryFn: () => getDailySales(period),
    placeholderData: keepPreviousData, // 기간을 바꿀 때 이전 차트를 유지 (깜빡임 방지)
  })
}

export function useTopProducts(period: DashboardPeriod) {
  return useQuery({
    queryKey: dashboardKeys.top(period),
    queryFn: () => getTopProducts(period),
    placeholderData: keepPreviousData,
  })
}