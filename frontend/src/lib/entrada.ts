import { TAMANHOS } from '@/lib/estoque'
import type { ItemEntradaPayload } from '@/lib/types/lote'
import type { Sexo, Tamanho } from '@/lib/types/uniforme'

export const SEXOS: readonly Sexo[] = ['FEMININO', 'MASCULINO']

/** Uma linha da nota. A mesma peça pode se repetir; o backend soma. */
export type LinhaEntrada = ItemEntradaPayload & { chave: string }

let sequencia = 0
export function novaLinhaEntrada(parcial: Partial<ItemEntradaPayload> = {}): LinhaEntrada {
  sequencia += 1
  return { tipoUniformeId: '', sexo: 'FEMININO', tamanho: 'M', quantidade: 1, ...parcial, chave: `entrada-${sequencia}` }
}

const chaveCelula = (tipoUniformeId: string, sexo: Sexo, tamanho: Tamanho) => `${tipoUniformeId}|${sexo}|${tamanho}`

/** Total por tipo × sexo × tamanho: o que a grade mostra e o que o estoque recebe. */
export function somarCelulas(linhas: LinhaEntrada[]) {
  const soma = new Map<string, { quantidade: number; linhas: number }>()
  for (const linha of linhas) {
    if (!linha.tipoUniformeId) continue
    const chave = chaveCelula(linha.tipoUniformeId, linha.sexo, linha.tamanho)
    const atual = soma.get(chave) ?? { quantidade: 0, linhas: 0 }
    soma.set(chave, { quantidade: atual.quantidade + (linha.quantidade || 0), linhas: atual.linhas + 1 })
  }
  return {
    de: (tipoUniformeId: string, sexo: Sexo, tamanho: Tamanho) => soma.get(chaveCelula(tipoUniformeId, sexo, tamanho)),
    repetidas: [...soma.values()].filter((celula) => celula.linhas > 1).length,
  }
}

/** Editar uma célula da grade troca as linhas daquela combinação por uma só, com o valor digitado. */
export function definirCelula(
  linhas: LinhaEntrada[],
  tipoUniformeId: string,
  sexo: Sexo,
  tamanho: Tamanho,
  quantidade: number,
): LinhaEntrada[] {
  const mesma = (linha: LinhaEntrada) =>
    linha.tipoUniformeId === tipoUniformeId && linha.sexo === sexo && linha.tamanho === tamanho
  const indice = linhas.findIndex(mesma)
  const restantes = linhas.filter((linha) => !mesma(linha))
  if (quantidade <= 0) return restantes
  const atualizada = indice >= 0 ? { ...linhas[indice], quantidade } : novaLinhaEntrada({ tipoUniformeId, sexo, tamanho, quantidade })
  const posicao = indice >= 0 ? linhas.slice(0, indice).filter((linha) => !mesma(linha)).length : restantes.length
  return [...restantes.slice(0, posicao), atualizada, ...restantes.slice(posicao)]
}

export function tiposNaGrade(linhas: LinhaEntrada[], extras: string[]) {
  return [...new Set([...linhas.map((linha) => linha.tipoUniformeId).filter(Boolean), ...extras])]
}

export { TAMANHOS }
