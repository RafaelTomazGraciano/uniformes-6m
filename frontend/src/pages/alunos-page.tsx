import { useMemo, useState } from 'react'
import { Pencil, Plus, Search, Trash2, Users, X } from 'lucide-react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'

import { PainelAluno } from '@/components/alunos/painel-aluno'
import { ButtonLink } from '@/components/shared/button-link'
import { AppLayout } from '@/components/layout/app-layout'
import { AcoesLinha } from '@/components/shared/acoes-linha'
import { ConfirmarExclusao } from '@/components/shared/confirmar-exclusao'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Paginacao } from '@/components/shared/paginacao'
import { Button } from '@/components/ui/button'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { SelectItens } from '@/components/shared/select-itens'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useAllAlunosQuery, useDeleteAlunoMutation } from '@/hooks/use-alunos'
import { useTurmasQuery } from '@/hooks/use-turmas'
import { filtrarAluno } from '@/lib/alunos'
import { turnoLabels } from '@/lib/labels'
import { toSelectItems } from '@/lib/select-items'
import type { Aluno } from '@/lib/types/aluno'

const POR_PAGINA = 15
export const TODAS_AS_TURMAS = 'todas'

export function AlunosPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const busca = searchParams.get('busca') ?? ''
  const turmaId = searchParams.get('turma') ?? TODAS_AS_TURMAS
  const verId = searchParams.get('ver')
  const page = Number(searchParams.get('pagina') ?? 0) || 0

  /* A busca é feita no cliente sobre a lista completa: o endpoint de alunos não
     aceita filtro, e paginar no servidor buscaria só dentro da página visível. */
  const { data: alunos, isLoading, isError, refetch } = useAllAlunosQuery()
  const turmas = useTurmasQuery()
  const deleteMutation = useDeleteAlunoMutation()
  const [excluindo, setExcluindo] = useState<Aluno | null>(null)

  const itensTurma = useMemo(
    () => [
      { value: TODAS_AS_TURMAS, label: 'Todas as turmas' },
      ...toSelectItems(turmas.data, (turma) => turma.id, (turma) => `${turma.nome} — ${turnoLabels[turma.turno]}`),
    ],
    [turmas.data],
  )

  const filtrados = useMemo(
    () =>
      (alunos ?? []).filter(
        (aluno) => filtrarAluno(aluno, busca) && (turmaId === TODAS_AS_TURMAS || aluno.turmaId === turmaId),
      ),
    [alunos, busca, turmaId],
  )
  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / POR_PAGINA))
  const paginaAtual = Math.min(page, totalPaginas - 1)
  const visiveis = filtrados.slice(paginaAtual * POR_PAGINA, (paginaAtual + 1) * POR_PAGINA)
  const temFiltro = busca.trim() !== '' || turmaId !== TODAS_AS_TURMAS
  const selecionado = alunos?.find((aluno) => aluno.id === verId)
  const nomeTurma = itensTurma.find((item) => item.value === turmaId)?.label

  function atualizar(mudanca: Record<string, string | null>, resetarPagina = true) {
    setSearchParams(
      (atual) => {
        const params = new URLSearchParams(atual)
        for (const [chave, valor] of Object.entries(mudanca)) {
          if (valor === null || valor === '') params.delete(chave)
          else params.set(chave, valor)
        }
        if (resetarPagina) params.delete('pagina')
        return params
      },
      { replace: true },
    )
  }

  function confirmarExclusao() {
    if (!excluindo) return
    deleteMutation.mutate(excluindo.id, {
      onSuccess: () => {
        toast.success(`${excluindo.nome} foi excluído.`)
        if (verId === excluindo.id) atualizar({ ver: null }, false)
        setExcluindo(null)
      },
      onError: (error: Error) => toast.error(error.message),
    })
  }

  const novoAluno = (
    <ButtonLink size="lg" to={turmaId !== TODAS_AS_TURMAS ? `/alunos/novo?turma=${turmaId}` : '/alunos/novo'}>
      <Plus aria-hidden />
      Novo aluno
    </ButtonLink>
  )

  return (
    <AppLayout
      title="Alunos"
      description="Quem pode receber uniformes, por turma. Escolha um aluno para ver o que ele já recebeu."
      count={alunos?.length}
      headerActions={novoAluno}
    >
      {isError && <EstadoErro titulo="Não foi possível carregar os alunos" onRetry={() => refetch()} />}

      {alunos && alunos.length === 0 && (
        <EstadoVazio
          icone={<Users />}
          titulo="Nenhum aluno cadastrado"
          descricao="Cadastre os alunos para registrar a entrega de uniformes a cada um. Todo aluno pertence a uma turma."
          acao={novoAluno}
        />
      )}

      {(isLoading || (alunos && alunos.length > 0)) && (
        <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
          <section aria-label="Lista de alunos" className="flex min-w-0 flex-col gap-4">
            <div className="flex flex-wrap items-center gap-3">
              <InputGroup className="h-9 max-w-md min-w-56 flex-1">
                <InputGroupAddon>
                  <Search aria-hidden />
                </InputGroupAddon>
                <InputGroupInput
                  type="search"
                  aria-label="Buscar aluno"
                  placeholder="Buscar por nome ou turma"
                  autoComplete="off"
                  value={busca}
                  onChange={(event) => atualizar({ busca: event.target.value })}
                />
              </InputGroup>
              <SelectItens
                className="w-60"
                aria-label="Filtrar por turma"
                items={itensTurma}
                value={turmaId}
                onValueChange={(valor) => atualizar({ turma: valor === TODAS_AS_TURMAS || !valor ? null : valor })}
              />
              {temFiltro && (
                <Button variant="ghost" onClick={() => atualizar({ busca: null, turma: null })}>
                  <X aria-hidden />
                  Limpar filtros
                </Button>
              )}
            </div>

            {isLoading && <EstadoCarregando rotulo="Carregando alunos" colunas={2} />}

            {alunos && filtrados.length === 0 && (
              <EstadoVazio
                icone={<Search />}
                titulo={busca ? `Nenhum aluno com “${busca}”` : 'Nenhum aluno nesta turma'}
                descricao={
                  turmaId !== TODAS_AS_TURMAS
                    ? `Confira a grafia ou procure em todas as turmas. O filtro atual é “${nomeTurma}”.`
                    : 'Confira a grafia ou cadastre o aluno.'
                }
                acao={
                  <>
                    {turmaId !== TODAS_AS_TURMAS && (
                      <Button variant="outline" onClick={() => atualizar({ turma: null })}>
                        Buscar em todas as turmas
                      </Button>
                    )}
                    <Button variant="outline" onClick={() => atualizar({ busca: null, turma: null })}>
                      Limpar filtros
                    </Button>
                  </>
                }
              />
            )}

            {visiveis.length > 0 && (
              <div className="overflow-hidden rounded-xl border border-border bg-card">
                <Table>
                  <TableCaption className="sr-only">
                    Alunos{turmaId !== TODAS_AS_TURMAS ? ` da turma ${nomeTurma}` : ''}
                  </TableCaption>
                  <TableHeader>
                    <TableRow>
                      <TableHead scope="col">Nome</TableHead>
                      <TableHead scope="col">Turma</TableHead>
                      <TableHead scope="col" className="w-14">
                        <span className="sr-only">Ações</span>
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {visiveis.map((aluno) => (
                      <TableRow key={aluno.id} data-state={aluno.id === verId ? 'selected' : undefined}>
                        <TableCell className="font-medium">
                          <Link
                            to={`?${new URLSearchParams({ ...Object.fromEntries(searchParams), ver: aluno.id })}`}
                            aria-current={aluno.id === verId ? 'true' : undefined}
                            className="underline-offset-4 hover:underline"
                          >
                            {aluno.nome}
                          </Link>
                        </TableCell>
                        <TableCell className="text-muted-foreground">{aluno.turmaNome}</TableCell>
                        <TableCell className="text-right">
                          <AcoesLinha
                            rotulo={aluno.nome}
                            acoes={[
                              { label: 'Nova entrega', icone: <Plus aria-hidden />, onSelect: () => navigate(`/pedidos/nova?aluno=${aluno.id}`) },
                              { label: 'Editar', icone: <Pencil aria-hidden />, onSelect: () => navigate(`/alunos/${aluno.id}/editar`) },
                              { label: 'Excluir', icone: <Trash2 aria-hidden />, destrutiva: true, onSelect: () => setExcluindo(aluno) },
                            ]}
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}

            <Paginacao
              pagina={paginaAtual}
              totalPaginas={totalPaginas}
              totalItens={filtrados.length}
              porPagina={POR_PAGINA}
              rotuloItens={['aluno', 'alunos']}
              onChange={(proxima) => atualizar({ pagina: String(proxima) }, false)}
            />
          </section>

          {selecionado ? (
            <PainelAluno aluno={selecionado} onFechar={() => atualizar({ ver: null }, false)} />
          ) : (
            <div className="hidden rounded-xl border border-dashed border-border p-6 text-center text-sm text-muted-foreground lg:block">
              Escolha um aluno na lista para ver a ficha e o histórico de entregas.
            </div>
          )}
        </div>
      )}

      <ConfirmarExclusao
        aberto={Boolean(excluindo)}
        titulo="Excluir aluno"
        descricao={`Excluir “${excluindo?.nome ?? ''}”? Essa ação não pode ser desfeita.`}
        carregando={deleteMutation.isPending}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
      />
    </AppLayout>
  )
}
