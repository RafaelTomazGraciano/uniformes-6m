import type { Pedido } from '@/lib/types/pedido'
import type { Sexo, Tamanho, Uniforme } from '@/lib/types/uniforme'

export const TAMANHOS: readonly Tamanho[] = ['PP', 'P', 'M', 'G', 'GG']

/** Abaixo disso o almoxarifado precisa repor antes que falte no balcão. */
export const LIMITE_ESTOQUE_BAIXO = 5

export type NivelEstoque = 'esgotado' | 'baixo' | 'ok'

export function nivelEstoque(quantidade: number): NivelEstoque {
  if (quantidade <= 0) return 'esgotado'
  if (quantidade <= LIMITE_ESTOQUE_BAIXO) return 'baixo'
  return 'ok'
}

export type LinhaEstoque = {
  sexo: Sexo
  total: number
  /** Ausente = combinação não cadastrada; 0 = cadastrada e esgotada. */
  porTamanho: Partial<Record<Tamanho, number>>
}

export type GrupoEstoque = {
  tipo: string
  total: number
  linhas: LinhaEstoque[]
}

const ORDEM_SEXO: Sexo[] = ['MASCULINO', 'FEMININO']

export function agruparPorTipo(uniformes: Uniforme[]): GrupoEstoque[] {
  const grupos = new Map<string, Map<Sexo, LinhaEstoque>>()

  for (const uniforme of uniformes) {
    const linhas = grupos.get(uniforme.tipoUniformeNome) ?? new Map<Sexo, LinhaEstoque>()
    const linha = linhas.get(uniforme.sexo) ?? { sexo: uniforme.sexo, total: 0, porTamanho: {} }

    linha.porTamanho[uniforme.tamanho] = (linha.porTamanho[uniforme.tamanho] ?? 0) + uniforme.quantidade
    linha.total += uniforme.quantidade

    linhas.set(uniforme.sexo, linha)
    grupos.set(uniforme.tipoUniformeNome, linhas)
  }

  return [...grupos.entries()]
    .map(([tipo, linhas]) => {
      const ordenadas = [...linhas.values()].sort((a, b) => ORDEM_SEXO.indexOf(a.sexo) - ORDEM_SEXO.indexOf(b.sexo))
      return {
        tipo,
        linhas: ordenadas,
        total: ordenadas.reduce((soma, linha) => soma + linha.total, 0),
      }
    })
    .sort((a, b) => a.tipo.localeCompare(b.tipo, 'pt-BR'))
}

/** Esgotados primeiro, depois os mais baixos: a ordem em que se compra. */
export function itensParaRepor(uniformes: Uniforme[]): Uniforme[] {
  return uniformes
    .filter((uniforme) => nivelEstoque(uniforme.quantidade) !== 'ok')
    .sort((a, b) => a.quantidade - b.quantidade || a.tipoUniformeNome.localeCompare(b.tipoUniformeNome, 'pt-BR'))
}

export type ResumoEstoque = {
  pecas: number
  esgotados: number
  baixos: number
}

export function resumoEstoque(uniformes: Uniforme[]): ResumoEstoque {
  return uniformes.reduce<ResumoEstoque>(
    (resumo, uniforme) => {
      const nivel = nivelEstoque(uniforme.quantidade)
      return {
        pecas: resumo.pecas + uniforme.quantidade,
        esgotados: resumo.esgotados + (nivel === 'esgotado' ? 1 : 0),
        baixos: resumo.baixos + (nivel === 'baixo' ? 1 : 0),
      }
    },
    { pecas: 0, esgotados: 0, baixos: 0 },
  )
}

export function contarEntregasNoMes(pedidos: Pedido[], referencia: Date = new Date()): number {
  return pedidos.filter((pedido) => {
    const data = new Date(pedido.dataEfetivada)
    return data.getFullYear() === referencia.getFullYear() && data.getMonth() === referencia.getMonth()
  }).length
}

export function contarPecasEntregues(pedidos: Pedido[]): number {
  return pedidos.reduce(
    (total, pedido) => total + pedido.itens.reduce((soma, item) => soma + item.quantidade, 0),
    0,
  )
}
