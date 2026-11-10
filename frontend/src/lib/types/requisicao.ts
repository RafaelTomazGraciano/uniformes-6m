import type { Sexo, Tamanho } from '@/lib/types/uniforme'

/** PENDENTE é o pedido "em andamento": ainda não chegou do fornecedor. */
export type StatusRequisicao = 'PENDENTE' | 'ATENDIDA' | 'CANCELADA'

export type Requisicao = {
  id: string
  tipoUniformeId: string
  tipoUniforme: string
  tamanho: Tamanho
  sexo: Sexo
  quantidade: number
  observacao: string | null
  status: StatusRequisicao
  usuarioId: string
  usuarioNome: string
  dataCriacao: string
  dataConclusao: string | null
}

export type CriarRequisicaoPayload = {
  tipoUniformeId: string
  tamanho: Tamanho
  sexo: Sexo
  quantidade: number
  observacao?: string
}
