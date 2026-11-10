import { z } from 'zod'

export const requisicaoSchema = z.object({
  tipoUniformeId: z.string({ message: 'Escolha o tipo de uniforme' }).min(1, 'Escolha o tipo de uniforme'),
  sexo: z.enum(['MASCULINO', 'FEMININO'], { message: 'Escolha o sexo' }),
  tamanho: z.enum(['PP', 'P', 'M', 'G', 'GG'], { message: 'Escolha o tamanho' }),
  quantidade: z
    .number({ message: 'Informe a quantidade' })
    .int('Use um número inteiro')
    .min(1, 'A quantidade mínima é 1'),
  observacao: z.string().trim().max(500, 'Use no máximo 500 caracteres'),
})

export type RequisicaoFormInput = z.input<typeof requisicaoSchema>
export type RequisicaoFormValues = z.output<typeof requisicaoSchema>
