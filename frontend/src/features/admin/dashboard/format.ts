/**
 * 읽기 쉬운 눈금 만들기: 간격을 1·2·2.5·5·10 × 10ⁿ 중에서 고름
 * 예) 최댓값 25,000 → 0, 10,000, 20,000, 30,000
 */
export function niceTicks(max: number, integerOnly: boolean, count = 4): number[] {
  if (max <= 0) return [0, 1]
  const rough = max / count
  const magnitude = 10 ** Math.floor(Math.log10(rough))
  let step = [1, 2, 2.5, 5, 10].map((m) => m * magnitude).find((s) => s >= rough) ?? 10 * magnitude
  if (integerOnly) step = Math.max(1, Math.ceil(step))
  return Array.from({ length: Math.ceil(max / step) + 1 }, (_, i) => i * step)
}

/** 눈금 전체를 같은 단위로: 가장 큰 눈금이 1만 이상이면 모두 '만' 단위 */
export function amountTickFormatter(ticks: number[]) {
  const inMan = (ticks.at(-1) ?? 0) >= 10_000
  return (value: number) => {
    if (value === 0) return '0'
    if (!inMan) return value.toLocaleString()
    return `${(value / 10_000).toLocaleString('ko-KR', { maximumFractionDigits: 2 })}만`
  }
}

/** 2026-11-03 → 11/3 (문자열을 직접 나눠서 시간대 영향을 받지 않음) */
export function formatShortDate(date: string): string {
  const [, month, day] = date.split('-')
  return `${Number(month)}/${Number(day)}`
}

/** 2026-11-03 → 11월 3일 (화) */
export function formatLongDate(date: string): string {
  const [year, month, day] = date.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  })
}