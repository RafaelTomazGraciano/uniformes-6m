import { z } from 'zod'

export const pedidoItemSchema = z.object({
  uniformeId: z.string().min(1, 'Selecione um item.'),
  quantidade: z.number().int().min(1, 'Quantidade mínima é 1.'),
})

export const pedidoSchema = z.object({
  alunoId: z.string().min(1, 'Selecione um aluno.'),
  itens: z
    .array(pedidoItemSchema)
    .min(1, 'Adicione ao menos um item.')
    .refine((itens) => new Set(itens.map((item) => item.uniformeId)).size === itens.length, {
      message: 'Não é possível repetir o mesmo item no pedido.',
    }),
})

export type PedidoItemFormValues = z.infer<typeof pedidoItemSchema>
export type PedidoFormValues = z.infer<typeof pedidoSchema>
