import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { getMe } from '../features/auth/api'
import { useAuthStore } from '../stores/authStore'

export default function HomePage() {
  const accessToken = useAuthStore((state) => state.accessToken)

  const { data: me, isLoading } = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    enabled: !!accessToken,
  })

  if (!accessToken) {
    return (
      <div className="py-16 text-center">
        <h1 className="mb-4 text-2xl font-bold">ShopLab에 오신 것을 환영합니다</h1>
        <Link to="/login" className="text-gray-900 underline">로그인하러 가기</Link>
      </div>
    )
  }

  if (isLoading) return <p className="py-16 text-center">불러오는 중...</p>

  return (
    <div className="mx-auto max-w-sm py-16">
      <h1 className="mb-6 text-2xl font-bold">{me?.name}님, 반갑습니다</h1>
      <dl className="space-y-2 rounded-lg border border-gray-200 p-4 text-sm">
        <div className="flex justify-between"><dt className="text-gray-500">이메일</dt><dd>{me?.email}</dd></div>
        <div className="flex justify-between"><dt className="text-gray-500">휴대폰</dt><dd>{me?.phone ?? '-'}</dd></div>
        <div className="flex justify-between"><dt className="text-gray-500">권한</dt><dd>{me?.role}</dd></div>
      </dl>
    </div>
  )
}