import { apiFetch } from '@/lib/api-client'
import { pageableToQuery, type Page } from '@/lib/types/page'
import type { Turma } from '@/lib/types/turma'

export async function listAllTurmas(): Promise<Turma[]> {
  const page = await apiFetch<Page<Turma>>(`/turma${pageableToQuery({ size: 100, sort: 'nome,asc' })}`)
  return page.content
}
