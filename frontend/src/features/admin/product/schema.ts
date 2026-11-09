import { z } from 'zod'
import type { DefaultValues } from 'react-hook-form'
import type { AdminProductDetail, AdminProductPayload } from './types'

export const productFormSchema = z.object({
  categoryId: z.number({ error: '카테고리를 선택해 주세요.' }),
  name: z.string().trim().min(1, '상품명을 입력해 주세요.').max(100, '상품명은 100자 이하로 입력해 주세요.'),
  description: z.string().trim().min(1, '상품 설명을 입력해 주세요.').max(5000, '상품 설명은 5000자 이하로 입력해 주세요.'),
  price: z
    .number({ error: '가격을 입력해 주세요.' })
    .int('가격은 정수로 입력해 주세요.')
    .min(100, '가격은 100원 이상이어야 합니다.')
    .max(10_000_000, '가격은 1천만 원 이하여야 합니다.'),
  stockQuantity: z
    .number({ error: '재고를 입력해 주세요.' })
    .int('재고는 정수로 입력해 주세요.')
    .min(0, '재고는 0개 이상이어야 합니다.')
    .max(99_999, '재고는 99,999개 이하여야 합니다.'),
  thumbnailUrl: z
    .string()
    .trim()
    .max(500, '이미지 주소가 너무 깁니다.')
    .refine((value) => value === '' || /^https?:\/\/\S+$/.test(value), '이미지 주소는 http:// 또는 https://로 시작해야 합니다.'),
})

export type ProductFormValues = z.infer<typeof productFormSchema>

export const EMPTY_PRODUCT_FORM: DefaultValues<ProductFormValues> = {
  name: '',
  description: '',
  stockQuantity: 0,
  thumbnailUrl: '',
}

export function toFormValues(product: AdminProductDetail): ProductFormValues {
  return {
    categoryId: product.categoryId,
    name: product.name,
    description: product.description ?? '',
    price: product.price,
    stockQuantity: product.stockQuantity,
    thumbnailUrl: product.thumbnailUrl ?? '',
  }
}

/** 폼 값 → API 요청. 이미지 주소가 비어 있으면 보내지 않음(null로 저장), 재고는 등록할 때만 */
export function toProductPayload(values: ProductFormValues, includeStock: boolean): AdminProductPayload {
  return {
    categoryId: values.categoryId,
    name: values.name,
    description: values.description,
    price: values.price,
    stockQuantity: includeStock ? values.stockQuantity : undefined,
    thumbnailUrl: values.thumbnailUrl || undefined,
  }
}