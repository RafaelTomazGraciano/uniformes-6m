import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/uniforme-service')
vi.mock('@/services/pedido-service')

import { NovaEntregaDialog } from '@/components/pedidos/nova-entrega-dialog'
import * as alunoService from '@/services/aluno-service'
import * as pedidoService from '@/services/pedido-service'
import * as uniformeService from '@/services/uniforme-service'
import { renderWithProviders } from '@/test/render'

const alunoMock = { id: 'a1', nome: 'Maria Silva', turmaId: 't1', turmaNome: '9º Ano A' }

const uniformeM = {
  id: 'u1',
  tipoUniformeId: 'tu1',
  tipoUniformeNome: 'Camiseta',
  tamanho: 'M' as const,
  quantidade: 5,
  sexo: 'MASCULINO' as const,
}

const uniformeGEsgotado = { ...uniformeM, id: 'u2', tamanho: 'G' as const, quantidade: 0 }

async function escolherAluno(user: ReturnType<typeof userEvent.setup>) {
  const campo = screen.getByLabelText('Aluno')
  await waitFor(() => expect(campo).toBeEnabled())
  await user.type(campo, 'Maria')
  await user.click(await screen.findByRole('option', { name: /Maria Silva/i }))
}

async function escolherUniforme(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByRole('combobox', { name: /uniforme do item 1/i }))
  await user.click(await screen.findByRole('option', { name: /Camiseta/i }))
}

describe('NovaEntregaDialog', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(alunoService.listAllAlunos).mockResolvedValue([alunoMock])
    vi.mocked(uniformeService.listAllUniformes).mockResolvedValue([uniformeM, uniformeGEsgotado])
  })

  it('registra a entrega com o aluno e o tamanho escolhidos', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaDialog open onOpenChange={() => {}} />)

    await escolherAluno(user)
    await escolherUniforme(user)
    await user.click(await screen.findByRole('button', { name: /tamanho M/i }))
    await user.click(screen.getByRole('button', { name: /^registrar entrega$/i }))

    expect(pedidoService.createPedido).toHaveBeenCalledWith({
      alunoId: 'a1',
      itens: [{ uniformeId: 'u1', quantidade: 1 }],
    })
  })

  it('não deixa escolher um tamanho sem estoque', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaDialog open onOpenChange={() => {}} />)

    await escolherAluno(user)
    await escolherUniforme(user)

    expect(await screen.findByRole('button', { name: /tamanho G/i })).toBeDisabled()
  })

  it('trava a quantidade no que existe em estoque', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaDialog open onOpenChange={() => {}} />)

    await escolherAluno(user)
    await escolherUniforme(user)
    await user.click(await screen.findByRole('button', { name: /tamanho M/i }))

    const aumentar = screen.getByRole('button', { name: /aumentar quantidade/i })
    for (let clique = 0; clique < 4; clique += 1) {
      await user.click(aumentar)
    }

    expect(screen.getByLabelText(/quantidade do item 1/i)).toHaveValue(5)
    expect(aumentar).toBeDisabled()
  })

  it('exige um aluno antes de registrar', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaDialog open onOpenChange={() => {}} />)

    await escolherUniforme(user)
    await user.click(await screen.findByRole('button', { name: /tamanho M/i }))
    await user.click(screen.getByRole('button', { name: /^registrar entrega$/i }))

    expect(await screen.findByText(/selecione um aluno/i)).toBeInTheDocument()
    expect(pedidoService.createPedido).not.toHaveBeenCalled()
  })
})
