import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/requisicao-service')

import { RequisicoesPage } from '@/pages/requisicoes-page'
import * as requisicaoService from '@/services/requisicao-service'
import type { Page } from '@/lib/types/page'
import type { Requisicao } from '@/lib/types/requisicao'
import { camiseta } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

function pagina(content: Requisicao[]): Page<Requisicao> {
  return { content, totalElements: content.length, totalPages: content.length ? 1 : 0, number: 0, size: 20, first: true, last: true }
}

const pendente: Requisicao = {
  id: 'r1',
  tipoUniformeId: camiseta.id,
  tipoUniforme: camiseta.tipo,
  tamanho: 'P',
  sexo: 'MASCULINO',
  quantidade: 30,
  observacao: null,
  status: 'PENDENTE',
  usuarioId: 'us1',
  usuarioNome: 'Rafael Lima',
  dataCriacao: '2026-11-10T10:00:00',
  dataConclusao: null,
}

describe('RequisicoesPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('avisa quando não há requisição em andamento', async () => {
    vi.mocked(requisicaoService.listRequisicoes).mockResolvedValue(pagina([]))
    renderWithProviders(<RequisicoesPage />)

    expect(await screen.findByText('Nenhuma requisição em andamento')).toBeInTheDocument()
    expect(requisicaoService.listRequisicoes).toHaveBeenCalledWith(expect.objectContaining({ sort: 'dataCriacao,desc' }), {
      status: ['PENDENTE'],
    })
  })

  it('lista os pedidos em andamento com solicitante e status', async () => {
    vi.mocked(requisicaoService.listRequisicoes).mockResolvedValue(pagina([pendente]))
    renderWithProviders(<RequisicoesPage />)

    expect(await screen.findByText('Rafael Lima')).toBeInTheDocument()
    expect(screen.getByText('Camiseta')).toBeInTheDocument()
    expect(screen.getByText('Masculino')).toBeInTheDocument()
    expect(screen.getByText('30')).toBeInTheDocument()
    expect(screen.getAllByText('Em andamento').length).toBeGreaterThan(0)
  })

  it('troca para atendidas e canceladas', async () => {
    const user = userEvent.setup()
    vi.mocked(requisicaoService.listRequisicoes).mockResolvedValue(pagina([]))
    renderWithProviders(<RequisicoesPage />)

    await screen.findByText('Nenhuma requisição em andamento')
    await user.click(screen.getByRole('button', { name: 'Atendidas e canceladas' }))

    expect(await screen.findByText('Nenhuma requisição concluída')).toBeInTheDocument()
    await waitFor(() =>
      expect(requisicaoService.listRequisicoes).toHaveBeenLastCalledWith(expect.anything(), { status: ['ATENDIDA', 'CANCELADA'] }),
    )
  })
})
