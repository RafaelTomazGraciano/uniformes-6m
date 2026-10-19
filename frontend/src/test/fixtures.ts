import type { Aluno } from '@/lib/types/aluno'
import type { TipoUniforme } from '@/lib/types/tipo-uniforme'
import type { Turma } from '@/lib/types/turma'
import type { Uniforme } from '@/lib/types/uniforme'

export const maria: Aluno = { id: 'a1', nome: 'Maria Silva', turmaId: 't1', turmaNome: '9º Ano A' }
export const joao: Aluno = { id: 'a2', nome: 'João Conceição', turmaId: 't2', turmaNome: '6º Ano B' }

export const turmaA: Turma = { id: 't1', nome: '9º Ano A', turno: 'DIURNO', ensino: 'FUNDAMENTAL', quantidadeAlunos: 1 }
export const turmaB: Turma = { id: 't2', nome: '6º Ano B', turno: 'VESPERTINO', ensino: 'FUNDAMENTAL', quantidadeAlunos: 1 }

export const camiseta: TipoUniforme = { id: '3f9c2a71-8d4e-4b0a-9e51-0c1d2e3f4a5b', tipo: 'Camiseta', estoqueBaixo: 5 }
export const bermuda: TipoUniforme = { id: 'b7e1a0c2-1111-4c4c-8888-aaaaaaaaaaaa', tipo: 'Bermuda', estoqueBaixo: 3 }

export function uniforme(parcial: Partial<Uniforme> & Pick<Uniforme, 'id'>): Uniforme {
  return {
    tipoUniformeId: camiseta.id,
    tipoUniformeNome: camiseta.tipo,
    tamanho: 'M',
    sexo: 'MASCULINO',
    quantidade: 10,
    estoqueBaixo: camiseta.estoqueBaixo,
    estoqueBaixoAlerta: false,
    ...parcial,
  }
}
