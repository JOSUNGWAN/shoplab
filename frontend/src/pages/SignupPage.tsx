import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router'
import { FormField } from '../components/FormField'
import { getErrorMessage } from '../api/client'
import { signup } from '../features/auth/api'
import { signupSchema, type SignupForm } from '../features/auth/schemas'

export default function SignupPage() {
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignupForm>({
    resolver: zodResolver(signupSchema),
    defaultValues: { phone: '' },
  })

  const mutation = useMutation({
    mutationFn: signup,
    onSuccess: () => navigate('/login', { state: { signedUp: true } }),
  })

  const onSubmit = ({ email, password, name, phone }: SignupForm) => {
    mutation.mutate({ email, password, name, phone: phone || undefined })
  }

  return (
    <div className="mx-auto max-w-sm py-16">
      <h1 className="mb-8 text-2xl font-bold">회원가입</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <FormField id="email" label="이메일" type="email" autoComplete="email"
          error={errors.email?.message} {...register('email')} />
        <FormField id="password" label="비밀번호" type="password" autoComplete="new-password"
          error={errors.password?.message} {...register('password')} />
        <FormField id="passwordConfirm" label="비밀번호 확인" type="password" autoComplete="new-password"
          error={errors.passwordConfirm?.message} {...register('passwordConfirm')} />
        <FormField id="name" label="이름" autoComplete="name"
          error={errors.name?.message} {...register('name')} />
        <FormField id="phone" label="휴대폰 번호 (선택)" placeholder="010-1234-5678" autoComplete="tel"
          error={errors.phone?.message} {...register('phone')} />

        {mutation.isError && (
          <p className="text-sm text-red-600">{getErrorMessage(mutation.error)}</p>
        )}

        <button type="submit" disabled={mutation.isPending}
          className="w-full rounded-lg bg-gray-900 py-3 font-medium text-white disabled:opacity-50">
          {mutation.isPending ? '가입 중...' : '가입하기'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-600">
        이미 회원이신가요?{' '}
        <Link to="/login" className="font-medium text-gray-900 underline">로그인</Link>
      </p>
    </div>
  )
}