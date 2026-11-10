import type { StatusRequisicao } from '@/lib/types/requisicao'
import type { Ensino, Turno } from '@/lib/types/turma'
import type { Sexo } from '@/lib/types/uniforme'

export const turnoLabels: Record<Turno, string > = {
  DIURNO: 'Diurno',
  VESPERTINO: 'Vespertino',
  NOTURNO: 'Noturno',
}

export const ensinoLabels: Record<Ensino, string > = {
  FUNDAMENTAL: 'Fundamental',
  MEDIO: 'Médio',
  TECNICO: 'Técnico',
}

export const sexoLabels: Record<Sexo, string > = {
  MASCULINO: 'Masculino',
  FEMININO: 'Feminino',
}

export const statusRequisicaoLabels: Record<StatusRequisicao, string > = {
  PENDENTE: 'Em andamento',
  ATENDIDA: 'Atendida',
  CANCELADA: 'Cancelada',
}
