const MAXIMO = 2

type ItemResumo = { chave: string; nome: string; tamanho: string; quantidade: number }

/** "Camiseta M ×2  Short-saia G ×1  +2": o que saiu, legível sem abrir o detalhe. */
export function ResumoItens({ itens }: { itens: ItemResumo[] }) {
  const visiveis = itens.slice(0, MAXIMO)
  const restantes = itens.length - visiveis.length
  const descricao = itens.map((item) => `${item.nome} ${item.tamanho} × ${item.quantidade}`).join(', ')

  return (
    <span className="flex flex-wrap gap-1" title={descricao}>
      <span className="sr-only">{descricao}</span>
      {visiveis.map((item) => (
        <span key={item.chave} aria-hidden className="rounded-md bg-muted px-2 py-0.5 text-xs whitespace-nowrap" data-numeric>
          {item.nome} {item.tamanho} ×{item.quantidade}
        </span>
      ))}
      {restantes > 0 && (
        <span aria-hidden className="rounded-md bg-muted px-2 py-0.5 text-xs text-muted-foreground">
          +{restantes}
        </span>
      )}
    </span>
  )
}
