import { useEffect, useState } from 'react'

type Health = {
  status: string
  time: string
}

function App() {
  const [health, setHealth] = useState<Health | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/health')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data: Health) => setHealth(data))
      .catch((err: Error) => setError(err.message))
  }, [])

  return (
    <div style={{ padding: 40 }}>
      <h1>ShopLab</h1>
      {error && <p>백엔드 연결 실패: {error}</p>}
      {!error && !health && <p>확인 중...</p>}
      {health && (
        <p>
          백엔드 상태: <strong>{health.status}</strong> ({health.time})
        </p>
      )}
    </div>
  )
}

export default App