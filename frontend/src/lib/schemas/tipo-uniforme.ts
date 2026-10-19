import { z } from 'zod'

export const tipoUniformeSchema = z.object({
  tipo: z.string().trim().min(1, 'Informe o tipo de uniforme.'),
  estoqueBaixo: z
    .number({ message: 'Informe a partir de quantas peças avisar.' })
    .int('Use um número inteiro.')
    .min(0, 'O limite não pode ser negativo.'),
})

export type TipoUniformeFormValues = z.infer<typeof tipoUniformeSchema>
