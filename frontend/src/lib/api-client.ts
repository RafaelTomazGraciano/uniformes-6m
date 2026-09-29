import { clearStoredSession, getStoredSession } from '@/lib/auth'

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'

export class ApiError extends Error {
  status: number
  errors?: Record<string, string>

  constructor(message: string, status: number, errors?: Record<string, string>) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.errors = errors
  }
}

type ApiFetchOptions = Omit<RequestInit, 'body'> & { body?: unknown }

async function parseErrorBody(response: Response): Promise<{ message?: string; errors?: Record<string, string> }> {
  try {
    return (await response.json()) as { message?: string; errors?: Record<string, string> }
  } catch {
    return {}
  }
}

async function request(path: string, options: ApiFetchOptions = {}): Promise<Response> {
  const session = getStoredSession()
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')

  if (session?.token) {
    headers.set('Authorization', `Bearer ${session.token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  if (response.status === 401) {
    clearStoredSession()
  }

  if (!response.ok) {
    const payload = await parseErrorBody(response)
    throw new ApiError(payload.message ?? 'Não foi possível concluir a operação.', response.status, payload.errors)
  }

  return response
}

export async function apiFetch<T>(path: string, options: ApiFetchOptions = {}): Promise<T> {
  const response = await request(path, options)

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

/** Os relatórios respondem application/pdf; `response.json()` os quebraria. */
export async function apiFetchBlob(path: string, options: ApiFetchOptions = {}): Promise<Blob> {
  const response = await request(path, options)
  return await response.blob()
}
