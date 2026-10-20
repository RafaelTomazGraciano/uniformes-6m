import { apiFetch } from '@/lib/api-client'
import { paraLocalDateTime } from '@/lib/format'
import type { CriarPedidoPayload, Pedido } from '@/lib/types/pedido'
import { pageableToQuery, type Page, type Pageable } from '@/lib/types/page'

export function listPedidos(
  pageable: Pageable = { sort: 'dataEfetivada,desc' },
  filtro: { alunoId?: string } = {},
): Promise<Page<Pedido>> {
  const query = pageableToQuery(pageable)
  const aluno = filtro.alunoId ? `${query ? '&' : '?'}alunoId=${encodeURIComponent(filtro.alunoId)}` : ''
  return apiFetch(`/pedido${query}${aluno}`)
}

export function getPedido(id: string): Promise<Pedido> {
  return apiFetch(`/pedido/${id}`)
}

export function createPedido(payload: CriarPedidoPayload): Promise<Pedido> {
  /* O backend grava `dataEfetivada` exatamente como chega e não tem default:
     omitir o campo registra a entrega sem data, e ela some dos relatórios. */
  return apiFetch('/pedido', {
    method: 'POST',
    body: { ...payload, dataEfetivada: paraLocalDateTime(new Date()) },
  })
}
