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

export function getStoredSession(): AuthSession | null {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as AuthSession
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
