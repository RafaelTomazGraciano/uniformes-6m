import { z } from 'zod'

export const alunoSchema = z.object({
  nome: z.string().min(1, 'Informe o nome do aluno.'),
  turmaId: z.string().min(1, 'Selecione uma turma.'),
})

export type AlunoFormValues = z.infer<typeof alunoSchema>
