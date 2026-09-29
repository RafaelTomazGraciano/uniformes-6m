import { apiFetch } from '@/lib/api-client'
import { fetchAllPages, pageableToQuery, type Page, type Pageable } from '@/lib/types/page'
import type {
  AtualizarTipoUniformePayload,
  CriarTipoUniformePayload,
  TipoUniforme,
} from '@/lib/types/tipo-uniforme'

export function listTiposUniforme(pageable: Pageable = { sort: 'tipo,asc' }): Promise<Page<TipoUniforme>> {
  return apiFetch(`/tipo-uniforme${pageableToQuery(pageable)}`)
}

export function listAllTiposUniforme(): Promise<TipoUniforme[]> {
  return fetchAllPages((pageable) => listTiposUniforme({ ...pageable, sort: 'tipo,asc' }))
}

export function createTipoUniforme(payload: CriarTipoUniformePayload): Promise<TipoUniforme> {
  return apiFetch('/tipo-uniforme', { method: 'POST', body: payload })
}

export function updateTipoUniforme(id: string, payload: AtualizarTipoUniformePayload): Promise<TipoUniforme> {
  return apiFetch(`/tipo-uniforme/${id}`, { method: 'PUT', body: payload })
}

export function deleteTipoUniforme(id: string): Promise<{ message: string }> {
  return apiFetch(`/tipo-uniforme/${id}`, { method: 'DELETE' })
}
