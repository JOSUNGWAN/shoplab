import { useState, type ReactNode } from 'react'
import { useForm, useWatch, type DefaultValues } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { productFormSchema, type ProductFormValues } from '../schema'
import type { CategoryOption } from '../types'

type Props = {
  mode: 'create' | 'edit'
  defaultValues: DefaultValues<ProductFormValues>
  categories: CategoryOption[]
  isSubmitting: boolean
  errorMessage?: string | null
  onSubmit: (values: ProductFormValues) => void
}

const inputClass = 'w-full rounded border border-gray-300 px-3 py-2 text-sm'

function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="block space-y-1">
      <span className="text-sm font-medium text-gray-700">{label}</span>
      {children}
      {error && <span className="block text-sm text-red-600">{error}</span>}
    </label>
  )
}

const URL_PATTERN = /^https?:\/\/\S+$/

function ImagePreview({ url }: { url: string }) {
  const [failed, setFailed] = useState(false)

  if (!URL_PATTERN.test(url) || failed) {
    return (
      <div className="flex size-32 items-center justify-center rounded border border-dashed border-gray-300 text-xs text-gray-400">
        {failed ? '이미지를 불러올 수 없음' : '미리보기 없음'}
      </div>
    )
  }
  return (
    <img src={url} alt="미리보기" onError={() => setFailed(true)} className="size-32 rounded border object-cover" />
  )
}

export default function ProductForm({ mode, defaultValues, categories, isSubmitting, errorMessage, onSubmit }: Props) {
  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<ProductFormValues>({
    resolver: zodResolver(productFormSchema),
    defaultValues,
  })
  const thumbnailUrl = useWatch({ control, name: 'thumbnailUrl' }) ?? ''

  return (
    <form
      onSubmit={(e) => handleSubmit(onSubmit)(e)}
      className="space-y-5 rounded-lg border border-gray-200 bg-white p-6"
    >
      <Field label="카테고리" error={errors.categoryId?.message}>
        <select {...register('categoryId', { valueAsNumber: true })} className={inputClass}>
          <option value="">선택하세요</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.label}
            </option>
          ))}
        </select>
      </Field>

      <Field label="상품명" error={errors.name?.message}>
        <input {...register('name')} maxLength={100} className={inputClass} />
      </Field>

      <div className="grid gap-5 sm:grid-cols-2">
        <Field label="판매가 (원)" error={errors.price?.message}>
          <input type="number" {...register('price', { valueAsNumber: true })} className={inputClass} />
        </Field>

        {mode === 'create' ? (
          <Field label="초기 재고 (개)" error={errors.stockQuantity?.message}>
            <input type="number" {...register('stockQuantity', { valueAsNumber: true })} className={inputClass} />
          </Field>
        ) : (
          <div className="space-y-1">
            <span className="text-sm font-medium text-gray-700">재고</span>
            <p className="rounded bg-gray-50 px-3 py-2 text-sm text-gray-500">오른쪽 재고 조정에서 입고·차감하세요.</p>
          </div>
        )}
      </div>

      <Field label="대표 이미지 주소" error={errors.thumbnailUrl?.message}>
        <input {...register('thumbnailUrl')} placeholder="https://..." className={inputClass} />
      </Field>
      <ImagePreview key={thumbnailUrl} url={thumbnailUrl} />

      <Field label="상품 설명" error={errors.description?.message}>
        <textarea {...register('description')} rows={6} className={inputClass} />
      </Field>

      {errorMessage && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{errorMessage}</p>}

      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full rounded bg-gray-900 py-2.5 text-sm font-semibold text-white disabled:bg-gray-400"
      >
        {isSubmitting ? '저장 중…' : mode === 'create' ? '상품 등록' : '변경 내용 저장'}
      </button>
    </form>
  )
}
