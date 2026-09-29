import { z } from 'zod'

export const tipoUniformeSchema = z.object({
  tipo: z.string().trim().min(1, 'Informe o tipo de uniforme.'),
})

export type TipoUniformeFormValues = z.infer<typeof tipoUniformeSchema>
