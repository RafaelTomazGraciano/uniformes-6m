import { apiFetch } from '@/lib/api-client'
import { fetchAllPages, pageableToQuery, type Page, type Pageable } from '@/lib/types/page'
import type { Uniforme } from '@/lib/types/uniforme'

export function listUniformes(pageable: Pageable = {}): Promise<Page<Uniforme>> {
  return apiFetch(`/uniforme${pageableToQuery(pageable)}`)
}

export function listAllUniformes(): Promise<Uniforme[]> {
  return fetchAllPages(listUniformes)
}

export function getUniforme(id: string): Promise<Uniforme> {
  return apiFetch(`/uniforme/${id}`)
}
