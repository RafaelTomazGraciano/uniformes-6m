import { Link } from 'react-router-dom'

import { BentoTile } from '@/components/dashboard/bento'
import { CardAction, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { formatarQuando } from '@/lib/format'
import type { Pedido } from '@/lib/types/pedido'

const LIMITE_VISIVEL = 6

export function EntregasRecentes({
  pedidos,
  isLoading,
  className,
}: {
  pedidos: Pedido[] | undefined
  isLoading: boolean
  className?: string
}) {
  const visiveis = (pedidos ?? []).slice(0, LIMITE_VISIVEL)

  return (
    <BentoTile className={className}>
      <CardHeader className="pb-3">
        <CardTitle>Últimas entregas</CardTitle>
        <CardAction>
          <Link to="/pedidos" className="text-sm text-primary underline-offset-4 hover:underline">
            Ver todas
          </Link>
        </CardAction>
      </CardHeader>

      <CardContent className="pb-4">
        {isLoading && (
          <div className="space-y-3">
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className="h-10 w-full" />
            ))}
          </div>
        )}

        {!isLoading && visiveis.length === 0 && (
          <p className="py-4 text-sm text-muted-foreground">
            Nenhuma entrega registrada ainda. A primeira aparece aqui assim que for feita.
          </p>
        )}

        {!isLoading && visiveis.length > 0 && (
          <ul className="lg:columns-2 lg:gap-x-10">
            {visiveis.map((pedido) => {
              const pecas = pedido.itens.reduce((soma, item) => soma + item.quantidade, 0)

              return (
                <li key={pedido.id} className="flex break-inside-avoid items-baseline justify-between gap-3 border-b border-border py-2.5 last:border-b-0">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium">{pedido.alunoNome}</p>
                    <p className="truncate text-xs text-muted-foreground">
                      {pecas} {pecas === 1 ? 'peça' : 'peças'}, entregue por {pedido.usuarioNome}
                    </p>
                  </div>
                  <span className="shrink-0 text-xs text-muted-foreground">
                    {formatarQuando(pedido.dataEfetivada)}
                  </span>
                </li>
              )
            })}
          </ul>
        )}
      </CardContent>
    </BentoTile>
  )
}
