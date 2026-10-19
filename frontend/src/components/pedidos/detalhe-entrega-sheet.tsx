import { Plus } from 'lucide-react'
import { ButtonLink } from '@/components/shared/button-link'

import { ItensAgrupados } from '@/components/shared/itens-agrupados'
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { Skeleton } from '@/components/ui/skeleton'
import { usePedidoQuery } from '@/hooks/use-pedidos'
import { formatarDataHora } from '@/lib/format'

type DetalheEntregaSheetProps = {
  pedidoId: string | null
  onClose: () => void
}

/** Opção B do plano: o detalhe abre sobre a lista, e a URL (?ver=id) mantém o link. */
export function DetalheEntregaSheet({ pedidoId, onClose }: DetalheEntregaSheetProps) {
  const { data: pedido, isLoading, isError } = usePedidoQuery(pedidoId ?? undefined)

  return (
    <Sheet open={Boolean(pedidoId)} onOpenChange={(aberto) => !aberto && onClose()}>
      <SheetContent className="w-full gap-0 data-[side=right]:sm:max-w-md">
        <SheetHeader className="border-b border-border p-6">
          <SheetTitle className="text-lg">{pedido ? pedido.alunoNome : 'Entrega'}</SheetTitle>
          <SheetDescription>
            {pedido
              ? `${formatarDataHora(pedido.dataEfetivada)} · registrada por ${pedido.usuarioNome}`
              : isError
                ? 'Não foi possível carregar esta entrega.'
                : 'Carregando…'}
          </SheetDescription>
        </SheetHeader>

        <div className="flex-1 overflow-y-auto px-6 py-4">
          {isLoading && (
            <div className="flex flex-col gap-3" aria-busy="true">
              {Array.from({ length: 3 }).map((_, index) => (
                <Skeleton key={index} className="h-12 w-full" />
              ))}
            </div>
          )}

          {pedido && <ItensAgrupados itens={pedido.itens.map((item) => ({ chave: item.id, tipo: item.uniformeTipoUniformeNome, sexo: item.uniformeSexo, tamanho: item.uniformeTamanho, quantidade: item.quantidade }))} />}
        </div>

        {pedido && (
          <SheetFooter className="border-t border-border p-6">
            <ButtonLink
              size="lg" to={`/pedidos/nova?aluno=${pedido.alunoId}`}>
              <Plus aria-hidden />
              Nova entrega para este aluno
            </ButtonLink>
            <ButtonLink variant="outline" size="lg" to={`/alunos?ver=${pedido.alunoId}`}>
              Ver ficha do aluno
            </ButtonLink>
          </SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  )
}
