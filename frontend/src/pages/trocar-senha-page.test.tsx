import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/usuario-service')

import { TrocarSenhaPage } from '@/pages/trocar-senha-page'
import { ApiError } from '@/lib/api-client'
import { trocarSenha } from '@/services/usuario-service'
import { renderWithProviders } from '@/test/render'

describe('TrocarSenhaPage', () => {
  beforeEach(() => {
    vi.mocked(trocarSenha).mockReset()
  })

  it('exige a senha atual antes de enviar', async () => {
    const user = userEvent.setup()
    renderWithProviders(<TrocarSenhaPage />, { rota: '/trocar-senha', caminho: '/trocar-senha' })

    await user.type(screen.getByLabelText('E-mail'), 'maria@teste.com')
    await user.type(screen.getByLabelText('Nova senha'), 'novaSenha1')
    await user.type(screen.getByLabelText('Confirmar nova senha'), 'novaSenha1')
    await user.click(screen.getByRole('button', { name: 'Trocar senha' }))

    expect(await screen.findByText('Informe a senha atual.')).toBeInTheDocument()
    expect(trocarSenha).not.toHaveBeenCalled()
  })

  it('envia e-mail, senha atual e nova senha e volta para o login', async () => {
    vi.mocked(trocarSenha).mockResolvedValue({ message: 'Senha atualizada com sucesso' })
    const user = userEvent.setup()
    const { router } = renderWithProviders(<TrocarSenhaPage />, { rota: '/trocar-senha', caminho: '/trocar-senha' })

    await user.type(screen.getByLabelText('E-mail'), 'maria@teste.com')
    await user.type(screen.getByLabelText('Senha atual'), '123456')
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
    await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
  })

  it('continua na página quando a senha atual está errada', async () => {
    vi.mocked(trocarSenha).mockRejectedValue(new ApiError('Senha atual incorreta', 400))
    const user = userEvent.setup()
    const { router } = renderWithProviders(<TrocarSenhaPage />, { rota: '/trocar-senha', caminho: '/trocar-senha' })

    await user.type(screen.getByLabelText('E-mail'), 'maria@teste.com')
    await user.type(screen.getByLabelText('Senha atual'), 'errada')
    await user.type(screen.getByLabelText('Nova senha'), 'novaSenha1')
    await user.type(screen.getByLabelText('Confirmar nova senha'), 'novaSenha1')
    await user.click(screen.getByRole('button', { name: 'Trocar senha' }))

    await waitFor(() => expect(trocarSenha).toHaveBeenCalled())
    expect(router.state.location.pathname).toBe('/trocar-senha')
  })
})
