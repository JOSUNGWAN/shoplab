import { Link, useNavigate } from 'react-router'
import { getErrorMessage } from '../../api/client'
import ProductForm from '../../features/admin/product/components/ProductForm'
import { useCategoryOptions, useCreateProduct } from '../../features/admin/product/hooks'
import { EMPTY_PRODUCT_FORM, toProductPayload, type ProductFormValues } from '../../features/admin/product/schema'

export default function AdminProductNewPage() {
  const navigate = useNavigate()
  const categories = useCategoryOptions()
  const create = useCreateProduct()

  function handleSubmit(values: ProductFormValues) {
    create.mutate(toProductPayload(values, true), {
      onSuccess: () => navigate('/admin/products'),
    })
  }

  return (
    <div className="max-w-3xl space-y-4">
      <Link to="/admin/products" className="text-sm text-gray-500 hover:text-gray-900">
        ← 상품 목록
      </Link>
      <h1 className="text-xl font-bold">상품 등록</h1>

      {categories.isPending ? (
        <p className="text-gray-400">불러오는 중…</p>
      ) : (
        <ProductForm
          mode="create"
          defaultValues={EMPTY_PRODUCT_FORM}
          categories={categories.data ?? []}
          isSubmitting={create.isPending}
          errorMessage={create.isError ? getErrorMessage(create.error) : null}
          onSubmit={handleSubmit}
        />
      )}
    </div>
  )
}
