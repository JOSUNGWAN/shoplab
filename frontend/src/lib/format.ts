const priceFormatter = new Intl.NumberFormat('ko-KR')

export function formatPrice(price: number) {
  return `${priceFormatter.format(price)}원`
}