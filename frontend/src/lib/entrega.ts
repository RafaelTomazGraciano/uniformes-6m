import { TAMANHOS } from '@/lib/estoque'
import type { Sexo, Uniforme } from '@/lib/types/uniforme'

/** Uma peça (tipo + modelo), com os tamanhos em ordem PP → GG. */
export type GrupoPeca = { chave: string; tipo: string; sexo: Sexo; uniformes: Uniforme[]; total: number }

export function chaveGrupo(uniforme: Pick<Uniforme, 'tipoUniformeNome' | 'sexo'>) {
  return `${uniforme.tipoUniformeNome}|${uniforme.sexo}`
}

export function agruparPecas(uniformes: Uniforme[]): GrupoPeca[] {
  const mapa = new Map<string, GrupoPeca>()

  for (const uniforme of uniformes) {
    const chave = chaveGrupo(uniforme)
    const grupo = mapa.get(chave) ?? { chave, tipo: uniforme.tipoUniformeNome, sexo: uniforme.sexo, uniformes: [], total: 0 }
    grupo.uniformes.push(uniforme)
    grupo.total += uniforme.quantidade
    mapa.set(chave, grupo)
  }

  return [...mapa.values()]
    .map((grupo) => ({
      ...grupo,
      uniformes: [...grupo.uniformes].sort((a, b) => TAMANHOS.indexOf(a.tamanho) - TAMANHOS.indexOf(b.tamanho)),
    }))
    .sort((a, b) => a.tipo.localeCompare(b.tipo, 'pt-BR') || a.sexo.localeCompare(b.sexo))
}

/** Uma linha da entrega. A mesma peça pode estar em várias linhas. */
export type LinhaEntrega = {
  chave: string
  /** "tipo|sexo" escolhido antes do tamanho; só vive na tela, não vai no payload. */
  grupo: string
  uniformeId: string
  quantidade: number
}

let sequencia = 0
export function novaLinha(parcial: Partial<LinhaEntrega> = {}): LinhaEntrega {
  sequencia += 1
  return { grupo: '', uniformeId: '', quantidade: 1, ...parcial, chave: `linha-${sequencia}` }
}

/** Soma por uniforme: é o total que sai do estoque, como o backend consolida. */
export function somarPorUniforme(linhas: LinhaEntrega[]): Map<string, { quantidade: number; linhas: number }> {
  const soma = new Map<string, { quantidade: number; linhas: number }>()
  for (const linha of linhas) {
    if (!linha.uniformeId) continue
    const atual = soma.get(linha.uniformeId) ?? { quantidade: 0, linhas: 0 }
    soma.set(linha.uniformeId, { quantidade: atual.quantidade + linha.quantidade, linhas: atual.linhas + 1 })
  }
  return soma
}

export type ErrosEntrega = { aluno?: string; geral?: string; linhas: Record<string, string > }

export function validarEntrega(alunoId: string, linhas: LinhaEntrega[], porId: Map<string, Uniforme>): ErrosEntrega {
  const erros: ErrosEntrega = { linhas: {} }
  if (!alunoId) erros.aluno = 'Escolha o aluno que vai receber as peças.'

  const preenchidas = linhas.filter((linha) => linha.uniformeId)
  if (preenchidas.length === 0) erros.geral = 'Adicione ao menos uma peça.'

  for (const linha of linhas) {
    if (!linha.uniformeId) erros.linhas[linha.chave] = linha.grupo ? 'Escolha o tamanho.' : 'Escolha a peça e o tamanho.'
    else if (!(linha.quantidade >= 1)) erros.linhas[linha.chave] = 'Informe a quantidade.'
  }

  for (const [uniformeId, { quantidade, linhas: quantasLinhas }] of somarPorUniforme(linhas)) {
    const uniforme = porId.get(uniformeId)
    if (!uniforme || quantidade <= uniforme.quantidade) continue
    const mensagem =
      quantasLinhas > 1
        ? `Somando as ${quantasLinhas} linhas são ${quantidade}, mas só há ${uniforme.quantidade} em estoque.`
        : `Só há ${uniforme.quantidade} em estoque.`
    for (const linha of linhas) if (linha.uniformeId === uniformeId) erros.linhas[linha.chave] = mensagem
  }

  return erros
}

export function temErros(erros: ErrosEntrega) {
  return Boolean(erros.aluno || erros.geral || Object.keys(erros.linhas).length)
}

export function descreverUniforme(uniforme: Pick<Uniforme, 'tipoUniformeNome' | 'sexo' | 'tamanho'>, sexoRotulo: string) {
  return `${uniforme.tipoUniformeNome} · ${sexoRotulo} · ${uniforme.tamanho}`
}
