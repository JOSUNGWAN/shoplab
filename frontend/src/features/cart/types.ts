export type CartItem = {
  id: number
  productId: number
  productName: string
  thumbnailUrl: string | null
  price: number
  quantity: number
  subtotal: number
  stockQuantity: number
  available: boolean
}

export type Cart = {
  items: CartItem[]
  totalQuantity: number
  totalPrice: number
}

export type AddCartItemRequest = {
  productId: number
  quantity: number
}