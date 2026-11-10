import { apiFetch } from '@/lib/api-client'
import { pageableToQuery, type Page, type Pageable } from '@/lib/types/page'
import type { CriarRequisicaoPayload, Requisicao, StatusRequisicao } from '@/lib/types/requisicao'

export function listRequisicoes(
  pageable: Pageable = { sort: 'dataCriacao,desc' },
  filtro: { status?: StatusRequisicao[] } = {},
): Promise<Page<Requisicao>> {
  const params = new URLSearchParams(pageableToQuery(pageable))
  for (const status of filtro.status ?? []) params.append('status', status)
  const query = params.toString()
  return apiFetch(`/requisicao${query ? `?${query}` : ''}`)
}

export function getRequisicao(id: string): Promise<Requisicao> {
  return apiFetch(`/requisicao/${id}`)
}

export function createRequisicao(payload: CriarRequisicaoPayload): Promise<Requisicao> {
  return apiFetch('/requisicao', { method: 'POST', body: payload })
}

/** Só muda o status: as peças entram no estoque pela entrada do lote. */
export function atenderRequisicao(id: string): Promise<Requisicao> {
  return apiFetch(`/requisicao/${id}/atender`, { method: 'PATCH' })
}

export function cancelarRequisicao(id: string): Promise<Requisicao> {
  return apiFetch(`/requisicao/${id}/cancelar`, { method: 'PATCH' })
}
