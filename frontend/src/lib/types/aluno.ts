export type Aluno = {
  id: string
  nome: string
  nomeResponsavel?: string | null
  /** Só dígitos, com DDD (10 ou 11). */
  telefoneResponsavel?: string | null
  turmaId: string
  turmaNome: string
}

export type CriarAlunoPayload = {
  nome: string
  nomeResponsavel?: string
  telefoneResponsavel?: string
  turmaId: string
}

export type AtualizarAlunoPayload = CriarAlunoPayload
