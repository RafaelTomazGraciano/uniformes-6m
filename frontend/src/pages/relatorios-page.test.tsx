import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/relatorio-service')
vi.mock('@/services/turma-service')
vi.mock('sonner', () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}))

import { RelatoriosPage } from '@/pages/relatorios-page'
import * as relatorioService from '@/services/relatorio-service'
import * as turmaService from '@/services/turma-service'
import { renderWithProviders } from '@/test/render'
import { toast } from 'sonner'

const pdf = new Blob(['%PDF'], { type: 'application/pdf' })
const csv = new Blob(['\uFEFFTipo;Tamanho'], { type: 'text/csv' })

const turmaA = { id: 't1', nome: '9º Ano A', turno: 'DIURNO' as const, ensino: 'FUNDAMENTAL' as const, quantidadeAlunos: 0 }
const turmaB = { id: 't2', nome: '6º Ano B', turno: 'VESPERTINO' as const, ensino: 'FUNDAMENTAL' as const, quantidadeAlunos: 0 }

function cardDe(titulo: string) {
  /* Ancorar no título evita pegar o card vizinho. Pelo heading, e não pelo
     texto: "Estoque" também é um item do menu. */
  const cabecalho = screen.getByRole('heading', { name: titulo })
  return cabecalho.closest('[data-slot="card"]') as HTMLElement
}

function cardBotao(titulo: string, formato: 'pdf' | 'csv' = 'pdf') {
  // Pelo nome, não pelo primeiro button: o card de turma tem um select antes.
  const nome = formato === 'pdf' ? 'Baixar PDF' : 'Baixar CSV (Excel)'
  return within(cardDe(titulo)).getByRole('button', { name: nome })
}

describe('RelatoriosPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(relatorioService.baixarRelatorioEstoque).mockResolvedValue(pdf)
    vi.mocked(relatorioService.baixarRelatorioComPeriodo).mockResolvedValue(pdf)
    vi.mocked(turmaService.listAllTurmas).mockResolvedValue([turmaA, turmaB])
  })

  it('baixa o relatório de estoque sem exigir período', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Estoque'))

    await waitFor(() => expect(relatorioService.baixarRelatorioEstoque).toHaveBeenCalledWith('pdf'))
    expect(relatorioService.salvarArquivo).toHaveBeenCalledWith(pdf, 'relatorio-estoque.pdf')
  })

  it('baixa o estoque em CSV para o Excel, com a extensão .csv', async () => {
    vi.mocked(relatorioService.baixarRelatorioEstoque).mockResolvedValue(csv)
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Estoque', 'csv'))

    await waitFor(() => expect(relatorioService.baixarRelatorioEstoque).toHaveBeenCalledWith('csv'))
    expect(relatorioService.salvarArquivo).toHaveBeenCalledWith(csv, 'relatorio-estoque.csv')
  })

  it('escolhe o formato em cada relatório com período', async () => {
    vi.mocked(relatorioService.baixarRelatorioComPeriodo).mockResolvedValue(csv)
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Entregas por turma', 'csv'))

    await waitFor(() => expect(relatorioService.baixarRelatorioComPeriodo).toHaveBeenCalled())
    const [relatorio, periodo, turmaId, formato] = vi.mocked(relatorioService.baixarRelatorioComPeriodo).mock.calls[0]
    expect(relatorio).toBe('entregas-turma')
    expect(periodo.anoInicio).toBe(new Date().getFullYear())
    expect(turmaId).toBeUndefined()
    expect(formato).toBe('csv')
    expect(relatorioService.salvarArquivo).toHaveBeenCalledWith(csv, 'relatorio-entregas-turma.csv')
  })

  it('envia o período escolhido nos relatórios que dependem dele', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Saídas'))

    await waitFor(() => expect(relatorioService.baixarRelatorioComPeriodo).toHaveBeenCalled())

    const [relatorio, periodo, , formato] = vi.mocked(relatorioService.baixarRelatorioComPeriodo).mock.calls[0]
    expect(relatorio).toBe('saida')
    expect(formato).toBe('pdf')
    expect(periodo.tipo).toBe('MES')
    expect(periodo.anoInicio).toBe(new Date().getFullYear())
  })

  it('mostra o erro quando o relatório não pode ser gerado', async () => {
    vi.mocked(relatorioService.baixarRelatorioComPeriodo).mockRejectedValue(new Error('Falha ao gerar o relatório'))

    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Saídas', 'csv'))

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith('Falha ao gerar o relatório'))
    expect(relatorioService.salvarArquivo).not.toHaveBeenCalled()
  })

  it('baixa as entregas de todas as turmas quando nenhuma é escolhida', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Entregas por turma'))

    await waitFor(() => expect(relatorioService.baixarRelatorioComPeriodo).toHaveBeenCalled())

    const [relatorio, , turmaId] = vi.mocked(relatorioService.baixarRelatorioComPeriodo).mock.calls[0]
    expect(relatorio).toBe('entregas-turma')
    expect(turmaId).toBeUndefined()
    expect(relatorioService.salvarArquivo).toHaveBeenCalledWith(pdf, 'relatorio-entregas-turma.pdf')
  })

  it('restringe as entregas à turma escolhida', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(await screen.findByLabelText('Turma'))
    await user.click(await screen.findByRole('option', { name: /6º Ano B/ }))
    await user.click(cardBotao('Entregas por turma'))

    await waitFor(() => expect(relatorioService.baixarRelatorioComPeriodo).toHaveBeenCalled())

    const [relatorio, periodo, turmaId] = vi.mocked(relatorioService.baixarRelatorioComPeriodo).mock.calls[0]
    expect(relatorio).toBe('entregas-turma')
    expect(turmaId).toBe('t2')
    expect(periodo.anoInicio).toBe(new Date().getFullYear())
  })

  it('exige o mês inicial quando o filtro é mensal', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(screen.getByLabelText('Mês inicial'))
    await user.keyboard('{Escape}')

    // Zera o ano para disparar a validação sem depender do mês pré-preenchido.
    await user.clear(screen.getByLabelText('Ano inicial'))
    await user.click(cardBotao('Entradas'))

    expect(await screen.findByText('Informe o ano.')).toBeInTheDocument()
    expect(relatorioService.baixarRelatorioComPeriodo).not.toHaveBeenCalled()
  })
})
