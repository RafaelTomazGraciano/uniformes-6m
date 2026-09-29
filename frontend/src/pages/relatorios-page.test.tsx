import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/relatorio-service')
vi.mock('sonner', () => ({
  toast: { success: vi.fn(), error: vi.fn(), info: vi.fn() },
}))

import { ApiError } from '@/lib/api-client'
import { RelatoriosPage } from '@/pages/relatorios-page'
import * as relatorioService from '@/services/relatorio-service'
import { renderWithProviders } from '@/test/render'
import { toast } from 'sonner'

const pdf = new Blob(['%PDF'], { type: 'application/pdf' })

function cardBotao(titulo: string) {
  /* Cada card tem um "Baixar PDF"; ancorar no título evita pegar o do vizinho.
     Pelo heading, e não pelo texto: "Estoque" também é um item do menu. */
  const cabecalho = screen.getByRole('heading', { name: titulo })
  const card = cabecalho.closest('[data-slot="card"]') as HTMLElement
  return card.querySelector('button') as HTMLButtonElement
}

describe('RelatoriosPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(relatorioService.baixarRelatorioEstoque).mockResolvedValue(pdf)
    vi.mocked(relatorioService.baixarRelatorioComPeriodo).mockResolvedValue(pdf)
  })

  it('baixa o relatório de estoque sem exigir período', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Estoque'))

    await waitFor(() => expect(relatorioService.baixarRelatorioEstoque).toHaveBeenCalled())
    expect(relatorioService.salvarArquivo).toHaveBeenCalledWith(pdf, 'relatorio-estoque.pdf')
  })

  it('envia o período escolhido nos relatórios que dependem dele', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Saídas'))

    await waitFor(() => expect(relatorioService.baixarRelatorioComPeriodo).toHaveBeenCalled())

    const [relatorio, periodo] = vi.mocked(relatorioService.baixarRelatorioComPeriodo).mock.calls[0]
    expect(relatorio).toBe('saida')
    expect(periodo.tipo).toBe('MES')
    expect(periodo.anoInicio).toBe(new Date().getFullYear())
  })

  it('trata período sem movimento como aviso, não como erro', async () => {
    vi.mocked(relatorioService.baixarRelatorioComPeriodo).mockRejectedValue(
      new ApiError('Não há registros de saída no período informado', 404),
    )

    const user = userEvent.setup()
    renderWithProviders(<RelatoriosPage />)

    await user.click(cardBotao('Saídas'))

    await waitFor(() => expect(toast.info).toHaveBeenCalledWith('Não há registros de saída no período informado'))
    expect(toast.error).not.toHaveBeenCalled()
    expect(relatorioService.salvarArquivo).not.toHaveBeenCalled()
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
