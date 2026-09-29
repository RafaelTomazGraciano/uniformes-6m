import { Link } from 'react-router-dom'

import { BentoTile } from '@/components/dashboard/bento'
import { Badge } from '@/components/ui/badge'
import { CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { itensParaRepor, nivelEstoque } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'
import type { Uniforme } from '@/lib/types/uniforme'

const LIMITE_VISIVEL = 6

export function ListaReposicao({
  uniformes,
  isLoading,
  className,
}: {
  uniformes: Uniforme[] | undefined
  isLoading: boolean
  className?: string
}) {
  const itens = itensParaRepor(uniformes ?? [])
  const visiveis = itens.slice(0, LIMITE_VISIVEL)
  const restantes = itens.length - visiveis.length

  return (
    <BentoTile className={className}>
      <CardHeader className="pb-3">
        <div>
          <CardTitle>Repor primeiro</CardTitle>
          <CardDescription>Do que acabou para o que está acabando.</CardDescription>
        </div>
      </CardHeader>

      <CardContent className="pb-4">
        {isLoading && (
          <div className="space-y-3">
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className="h-10 w-full" />
            ))}
          </div>
        )}

        {!isLoading && itens.length === 0 && (
          <p className="py-4 text-sm text-muted-foreground">
            Todo tamanho tem mais de 5 peças. Nada a repor por enquanto.
          </p>
        )}

        {!isLoading && visiveis.length > 0 && (
          <ul className="divide-y divide-border">
            {visiveis.map((uniforme) => {
              const esgotado = nivelEstoque(uniforme.quantidade) === 'esgotado'

              return (
                <li key={uniforme.id} className="flex items-center justify-between gap-3 py-2.5 first:pt-0">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium">{uniforme.tipoUniformeNome}</p>
                    <p className="truncate text-xs text-muted-foreground">
                      {sexoLabels[uniforme.sexo]}, tamanho {uniforme.tamanho}
                    </p>
                  </div>
                  <Badge variant={esgotado ? 'out' : 'low'} data-numeric>
                    {esgotado ? 'esgotado' : `${uniforme.quantidade} ${uniforme.quantidade === 1 ? 'peça' : 'peças'}`}
                  </Badge>
                </li>
              )
            })}
          </ul>
        )}

        {restantes > 0 && (
          <Link
            to="/estoque"
            className="mt-3 inline-block text-sm text-primary underline-offset-4 hover:underline"
          >
            Ver os outros {restantes} tamanhos
          </Link>
        )}
      </CardContent>
    </BentoTile>
  )
}
