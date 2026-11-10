export type Aluno = {
  id: string
  nome: string
  nomeResponsavel?: string | null
  /** Só dígitos, com DDD (10 ou 11). */
  telefoneResponsavel?: string | null
  turmaId: string
  turmaNome: string
  /** `false` depois de "Registrar saída": o histórico fica, novas entregas não. */
  ativo: boolean
  /** LocalDateTime da saída; nulo enquanto o aluno está ativo. */
  dataSaida?: string | null
}

/** Filtros da listagem, aplicados no servidor. */
export type FiltroAlunos = {
  nome?: string
  turmaId?: string
  ativo?: boolean
}

export type CriarAlunoPayload = {
  nome: string
  nomeResponsavel?: string
  telefoneResponsavel?: string
  turmaId: string
}

export type AtualizarAlunoPayload = CriarAlunoPayload
