import { useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { TurmaFormDialog } from '@/components/turmas/turma-form-dialog'
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
import { useDeleteTurmaMutation, useTurmasQuery } from '@/hooks/use-turmas'
import { ensinoLabels, turnoLabels } from '@/lib/labels'
import type { Turma } from '@/lib/types/turma'

export function TurmasPage() {
  const [formAberto, setFormAberto] = useState(false)
  const [emEdicao, setEmEdicao] = useState<Turma | undefined>(undefined)
  const [emExclusao, setEmExclusao] = useState<Turma | null>(null)

  const { data: turmas, isLoading, isError } = useTurmasQuery()
  const deleteMutation = useDeleteTurmaMutation()

  function abrirCriacao() {
    setEmEdicao(undefined)
    setFormAberto(true)
  }

  function abrirEdicao(turma: Turma) {
    setEmEdicao(turma)
    setFormAberto(true)
  }

  function confirmarExclusao() {
    if (!emExclusao) return

    deleteMutation.mutate(emExclusao.id, {
      onSuccess: () => {
        toast.success('Turma excluída.')
        setEmExclusao(null)
      },
      // O backend recusa a exclusão quando ainda há alunos vinculados.
      onError: (error: Error) => toast.error(error.message),
    })
  }

  return (
    <AppLayout
      title="Turmas"
      description="Como os alunos são agrupados — nome, turno e ensino."
      headerActions={
        <Button onClick={abrirCriacao}>
          <Plus className="size-4" />
          Nova turma
        </Button>
      }
    >
      <div className="rounded-xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nome</TableHead>
              <TableHead>Turno</TableHead>
              <TableHead>Ensino</TableHead>
              <TableHead className="w-24 text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading &&
              Array.from({ length: 4 }).map((_, index) => (
                <TableRow key={index}>
                  <TableCell colSpan={4}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))}

            {isError && (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-destructive">
                  Não foi possível carregar as turmas. Tente novamente mais tarde.
                </TableCell>
              </TableRow>
            )}

            {!isLoading && !isError && turmas?.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} className="py-10 text-center text-sm text-muted-foreground">
                  Nenhuma turma cadastrada. Todo aluno precisa de uma turma para ser criado.
                </TableCell>
              </TableRow>
            )}

            {turmas?.map((turma) => (
              <TableRow key={turma.id}>
                <TableCell className="font-medium">{turma.nome}</TableCell>
                <TableCell>{turnoLabels[turma.turno]}</TableCell>
                <TableCell>{ensinoLabels[turma.ensino]}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="icon-sm" onClick={() => abrirEdicao(turma)}>
                      <Pencil className="size-4" />
                      <span className="sr-only">Editar {turma.nome}</span>
                    </Button>
                    <Button variant="ghost" size="icon-sm" onClick={() => setEmExclusao(turma)}>
                      <Trash2 className="size-4" />
                      <span className="sr-only">Excluir {turma.nome}</span>
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <TurmaFormDialog open={formAberto} onOpenChange={setFormAberto} turma={emEdicao} />

      <AlertDialog open={Boolean(emExclusao)} onOpenChange={(aberto) => !aberto && setEmExclusao(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir turma</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja excluir "{emExclusao?.nome}"? Só é possível se não houver alunos vinculados a ela.
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
