import axios, { isAxiosError } from 'axios'
import { useAuthStore } from '../stores/authStore'

export const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// 요청 보내기 전: 토큰이 있으면 Authorization 헤더 추가
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 응답 받은 후: 401이면 로그아웃 처리 (토큰 만료 등)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (isAxiosError(error) && error.response?.status === 401) {
      useAuthStore.getState().logout()
    }
    return Promise.reject(error)
  },
)

// 백엔드 공통 응답의 message를 꺼내는 함수
export function getErrorMessage(error: unknown): string {
  if (isAxiosError(error)) {
    return error.response?.data?.message ?? '서버에 연결할 수 없습니다.'
  }
  return '알 수 없는 오류가 발생했습니다.'
}