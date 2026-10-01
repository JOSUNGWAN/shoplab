import { api } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type {
  LoginRequest,
  LoginResponse,
  Member,
  SignupRequest,
  SignupResponse,
} from './types'

export async function signup(body: SignupRequest) {
  const { data } = await api.post<ApiResponse<SignupResponse>>('/auth/signup', body)
  return data.data
}

export async function login(body: LoginRequest) {
  const { data } = await api.post<ApiResponse<LoginResponse>>('/auth/login', body)
  return data.data
}

export async function getMe() {
  const { data } = await api.get<ApiResponse<Member>>('/members/me')
  return data.data
}

export async function logout() {
  await api.post('/auth/logout')
}