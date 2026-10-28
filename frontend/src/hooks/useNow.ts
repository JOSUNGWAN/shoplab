import { useEffect, useState } from 'react'

/** 일정 간격으로 현재 시각(ms)을 갱신한다. 카운트다운용 */
export function useNow(intervalMs = 1000) {
  const [now, setNow] = useState(() => Date.now())

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), intervalMs)
    return () => clearInterval(timer)   // 화면을 벗어나면 타이머 정리
  }, [intervalMs])

  return now
}