import { apiFetch } from '@/lib/api-client'
import { pageableToQuery, type Page, type Pageable } from '@/lib/types/page'
import type { Uniforme } from '@/lib/types/uniforme'

export function listUniformes(pageable: Pageable = {}): Promise<Page<Uniforme>> {
  return apiFetch(`/uniforme${pageableToQuery(pageable)}`)
}

export async function listAllUniformes(): Promise<Uniforme[]> {
  const page = await apiFetch<Page<Uniforme>>(`/uniforme${pageableToQuery({ size: 100 })}`)
  return page.content
}

export function getUniforme(id: string): Promise<Uniforme> {
  return apiFetch(`/uniforme/${id}`)
}
