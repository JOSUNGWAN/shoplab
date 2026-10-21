import { z } from 'zod'

export const shippingSchema = z.object({
  recipient: z.string().min(1, '받는 분 이름을 입력해 주세요.').max(20, '20자 이하로 입력해 주세요.'),
  phone: z.string().regex(/^01[0-9]-?\d{3,4}-?\d{4}$/, '연락처 형식이 올바르지 않습니다.'),
  zipcode: z.string().regex(/^\d{5}$/, '우편번호는 5자리 숫자입니다.'),
  address1: z.string().min(1, '주소를 입력해 주세요.').max(200),
  address2: z.string().max(200),
})

export type ShippingForm = z.infer<typeof shippingSchema>