import { BentoTile } from '@/components/dashboard/bento'
import { CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { TAMANHOS, agruparPorTipo, nivelEstoque } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'
import type { Uniforme } from '@/lib/types/uniforme'

type MatrizEstoqueProps = {
  uniformes: Uniforme[] | undefined
  isLoading: boolean
  isError: boolean
  className?: string
}

export function MatrizEstoque({ uniformes, isLoading, isError, className }: MatrizEstoqueProps) {
  const grupos = agruparPorTipo(uniformes ?? [])

  return (
    <BentoTile className={className}>
      <CardHeader className="border-b border-border">
        <div>
          <CardTitle>Estoque por tamanho</CardTitle>
          <CardDescription>Peças disponíveis hoje. Destacadas, as que precisam de reposição.</CardDescription>
        </div>
        <CardAction className="hidden items-center gap-4 pt-1 text-xs text-muted-foreground sm:flex">
          <Legenda className="bg-signal-out-surface ring-1 ring-signal-out/35">esgotado</Legenda>
          <Legenda className="bg-signal-low-surface">5 ou menos</Legenda>
        </CardAction>
      </CardHeader>

      <CardContent className="px-0 pb-0">
        {isError && (
          <p className="px-5 py-10 text-center text-sm text-signal-out">
            Não foi possível carregar o estoque. Recarregue a página para tentar de novo.
          </p>
        )}

        {isLoading && (
          <div className="space-y-3 px-5 py-5">
            {Array.from({ length: 6 }).map((_, index) => (
              <Skeleton key={index} className="h-9 w-full" />
            ))}
          </div>
        )}

        {!isLoading && !isError && grupos.length === 0 && (
          <p className="px-5 py-10 text-center text-sm text-muted-foreground">
            Nenhum uniforme em estoque ainda. Quando um lote der entrada, os tamanhos aparecem aqui.
          </p>
        )}

        {!isLoading && !isError && grupos.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[26rem] border-collapse text-sm">
              <caption className="sr-only">Quantidade de peças em estoque por tipo de uniforme, sexo e tamanho</caption>
              <thead>
                <tr className="border-b border-border text-xs text-muted-foreground">
                  <th scope="col" className="py-2 pr-3 pl-5 text-left font-medium">
                    Uniforme
                  </th>
                  {TAMANHOS.map((tamanho) => (
                    <th key={tamanho} scope="col" className="px-1 py-2 text-center font-medium">
                      {tamanho}
                    </th>
                  ))}
                  <th scope="col" className="w-16 py-2 pr-5 pl-3 text-right font-medium">
                    Total
                  </th>
                </tr>
              </thead>

              {grupos.map((grupo) => (
                <tbody key={grupo.tipo} className="border-b border-border last:border-0">
                  <tr>
                    <th
                      scope="colgroup"
                      colSpan={TAMANHOS.length + 1}
                      className="bg-muted/60 py-1.5 pr-3 pl-5 text-left text-[0.8125rem] font-semibold"
                    >
                      {grupo.tipo}
                    </th>
                    <td className="bg-muted/60 py-1.5 pr-5 pl-3 text-right text-[0.8125rem] font-semibold">
                      {grupo.total}
                    </td>
                  </tr>

                  {grupo.linhas.map((linha) => (
                    <tr key={linha.sexo}>
                      <th scope="row" className="py-1 pr-3 pl-5 text-left font-normal text-muted-foreground">
                        {sexoLabels[linha.sexo]}
                      </th>
                      {TAMANHOS.map((tamanho) => (
                        <Celula
                          key={tamanho}
                          quantidade={linha.porTamanho[tamanho]}
                          descricao={`${grupo.tipo} ${sexoLabels[linha.sexo].toLowerCase()}, tamanho ${tamanho}`}
                        />
                      ))}
                      <td className="py-1 pr-5 pl-3 text-right font-medium">{linha.total}</td>
                    </tr>
                  ))}
                </tbody>
              ))}
            </table>
          </div>
        )}
      </CardContent>
    </BentoTile>
  )
}

function Celula({ quantidade, descricao }: { quantidade: number | undefined; descricao: string }) {
  if (quantidade === undefined) {
    return (
      <td className="px-1 py-1 text-center">
        <span className="sr-only">{descricao}: não cadastrado</span>
        <span aria-hidden className="text-muted-foreground/40">
          –
        </span>
      </td>
    )
  }

  const nivel = nivelEstoque(quantidade)
  const estilo =
    nivel === 'esgotado'
      ? 'bg-signal-out-surface text-signal-out font-semibold ring-1 ring-signal-out/35'
      : nivel === 'baixo'
        ? 'bg-signal-low-surface text-signal-low font-medium'
        : 'text-foreground'

  return (
    <td className="px-1 py-1 text-center">
      <span
        className={`mx-auto flex h-8 w-12 items-center justify-center rounded-md ${estilo}`}
        title={`${descricao}: ${quantidade} ${quantidade === 1 ? 'peça' : 'peças'}`}
      >
        <span className="sr-only">
          {descricao}: {quantidade} em estoque
          {nivel === 'esgotado' ? ', esgotado' : nivel === 'baixo' ? ', estoque baixo' : ''}
        </span>
        <span aria-hidden data-numeric>
          {quantidade}
        </span>
      </span>
    </td>
  )
}

function Legenda({ className, children }: { className: string; children: React.ReactNode }) {
  return (
    <span className="flex items-center gap-1.5">
      <span aria-hidden className={`size-3 rounded-sm ${className}`} />
      {children}
    </span>
  )
}
