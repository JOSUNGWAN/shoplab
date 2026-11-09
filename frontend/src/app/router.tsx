import { createBrowserRouter, Navigate } from 'react-router'
import UserLayout from '../layouts/UserLayout'
import HomePage from '../pages/HomePage'
import LoginPage from '../pages/LoginPage'
import SignupPage from '../pages/SignupPage'
import ProductDetailPage from '../pages/ProductDetailPage'
import ProductListPage from '../pages/ProductListPage'
import { ProtectedRoute } from '../components/ProtectedRoute'
import CartPage from '../pages/CartPage'
import CheckoutPage from '../pages/CheckoutPage'
import OrderDetailPage from '../pages/OrderDetailPage'
import OrderListPage from '../pages/OrderListPage'
import PaymentFailPage from '../pages/PaymentFailPage'
import PaymentSuccessPage from '../pages/PaymentSuccessPage'
import TimeDealDetailPage from '../pages/TimeDealDetailPage'
import TimeDealListPage from '../pages/TimeDealListPage'
import AdminRoute from '../components/AdminRoute'
import AdminLayout from '../layouts/AdminLayout'
import AdminOrdersPage from '../pages/admin/AdminOrdersPage'
import AdminProductsPage from '../pages/admin/AdminProductsPage'
import AdminProductNewPage from '../pages/admin/AdminProductNewPage'
import AdminProductEditPage from '../pages/admin/AdminProductEditPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <UserLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'products', element: <ProductListPage /> },
      { path: 'products/:productId', element: <ProductDetailPage /> },
      { path: 'time-deals', element: <TimeDealListPage /> },
      { path: 'time-deals/:timeDealId', element: <TimeDealDetailPage /> },
      {
        path: 'cart',
        element: (
          <ProtectedRoute>
            <CartPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'checkout',
        element: (
          <ProtectedRoute>
            <CheckoutPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'payments/success',
        element: (
          <ProtectedRoute>
            <PaymentSuccessPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'payments/fail',
        element: (
          <ProtectedRoute>
            <PaymentFailPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'mypage/orders',
        element: (
          <ProtectedRoute>
            <OrderListPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'mypage/orders/:orderNumber',
        element: (
          <ProtectedRoute>
            <OrderDetailPage />
          </ProtectedRoute>
        ),
      },
    ],
  },
  {
    path: '/admin',
    element: <AdminRoute />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { index: true, element: <Navigate to="orders" replace /> },
          { path: 'orders', element: <AdminOrdersPage /> },
          { path: 'products', element: <AdminProductsPage /> },
          { path: 'products/new', element: <AdminProductNewPage /> },
          { path: 'products/:productId/edit', element: <AdminProductEditPage /> },
        ],
      },
    ],
  },
])
