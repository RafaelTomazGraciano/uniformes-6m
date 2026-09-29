export type Turno = 'DIURNO' | 'VESPERTINO' | 'NOTURNO'
export type Ensino = 'FUNDAMENTAL' | 'MEDIO' | 'TECNICO'

export type Turma = {
  id: string
  nome: string
  turno: Turno
  ensino: Ensino
}
