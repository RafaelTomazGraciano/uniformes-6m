import { z } from 'zod'

export const alunoSchema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome do aluno.'),
  nomeResponsavel: z.string().trim().max(255, 'Use no máximo 255 caracteres.'),
  /* O backend guarda só dígitos (DDD + número); máscara e espaços saem antes do envio. */
  telefoneResponsavel: z
    .string()
    .transform((valor) => valor.replace(/\D/g, ''))
    .refine((digitos) => digitos === '' || digitos.length === 10 || digitos.length === 11, {
      message: 'Informe o telefone com DDD: 10 ou 11 dígitos.',
    }),
  turmaId: z.string().min(1, 'Selecione uma turma.'),
})

export type AlunoFormInput = z.input<typeof alunoSchema>
export type AlunoFormValues = z.output<typeof alunoSchema>
