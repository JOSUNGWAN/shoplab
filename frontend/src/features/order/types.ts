export type OrderStatus = 'PENDING_PAYMENT' | 'PAID' | 'PREPARING' | 'SHIPPING' | 'DELIVERED' | 'CANCELED'

export type OrderItem = {
  productId: number
  productName: string
  orderPrice: number
  quantity: number
  subtotal: number
}

export type OrderDetail = {
  orderNumber: string
  status: OrderStatus
  totalAmount: number
  orderedAt: string
  recipient: string
  phone: string
  zipcode: string
  address1: string
  address2: string | null
  items: OrderItem[]
}

export type OrderSummary = {
  orderNumber: string
  status: OrderStatus
  totalAmount: number
  orderedAt: string
  title: string
  itemCount: number
}

export type CreateOrderRequest = {
  items: { productId: number; quantity: number }[]
  recipient: string
  phone: string
  zipcode: string
  address1: string
  address2?: string
}

// 장바구니 → 주문서로 넘기는 상품 정보 (화면 표시용)
export type CheckoutItem = {
  productId: number
  productName: string
  price: number
  quantity: number
  thumbnailUrl: string | null
}