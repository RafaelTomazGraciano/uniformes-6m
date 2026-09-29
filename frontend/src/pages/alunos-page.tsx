import { useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'

import { AlunoFormDialog } from '@/components/alunos/aluno-form-dialog'
import { AppLayout } from '@/components/layout/app-layout'
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
import { useAlunosQuery, useDeleteAlunoMutation } from '@/hooks/use-alunos'
import type { Aluno } from '@/lib/types/aluno'

export function AlunosPage() {
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [editingAluno, setEditingAluno] = useState<Aluno | undefined>(undefined)
  const [deletingAluno, setDeletingAluno] = useState<Aluno | null>(null)

  const { data, isLoading, isError } = useAlunosQuery({ page, sort: 'nome,asc' })
  const deleteMutation = useDeleteAlunoMutation()

  const openCreateDialog = () => {
    setEditingAluno(undefined)
    setFormOpen(true)
  }

  const openEditDialog = (aluno: Aluno) => {
    setEditingAluno(aluno)
    setFormOpen(true)
  }

  const confirmDelete = () => {
    if (!deletingAluno) return

    deleteMutation.mutate(deletingAluno.id, {
      onSuccess: () => {
        toast.success('Aluno excluído com sucesso.')
        setDeletingAluno(null)
      },
      onError: (error: Error) => toast.error(error.message),
    })
  }

  return (
    <AppLayout
      title="Alunos"
      headerActions={
        <Button onClick={openCreateDialog}>
          <Plus className="size-4" />
          Novo aluno
        </Button>
      }
    >
      <div className="rounded-xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nome</TableHead>
              <TableHead>Turma</TableHead>
              <TableHead className="w-24 text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading &&
              Array.from({ length: 5 }).map((_, index) => (
                <TableRow key={index}>
                  <TableCell colSpan={3}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))}

            {isError && (
              <TableRow>
                <TableCell colSpan={3} className="text-center text-destructive">
                  Não foi possível carregar os alunos. Tente novamente mais tarde.
                </TableCell>
              </TableRow>
            )}

            {!isLoading && !isError && data?.content.length === 0 && (
              <TableRow>
                <TableCell colSpan={3} className="text-center text-muted-foreground">
                  Nenhum aluno cadastrado.
                </TableCell>
              </TableRow>
            )}

            {data?.content.map((aluno) => (
              <TableRow key={aluno.id}>
                <TableCell>{aluno.nome}</TableCell>
                <TableCell>{aluno.turmaNome}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="icon-sm" onClick={() => openEditDialog(aluno)}>
                      <Pencil className="size-4" />
                      <span className="sr-only">Editar</span>
                    </Button>
                    <Button variant="ghost" size="icon-sm" onClick={() => setDeletingAluno(aluno)}>
                      <Trash2 className="size-4" />
                      <span className="sr-only">Excluir</span>
                    </Button>
                  </div>
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

      <AlunoFormDialog open={formOpen} onOpenChange={setFormOpen} aluno={editingAluno} />

      <AlertDialog open={Boolean(deletingAluno)} onOpenChange={(open) => !open && setDeletingAluno(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir aluno</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja excluir "{deletingAluno?.nome}"? Essa ação não pode ser desfeita.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction disabled={deleteMutation.isPending} onClick={confirmDelete}>
              Excluir
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </AppLayout>
  )
}
