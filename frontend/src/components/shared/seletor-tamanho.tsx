import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import type { Tamanho } from '@/lib/types/uniforme'
import { cn } from '@/lib/utils'

export type OpcaoTamanho = {
  valor: string
  tamanho: Tamanho
  /** Ausente: não mostra contagem (entrada de estoque). */
  estoque?: number
  desabilitado?: boolean
}

type SeletorTamanhoProps = {
  opcoes: OpcaoTamanho[]
  valor: string
  onChange: (valor: string) => void
  rotulo: string
  invalido?: boolean
  className?: string
}

/** Tamanho como grupo de botões alternáveis (um só ativo), com o saldo de cada um. */
export function SeletorTamanho({ opcoes, valor, onChange, rotulo, invalido, className }: SeletorTamanhoProps) {
  return (
    <ToggleGroup
      aria-label={rotulo}
      aria-invalid={invalido || undefined}
      variant="outline"
      spacing={1}
      className={cn('flex-wrap', className)}
      value={valor ? [valor] : []}
      onValueChange={(valores: string[]) => {
        const proximo = valores[valores.length - 1]
        if (proximo) onChange(proximo)
      }}
    >
      {opcoes.map((opcao) => {
        const esgotado = opcao.estoque !== undefined && opcao.estoque <= 0
        return (
          <ToggleGroupItem
            key={opcao.valor}
            value={opcao.valor}
            disabled={opcao.desabilitado || esgotado}
            aria-label={
              opcao.estoque === undefined
                ? `Tamanho ${opcao.tamanho}`
                : `Tamanho ${opcao.tamanho}, ${esgotado ? 'esgotado' : `${opcao.estoque} em estoque`}`
            }
            className={cn(
              'flex-col gap-0 data-pressed:border-primary data-pressed:bg-primary data-pressed:text-primary-foreground',
              opcao.estoque === undefined ? 'h-9 min-w-10' : 'h-12 min-w-12',
              esgotado && '[&_[data-tamanho]]:line-through',
            )}
          >
            <span data-tamanho className="text-sm leading-none font-semibold">
              {opcao.tamanho}
            </span>
            {opcao.estoque !== undefined && (
              <span aria-hidden data-numeric className="mt-1 text-[0.6875rem] leading-none opacity-80">
                {esgotado ? 'esgotado' : opcao.estoque}
              </span>
            )}
          </ToggleGroupItem>
        )
      })}
    </ToggleGroup>
  )
}
