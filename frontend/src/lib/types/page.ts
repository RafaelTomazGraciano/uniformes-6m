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

export function pageableToQuery(pageable: Pageable = {}): string {
  const params = new URLSearchParams()

  if (pageable.page !== undefined) params.set('page', String(pageable.page))
  if (pageable.size !== undefined) params.set('size', String(pageable.size))
  if (pageable.sort) params.set('sort', pageable.sort)

  const query = params.toString()
  return query ? `?${query}` : ''
}
