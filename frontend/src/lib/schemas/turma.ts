import { z } from 'zod'

export const turmaSchema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome da turma.'),
  turno: z.enum(['DIURNO', 'VESPERTINO', 'NOTURNO'], { message: 'Selecione o turno.' }),
  ensino: z.enum(['FUNDAMENTAL', 'MEDIO', 'TECNICO'], { message: 'Selecione o ensino.' }),
})

export type TurmaFormValues = z.infer<typeof turmaSchema>
