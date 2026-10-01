import { useQueryClient } from '@tanstack/react-query'
import { Link, Outlet, useNavigate } from 'react-router'
import { useAuthStore } from '../stores/authStore'
import { logout as logoutApi } from '../features/auth/api'

export default function UserLayout() {
  const accessToken = useAuthStore((state) => state.accessToken)
  const logout = useAuthStore((state) => state.logout)
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await logoutApi()
    } finally {
      logout()
      queryClient.removeQueries({ queryKey: ['me'] })
      navigate('/')
    }
  }

  return (
    <div className="min-h-screen bg-white text-gray-900">
      <header className="border-b border-gray-200">
        <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
          <Link to="/" className="text-lg font-bold">ShopLab</Link>
          <nav className="flex gap-4 text-sm">
            {accessToken ? (
              <button onClick={handleLogout}>로그아웃</button>
            ) : (
              <>
                <Link to="/login">로그인</Link>
                <Link to="/signup">회원가입</Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4">
        <Outlet />
      </main>
    </div>
  )
}