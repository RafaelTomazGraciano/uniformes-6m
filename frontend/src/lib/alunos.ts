import type { Aluno } from '@/lib/types/aluno'

export function normalizar(texto: string) {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}

/* A busca ignora acentos e também casa pelo nome da turma, que é como a
   secretaria costuma lembrar do aluno ("Joana do 6B"). */
export function filtrarAluno(aluno: Aluno, busca: string) {
  const termo = normalizar(busca.trim())
  if (!termo) return true
  return normalizar(`${aluno.nome} ${aluno.turmaNome}`).includes(termo)
}
