import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/uniforme-service')
vi.mock('@/services/pedido-service')

import { NovaEntregaPage } from '@/pages/nova-entrega-page'
import * as alunoService from '@/services/aluno-service'
import * as pedidoService from '@/services/pedido-service'
import * as uniformeService from '@/services/uniforme-service'
import { maria, uniforme } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

const camisetaM = uniforme({ id: 'u1', tamanho: 'M', quantidade: 5 })
const camisetaG = uniforme({ id: 'u2', tamanho: 'G', quantidade: 0 })

type Usuario = ReturnType<typeof userEvent.setup>

async function escolherAluno(user: Usuario) {
  const campo = screen.getByLabelText('Aluno')
  await waitFor(() => expect(campo).toBeEnabled())
  await user.type(campo, 'Maria')
  await user.click(await screen.findByRole('option', { name: /Maria Silva/i }))
}

async function preencherLinha(user: Usuario, numero: number, tamanho: string) {
  await user.click(screen.getByRole('combobox', { name: `Peça da linha ${numero}` }))
  await user.click(await screen.findByRole('option', { name: /Camiseta — Masculino/i }))
  const grupo = screen.getByRole('group', { name: `Tamanho da linha ${numero}` })
  await user.click(within(grupo).getByRole('button', { name: new RegExp(`tamanho ${tamanho}`, 'i') }))
}

describe('NovaEntregaPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(alunoService.listAllAlunos).mockResolvedValue([maria])
    vi.mocked(uniformeService.listAllUniformes).mockResolvedValue([camisetaM, camisetaG])
    vi.mocked(pedidoService.createPedido).mockResolvedValue({
      id: 'p1',
      alunoId: maria.id,
      alunoNome: maria.nome,
      usuarioId: 'u',
      usuarioNome: 'Ana',
      dataEfetivada: '2026-10-18T10:00:00',
      itens: [],
    })
  })

  it('aceita a mesma peça em duas linhas com quantidades diferentes', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaPage />)

    await escolherAluno(user)
    await preencherLinha(user, 1, 'M')
    await user.click(screen.getByRole('button', { name: /aumentar quantidade da linha 1/i }))
    await user.click(screen.getByRole('button', { name: /adicionar linha/i }))
    await preencherLinha(user, 2, 'M')

    expect(screen.getAllByText(/no total 3 de 5 em estoque/i)).toHaveLength(2)
    await user.click(screen.getByRole('button', { name: /^registrar entrega$/i }))

    await waitFor(() =>
      expect(pedidoService.createPedido).toHaveBeenCalledWith({
        alunoId: 'a1',
        itens: [
          { uniformeId: 'u1', quantidade: 2 },
          { uniformeId: 'u1', quantidade: 1 },
        ],
      }),
    )
  })

  it('bloqueia quando a soma das linhas passa do estoque', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaPage />)

    await escolherAluno(user)
    await preencherLinha(user, 1, 'M')
    const quantidade1 = screen.getByLabelText('Quantidade da linha 1')
    await user.clear(quantidade1)
    await user.type(quantidade1, '4')
    await user.click(screen.getByRole('button', { name: /adicionar linha/i }))
    await preencherLinha(user, 2, 'M')
    const quantidade2 = screen.getByLabelText('Quantidade da linha 2')
    await user.clear(quantidade2)
    await user.type(quantidade2, '3')
    await user.click(screen.getByRole('button', { name: /^registrar entrega$/i }))

    expect((await screen.findAllByText(/somando as 2 linhas são 7, mas só há 5/i)).length).toBe(2)
    expect(pedidoService.createPedido).not.toHaveBeenCalled()
  })

  it('não deixa escolher um tamanho esgotado', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaPage />)

    await user.click(await screen.findByRole('combobox', { name: 'Peça da linha 1' }))
    await user.click(await screen.findByRole('option', { name: /Camiseta — Masculino/i }))

    expect(screen.getByRole('button', { name: /tamanho G, esgotado/i })).toBeDisabled()
  })

  it('exige um aluno antes de registrar', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaPage />)

    await preencherLinha(user, 1, 'M')
    await user.click(screen.getByRole('button', { name: /^registrar entrega$/i }))

    expect(await screen.findByText(/escolha o aluno que vai receber/i)).toBeInTheDocument()
    expect(pedidoService.createPedido).not.toHaveBeenCalled()
  })

  it('no catálogo, cada “Adicionar” vira uma linha nova na sacola', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntregaPage />, { rota: '/?modo=catalogo&aluno=a1' })

    const tamanhos = await screen.findByRole('group', { name: /tamanho de camiseta masculino/i })
    await user.click(within(tamanhos).getByRole('button', { name: /tamanho M/i }))
    await user.click(screen.getByRole('button', { name: /^adicionar$/i }))
    await user.click(within(tamanhos).getByRole('button', { name: /tamanho M/i }))
    await user.click(screen.getByRole('button', { name: /^adicionar$/i }))

    const sacola = screen.getByText('Sacola da entrega').closest('[data-slot="card"]') as HTMLElement
    expect(within(sacola).getAllByRole('button', { name: /remover linha/i })).toHaveLength(2)
    expect(within(sacola).getByText(/\(2 linhas\)/)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /^registrar entrega$/i }))
    await waitFor(() =>
      expect(pedidoService.createPedido).toHaveBeenCalledWith({
        alunoId: 'a1',
        itens: [
          { uniformeId: 'u1', quantidade: 1 },
          { uniformeId: 'u1', quantidade: 1 },
        ],
      }),
    )
  })
})
