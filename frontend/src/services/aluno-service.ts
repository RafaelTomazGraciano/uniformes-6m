import { apiFetch } from '@/lib/api-client'
import type { AtualizarAlunoPayload, Aluno, CriarAlunoPayload, FiltroAlunos } from '@/lib/types/aluno'
import { fetchAllPages, pageableToQuery, type Page, type Pageable } from '@/lib/types/page'

/* A busca por nome (ou nome da turma), a turma e a situação são filtradas no
   servidor, que também ignora acentos e maiúsculas. */
function filtroToQuery(filtro: FiltroAlunos): string {
  const params = new URLSearchParams()
  const nome = filtro.nome?.trim()
  if (nome) params.set('nome', nome)
  if (filtro.turmaId) params.set('turmaId', filtro.turmaId)
  if (filtro.ativo !== undefined) params.set('ativo', String(filtro.ativo))
  return params.toString()
}

export function listAlunos(pageable: Pageable = { sort: 'nome,asc' }, filtro: FiltroAlunos = {}): Promise<Page<Aluno>> {
  const query = pageableToQuery(pageable)
  const extra = filtroToQuery(filtro)
  return apiFetch(`/aluno${query}${extra ? `${query ? '&' : '?'}${extra}` : ''}`)
}

export function listAllAlunos(filtro: FiltroAlunos = {}): Promise<Aluno[]> {
  return fetchAllPages((pageable) => listAlunos({ ...pageable, sort: 'nome,asc' }, filtro))
}

export function getAluno(id: string): Promise<Aluno> {
  return apiFetch(`/aluno/${id}`)
}

export function createAluno(payload: CriarAlunoPayload): Promise<Aluno> {
  return apiFetch('/aluno', { method: 'POST', body: payload })
}

export function updateAluno(id: string, payload: AtualizarAlunoPayload): Promise<Aluno> {
  return apiFetch(`/aluno/${id}`, { method: 'PUT', body: payload })
}

export function deleteAluno(id: string): Promise<{ message: string }> {
  return apiFetch(`/aluno/${id}`, { method: 'DELETE' })
}

/** Desligamento: o aluno deixa de receber entregas, mas o histórico continua. */
export function registrarSaidaAluno(id: string): Promise<Aluno> {
  return apiFetch(`/aluno/${id}/saida`, { method: 'PATCH' })
}

export function reativarAluno(id: string): Promise<Aluno> {
  return apiFetch(`/aluno/${id}/reativar`, { method: 'PATCH' })
}
