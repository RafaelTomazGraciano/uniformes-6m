import { apiFetch } from '@/lib/api-client'
import { fetchAllPages, pageableToQuery, type Page, type Pageable } from '@/lib/types/page'
import type { AtualizarTurmaPayload, CriarTurmaPayload, Turma } from '@/lib/types/turma'

export function listTurmas(pageable: Pageable = { sort: 'nome,asc' }): Promise<Page<Turma>> {
  return apiFetch(`/turma${pageableToQuery(pageable)}`)
}

export function listAllTurmas(): Promise<Turma[]> {
  return fetchAllPages((pageable) => listTurmas({ ...pageable, sort: 'nome,asc' }))
}

export function createTurma(payload: CriarTurmaPayload): Promise<Turma> {
  return apiFetch('/turma', { method: 'POST', body: payload })
}

export function updateTurma(id: string, payload: AtualizarTurmaPayload): Promise<Turma> {
  return apiFetch(`/turma/${id}`, { method: 'PUT', body: payload })
}

export function deleteTurma(id: string): Promise<{ message: string }> {
  return apiFetch(`/turma/${id}`, { method: 'DELETE' })
}
