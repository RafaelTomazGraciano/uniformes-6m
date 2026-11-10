import type { Sexo, Tamanho } from '@/lib/types/uniforme'

export type PedidoUniforme = {
  id: string
  pedidoId: string
  uniformeId: string
  uniformeTipoUniformeNome: string
  uniformeTamanho: Tamanho
  uniformeSexo: Sexo
  quantidade: number
}

export type Pedido = {
  id: string
  alunoId: string
  alunoNome: string
  /** `false` quando o aluno já saiu: a entrega segue no histórico, mas não cabe uma nova. */
  alunoAtivo: boolean
  usuarioId: string
  usuarioNome: string
  dataEfetivada: string
  itens: PedidoUniforme[]
}

export type ItemSaidaPayload = {
  uniformeId: string
  quantidade: number
}

export type CriarPedidoPayload = {
  alunoId: string
  itens: ItemSaidaPayload[]
}
