import { apiFetch } from '@/lib/api-client'
import type { AtualizarLotePayload, CriarLotePayload, Lote } from '@/lib/types/lote'
import { pageableToQuery, type Page, type Pageable } from '@/lib/types/page'

export function listLotes(pageable: Pageable = { sort: 'dataEntrega,desc' }): Promise<Page<Lote>> {
  return apiFetch(`/lote${pageableToQuery(pageable)}`)
}

export function getLote(id: string): Promise<Lote> {
  return apiFetch(`/lote/${id}`)
}

export function createLote(payload: CriarLotePayload): Promise<Lote> {
  return apiFetch('/lote', { method: 'POST', body: payload })
}

export function updateLote(id: string, payload: AtualizarLotePayload): Promise<Lote> {
  return apiFetch(`/lote/${id}`, { method: 'PUT', body: payload })
}
