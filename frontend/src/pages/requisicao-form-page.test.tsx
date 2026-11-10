import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/tipo-uniforme-service')
vi.mock('@/services/uniforme-service')
vi.mock('@/services/requisicao-service')

import { RequisicaoFormPage } from '@/pages/requisicao-form-page'
import * as requisicaoService from '@/services/requisicao-service'
import * as tipoService from '@/services/tipo-uniforme-service'
import * as uniformeService from '@/services/uniforme-service'
import { bermuda, camiseta, uniforme } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

type Usuario = ReturnType<typeof userEvent.setup>

async function escolherTipo(user: Usuario, nome: RegExp) {
  await user.click(screen.getByLabelText('Tipo de uniforme'))
  await user.click(await screen.findByRole('option', { name: nome }))
}

function escolherTamanho(user: Usuario, tamanho: string) {
  const grupo = screen.getByRole('group', { name: 'Tamanho' })
  return user.click(within(grupo).getByRole('button', { name: `Tamanho ${tamanho}` }))
}

describe('RequisicaoFormPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(tipoService.listAllTiposUniforme).mockResolvedValue([camiseta, bermuda])
    vi.mocked(uniformeService.listAllUniformes).mockResolvedValue([uniforme({ id: 'u1', tamanho: 'P', quantidade: 2 })])
    vi.mocked(requisicaoService.createRequisicao).mockResolvedValue({
      id: 'r1',
      tipoUniformeId: camiseta.id,
      tipoUniforme: camiseta.tipo,
      tamanho: 'P',
      sexo: 'MASCULINO',
      quantidade: 30,
      observacao: null,
      status: 'PENDENTE',
      usuarioId: 'us1',
      usuarioNome: 'Rafael',
      dataCriacao: '2026-11-10T10:00:00',
      dataConclusao: null,
    })
  })

  it('envia a requisição com tipo, sexo, tamanho, quantidade e observação', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RequisicaoFormPage />)

    await escolherTipo(user, /Camiseta/)
    await user.click(screen.getByRole('button', { name: 'Masculino' }))
    await escolherTamanho(user, 'P')
    const quantidade = screen.getByLabelText('Quantidade')
    await user.clear(quantidade)
    await user.type(quantidade, '30')
    await user.type(screen.getByLabelText('Observação (opcional)'), 'Turma nova no 6º ano')

    expect(await screen.findByText(/30 × Camiseta masculino tamanho P/)).toBeInTheDocument()
    expect(screen.getByText('2 peças')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /enviar requisição/i }))

    await waitFor(() =>
      expect(requisicaoService.createRequisicao).toHaveBeenCalledWith({
        tipoUniformeId: camiseta.id,
        sexo: 'MASCULINO',
        tamanho: 'P',
        quantidade: 30,
        observacao: 'Turma nova no 6º ano',
      }),
    )
  })

  it('bloqueia o envio sem tamanho e avisa no campo', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RequisicaoFormPage />)

    await escolherTipo(user, /Camiseta/)
    await user.click(screen.getByRole('button', { name: 'Masculino' }))
    await user.click(screen.getByRole('button', { name: /enviar requisição/i }))

    expect(await screen.findByText('Escolha o tamanho')).toBeInTheDocument()
    expect(screen.getByRole('group', { name: 'Tamanho' })).toHaveAttribute('aria-invalid', 'true')
    expect(requisicaoService.createRequisicao).not.toHaveBeenCalled()
  })

  it('chega preenchido pelo link "Solicitar" da reposição', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RequisicaoFormPage />, { rota: `/?tipo=${bermuda.id}&sexo=FEMININO&tamanho=G` })

    expect(await screen.findByText(/1 × Bermuda feminino tamanho G/)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /enviar requisição/i }))

    await waitFor(() =>
      expect(requisicaoService.createRequisicao).toHaveBeenCalledWith({
        tipoUniformeId: bermuda.id,
        sexo: 'FEMININO',
        tamanho: 'G',
        quantidade: 1,
        observacao: undefined,
      }),
    )
  })
})
