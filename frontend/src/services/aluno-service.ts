import { apiFetch } from '@/lib/api-client'
import type { AtualizarAlunoPayload, Aluno, CriarAlunoPayload } from '@/lib/types/aluno'
import { pageableToQuery, type Page, type Pageable } from '@/lib/types/page'

export function listAlunos(pageable: Pageable = { sort: 'nome,asc' }): Promise<Page<Aluno>> {
  return apiFetch(`/aluno${pageableToQuery(pageable)}`)
}

export async function listAllAlunos(): Promise<Aluno[]> {
  const page = await apiFetch<Page<Aluno>>(`/aluno${pageableToQuery({ size: 100, sort: 'nome,asc' })}`)
  return page.content
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
