import { useState } from 'react'
import { Eye } from 'lucide-react'

import { AppLayout } from '@/components/layout/app-layout'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { usePedidosQuery } from '@/hooks/use-pedidos'
import { sexoLabels } from '@/lib/labels'
import type { Pedido } from '@/lib/types/pedido'

function formatDataEfetivada(value: string) {
  return new Date(value).toLocaleString('pt-BR')
}

export function PedidosPage() {
  const [page, setPage] = useState(0)
  const [selectedPedido, setSelectedPedido] = useState<Pedido | null>(null)

  const { data, isLoading, isError } = usePedidosQuery({ page })

  return (
    <AppLayout title="Pedidos">
      <div className="rounded-xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Aluno</TableHead>
              <TableHead>Responsável</TableHead>
              <TableHead>Data</TableHead>
              <TableHead className="text-right">Itens</TableHead>
              <TableHead className="w-16 text-right">Ações</TableHead>
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
                  Não foi possível carregar as entregas. Tente novamente mais tarde.
                </TableCell>
              </TableRow>
            )}

            {!isLoading && !isError && data?.content.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-muted-foreground">
                  Nenhuma entrega registrada.
                </TableCell>
              </TableRow>
            )}

            {data?.content.map((pedido) => (
              <TableRow key={pedido.id}>
                <TableCell>{pedido.alunoNome}</TableCell>
                <TableCell>{pedido.usuarioNome}</TableCell>
                <TableCell>{formatDataEfetivada(pedido.dataEfetivada)}</TableCell>
                <TableCell className="text-right">{pedido.itens.length}</TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="icon-sm" onClick={() => setSelectedPedido(pedido)}>
                    <Eye className="size-4" />
                    <span className="sr-only">Ver itens</span>
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

      <Dialog open={Boolean(selectedPedido)} onOpenChange={(open) => !open && setSelectedPedido(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Itens da entrega</DialogTitle>
          </DialogHeader>

          <div className="space-y-2">
            {selectedPedido?.itens.map((item) => (
              <div
                key={item.id}
                className="flex items-center justify-between rounded-md border border-border px-3 py-2 text-sm"
              >
                <span>
                  {item.uniformeTipoUniformeNome} — {item.uniformeTamanho} — {sexoLabels[item.uniformeSexo]}
                </span>
                <span className="font-medium">x{item.quantidade}</span>
              </div>
            ))}
          </div>
        </DialogContent>
      </Dialog>
    </AppLayout>
  )
}
