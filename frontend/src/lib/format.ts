const priceFormatter = new Intl.NumberFormat('ko-KR')
const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' })

export function formatPrice(price: number) {
  return `${priceFormatter.format(price)}원`
}

export function formatDateTime(value: string) {
  return dateTimeFormatter.format(new Date(value))
}