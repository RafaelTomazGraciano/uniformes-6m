import { apiFetchBlob } from '@/lib/api-client'
import type { PeriodoFormValues } from '@/lib/schemas/relatorio'

export type RelatorioComPeriodo = 'entrada' | 'saida' | 'transacoes'

function periodoParaQuery(periodo: PeriodoFormValues): string {
  const params = new URLSearchParams({ tipo: periodo.tipo, anoInicio: String(periodo.anoInicio) })

  // Mês só faz sentido no filtro mensal; no anual o backend recusa o parâmetro extra.
  if (periodo.tipo === 'MES' && periodo.mesInicio !== undefined) {
    params.set('mesInicio', String(periodo.mesInicio))
  }
  if (periodo.anoFim !== undefined) params.set('anoFim', String(periodo.anoFim))
  if (periodo.tipo === 'MES' && periodo.mesFim !== undefined) {
    params.set('mesFim', String(periodo.mesFim))
  }

  return params.toString()
}

export function baixarRelatorioEstoque(): Promise<Blob> {
  return apiFetchBlob('/relatorio/estoque')
}

export function baixarRelatorioComPeriodo(relatorio: RelatorioComPeriodo, periodo: PeriodoFormValues): Promise<Blob> {
  return apiFetchBlob(`/relatorio/${relatorio}?${periodoParaQuery(periodo)}`)
}

export function salvarArquivo(blob: Blob, nomeArquivo: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = nomeArquivo
  document.body.append(link)
  link.click()
  link.remove()
  // Sem revogar, o blob fica preso na memória da aba até o reload.
  URL.revokeObjectURL(url)
}
