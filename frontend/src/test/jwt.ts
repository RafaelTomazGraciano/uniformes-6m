import type { AuthSession } from '@/lib/auth'

function base64url(valor: string) {
  return btoa(valor).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

/** JWT falso (assinatura qualquer): o frontend só decodifica o payload, quem valida é o backend. */
export function fakeJwt(payload: Record<string, unknown>) {
  return `${base64url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))}.${base64url(JSON.stringify(payload))}.assinatura`
}

export function sessaoCom(expSegundos: number, email = 'fulano@test.com'): AuthSession {
  return {
    token: fakeJwt({ sub: email, exp: expSegundos }),
    user: { id: '1', name: 'Fulano', email },
  }
}

export const agoraEmSegundos = () => Math.floor(Date.now() / 1000)

export function guardarSessao(session: AuthSession) {
  localStorage.setItem('uniformes-auth-session', JSON.stringify(session))
}
