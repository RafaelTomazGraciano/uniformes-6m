import { apiFetch } from '@/lib/api-client'
import type { AtualizarAlunoPayload, Aluno, CriarAlunoPayload } from '@/lib/types/aluno'
import { fetchAllPages, pageableToQuery, type Page, type Pageable } from '@/lib/types/page'

export function listAlunos(pageable: Pageable = { sort: 'nome,asc' }): Promise<Page<Aluno>> {
  return apiFetch(`/aluno${pageableToQuery(pageable)}`)
}

export function listAllAlunos(): Promise<Aluno[]> {
  return fetchAllPages((pageable) => listAlunos({ ...pageable, sort: 'nome,asc' }))
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
