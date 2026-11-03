type TokenPayload = {
  role?: string
}

/**
 * 액세스 토큰(JWT)의 payload에서 role을 읽는다.
 * 서명 검증은 하지 않는다. 메뉴 표시용일 뿐이고, 실제 권한 검사는 서버가 한다.
 */
export function getTokenRole(token: string | null): string | null {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return (JSON.parse(json) as TokenPayload).role ?? null
  } catch {
    return null
  }
}