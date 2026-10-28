const priceFormatter = new Intl.NumberFormat('ko-KR')
const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' })

export function formatPrice(price: number) {
  return `${priceFormatter.format(price)}원`
}

export function formatDateTime(value: string) {
  return dateTimeFormatter.format(new Date(value))
}

/** 남은 시간(ms)을 "1일 02:03:04" 또는 "02:03:04"로 */
export function formatDuration(ms: number) {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000))
  const days = Math.floor(totalSeconds / 86400)
  const h = Math.floor((totalSeconds % 86400) / 3600)
  const m = Math.floor((totalSeconds % 3600) / 60)
  const s = totalSeconds % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  const time = `${pad(h)}:${pad(m)}:${pad(s)}`
  return days > 0 ? `${days}일 ${time}` : time
}