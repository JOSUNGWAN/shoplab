import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../../stores/authStore'
import { addCartItem, getCart, getCartCount, removeCartItems, updateCartItem } from './api'

export const cartKeys = {
  all: ['cart'] as const,
  detail: ['cart', 'detail'] as const,
  count: ['cart', 'count'] as const,
}

export function useCart() {
  return useQuery({ queryKey: cartKeys.detail, queryFn: getCart })
}

export function useCartCount() {
  const accessToken = useAuthStore((state) => state.accessToken)
  return useQuery({
    queryKey: cartKeys.count,
    queryFn: getCartCount,
    enabled: !!accessToken, // 로그인했을 때만 조회
  })
}

// 장바구니가 바뀌면 목록·개수 캐시를 한 번에 새로고침
function useInvalidateCart() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: cartKeys.all })
}

export function useAddCartItem() {
  const invalidate = useInvalidateCart()
  return useMutation({ mutationFn: addCartItem, onSuccess: invalidate })
}

export function useUpdateCartItem() {
  const invalidate = useInvalidateCart()
  return useMutation({ mutationFn: updateCartItem, onSuccess: invalidate })
}

export function useRemoveCartItems() {
  const invalidate = useInvalidateCart()
  return useMutation({ mutationFn: removeCartItems, onSuccess: invalidate })
}