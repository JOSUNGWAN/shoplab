import { useEffect, type ReactNode } from 'react'
import { refreshAccessToken } from '../api/client'
import { useAuthStore } from '../stores/authStore'

export function AuthBootstrap({ children }: { children: ReactNode }) {
  const isInitialized = useAuthStore((state) => state.isInitialized)

  useEffect(() => {
    refreshAccessToken()
      .catch(() => {
        // 쿠키가 없거나 만료 → 비로그인 상태로 시작
      })
      .finally(() => useAuthStore.getState().setInitialized())
  }, [])

  if (!isInitialized) {
    return <p className="py-16 text-center text-gray-500">불러오는 중...</p>
  }

  return children
}