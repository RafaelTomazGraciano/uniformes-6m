import { Pencil } from 'lucide-react'
import { ButtonLink } from '@/components/shared/button-link'

import { ItensAgrupados } from '@/components/shared/itens-agrupados'
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { Skeleton } from '@/components/ui/skeleton'
import { useLoteQuery } from '@/hooks/use-lotes'
import { formatarChaveAcesso, formatarData } from '@/lib/format'

export function DetalheEntradaSheet({ loteId, onClose }: { loteId: string | null; onClose: () => void }) {
  const { data: lote, isLoading, isError } = useLoteQuery(loteId ?? undefined)

  return (
    <Sheet open={Boolean(loteId)} onOpenChange={(aberto) => !aberto && onClose()}>
      <SheetContent className="w-full gap-0 data-[side=right]:sm:max-w-md">
        <SheetHeader className="border-b border-border p-6">
          <SheetTitle className="text-lg">{lote ? lote.fornecedor : 'Entrada'}</SheetTitle>
          <SheetDescription>
            {lote ? `Entregue em ${formatarData(lote.dataEntrega)}` : isError ? 'Não foi possível carregar esta entrada.' : 'Carregando…'}
          </SheetDescription>
          {lote && (
            <p className="font-mono text-xs break-all text-muted-foreground" aria-label="Chave de acesso da nota fiscal">
              {formatarChaveAcesso(lote.notaFiscalChaveAcesso)}
            </p>
          )}
        </SheetHeader>

        <div className="flex-1 overflow-y-auto px-6 py-4">
          {isLoading && (
            <div className="flex flex-col gap-3" aria-busy="true">
              {Array.from({ length: 3 }).map((_, index) => (
                <Skeleton key={index} className="h-12 w-full" />
              ))}
            </div>
          )}
          {lote && <ItensAgrupados itens={lote.itens.map((item) => ({ chave: item.id, tipo: item.tipoUniformeNome, sexo: item.sexo, tamanho: item.tamanho, quantidade: item.quantidade }))} />}
        </div>

        {lote && (
          <SheetFooter className="border-t border-border p-6">
            <ButtonLink size="lg" to={`/entradas/${lote.id}/editar`}>
              <Pencil aria-hidden />
              Editar entrada
            </ButtonLink>
          </SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  )
}
