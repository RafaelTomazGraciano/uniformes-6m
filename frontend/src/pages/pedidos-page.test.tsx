import { screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/pedido-service')

import type { Pedido } from '@/lib/types/pedido'
import { PedidosPage } from '@/pages/pedidos-page'
import * as pedidoService from '@/services/pedido-service'
import { renderWithProviders } from '@/test/render'

const valido: Pedido = {
  id: 'p1',
  alunoId: 'a1',
  alunoNome: 'Maria Silva',
  alunoAtivo: true,
  usuarioId: 'u1',
  usuarioNome: 'Ana',
  dataEfetivada: '2026-10-18T10:00:00',
  itens: [
    {
      id: 'i1',
      pedidoId: 'p1',
      uniformeId: 'un1',
      uniformeTipoUniformeNome: 'Camiseta',
      uniformeTamanho: 'M',
      uniformeSexo: 'FEMININO',
      quantidade: 2,
    },
  ],
}

const desligado: Pedido = { ...valido, id: 'p2', alunoId: 'a3', alunoNome: 'Pedro Alves', alunoAtivo: false }

/* Simula dados inesperados vindos da API: campos nulos e um item que nem é objeto. */
const incompleto = { ...valido, id: 'p3', alunoNome: null, usuarioNome: null, itens: null } as unknown as Pedido
const quebrado = null as unknown as Pedido

describe('PedidosPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  it('mantém a lista de pé quando uma linha vem com dados inválidos', async () => {
    vi.mocked(pedidoService.listPedidos).mockResolvedValue({
      content: [valido, desligado, incompleto, quebrado],
      totalElements: 4,
      totalPages: 1,
      number: 0,
      size: 20,
      first: true,
      last: true,
    })

    renderWithProviders(<PedidosPage />)

    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
    expect(screen.getByText('Pedro Alves')).toBeInTheDocument()
    expect(screen.getByText('Saiu')).toBeInTheDocument()
    expect(screen.getByText('Aluno não identificado')).toBeInTheDocument()
    expect(screen.getByText('Não foi possível exibir esta entrega.')).toBeInTheDocument()
    expect(screen.queryByText(/não foi possível carregar as entregas/i)).not.toBeInTheDocument()
  })
})
