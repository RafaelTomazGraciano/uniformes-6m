import { useMemo, useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'

import { AlunoFormDialog } from '@/components/alunos/aluno-form-dialog'
import { AlunosFiltros, TODAS_AS_TURMAS } from '@/components/alunos/alunos-filtros'
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
import { useAllAlunosQuery, useDeleteAlunoMutation } from '@/hooks/use-alunos'
import { filtrarAluno } from '@/lib/alunos'
import type { Aluno } from '@/lib/types/aluno'

const POR_PAGINA = 10

export function AlunosPage() {
  const [page, setPage] = useState(0)
  const [busca, setBusca] = useState('')
  const [turmaId, setTurmaId] = useState(TODAS_AS_TURMAS)
  const [formOpen, setFormOpen] = useState(false)
  const [editingAluno, setEditingAluno] = useState<Aluno | undefined>(undefined)
  const [deletingAluno, setDeletingAluno] = useState<Aluno | null>(null)

  /* A busca é feita no cliente sobre a lista completa: o endpoint de alunos não
     aceita filtro, e paginar no servidor buscaria só dentro da página visível. */
  const { data: alunos, isLoading, isError } = useAllAlunosQuery()
  const deleteMutation = useDeleteAlunoMutation()

  const filtrados = useMemo(() => {
    return (alunos ?? []).filter(
      (aluno) => filtrarAluno(aluno, busca) && (turmaId === TODAS_AS_TURMAS || aluno.turmaId === turmaId),
    )
  }, [alunos, busca, turmaId])

  const totalPages = Math.max(1, Math.ceil(filtrados.length / POR_PAGINA))
  /* Filtrar encolhe a lista e pode deixar `page` além do fim; ancorar no total
     evita a tela em branco sem precisar sincronizar estado num efeito. */
  const paginaAtual = Math.min(page, totalPages - 1)
  const visiveis = filtrados.slice(paginaAtual * POR_PAGINA, paginaAtual * POR_PAGINA + POR_PAGINA)

  const temFiltro = busca.trim() !== '' || turmaId !== TODAS_AS_TURMAS

  function filtrar(aplicar: () => void) {
    aplicar()
    setPage(0)
  }

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
      description="Quem pode receber uniformes, por turma."
      headerActions={
        <Button onClick={openCreateDialog}>
          <Plus className="size-4" />
          Novo aluno
        </Button>
      }
    >
      <AlunosFiltros
        busca={busca}
        onBuscaChange={(valor) => filtrar(() => setBusca(valor))}
        turmaId={turmaId}
        onTurmaChange={(valor) => filtrar(() => setTurmaId(valor))}
      />

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

            {!isLoading && !isError && filtrados.length === 0 && (
              <TableRow>
                <TableCell colSpan={3} className="py-10 text-center text-sm text-muted-foreground">
                  {temFiltro
                    ? 'Nenhum aluno encontrado para esta busca. Verifique a grafia ou limpe os filtros.'
                    : 'Nenhum aluno cadastrado ainda. Comece criando o primeiro.'}
                </TableCell>
              </TableRow>
            )}

            {visiveis.map((aluno) => (
              <TableRow key={aluno.id}>
                <TableCell className="font-medium">{aluno.nome}</TableCell>
                <TableCell>{aluno.turmaNome}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="icon-sm" onClick={() => openEditDialog(aluno)}>
                      <Pencil className="size-4" />
                      <span className="sr-only">Editar {aluno.nome}</span>
                    </Button>
                    <Button variant="ghost" size="icon-sm" onClick={() => setDeletingAluno(aluno)}>
                      <Trash2 className="size-4" />
                      <span className="sr-only">Excluir {aluno.nome}</span>
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {filtrados.length > 0 && (
        <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
          <span>
            {filtrados.length} {filtrados.length === 1 ? 'aluno' : 'alunos'}
            {totalPages > 1 && ` · página ${paginaAtual + 1} de ${totalPages}`}
          </span>
          {totalPages > 1 && (
            <div className="flex gap-2">
              <Button variant="outline" size="sm" disabled={paginaAtual === 0} onClick={() => setPage(paginaAtual - 1)}>
                Anterior
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={paginaAtual >= totalPages - 1}
                onClick={() => setPage(paginaAtual + 1)}
              >
                Próxima
              </Button>
            </div>
          )}
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
