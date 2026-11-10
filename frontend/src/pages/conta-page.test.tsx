import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/usuario-service')

import { ContaPage } from '@/pages/conta-page'
import { atualizarUsuario, trocarSenha } from '@/services/usuario-service'
import { agoraEmSegundos, guardarSessao, sessaoCom } from '@/test/jwt'
import { renderWithProviders } from '@/test/render'

const AVISO = 'Ao trocar o e-mail você precisará entrar novamente.'

describe('ContaPage', () => {
  beforeEach(() => {
    guardarSessao(sessaoCom(agoraEmSegundos() + 3600, 'maria@teste.com'))
  })

  afterEach(() => {
    localStorage.clear()
    vi.mocked(atualizarUsuario).mockReset()
    vi.mocked(trocarSenha).mockReset()
  })

  it('mantendo o e-mail, salva e continua logado', async () => {
    vi.mocked(atualizarUsuario).mockResolvedValue({ idUsuario: 'u1', nome: 'Maria Silva', email: 'maria@teste.com' })
    const user = userEvent.setup()
    const { router } = renderWithProviders(<ContaPage />, { rota: '/conta', caminho: '/conta' })

    const nome = await screen.findByLabelText('Nome')
    await user.clear(nome)
    await user.type(nome, 'Maria Silva')
    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Salvar dados' }))

    await waitFor(() => expect(atualizarUsuario).toHaveBeenCalled())
    expect(router.state.location.pathname).toBe('/conta')
    expect(localStorage.getItem('uniformes-auth-session')).not.toBeNull()
  })

  it('avisa e encerra a sessão depois de trocar o e-mail', async () => {
    vi.mocked(atualizarUsuario).mockResolvedValue({ idUsuario: 'u1', nome: 'maria', email: 'nova@teste.com' })
    const user = userEvent.setup()
    const { router } = renderWithProviders(<ContaPage />, { rota: '/conta', caminho: '/conta' })

    const email = await screen.findByLabelText('E-mail')
    await user.clear(email)
    await user.type(email, 'nova@teste.com')
    expect(screen.getByText(AVISO)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Salvar dados' }))

    await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
    expect(localStorage.getItem('uniformes-auth-session')).toBeNull()
  })

  it('envia a senha atual ao trocar a senha', async () => {
    vi.mocked(trocarSenha).mockResolvedValue({ message: 'Senha atualizada com sucesso' })
    const user = userEvent.setup()
    renderWithProviders(<ContaPage />, { rota: '/conta', caminho: '/conta' })

    await user.type(await screen.findByLabelText('Senha atual'), '123456')
    await user.type(screen.getByLabelText('Nova senha'), 'novaSenha1')
    await user.type(screen.getByLabelText('Confirmar nova senha'), 'novaSenha1')
    await user.click(screen.getByRole('button', { name: 'Trocar senha' }))

    await waitFor(() =>
      expect(trocarSenha).toHaveBeenCalledWith({
        email: 'maria@teste.com',
        senhaAtual: '123456',
        novaSenha: 'novaSenha1',
      }),
    )
  })
})
