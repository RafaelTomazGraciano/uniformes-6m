import { describe, expect, it } from 'vitest'

import { filtrarAluno, normalizar } from '@/lib/alunos'
import type { Aluno } from '@/lib/types/aluno'

const joana: Aluno = { id: 'a1', nome: 'Joana Conceição', turmaId: 't1', turmaNome: '6B' }

describe('normalizar', () => {
  it('remove acentos e caixa', () => {
    expect(normalizar('Conceição')).toBe('conceicao')
    expect(normalizar('ÁÉÍÓÚ')).toBe('aeiou')
  })
})

describe('filtrarAluno', () => {
  it('aceita todos quando o termo está vazio ou só com espaços', () => {
    expect(filtrarAluno(joana, '')).toBe(true)
    expect(filtrarAluno(joana, '   ')).toBe(true)
  })

  it('casa o nome ignorando acentos', () => {
    expect(filtrarAluno(joana, 'conceicao')).toBe(true)
    expect(filtrarAluno(joana, 'CONCEIÇÃO')).toBe(true)
  })

  it('casa também pelo nome da turma', () => {
    expect(filtrarAluno(joana, '6b')).toBe(true)
    expect(filtrarAluno(joana, 'joana do 6b')).toBe(false)
  })

  it('rejeita quem não corresponde', () => {
    expect(filtrarAluno(joana, 'pedro')).toBe(false)
  })
})
