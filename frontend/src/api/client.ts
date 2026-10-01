import axios, { isAxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '../stores/authStore'
import type { ApiResponse } from '../types/api'
import type { LoginResponse } from '../features/auth/types'

export const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// 재발급 전용 (인터셉터 없음 → 재발급 요청이 다시 재발급을 부르는 무한 루프 방지)
const authClient = axios.create({ baseURL: '/api' })

// 동시에 여러 요청이 401을 받아도 재발급은 한 번만
let refreshPromise: Promise<string> | null = null

export function refreshAccessToken(): Promise<string> {
  if (!refreshPromise) {
    refreshPromise = authClient
      .post<ApiResponse<LoginResponse>>('/auth/reissue')
      .then(({ data }) => {
        const token = data.data.accessToken
        useAuthStore.getState().setAccessToken(token)
        return token
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

type RetryConfig = InternalAxiosRequestConfig & { _retry?: boolean }

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (!isAxiosError(error) || !error.config) {
      return Promise.reject(error)
    }

    const original = error.config as RetryConfig
    const isAuthRequest = original.url?.startsWith('/auth/')

    if (error.response?.status === 401 && !original._retry && !isAuthRequest) {
      original._retry = true
      try {
        const token = await refreshAccessToken()
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      } catch (refreshError) {
        useAuthStore.getState().logout()
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

export function getErrorMessage(error: unknown): string {
  if (isAxiosError(error)) {
    return error.response?.data?.message ?? '서버에 연결할 수 없습니다.'
  }
  return '알 수 없는 오류가 발생했습니다.'
}