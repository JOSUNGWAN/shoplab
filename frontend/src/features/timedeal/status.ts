import type { TimeDeal, TimeDealStatus } from './types'

export const TIME_DEAL_STATUS_LABEL: Record<TimeDealStatus, string> = {
  UPCOMING: '오픈 예정',
  ONGOING: '진행 중',
  SOLD_OUT: '품절',
  ENDED: '종료',
}

/**
 * 화면 표시용 상태. 서버 응답 이후 시간이 흘러도(오픈 시각이 지나도) 바로 바뀌도록
 * 현재 시각으로 다시 계산한다. 구매 가능 여부의 최종 판단은 서버가 한다.
 */
export function currentStatus(deal: TimeDeal, now: number): TimeDealStatus {
  const start = new Date(deal.startAt).getTime()
  const end = new Date(deal.endAt).getTime()
  if (now < start) return 'UPCOMING'
  if (now >= end) return 'ENDED'
  if (deal.remainingQuantity <= 0) return 'SOLD_OUT'
  return 'ONGOING'
}

/** 상태별로 카운트다운할 기준 시각 */
export function countdownTarget(deal: TimeDeal, status: TimeDealStatus) {
  if (status === 'UPCOMING') return new Date(deal.startAt).getTime()
  if (status === 'ONGOING') return new Date(deal.endAt).getTime()
  return null
}