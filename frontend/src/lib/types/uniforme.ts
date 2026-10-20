export type Tamanho = 'PP' | 'P' | 'M' | 'G' | 'GG'
export type Sexo = 'MASCULINO' | 'FEMININO'

export type Uniforme = {
  id: string
  tipoUniformeId: string
  tipoUniformeNome: string
  tamanho: Tamanho
  quantidade: number
  sexo: Sexo
  /** O limite vem do tipo; o backend já diz se esta combinação passou dele. */
  estoqueBaixo: number
  estoqueBaixoAlerta: boolean
}
