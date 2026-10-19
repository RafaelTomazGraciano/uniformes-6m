import { Item, ItemActions, ItemContent, ItemDescription, ItemGroup, ItemTitle } from '@/components/ui/item'
import { TAMANHOS } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'
import type { Sexo, Tamanho } from '@/lib/types/uniforme'

export type ItemDetalhe = { chave: string; tipo: string; sexo: Sexo; tamanho: Tamanho; quantidade: number }

/* Uma linha por peça e modelo, com os tamanhos lado a lado: uma nota de 22 itens
   vira 5 linhas legíveis, em vez de uma coluna que obriga a rolar. */
export function ItensAgrupados({ itens }: { itens: ItemDetalhe[] }) {
  const grupos = new Map<string, { tipo: string; sexo: Sexo; tamanhos: Map<Tamanho, number> }>()
  for (const item of itens) {
    const chave = `${item.tipo}|${item.sexo}`
    const grupo = grupos.get(chave) ?? { tipo: item.tipo, sexo: item.sexo, tamanhos: new Map() }
    grupo.tamanhos.set(item.tamanho, (grupo.tamanhos.get(item.tamanho) ?? 0) + item.quantidade)
    grupos.set(chave, grupo)
  }
  const total = itens.reduce((soma, item) => soma + item.quantidade, 0)

  return (
    <div className="flex flex-col gap-3">
      <ItemGroup className="gap-2">
        {[...grupos.entries()].map(([chave, grupo]) => {
          const tamanhos = TAMANHOS.filter((tamanho) => grupo.tamanhos.has(tamanho))
          const subtotal = tamanhos.reduce((soma, tamanho) => soma + (grupo.tamanhos.get(tamanho) ?? 0), 0)
          return (
            <Item key={chave} variant="outline">
              <ItemContent className="gap-2">
                <div>
                  <ItemTitle>{grupo.tipo}</ItemTitle>
                  <ItemDescription>{sexoLabels[grupo.sexo]}</ItemDescription>
                </div>
                <ul className="flex flex-wrap gap-1.5" aria-label={`Tamanhos de ${grupo.tipo} ${sexoLabels[grupo.sexo].toLowerCase()}`}>
                  {tamanhos.map((tamanho) => (
                    <li key={tamanho} className="rounded-md bg-muted px-2 py-0.5 text-xs" data-numeric>
                      <span className="font-semibold">{tamanho}</span> ×{grupo.tamanhos.get(tamanho)}
                    </li>
                  ))}
                </ul>
              </ItemContent>
              <ItemActions className="self-start font-semibold" data-numeric>
                {subtotal} {subtotal === 1 ? 'peça' : 'peças'}
              </ItemActions>
            </Item>
          )
        })}
      </ItemGroup>
      <p className="flex justify-between border-t border-border px-3 pt-3 font-semibold" data-numeric>
        <span>Total</span>
        <span>
          {total} {total === 1 ? 'peça' : 'peças'}
        </span>
      </p>
    </div>
  )
}
