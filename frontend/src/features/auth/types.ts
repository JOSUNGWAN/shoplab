export type SignupRequest = {
  email: string
  password: string
  name: string
  phone?: string
}

export type SignupResponse = {
  id: number
  email: string
  name: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type LoginResponse = {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export type Member = {
  id: number
  email: string
  name: string
  phone: string | null
  role: 'USER' | 'ADMIN'
}