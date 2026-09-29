import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/uniforme-service')
vi.mock('@/services/pedido-service')

import { NovaEntregaPage } from '@/pages/nova-entrega-page'
import * as alunoService from '@/services/aluno-service'
import * as pedidoService from '@/services/pedido-service'
import * as uniformeService from '@/services/uniforme-service'
import { renderWithProviders } from '@/test/render'

const alunoMock = { id: 'a1', nome: 'Maria Silva', turmaId: 't1', turmaNome: '9º Ano A' }
const uniformeMock = {
  id: 'u1',
  tipoUniformeId: 'tu1',
  tipoUniformeNome: 'Camiseta',
  tamanho: 'M' as const,
  quantidade: 5,
  sexo: 'MASCULINO' as const,
}

async function selectOption(user: ReturnType<typeof userEvent.setup>, comboboxIndex: number, optionName: RegExp) {
  const comboboxes = screen.getAllByRole('combobox')
  await user.click(comboboxes[comboboxIndex]!)
  await user.click(await screen.findByRole('option', { name: optionName }))
}

describe('NovaEntregaPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(alunoService.listAllAlunos).mockResolvedValue([alunoMock])
    vi.mocked(uniformeService.listAllUniformes).mockResolvedValue([uniformeMock])
  })

  it('blocks submit and shows an error when the same item is selected twice', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaPage />)

    await selectOption(user, 0, /Maria Silva/i)
    await selectOption(user, 1, /Camiseta/i)

    await user.click(screen.getByRole('button', { name: /adicionar item/i }))
    await selectOption(user, 2, /Camiseta/i)

    await user.click(screen.getByRole('button', { name: /registrar entrega/i }))

    expect(await screen.findByText(/não é possível repetir o mesmo item/i)).toBeInTheDocument()
    expect(pedidoService.createPedido).not.toHaveBeenCalled()
  })

  it('blocks submit and shows an error when quantity exceeds available stock', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaPage />)

    await selectOption(user, 0, /Maria Silva/i)
    await selectOption(user, 1, /Camiseta/i)

    const quantidadeInput = screen.getAllByRole('spinbutton')[0]!
    await user.clear(quantidadeInput)
    await user.type(quantidadeInput, '10')

    await user.click(screen.getByRole('button', { name: /registrar entrega/i }))

    expect(await screen.findByText(/estoque insuficiente/i)).toBeInTheDocument()
    expect(pedidoService.createPedido).not.toHaveBeenCalled()
  })
})
