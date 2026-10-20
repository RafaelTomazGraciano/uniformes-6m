import { z } from 'zod'

export const itemEntradaSchema = z.object({
  tipoUniformeId: z.string().min(1, 'Selecione o tipo.'),
  tamanho: z.enum(['PP', 'P', 'M', 'G', 'GG'], { message: 'Selecione o tamanho.' }),
  sexo: z.enum(['MASCULINO', 'FEMININO'], { message: 'Selecione o sexo.' }),
  quantidade: z.number({ message: 'Informe a quantidade.' }).int().min(1, 'Quantidade mínima é 1.'),
})

export const loteSchema = z.object({
  /* A NF-e tem 44 dígitos, mas o backend aceita qualquer chave e há lotes antigos
     fora do padrão: o formulário mostra a contagem em vez de bloquear. */
  chaveAcesso: z.string().trim().min(1, 'Informe a chave de acesso da nota fiscal.'),
  fornecedor: z.string().trim().min(1, 'Informe o fornecedor.'),
  dataEntrega: z.string().min(1, 'Informe a data de entrega.'),
  /* Linhas repetidas (mesmo tipo, tamanho e sexo) são somadas pelo backend
     (ItemLoteService.consolidarItens), como na nota com o item em duas linhas. */
  itens: z.array(itemEntradaSchema).min(1, 'Adicione ao menos um item.'),
})

export type ItemEntradaFormValues = z.infer<typeof itemEntradaSchema>
export type LoteFormValues = z.infer<typeof loteSchema>
