export type AuthUser = {
  id: string
  name: string
  email: string
}

export type AuthSession = {
  token: string
  user: AuthUser
}

export type LoginCredentials = {
  email: string
  senha: string
}

export type RegisterPayload = {
  nome: string
  email: string
  senha: string
}

const AUTH_STORAGE_KEY = 'uniformes-auth-session'
const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'

/** Disparado na própria aba quando a sessão é encerrada por expiração ou 401.
    O evento `storage` só chega às outras abas, então a aba atual precisa deste. */
export const SESSION_CLEARED_EVENT = 'auth:session-cleared'

export const MENSAGEM_SESSAO_EXPIRADA = 'Sua sessão expirou. Entre novamente.'

/** Lê o `exp` (em segundos) do payload do JWT. Só decodifica, não valida a assinatura:
    quem valida é o backend. Devolve null se o token estiver malformado ou sem `exp`. */
export function getTokenExpiration(token: string): number | null {
  const payload = token.split('.')[1]
  if (!payload) return null

  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    const json = JSON.parse(atob(padded)) as { exp?: unknown }
    return typeof json.exp === 'number' ? json.exp : null
  } catch {
    return null
  }
}

export function isTokenExpired(token: string, agoraMs = Date.now()): boolean {
  const exp = getTokenExpiration(token)
  return exp !== null && exp * 1000 <= agoraMs
}

export function getStoredSession(): AuthSession | null {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)

  if (!raw) {
    return null
  }

  try {
    const session = JSON.parse(raw) as AuthSession

    // Token vencido guardado de outra visita: sem isso a tela abria "logada" e toda consulta voltava vazia.
    if (!session?.token || isTokenExpired(session.token)) {
      localStorage.removeItem(AUTH_STORAGE_KEY)
      return null
    }

    return session
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

export function setStoredSession(session: AuthSession) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearStoredSession() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

/** Encerra a sessão por expiração (401 da API ou `exp` atingido) e avisa a aba atual. */
export function expireSession() {
  clearStoredSession()
  window.dispatchEvent(new CustomEvent(SESSION_CLEARED_EVENT))
}

async function parseErrorMessage(response: Response) {
  try {
    const payload = (await response.json()) as { message?: string; error?: string }
    return payload.message ?? payload.error ?? 'Não foi possível concluir a autenticação.'
  } catch {
    return 'Não foi possível concluir a autenticação.'
  }
}

export async function loginUser(credentials: LoginCredentials): Promise<AuthSession> {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: credentials.email,
      senha: credentials.senha,
    }),
  })

  if (!response.ok) {
    const message = await parseErrorMessage(response)
    throw new Error(message)
  }

  const payload = (await response.json()) as { token?: string }

  if (!payload.token) {
    throw new Error('Resposta inválida do servidor de autenticação.')
  }

  const email = credentials.email.trim()

  const session: AuthSession = {
    token: payload.token,
    user: {
      id: crypto.randomUUID(),
      email,
      name: email.split('@')[0] ?? 'Usuário',
    },
  }

  return session
}

export async function registerUser(payload: RegisterPayload): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/usuario/registrar`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      nome: payload.nome,
      email: payload.email,
      senha: payload.senha,
    }),
  })

  if (!response.ok) {
    const message = await parseErrorMessage(response)
    throw new Error(message)
  }
}
