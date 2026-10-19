import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/tipo-uniforme-service')
vi.mock('@/services/uniforme-service')

import { TipoUniformeFormPage } from '@/pages/tipo-uniforme-form-page'
import * as tipoService from '@/services/tipo-uniforme-service'
import * as uniformeService from '@/services/uniforme-service'
import { camiseta, uniforme } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

describe('TipoUniformeFormPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(tipoService.listAllTiposUniforme).mockResolvedValue([camiseta])
    vi.mocked(uniformeService.listAllUniformes).mockResolvedValue([
      uniforme({ id: 'u1', tamanho: 'P', quantidade: 4 }),
      uniforme({ id: 'u2', tamanho: 'M', quantidade: 12 }),
      uniforme({ id: 'u3', tamanho: 'G', quantidade: 0 }),
    ])
    vi.mocked(tipoService.createTipoUniforme).mockResolvedValue({ ...camiseta, id: 'novo' })
    vi.mocked(tipoService.updateTipoUniforme).mockResolvedValue(camiseta)
  })

  it('envia o limite de estoque baixo junto com o nome', async () => {
    const user = userEvent.setup()
    renderWithProviders(<TipoUniformeFormPage />)

    await user.type(screen.getByLabelText('Nome do tipo'), 'Jaqueta')
    const limite = screen.getByLabelText('Avisar quando restar')
    await user.clear(limite)
    await user.type(limite, '2')
    await user.click(screen.getByRole('button', { name: /criar tipo/i }))

    await waitFor(() => expect(tipoService.createTipoUniforme).toHaveBeenCalledWith({ tipo: 'Jaqueta', estoqueBaixo: 2 }))
  })

  it('mostra a prévia de alertas com o limite digitado', async () => {
    const user = userEvent.setup()
    renderWithProviders(<TipoUniformeFormPage />, {
      rota: `/tipos-uniforme/${camiseta.id}/editar`,
      caminho: '/tipos-uniforme/:id/editar',
    })

    expect(await screen.findByDisplayValue('Camiseta')).toBeInTheDocument()
    expect(await screen.findByText(/2 de 3 combinações ficam em alerta, 1 esgotada/i)).toBeInTheDocument()

    const limite = screen.getByLabelText('Avisar quando restar')
    await user.clear(limite)
    await user.type(limite, '12')
    expect(screen.getByText(/3 de 3 combinações ficam em alerta/i)).toBeInTheDocument()
  })

  it('recusa limite negativo', async () => {
    const user = userEvent.setup()
    renderWithProviders(<TipoUniformeFormPage />)

    await user.type(screen.getByLabelText('Nome do tipo'), 'Jaqueta')
    const limite = screen.getByLabelText('Avisar quando restar')
    await user.clear(limite)
    await user.type(limite, '-1')
    await user.click(screen.getByRole('button', { name: /criar tipo/i }))

    expect(await screen.findByText(/não pode ser negativo/i)).toBeInTheDocument()
    expect(tipoService.createTipoUniforme).not.toHaveBeenCalled()
  })
})
