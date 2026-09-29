import { useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { TipoUniformeFormDialog } from '@/components/tipos-uniforme/tipo-uniforme-form-dialog'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useDeleteTipoUniformeMutation, useTiposUniformeQuery } from '@/hooks/use-tipos-uniforme'
import type { TipoUniforme } from '@/lib/types/tipo-uniforme'

export function TiposUniformePage() {
  const [formAberto, setFormAberto] = useState(false)
  const [emEdicao, setEmEdicao] = useState<TipoUniforme | undefined>(undefined)
  const [emExclusao, setEmExclusao] = useState<TipoUniforme | null>(null)

  const { data: tipos, isLoading, isError } = useTiposUniformeQuery()
  const deleteMutation = useDeleteTipoUniformeMutation()

  function abrirCriacao() {
    setEmEdicao(undefined)
    setFormAberto(true)
  }

  function abrirEdicao(tipo: TipoUniforme) {
    setEmEdicao(tipo)
    setFormAberto(true)
  }

  function confirmarExclusao() {
    if (!emExclusao) return

    deleteMutation.mutate(emExclusao.id, {
      onSuccess: () => {
        toast.success('Tipo de uniforme excluído.')
        setEmExclusao(null)
      },
      /* O backend recusa a exclusão quando há uniformes ou itens de lote
         vinculados; a mensagem dele explica o motivo melhor que um texto fixo. */
      onError: (error: Error) => toast.error(error.message),
    })
  }

  return (
    <AppLayout
      title="Tipos de uniforme"
      description="As peças que a escola distribui. Cada entrada de lote se apoia nesta lista."
      headerActions={
        <Button onClick={abrirCriacao}>
          <Plus className="size-4" />
          Novo tipo
        </Button>
      }
    >
      <div className="rounded-xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Tipo</TableHead>
              <TableHead className="w-24 text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading &&
              Array.from({ length: 4 }).map((_, index) => (
                <TableRow key={index}>
                  <TableCell colSpan={2}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))}

            {isError && (
              <TableRow>
                <TableCell colSpan={2} className="text-center text-destructive">
                  Não foi possível carregar os tipos de uniforme. Tente novamente mais tarde.
                </TableCell>
              </TableRow>
            )}

            {!isLoading && !isError && tipos?.length === 0 && (
              <TableRow>
                <TableCell colSpan={2} className="py-10 text-center text-sm text-muted-foreground">
                  Nenhum tipo cadastrado. Sem um tipo não é possível dar entrada em estoque.
                </TableCell>
              </TableRow>
            )}

            {tipos?.map((tipo) => (
              <TableRow key={tipo.id}>
                <TableCell className="font-medium">{tipo.tipo}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="icon-sm" onClick={() => abrirEdicao(tipo)}>
                      <Pencil className="size-4" />
                      <span className="sr-only">Editar {tipo.tipo}</span>
                    </Button>
                    <Button variant="ghost" size="icon-sm" onClick={() => setEmExclusao(tipo)}>
                      <Trash2 className="size-4" />
                      <span className="sr-only">Excluir {tipo.tipo}</span>
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <TipoUniformeFormDialog open={formAberto} onOpenChange={setFormAberto} tipoUniforme={emEdicao} />

      <AlertDialog open={Boolean(emExclusao)} onOpenChange={(aberto) => !aberto && setEmExclusao(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir tipo de uniforme</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja excluir "{emExclusao?.tipo}"? Só é possível se não houver peças nem entradas
              ligadas a ele.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction disabled={deleteMutation.isPending} onClick={confirmarExclusao}>
              Excluir
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </AppLayout>
  )
}
