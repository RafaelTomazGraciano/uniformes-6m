import { z } from 'zod'

const ANO_MINIMO = 2000

export const periodoSchema = z
  .object({
    tipo: z.enum(['MES', 'ANO']),
    anoInicio: z.number({ message: 'Informe o ano.' }).int().min(ANO_MINIMO, 'Ano inválido.'),
    mesInicio: z.number().int().min(1).max(12).optional(),
    anoFim: z.number().int().min(ANO_MINIMO, 'Ano inválido.').optional(),
    mesFim: z.number().int().min(1).max(12).optional(),
  })
  .superRefine((valores, ctx) => {
    if (valores.tipo === 'MES' && valores.mesInicio === undefined) {
      ctx.addIssue({ code: 'custom', path: ['mesInicio'], message: 'Escolha o mês inicial.' })
    }

    if (valores.anoFim !== undefined && valores.anoFim < valores.anoInicio) {
      ctx.addIssue({ code: 'custom', path: ['anoFim'], message: 'O fim não pode ser antes do início.' })
    }
  })

export type PeriodoFormValues = z.infer<typeof periodoSchema>
