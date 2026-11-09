import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { getErrorMessage } from '../../api/client'
import ProductForm from '../../features/admin/product/components/ProductForm'
import ProductStatusBadge from '../../features/admin/product/components/ProductStatusBadge'
import StockAdjustPanel from '../../features/admin/product/components/StockAdjustPanel'
import {
  useAdminProduct,
  useCategoryOptions,
  useChangeVisibility,
  useUpdateProduct,
} from '../../features/admin/product/hooks'
import { toFormValues, toProductPayload, type ProductFormValues } from '../../features/admin/product/schema'

export default function AdminProductEditPage() {
  const { productId } = useParams()
  const id = Number(productId)
  const product = useAdminProduct(id)
  const categories = useCategoryOptions()
  const update = useUpdateProduct()
  const visibility = useChangeVisibility()
  const [saved, setSaved] = useState(false)

  const backLink = (
    <Link to="/admin/products" className="text-sm text-gray-500 hover:text-gray-900">
      ← 상품 목록
    </Link>
  )

  if (!Number.isInteger(id) || id <= 0) {
    return (
      <div className="space-y-4">
        {backLink}
        <p>잘못된 상품 번호입니다.</p>
      </div>
    )
  }
  if (product.isError) {
    return (
      <div className="space-y-4">
        {backLink}
        <p className="rounded bg-red-50 px-4 py-3 text-sm text-red-700">{getErrorMessage(product.error)}</p>
      </div>
    )
  }
  if (product.isPending || categories.isPending) {
    return <p className="text-gray-400">불러오는 중…</p>
  }

  const current = product.data
  const hidden = current.status === 'HIDDEN'

  function handleSubmit(values: ProductFormValues) {
    setSaved(false)
    update.mutate({ id: current.id, ...toProductPayload(values, false) }, { onSuccess: () => setSaved(true) })
  }

  return (
    <div className="space-y-4">
      {backLink}
      <h1 className="text-xl font-bold">
        상품 수정 <span className="text-base font-normal text-gray-400">#{current.id}</span>
      </h1>

      <div className="grid items-start gap-6 lg:grid-cols-[1fr_320px]">
        <div className="space-y-3">
          {saved && <p className="rounded bg-green-50 px-3 py-2 text-sm text-green-700">저장했습니다.</p>}
          <ProductForm
            mode="edit"
            defaultValues={toFormValues(current)}
            categories={categories.data ?? []}
            isSubmitting={update.isPending}
            errorMessage={update.isError ? getErrorMessage(update.error) : null}
            onSubmit={handleSubmit}
          />
        </div>

        <div className="space-y-4">
          <StockAdjustPanel product={current} />

          <section className="space-y-3 rounded-lg border border-gray-200 bg-white p-5">
            <h2 className="font-semibold">노출 상태</h2>
            <div className="flex items-center gap-2">
              <ProductStatusBadge status={current.status} />
              <span className="text-sm text-gray-500">
                {hidden ? '쇼핑몰에 보이지 않습니다.' : '쇼핑몰에 노출 중입니다.'}
              </span>
            </div>
            <button
              type="button"
              onClick={() => visibility.mutate({ id: current.id, visible: hidden })}
              disabled={visibility.isPending}
              className="w-full rounded border border-gray-300 py-2 text-sm font-semibold hover:bg-gray-50 disabled:opacity-40"
            >
              {hidden ? '다시 보이기' : '숨기기'}
            </button>
            {visibility.isError && <p className="text-sm text-red-600">{getErrorMessage(visibility.error)}</p>}
          </section>
        </div>
      </div>
    </div>
  )
}
