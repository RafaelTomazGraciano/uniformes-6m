import { describe, expect, it } from 'vitest'

import { agruparPorTipo, itensParaRepor, nivelDoUniforme, nivelEstoque } from '@/lib/estoque'
import { bermuda, uniforme } from '@/test/fixtures'

describe('nivelEstoque', () => {
  it('usa o limite de cada tipo, não um número fixo', () => {
    expect(nivelEstoque(5, 5)).toBe('baixo')
    expect(nivelEstoque(6, 5)).toBe('ok')
    expect(nivelEstoque(3, 2)).toBe('ok')
    expect(nivelEstoque(0, 0)).toBe('esgotado')
  })
})

describe('nivelDoUniforme', () => {
  it('confia no alerta calculado pelo backend e separa o esgotado', () => {
    expect(nivelDoUniforme(uniforme({ id: 'a', quantidade: 3, estoqueBaixoAlerta: true }))).toBe('baixo')
    expect(nivelDoUniforme(uniforme({ id: 'b', quantidade: 0, estoqueBaixoAlerta: true }))).toBe('esgotado')
    expect(nivelDoUniforme(uniforme({ id: 'c', quantidade: 3, estoqueBaixoAlerta: false }))).toBe('ok')
  })
})

describe('agruparPorTipo / itensParaRepor', () => {
  const lista = [
    uniforme({ id: '1', tamanho: 'M', quantidade: 2, estoqueBaixoAlerta: true }),
    uniforme({ id: '2', tamanho: 'G', quantidade: 9 }),
    uniforme({
      id: '3',
      tipoUniformeId: bermuda.id,
      tipoUniformeNome: bermuda.tipo,
      estoqueBaixo: bermuda.estoqueBaixo,
      quantidade: 0,
      estoqueBaixoAlerta: true,
    }),
  ]

  it('leva o limite do tipo para o grupo', () => {
    const grupos = agruparPorTipo(lista)
    expect(grupos.map((grupo) => [grupo.tipo, grupo.limite])).toEqual([
      ['Bermuda', 3],
      ['Camiseta', 5],
    ])
  })

  it('lista o que repor, esgotados primeiro', () => {
    expect(itensParaRepor(lista).map((item) => item.id)).toEqual(['3', '1'])
  })
})
