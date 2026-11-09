import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  adjustStock,
  changeVisibility,
  createProduct,
  getAdminProduct,
  getCategories,
  searchAdminProducts,
  updateProduct,
} from './api'
import { toCategoryOptions } from './categories'
import type { AdminProductDetail, AdminProductSearchParams } from './types'

export const adminProductKeys = {
  all: ['admin', 'products'] as const,
  lists: () => [...adminProductKeys.all, 'list'] as const,
  list: (params: AdminProductSearchParams) => [...adminProductKeys.lists(), params] as const,
  detail: (id: number) => [...adminProductKeys.all, 'detail', id] as const,
}

export function useAdminProducts(params: AdminProductSearchParams) {
  return useQuery({
    queryKey: adminProductKeys.list(params),
    queryFn: () => searchAdminProducts(params),
    placeholderData: keepPreviousData,
  })
}

export function useAdminProduct(id: number) {
  return useQuery({
    queryKey: adminProductKeys.detail(id),
    queryFn: () => getAdminProduct(id),
    enabled: Number.isInteger(id) && id > 0,
  })
}

export function useCategoryOptions() {
  return useQuery({
    queryKey: ['categories', 'options'],
    queryFn: getCategories,
    select: (nodes) => toCategoryOptions(nodes),
    staleTime: 5 * 60 * 1000, // 카테고리는 자주 안 바뀜
  })
}

/** 상품을 바꾸는 API는 모두 바뀐 상품을 돌려준다 → 상세 캐시는 바로 교체, 목록만 다시 조회 */
function useProductMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<AdminProductDetail>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: (product) => {
      queryClient.setQueryData(adminProductKeys.detail(product.id), product)
      queryClient.invalidateQueries({ queryKey: adminProductKeys.lists() })
    },
  })
}

export const useCreateProduct = () => useProductMutation(createProduct)
export const useUpdateProduct = () => useProductMutation(updateProduct)
export const useAdjustStock = () => useProductMutation(adjustStock)
export const useChangeVisibility = () => useProductMutation(changeVisibility)