import { z } from 'zod'

const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,20}$/
const PHONE_REGEX = /^01[0-9]-?\d{3,4}-?\d{4}$/

const email = z
  .string()
  .min(1, '이메일을 입력해 주세요.')
  .email('이메일 형식이 올바르지 않습니다.')

export const loginSchema = z.object({
  email,
  password: z.string().min(1, '비밀번호를 입력해 주세요.'),
})

export const signupSchema = z
  .object({
    email,
    password: z
      .string()
      .regex(PASSWORD_REGEX, '비밀번호는 영문, 숫자, 특수문자(!@#$%^&*)를 포함해 8~20자여야 합니다.'),
    passwordConfirm: z.string().min(1, '비밀번호를 한 번 더 입력해 주세요.'),
    name: z.string().min(2, '이름은 2~20자여야 합니다.').max(20, '이름은 2~20자여야 합니다.'),
    phone: z.string().refine((v) => v === '' || PHONE_REGEX.test(v), '휴대폰 번호 형식이 올바르지 않습니다.'),
  })
  .refine((data) => data.password === data.passwordConfirm, {
    message: '비밀번호가 일치하지 않습니다.',
    path: ['passwordConfirm'],
  })

export type LoginForm = z.infer<typeof loginSchema>
export type SignupForm = z.infer<typeof signupSchema>