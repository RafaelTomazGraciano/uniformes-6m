import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/lib/auth', () => ({
  getStoredSession: vi.fn(),
  clearStoredSession: vi.fn(),
}))

import { ApiError, apiFetch } from '@/lib/api-client'
import { clearStoredSession, getStoredSession } from '@/lib/auth'

describe('apiFetch', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('injects the Authorization header when there is a stored session', async () => {
    vi.mocked(getStoredSession).mockReturnValue({
      token: 'abc123',
      user: { id: '1', name: 'Fulano', email: 'fulano@test.com' },
    })
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(new Response(JSON.stringify({ ok: true }), { status: 200 }))

    await apiFetch('/aluno')

    const [, options] = fetchMock.mock.calls[0]!
    const headers = options?.headers as Headers
    expect(headers.get('Authorization')).toBe('Bearer abc123')
  })

  it('does not send an Authorization header when there is no session', async () => {
    vi.mocked(getStoredSession).mockReturnValue(null)
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(new Response(JSON.stringify({ ok: true }), { status: 200 }))

    await apiFetch('/aluno')

    const [, options] = fetchMock.mock.calls[0]!
    const headers = options?.headers as Headers
    expect(headers.get('Authorization')).toBeNull()
  })

  it('throws an ApiError with the backend message on error responses', async () => {
    vi.mocked(getStoredSession).mockReturnValue(null)
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ message: 'Turma não encontrada' }), { status: 404 }),
    )

    await expect(apiFetch('/aluno')).rejects.toMatchObject(
      expect.objectContaining({ message: 'Turma não encontrada' }),
    )
  })

  it('clears the stored session on 401 responses', async () => {
    vi.mocked(getStoredSession).mockReturnValue({
      token: 'expired',
      user: { id: '1', name: 'Fulano', email: 'fulano@test.com' },
    })
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ message: 'Token expirado' }), { status: 401 }),
    )

    await expect(apiFetch('/aluno')).rejects.toBeInstanceOf(ApiError)
    expect(clearStoredSession).toHaveBeenCalled()
  })
})
