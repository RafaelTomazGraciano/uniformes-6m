export type Aluno = {
  id: string
  nome: string
  turmaId: string
  turmaNome: string
}

export type CriarAlunoPayload = {
  nome: string
  turmaId: string
}

export type AtualizarAlunoPayload = {
  nome: string
  turmaId: string
}
