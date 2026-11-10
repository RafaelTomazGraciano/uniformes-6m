import { act, screen, waitFor } from '@testing-library/react'
import { toast } from 'sonner'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { ProtectedRoute } from '@/components/auth/protected-route'
import { apiFetch } from '@/lib/api-client'
import { MENSAGEM_SESSAO_EXPIRADA, expireSession } from '@/lib/auth'
import { agoraEmSegundos, guardarSessao, sessaoCom } from '@/test/jwt'
import { renderWithProviders } from '@/test/render'

function renderProtegida() {
  return renderWithProviders(
    <ProtectedRoute>
      <p>Área logada</p>
    </ProtectedRoute>,
    { rota: '/dashboard', caminho: '/dashboard' },
  )
}

describe('ProtectedRoute e expiração da sessão', () => {
  afterEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  it('mostra o conteúdo com uma sessão válida', () => {
    guardarSessao(sessaoCom(agoraEmSegundos() + 3600))
    renderProtegida()

    expect(screen.getByText('Área logada')).toBeInTheDocument()
  })

  it('manda para /login quando o token guardado já venceu', () => {
    guardarSessao(sessaoCom(agoraEmSegundos() - 60))
    const { router } = renderProtegida()

    expect(screen.queryByText('Área logada')).not.toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/login')
  })

  it('redireciona a aba atual para /login com aviso quando a sessão é encerrada', async () => {
    const toastErro = vi.spyOn(toast, 'error')
    guardarSessao(sessaoCom(agoraEmSegundos() + 3600))
    const { router } = renderProtegida()
    expect(screen.getByText('Área logada')).toBeInTheDocument()

    act(() => expireSession())

    await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
    expect(toastErro).toHaveBeenCalledWith(MENSAGEM_SESSAO_EXPIRADA, { id: 'sessao-expirada' })
  })

  it('um 401 da API encerra a sessão e redireciona para /login', async () => {
    guardarSessao(sessaoCom(agoraEmSegundos() + 3600))
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ message: 'Sessão expirada ou inválida. Entre novamente.' }), { status: 401 }),
    )
    const { router } = renderProtegida()

    await act(async () => {
      await expect(apiFetch('/turma')).rejects.toMatchObject({ status: 401 })
    })

    await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
    expect(localStorage.getItem('uniformes-auth-session')).toBeNull()
  })

  it('sai sozinho quando o exp do token chega', async () => {
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout', 'Date'] })
    guardarSessao(sessaoCom(agoraEmSegundos() + 60))
    const { router } = renderProtegida()
    expect(screen.getByText('Área logada')).toBeInTheDocument()

    await act(async () => {
      vi.advanceTimersByTime(61_000)
    })

    expect(router.state.location.pathname).toBe('/login')
  })
})
