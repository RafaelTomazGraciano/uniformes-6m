import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/tipo-uniforme-service')
vi.mock('@/services/lote-service')

import { EntradaFormPage } from '@/pages/entrada-form-page'
import * as loteService from '@/services/lote-service'
import * as tipoService from '@/services/tipo-uniforme-service'
import { bermuda, camiseta } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

type Usuario = ReturnType<typeof userEvent.setup>

const loteSalvo = {
  id: 'l1',
  notaFiscalId: 'n1',
  notaFiscalChaveAcesso: '1',
  fornecedor: 'Confecções Aurora',
  dataEntrega: '2026-10-18T00:00:00',
  itens: [],
}

async function preencherNota(user: Usuario) {
  await user.type(screen.getByLabelText('Fornecedor'), 'Confecções Aurora')
  await user.type(screen.getByLabelText('Chave de acesso da NF-e'), '1234')
}

async function escolherTipo(user: Usuario, linha: number, nome: RegExp) {
  await user.click(screen.getByRole('combobox', { name: `Tipo da linha ${linha}` }))
  await user.click(await screen.findByRole('option', { name: nome }))
}

describe('EntradaFormPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(tipoService.listAllTiposUniforme).mockResolvedValue([camiseta, bermuda])
    vi.mocked(loteService.createLote).mockResolvedValue(loteSalvo)
  })

  it('mostra o nome do tipo escolhido, nunca o UUID', async () => {
    const user = userEvent.setup()
    renderWithProviders(<EntradaFormPage />)

    await escolherTipo(user, 1, /Camiseta/)

    const gatilho = screen.getByRole('combobox', { name: 'Tipo da linha 1' })
    expect(gatilho).toHaveTextContent('Camiseta')
    expect(gatilho).not.toHaveTextContent(camiseta.id)
  })

  it('envia a mesma peça repetida em duas linhas', async () => {
    const user = userEvent.setup()
    renderWithProviders(<EntradaFormPage />)

    await preencherNota(user)
    await escolherTipo(user, 1, /Camiseta/)
    const quantidade = screen.getByLabelText('Quantidade da linha 1')
    await user.clear(quantidade)
    await user.type(quantidade, '20')
    await user.click(screen.getByRole('button', { name: /duplicar última/i }))
    const quantidade2 = screen.getByLabelText('Quantidade da linha 2')
    await user.clear(quantidade2)
    await user.type(quantidade2, '10')

    expect(screen.getAllByText(/serão somadas, 30 camiseta/i).length).toBeGreaterThan(0)
    await user.click(screen.getByRole('button', { name: /registrar entrada/i }))

    await waitFor(() => expect(loteService.createLote).toHaveBeenCalled())
    expect(vi.mocked(loteService.createLote).mock.calls[0][0].itens).toEqual([
      { tipoUniformeId: camiseta.id, sexo: 'FEMININO', tamanho: 'M', quantidade: 20 },
      { tipoUniformeId: camiseta.id, sexo: 'FEMININO', tamanho: 'M', quantidade: 10 },
    ])
  })

  it('lança pela grade rápida', async () => {
    const user = userEvent.setup()
    renderWithProviders(<EntradaFormPage />, { rota: '/?modo=grade' })

    await preencherNota(user)
    await user.click(await screen.findByRole('combobox', { name: /tipo para adicionar à grade/i }))
    await user.click(await screen.findByRole('option', { name: /Bermuda/ }))
    await user.click(screen.getByRole('button', { name: /adicionar tipo à grade/i }))

    const tabela = await screen.findByRole('table', { name: /quantidades recebidas de bermuda/i })
    await user.type(within(tabela).getByLabelText('Bermuda masculino tamanho P'), '12')
    await user.type(within(tabela).getByLabelText('Bermuda feminino tamanho G'), '4')
    await user.click(screen.getByRole('button', { name: /registrar entrada/i }))

    await waitFor(() => expect(loteService.createLote).toHaveBeenCalled())
    expect(vi.mocked(loteService.createLote).mock.calls[0][0].itens).toEqual(
      expect.arrayContaining([
        { tipoUniformeId: bermuda.id, sexo: 'MASCULINO', tamanho: 'P', quantidade: 12 },
        { tipoUniformeId: bermuda.id, sexo: 'FEMININO', tamanho: 'G', quantidade: 4 },
      ]),
    )
  })

  it('aponta os campos da nota que faltam', async () => {
    const user = userEvent.setup()
    renderWithProviders(<EntradaFormPage />)

    await screen.findByRole('combobox', { name: 'Tipo da linha 1' })
    await user.click(screen.getByRole('button', { name: /registrar entrada/i }))

    expect(await screen.findByText(/informe o fornecedor/i)).toBeInTheDocument()
    expect(screen.getByLabelText('Fornecedor')).toHaveAttribute('aria-invalid', 'true')
    expect(loteService.createLote).not.toHaveBeenCalled()
  })
})
