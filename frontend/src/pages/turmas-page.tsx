import { useState } from 'react'
import { GraduationCap, Moon, Pencil, Plus, Sun, Sunset, Trash2, UserPlus } from 'lucide-react'
import { ButtonLink } from '@/components/shared/button-link'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { AcoesLinha } from '@/components/shared/acoes-linha'
import { ConfirmarExclusao } from '@/components/shared/confirmar-exclusao'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { useDeleteTurmaMutation, useTurmasQuery } from '@/hooks/use-turmas'
import { ensinoLabels, turnoLabels } from '@/lib/labels'
import type { Turma, Turno } from '@/lib/types/turma'
import { cn } from '@/lib/utils'

const TURNOS: { turno: Turno; icone: React.ReactNode }[] = [
  { turno: 'DIURNO', icone: <Sun aria-hidden className="size-4" /> },
  { turno: 'VESPERTINO', icone: <Sunset aria-hidden className="size-4" /> },
  { turno: 'NOTURNO', icone: <Moon aria-hidden className="size-4" /> },
]

export function TurmasPage() {
  const navigate = useNavigate()
  const { data: turmas, isLoading, isError, refetch } = useTurmasQuery()
  const deleteMutation = useDeleteTurmaMutation()
  const [excluindo, setExcluindo] = useState<Turma | null>(null)

  const novaTurma = (
    <ButtonLink size="lg" to="/turmas/nova">
      <Plus aria-hidden />
      Nova turma
    </ButtonLink>
  )

  function confirmarExclusao() {
    if (!excluindo) return
    deleteMutation.mutate(excluindo.id, {
      onSuccess: () => {
        toast.success(`Turma ${excluindo.nome} excluída.`)
        setExcluindo(null)
      },
      onError: (error: Error) => toast.error(error.message),
    })
  }

  return (
    <AppLayout
      title="Turmas"
      description="Como os alunos são agrupados. Abra uma turma para ver os alunos dela."
      count={turmas?.length}
      headerActions={novaTurma}
    >
      {isLoading && <EstadoCarregando rotulo="Carregando turmas" colunas={3} />}
      {isError && <EstadoErro titulo="Não foi possível carregar as turmas" onRetry={() => refetch()} />}

      {turmas && turmas.length === 0 && (
        <EstadoVazio
          icone={<GraduationCap />}
          titulo="Nenhuma turma cadastrada"
          descricao="Todo aluno precisa de uma turma. Crie as turmas primeiro e depois cadastre os alunos."
          acao={novaTurma}
        />
      )}

      {turmas && turmas.length > 0 && (
        <div className="flex flex-col gap-8">
          {TURNOS.map(({ turno, icone }) => {
            const doTurno = turmas.filter((turma) => turma.turno === turno)
            if (doTurno.length === 0) return null
            return (
              <section key={turno} aria-labelledby={`turno-${turno}`} className="flex flex-col gap-3">
                <h2
                  id={`turno-${turno}`}
                  className="flex items-center gap-2 text-sm font-semibold tracking-wide text-muted-foreground uppercase"
                >
                  {icone}
                  {turnoLabels[turno]} · {doTurno.length} {doTurno.length === 1 ? 'turma' : 'turmas'}
                </h2>
                <ul className="grid gap-4 [grid-template-columns:repeat(auto-fill,minmax(15rem,1fr))]">
                  {doTurno.map((turma) => {
                    const vazia = turma.quantidadeAlunos === 0
                    return (
                      <li key={turma.id}>
                        <Card className={cn('h-full', vazia && 'border-dashed bg-muted/30')}>
                          <CardHeader>
                            <CardTitle className="text-base">
                              <Link to={`/alunos?turma=${turma.id}`} className="underline-offset-4 hover:underline">
                                {turma.nome}
                              </Link>
                            </CardTitle>
                            <CardDescription>{ensinoLabels[turma.ensino]}</CardDescription>
                          </CardHeader>
                          <CardContent>
                            <p className={cn('text-sm', vazia && 'text-signal-low')} data-numeric>
                              {vazia ? (
                                'Nenhum aluno ainda'
                              ) : (
                                <>
                                  <strong className="text-lg font-semibold">{turma.quantidadeAlunos}</strong>{' '}
                                  {turma.quantidadeAlunos === 1 ? 'aluno' : 'alunos'}
                                </>
                              )}
                            </p>
                          </CardContent>
                          <CardFooter className="mt-auto justify-between">
                            <Link
                              to={vazia ? `/alunos/novo?turma=${turma.id}` : `/alunos?turma=${turma.id}`}
                              className="text-sm text-primary underline-offset-4 hover:underline"
                            >
                              {vazia ? 'Adicionar aluno' : 'Ver alunos'}
                            </Link>
                            <AcoesLinha
                              rotulo={turma.nome}
                              acoes={[
                                { label: 'Editar', icone: <Pencil aria-hidden />, onSelect: () => navigate(`/turmas/${turma.id}/editar`) },
                                { label: 'Adicionar aluno', icone: <UserPlus aria-hidden />, onSelect: () => navigate(`/alunos/novo?turma=${turma.id}`) },
                                { label: 'Excluir', icone: <Trash2 aria-hidden />, destrutiva: true, onSelect: () => setExcluindo(turma) },
                              ]}
                            />
                          </CardFooter>
                        </Card>
                      </li>
                    )
                  })}
                </ul>
              </section>
            )
          })}
        </div>
      )}

      <ConfirmarExclusao
        aberto={Boolean(excluindo)}
        titulo="Excluir turma"
        descricao={
          excluindo && excluindo.quantidadeAlunos > 0
            ? `A turma ${excluindo.nome} tem ${excluindo.quantidadeAlunos} alunos. Mova ou exclua os alunos antes de excluir a turma.`
            : `Excluir a turma ${excluindo?.nome ?? ''}? Essa ação não pode ser desfeita.`
        }
        carregando={deleteMutation.isPending}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
      />
    </AppLayout>
  )
}
