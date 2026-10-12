export type ApiResponse<T> = {
  success: boolean
  code: string
  message: string | null
  data: T
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}