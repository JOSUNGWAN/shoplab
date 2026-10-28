export type TimeDealStatus = 'UPCOMING' | 'ONGOING' | 'SOLD_OUT' | 'ENDED'

export type TimeDeal = {
  id: number
  productId: number
  productName: string
  thumbnailUrl: string | null
  originalPrice: number
  dealPrice: number
  discountRate: number
  totalQuantity: number
  remainingQuantity: number
  startAt: string
  endAt: string
  status: TimeDealStatus
}

export type TimeDealOrderRequest = {
  recipient: string
  phone: string
  zipcode: string
  address1: string
  address2?: string
}