import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/lote-service')
vi.mock('@/services/tipo-uniforme-service')

import { NovaEntradaDialog } from '@/components/entradas/nova-entrada-dialog'
import * as loteService from '@/services/lote-service'
import * as tipoService from '@/services/tipo-uniforme-service'
import { renderWithProviders } from '@/test/render'

const camiseta = { id: 'tu1', tipo: 'Camiseta' }
const calca = { id: 'tu2', tipo: 'Calça' }

const loteCriado = {
  id: 'l1',
  notaFiscalId: 'nf1',
  notaFiscalChaveAcesso: '123',
  fornecedor: 'Aurora',
  dataEntrega: '2026-09-29T00:00:00',
  itens: [],
}

async function preencherCabecalho(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Fornecedor'), 'Aurora')
  await user.type(screen.getByLabelText(/chave de acesso/i), '123')
}

describe('NovaEntradaDialog', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(tipoService.listAllTiposUniforme).mockResolvedValue([camiseta, calca])
    vi.mocked(loteService.createLote).mockResolvedValue(loteCriado)
  })

  it('envia a entrada com a data convertida para LocalDateTime', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntradaDialog open onOpenChange={() => {}} />)

    await screen.findByLabelText('Fornecedor')
    await preencherCabecalho(user)

    await user.click(screen.getByLabelText('Tipo do item 1'))
    await user.click(await screen.findByRole('option', { name: 'Camiseta' }))

    await user.clear(screen.getByLabelText('Quantidade do item 1'))
    await user.type(screen.getByLabelText('Quantidade do item 1'), '40')

    await user.click(screen.getByRole('button', { name: /registrar entrada/i }))

    await waitFor(() => expect(loteService.createLote).toHaveBeenCalled())

    const payload = vi.mocked(loteService.createLote).mock.calls[0][0]
    expect(payload.fornecedor).toBe('Aurora')
    expect(payload.chaveAcesso).toBe('123')
    // O backend espera LocalDateTime: sem fuso e sem "Z".
    expect(payload.dataEntrega).toMatch(/^\d{4}-\d{2}-\d{2}T00:00:00$/)
    expect(payload.itens).toEqual([{ tipoUniformeId: 'tu1', tamanho: 'M', sexo: 'MASCULINO', quantidade: 40 }])
  })

  it('recusa quantidade menor que 1', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntradaDialog open onOpenChange={() => {}} />)

    await screen.findByLabelText('Fornecedor')
    await preencherCabecalho(user)

    await user.click(screen.getByLabelText('Tipo do item 1'))
    await user.click(await screen.findByRole('option', { name: 'Camiseta' }))

    await user.clear(screen.getByLabelText('Quantidade do item 1'))
    await user.type(screen.getByLabelText('Quantidade do item 1'), '0')

    await user.click(screen.getByRole('button', { name: /registrar entrada/i }))

    expect(await screen.findByText('Quantidade mínima é 1.')).toBeInTheDocument()
    expect(loteService.createLote).not.toHaveBeenCalled()
  })

  it('não deixa repetir a mesma combinação de tipo, tamanho e sexo', async () => {
    const user = userEvent.setup()
    renderWithProviders(<NovaEntradaDialog open onOpenChange={() => {}} />)

    await screen.findByLabelText('Fornecedor')
    await preencherCabecalho(user)

    await user.click(screen.getByLabelText('Tipo do item 1'))
    await user.click(await screen.findByRole('option', { name: 'Camiseta' }))

    await user.click(screen.getByRole('button', { name: /adicionar item/i }))
    await user.click(screen.getByLabelText('Tipo do item 2'))
    await user.click(await screen.findByRole('option', { name: 'Camiseta' }))

    await user.click(screen.getByRole('button', { name: /registrar entrada/i }))

    expect(await screen.findByText(/itens repetidos/i)).toBeInTheDocument()
    expect(loteService.createLote).not.toHaveBeenCalled()
  })
})
