import { z } from 'zod'

export const pedidoItemSchema = z.object({
  uniformeId: z.string().min(1, 'Escolha a peça e o tamanho.'),
  quantidade: z.number({ message: 'Informe a quantidade.' }).int().min(1, 'Quantidade mínima é 1.'),
})

/* A mesma peça pode aparecer em várias linhas: o backend soma as repetições
   (PedidoUniformeService.consolidarItens). O estoque é conferido pela soma. */
export const pedidoSchema = z.object({
  alunoId: z.string().min(1, 'Selecione um aluno.'),
  itens: z.array(pedidoItemSchema).min(1, 'Adicione ao menos uma peça.'),
})

export type PedidoItemFormValues = z.infer<typeof pedidoItemSchema>
export type PedidoFormValues = z.infer<typeof pedidoSchema>
