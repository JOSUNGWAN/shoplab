import { api } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type { AddCartItemRequest, Cart, CartItem } from './types'

export async function getCart() {
  const { data } = await api.get<ApiResponse<Cart>>('/cart')
  return data.data
}

export async function getCartCount() {
  const { data } = await api.get<ApiResponse<{ count: number }>>('/cart/count')
  return data.data.count
}

export async function addCartItem(body: AddCartItemRequest) {
  const { data } = await api.post<ApiResponse<CartItem>>('/cart/items', body)
  return data.data
}

export async function updateCartItem({ cartItemId, quantity }: { cartItemId: number; quantity: number }) {
  const { data } = await api.patch<ApiResponse<CartItem>>(`/cart/items/${cartItemId}`, { quantity })
  return data.data
}

export async function removeCartItems(cartItemIds: number[]) {
  await api.delete('/cart/items', { params: { ids: cartItemIds.join(',') } })
}