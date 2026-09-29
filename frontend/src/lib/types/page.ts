export type Page<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export type Pageable = {
  page?: number
  size?: number
  sort?: string
}

/* O backend só entrega lista paginada. Telas que precisam do conjunto inteiro
   (busca no cliente, painel de estoque, selects) percorrem todas as páginas:
   fixar um `size` grande e parar por aí descarta o excedente em silêncio. */
const TAMANHO_PAGINA = 100

export async function fetchAllPages<T>(carregar: (pageable: Pageable) => Promise<Page<T>>): Promise<T[]> {
  const primeira = await carregar({ page: 0, size: TAMANHO_PAGINA })
  const itens = [...primeira.content]

  for (let page = 1; page < primeira.totalPages; page++) {
    const proxima = await carregar({ page, size: TAMANHO_PAGINA })
    itens.push(...proxima.content)
    if (proxima.last) break
  }

  return itens
}

export function pageableToQuery(pageable: Pageable = {}): string {
  const params = new URLSearchParams()

  if (pageable.page !== undefined) params.set('page', String(pageable.page))
  if (pageable.size !== undefined) params.set('size', String(pageable.size))
  if (pageable.sort) params.set('sort', pageable.sort)

  const query = params.toString()
  return query ? `?${query}` : ''
}
