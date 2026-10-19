import { describe, expect, it } from 'vitest'

import { novaLinha, somarPorUniforme, temErros, validarEntrega } from '@/lib/entrega'
import { uniforme } from '@/test/fixtures'

const camisetaM = uniforme({ id: 'u1', quantidade: 5 })
const porId = new Map([[camisetaM.id, camisetaM]])

describe('entrega com linhas repetidas', () => {
  it('soma a mesma peça vinda de várias linhas', () => {
    const linhas = [novaLinha({ uniformeId: 'u1', quantidade: 2 }), novaLinha({ uniformeId: 'u1', quantidade: 1 })]
    expect(somarPorUniforme(linhas).get('u1')).toEqual({ quantidade: 3, linhas: 2 })
    expect(temErros(validarEntrega('a1', linhas, porId))).toBe(false)
  })

  it('aponta todas as linhas quando a soma passa do estoque', () => {
    const linhas = [novaLinha({ uniformeId: 'u1', quantidade: 4 }), novaLinha({ uniformeId: 'u1', quantidade: 2 })]
    const erros = validarEntrega('a1', linhas, porId)
    expect(Object.values(erros.linhas)).toEqual([
      'Somando as 2 linhas são 6, mas só há 5 em estoque.',
      'Somando as 2 linhas são 6, mas só há 5 em estoque.',
    ])
  })

  it('exige aluno e ao menos uma peça', () => {
    const erros = validarEntrega('', [], porId)
    expect(erros.aluno).toBeTruthy()
    expect(erros.geral).toBeTruthy()
  })
})
