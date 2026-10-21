import { useQueryClient } from '@tanstack/react-query'
import { Link, Outlet, useNavigate } from 'react-router'
import { useAuthStore } from '../stores/authStore'
import { logout as logoutApi } from '../features/auth/api'
import { cartKeys, useCartCount } from '../features/cart/hooks'

export default function UserLayout() {
  const accessToken = useAuthStore((state) => state.accessToken)
  const logout = useAuthStore((state) => state.logout)
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { data: cartCount } = useCartCount()

  const handleLogout = async () => {
    try {
      await logoutApi()
    } finally {
      logout()
      queryClient.removeQueries({ queryKey: ['me'] })
      queryClient.removeQueries({ queryKey: cartKeys.all })
      navigate('/')
    }
  }

  return (
    <div className="min-h-screen bg-white text-gray-900">
      <header className="border-b border-gray-200">
        <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
          <div className="flex items-center gap-8">
            <Link to="/" className="text-lg font-bold">
              ShopLab
            </Link>
            <Link to="/products" className="text-sm text-gray-700 hover:text-gray-900">
              상품
            </Link>
          </div>
          <nav className="flex items-center gap-4 text-sm">
            <Link to="/cart" className="flex items-center gap-1">
              장바구니
              {!!cartCount && (
                <span className="min-w-5 rounded-full bg-gray-900 px-1.5 text-center text-xs leading-5 text-white">
                  {cartCount}
                </span>
              )}
            </Link>
            {accessToken ? (
              <>
                <Link to="/mypage/orders">주문내역</Link>
                <button onClick={handleLogout}>로그아웃</button>
              </>
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
