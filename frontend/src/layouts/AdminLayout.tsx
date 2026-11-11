import { Link, NavLink, Outlet } from 'react-router'

const navClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? 'font-semibold text-white' : 'text-gray-400 hover:text-white'

export default function AdminLayout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-gray-900">
        <div className="mx-auto flex h-14 max-w-7xl items-center gap-8 px-4">
          <Link to="/admin" className="font-bold text-white">
            ShopLab <span className="text-sm font-normal text-gray-400">Admin</span>
          </Link>
          <nav className="flex gap-5 text-sm">
            <NavLink to="/admin/dashboard" className={navClass}>
              대시보드
            </NavLink>
            <NavLink to="/admin/orders" className={navClass}>
              주문 관리
            </NavLink>
            <NavLink to="/admin/products" className={navClass}>
              상품 관리
            </NavLink>
          </nav>
          <Link to="/" className="ml-auto text-sm text-gray-400 hover:text-white">
            쇼핑몰로 이동 →
          </Link>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
