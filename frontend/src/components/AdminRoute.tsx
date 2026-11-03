import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuthStore } from '../stores/authStore'
import { getTokenRole } from '../lib/token'

export default function AdminRoute() {
  const accessToken = useAuthStore((state) => state.accessToken)
  const location = useLocation()

  if (!accessToken) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }
  if (getTokenRole(accessToken) !== 'ADMIN') {
    return <Navigate to="/" replace />
  }
  return <Outlet />
}
