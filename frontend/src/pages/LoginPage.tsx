import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useLocation, useNavigate } from 'react-router'
import { FormField } from '../components/FormField'
import { getErrorMessage } from '../api/client'
import { login } from '../features/auth/api'
import { loginSchema, type LoginForm } from '../features/auth/schemas'
import { useAuthStore } from '../stores/authStore'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const signedUp = (location.state as { signedUp?: boolean } | null)?.signedUp
  const setAccessToken = useAuthStore((state) => state.setAccessToken)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) })

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      setAccessToken(data.accessToken)
      navigate('/')
    },
  })

  return (
    <div className="mx-auto max-w-sm py-16">
      <h1 className="mb-8 text-2xl font-bold">로그인</h1>

      {signedUp && (
        <p className="mb-6 rounded-lg bg-green-50 px-4 py-3 text-sm text-green-700">
          회원가입이 완료되었습니다. 로그인해 주세요.
        </p>
      )}

      <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4" noValidate>
        <FormField id="email" label="이메일" type="email" autoComplete="email"
          error={errors.email?.message} {...register('email')} />
        <FormField id="password" label="비밀번호" type="password" autoComplete="current-password"
          error={errors.password?.message} {...register('password')} />

        {mutation.isError && (
          <p className="text-sm text-red-600">{getErrorMessage(mutation.error)}</p>
        )}

        <button type="submit" disabled={mutation.isPending}
          className="w-full rounded-lg bg-gray-900 py-3 font-medium text-white disabled:opacity-50">
          {mutation.isPending ? '로그인 중...' : '로그인'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-600">
        아직 회원이 아니신가요?{' '}
        <Link to="/signup" className="font-medium text-gray-900 underline">회원가입</Link>
      </p>
    </div>
  )
}