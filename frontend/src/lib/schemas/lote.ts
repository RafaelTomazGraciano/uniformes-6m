import { z } from 'zod'

export const itemEntradaSchema = z.object({
  tipoUniformeId: z.string().min(1, 'Selecione o tipo.'),
  tamanho: z.enum(['PP', 'P', 'M', 'G', 'GG'], { message: 'Selecione o tamanho.' }),
  sexo: z.enum(['MASCULINO', 'FEMININO'], { message: 'Selecione o sexo.' }),
  quantidade: z.number({ message: 'Informe a quantidade.' }).int().min(1, 'Quantidade mínima é 1.'),
})

export const loteSchema = z.object({
  chaveAcesso: z.string().trim().min(1, 'Informe a chave de acesso da nota fiscal.'),
  fornecedor: z.string().trim().min(1, 'Informe o fornecedor.'),
  dataEntrega: z.string().min(1, 'Informe a data de entrega.'),
  itens: z
    .array(itemEntradaSchema)
    .min(1, 'Adicione ao menos um item.')
    /* Repetir tipo+tamanho+sexo somaria duas linhas no mesmo saldo e deixaria a
       nota impossível de conferir depois. Melhor somar antes de enviar. */
    .refine(
      (itens) =>
        new Set(itens.map((item) => `${item.tipoUniformeId}|${item.tamanho}|${item.sexo}`)).size === itens.length,
      { message: 'Há itens repetidos: junte a quantidade numa linha só.' },
    ),
})

export type ItemEntradaFormValues = z.infer<typeof itemEntradaSchema>
export type LoteFormValues = z.infer<typeof loteSchema>
