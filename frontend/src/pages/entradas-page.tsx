import { useState } from 'react'
import { Pencil, Plus } from 'lucide-react'

import { NovaEntradaDialog } from '@/components/entradas/nova-entrada-dialog'
import { AppLayout } from '@/components/layout/app-layout'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useLotesQuery } from '@/hooks/use-lotes'
import { formatarData } from '@/lib/format'
import { sexoLabels } from '@/lib/labels'
import type { Lote } from '@/lib/types/lote'

function contarPecas(lote: Lote) {
  return lote.itens.reduce((total, item) => total + item.quantidade, 0)
}

export function EntradasPage() {
  const [page, setPage] = useState(0)
  const [formAberto, setFormAberto] = useState(false)
  const [emEdicao, setEmEdicao] = useState<Lote | undefined>(undefined)
  const [detalhe, setDetalhe] = useState<Lote | null>(null)

  const { data, isLoading, isError } = useLotesQuery({ page })

  function abrirCriacao() {
    setEmEdicao(undefined)
    setFormAberto(true)
  }

  function abrirEdicao(lote: Lote) {
    setEmEdicao(lote)
    setFormAberto(true)
  }

  return (
    <AppLayout
      title="Entradas"
      description="Os lotes recebidos dos fornecedores. É por aqui que o estoque sobe."
      headerActions={
        <Button onClick={abrirCriacao}>
          <Plus className="size-4" />
          Nova entrada
        </Button>
      }
    >
      <div className="rounded-xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Fornecedor</TableHead>
              <TableHead>Nota fiscal</TableHead>
              <TableHead>Entrega</TableHead>
              <TableHead className="text-right">Peças</TableHead>
              <TableHead className="w-24 text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading &&
              Array.from({ length: 5 }).map((_, index) => (
                <TableRow key={index}>
                  <TableCell colSpan={5}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))}

            {isError && (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-destructive">
                  Não foi possível carregar as entradas. Tente novamente mais tarde.
                </TableCell>
              </TableRow>
            )}

            {!isLoading && !isError && data?.content.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} className="py-10 text-center text-sm text-muted-foreground">
                  Nenhuma entrada registrada. Sem entrada não há estoque para entregar.
                </TableCell>
              </TableRow>
            )}

            {data?.content.map((lote) => (
              <TableRow key={lote.id}>
                <TableCell className="font-medium">{lote.fornecedor}</TableCell>
                <TableCell className="max-w-48 truncate text-muted-foreground" title={lote.notaFiscalChaveAcesso}>
                  {lote.notaFiscalChaveAcesso}
                </TableCell>
                <TableCell>{formatarData(lote.dataEntrega)}</TableCell>
                <TableCell className="text-right" data-numeric>
                  <button
                    type="button"
                    className="underline-offset-4 hover:underline"
                    onClick={() => setDetalhe(lote)}
                  >
                    {contarPecas(lote)}
                    <span className="sr-only"> peças — ver itens da entrada</span>
                  </button>
                </TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="icon-sm" onClick={() => abrirEdicao(lote)}>
                    <Pencil className="size-4" />
                    <span className="sr-only">Editar entrada de {lote.fornecedor}</span>
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
          <span>
            Página {data.number + 1} de {data.totalPages}
          </span>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled={data.first} onClick={() => setPage((p) => p - 1)}>
              Anterior
            </Button>
            <Button variant="outline" size="sm" disabled={data.last} onClick={() => setPage((p) => p + 1)}>
              Próxima
            </Button>
          </div>
        </div>
      )}

      <NovaEntradaDialog open={formAberto} onOpenChange={setFormAberto} lote={emEdicao} />

      <Dialog open={Boolean(detalhe)} onOpenChange={(aberto) => !aberto && setDetalhe(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Itens da entrada</DialogTitle>
            <DialogDescription>
              {detalhe?.fornecedor} · {formatarData(detalhe?.dataEntrega)}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            {detalhe?.itens.map((item) => (
              <div
                key={item.id}
                className="flex items-center justify-between rounded-md border border-border px-3 py-2 text-sm"
              >
                <span>
                  {item.tipoUniformeNome} — {item.tamanho} — {sexoLabels[item.sexo]}
                </span>
                <span className="font-medium" data-numeric>
                  x{item.quantidade}
                </span>
              </div>
            ))}
          </div>
        </DialogContent>
      </Dialog>
    </AppLayout>
  )
}
