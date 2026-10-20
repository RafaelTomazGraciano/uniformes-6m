import { describe, expect, it, vi } from 'vitest'

import { fetchAllPages, pageableToQuery, type Page, type Pageable } from '@/lib/types/page'

function paginaDe(content: number[], number: number, totalPages: number): Page<number> {
  return {
    content,
    totalElements: totalPages * content.length,
    totalPages,
    number,
    size: content.length,
    first: number === 0,
    last: number === totalPages - 1,
  }
}

describe('fetchAllPages', () => {
  it('concatena o conteúdo de todas as páginas', async () => {
    const carregar = vi.fn(async (pageable: Pageable) => {
      const paginas = [paginaDe([1, 2], 0, 3), paginaDe([3, 4], 1, 3), paginaDe([5, 6], 2, 3)]
      return paginas[pageable.page ?? 0]
    })

    await expect(fetchAllPages(carregar)).resolves.toEqual([1, 2, 3, 4, 5, 6])
    expect(carregar).toHaveBeenCalledTimes(3)
  })

  it('faz uma única chamada quando tudo cabe na primeira página', async () => {
    const carregar = vi.fn(async () => paginaDe([1], 0, 1))

    await expect(fetchAllPages(carregar)).resolves.toEqual([1])
    expect(carregar).toHaveBeenCalledTimes(1)
  })

  it('para assim que o backend sinaliza a última página', async () => {
    // totalPages exagerado: o `last` é quem manda, senão o laço buscaria páginas vazias.
    const carregar = vi.fn(async (pageable: Pageable) =>
      pageable.page === 0 ? paginaDe([1], 0, 9) : { ...paginaDe([2], 1, 9), last: true },
    )

    await expect(fetchAllPages(carregar)).resolves.toEqual([1, 2])
    expect(carregar).toHaveBeenCalledTimes(2)
  })
})

describe('pageableToQuery', () => {
  it('omite a query quando não há parâmetros', () => {
    expect(pageableToQuery()).toBe('')
  })

  it('inclui page zero, que é um valor válido', () => {
    expect(pageableToQuery({ page: 0, size: 20, sort: 'nome,asc' })).toBe('?page=0&size=20&sort=nome%2Casc')
  })
})
