import type { ComponentProps } from 'react'

type FormFieldProps = ComponentProps<'input'> & {
  label: string
  error?: string
}

export function FormField({ label, error, id, ...inputProps }: FormFieldProps) {
  return (
    <div className="space-y-1">
      <label htmlFor={id} className="block text-sm font-medium text-gray-700">
        {label}
      </label>
      <input
        id={id}
        {...inputProps}
        className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:border-gray-900"
      />
      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  )
}