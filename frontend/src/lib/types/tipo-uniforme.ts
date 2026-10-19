export type TipoUniforme = {
  id: string
  tipo: string
  /** Com esta quantidade ou menos, cada tamanho do tipo entra em alerta de reposição. */
  estoqueBaixo: number
}

export type CriarTipoUniformePayload = {
  tipo: string
  estoqueBaixo: number
}

export type AtualizarTipoUniformePayload = CriarTipoUniformePayload
