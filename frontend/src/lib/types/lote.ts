import type { Sexo, Tamanho } from '@/lib/types/uniforme'

export type ItemLote = {
  id: string
  tipoUniformeId: string
  tipoUniformeNome: string
  loteId: string
  loteFornecedor: string
  tamanho: Tamanho
  quantidade: number
  sexo: Sexo
}

export type Lote = {
  id: string
  notaFiscalId: string
  notaFiscalChaveAcesso: string
  fornecedor: string
  dataEntrega: string
  itens: ItemLote[]
}

export type ItemEntradaPayload = {
  tipoUniformeId: string
  tamanho: Tamanho
  sexo: Sexo
  quantidade: number
}

export type CriarLotePayload = {
  chaveAcesso: string
  fornecedor: string
  dataEntrega: string
  itens: ItemEntradaPayload[]
}

export type AtualizarLotePayload = CriarLotePayload
