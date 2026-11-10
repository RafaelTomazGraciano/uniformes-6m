import { useEffect, useMemo, useState } from 'react'
import { LogOut, Pencil, Plus, RotateCcw, Search, Trash2, Users, X } from 'lucide-react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'

import { PainelAluno } from '@/components/alunos/painel-aluno'
import { BadgeSaida } from '@/components/alunos/situacao-aluno'
import { ButtonLink } from '@/components/shared/button-link'
import { AppLayout } from '@/components/layout/app-layout'
import { AcoesLinha, type AcaoLinha } from '@/components/shared/acoes-linha'
import { ConfirmarExclusao } from '@/components/shared/confirmar-exclusao'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Paginacao } from '@/components/shared/paginacao'
import { Button } from '@/components/ui/button'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { SelectItens } from '@/components/shared/select-itens'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  useAlunoQuery,
  useAlunosQuery,
  useDeleteAlunoMutation,
  useReativarAlunoMutation,
  useRegistrarSaidaMutation,
} from '@/hooks/use-alunos'
import { useDebouncedValue } from '@/hooks/use-debounced-value'
import { useTurmasQuery } from '@/hooks/use-turmas'
import { ApiError } from '@/lib/api-client'
import { turnoLabels } from '@/lib/labels'
import { toSelectItems } from '@/lib/select-items'
import type { Aluno, FiltroAlunos } from '@/lib/types/aluno'

const POR_PAGINA = 15
export const TODAS_AS_TURMAS = 'todas'

type Situacao = 'ativos' | 'desligados' | 'todos'

const itensSituacao: { value: Situacao; label: string }[] = [
  { value: 'ativos', label: 'Ativos' },
  { value: 'desligados', label: 'Desligados' },
  { value: 'todos', label: 'Todos' },
]

function lerSituacao(valor: string | null): Situacao {
  return valor === 'desligados' || valor === 'todos' ? valor : 'ativos'
}

const ativoPorSituacao: Record<Situacao, boolean | undefined> = {
  ativos: true,
  desligados: false,
  todos: undefined,
}

export function AlunosPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const busca = searchParams.get('busca') ?? ''
  const turmaId = searchParams.get('turma') ?? TODAS_AS_TURMAS
  const situacao = lerSituacao(searchParams.get('situacao'))
  const verId = searchParams.get('ver')
  const page = Number(searchParams.get('pagina') ?? 0) || 0

  /* A busca acontece no servidor (nome ou turma, sem acento nem caixa); o termo
     espera a pessoa parar de digitar para não disparar uma requisição por letra. */
  const buscaAplicada = useDebouncedValue(busca.trim())
  const filtro: FiltroAlunos = {
    nome: buscaAplicada || undefined,
    turmaId: turmaId === TODAS_AS_TURMAS ? undefined : turmaId,
    ativo: ativoPorSituacao[situacao],
  }

  const { data, isLoading, isError, refetch } = useAlunosQuery({ page, size: POR_PAGINA, sort: 'nome,asc' }, filtro)
  /* Total sem filtro: distingue "nenhum aluno cadastrado" de "nenhum aluno neste filtro". */
  const cadastrados = useAlunosQuery({ size: 1, sort: 'nome,asc' })
  const selecionado = useAlunoQuery(verId)
  const turmas = useTurmasQuery()
  const deleteMutation = useDeleteAlunoMutation()
  const saidaMutation = useRegistrarSaidaMutation()
  const reativarMutation = useReativarAlunoMutation()
  const [excluindo, setExcluindo] = useState<Aluno | null>(null)
  const [saindo, setSaindo] = useState<Aluno | null>(null)

  const itensTurma = useMemo(
    () => [
      { value: TODAS_AS_TURMAS, label: 'Todas as turmas' },
      ...toSelectItems(turmas.data, (turma) => turma.id, (turma) => `${turma.nome} — ${turnoLabels[turma.turno]}`),
    ],
    [turmas.data],
  )

  const alunos = data?.content ?? []
  const alunoSelecionado = verId ? selecionado.data : undefined
  const totalCadastrados = cadastrados.data?.totalElements
  const semCadastro = totalCadastrados === 0
  const temFiltro = busca.trim() !== '' || turmaId !== TODAS_AS_TURMAS || situacao !== 'ativos'
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

  /* Depois de uma saída ou exclusão, a última página pode ficar vazia: volta para a última que existe. */
  const ultimaPagina = data && data.content.length === 0 && data.totalElements > 0 && page > 0 ? data.totalPages - 1 : null
  useEffect(() => {
    if (ultimaPagina === null) return
    setSearchParams(
      (atual) => {
        const params = new URLSearchParams(atual)
        if (ultimaPagina > 0) params.set('pagina', String(ultimaPagina))
        else params.delete('pagina')
        return params
      },
      { replace: true },
    )
  }, [ultimaPagina, setSearchParams])

  function limparFiltros() {
    atualizar({ busca: null, turma: null, situacao: null })
  }

  function confirmarExclusao() {
    if (!excluindo) return
    const aluno = excluindo
    deleteMutation.mutate(aluno.id, {
      onSuccess: () => {
        toast.success(`${aluno.nome} foi excluído.`)
        if (verId === aluno.id) atualizar({ ver: null }, false)
        setExcluindo(null)
      },
      onError: (error: Error) => {
        setExcluindo(null)
        /* Aluno com entregas não pode ser excluído: o caminho é registrar a saída. */
        const comEntregas = error instanceof ApiError && error.status === 400 && aluno.ativo
        toast.error(error.message, comEntregas ? { action: { label: 'Registrar saída', onClick: () => setSaindo(aluno) } } : undefined)
      },
    })
  }

  function confirmarSaida() {
    if (!saindo) return
    const aluno = saindo
    saidaMutation.mutate(aluno.id, {
      onSuccess: () => {
        toast.success(`Saída de ${aluno.nome} registrada. O histórico de entregas foi mantido.`)
        setSaindo(null)
      },
      onError: (error: Error) => toast.error(error.message),
    })
  }

  function reativar(aluno: Aluno) {
    reativarMutation.mutate(aluno.id, {
      onSuccess: () => toast.success(`${aluno.nome} foi reativado e pode receber entregas.`),
      onError: (error: Error) => toast.error(error.message),
    })
  }

  function acoesDoAluno(aluno: Aluno): AcaoLinha[] {
    const editar: AcaoLinha = { label: 'Editar', icone: <Pencil aria-hidden />, onSelect: () => navigate(`/alunos/${aluno.id}/editar`) }
    const excluir: AcaoLinha = { label: 'Excluir', icone: <Trash2 aria-hidden />, destrutiva: true, onSelect: () => setExcluindo(aluno) }

    if (!aluno.ativo) {
      return [editar, { label: 'Reativar', icone: <RotateCcw aria-hidden />, onSelect: () => reativar(aluno) }, excluir]
    }
    return [
      { label: 'Nova entrega', icone: <Plus aria-hidden />, onSelect: () => navigate(`/pedidos/nova?aluno=${aluno.id}`) },
      editar,
      { label: 'Registrar saída', icone: <LogOut aria-hidden />, onSelect: () => setSaindo(aluno) },
      excluir,
    ]
  }

  const novoAluno = (
    <ButtonLink size="lg" to={turmaId !== TODAS_AS_TURMAS ? `/alunos/novo?turma=${turmaId}` : '/alunos/novo'}>
      <Plus aria-hidden />
      Novo aluno
    </ButtonLink>
  )

  function tituloVazio() {
    if (buscaAplicada) return `Nenhum aluno com “${buscaAplicada}”`
    if (turmaId !== TODAS_AS_TURMAS) return 'Nenhum aluno nesta turma'
    if (situacao === 'desligados') return 'Nenhum aluno desligado'
    if (situacao === 'ativos') return 'Nenhum aluno ativo'
    return 'Nenhum aluno encontrado'
  }

  return (
    <AppLayout
      title="Alunos"
      description="Quem pode receber uniformes, por turma. Escolha um aluno para ver o que ele já recebeu."
      /* O número acompanha o filtro: com "Desligados" ativo, contar todos os cadastrados confundia. */
      count={data?.totalElements ?? totalCadastrados}
      headerActions={novoAluno}
    >
      {isError && <EstadoErro titulo="Não foi possível carregar os alunos" onRetry={() => refetch()} />}

      {semCadastro && (
        <EstadoVazio
          icone={<Users />}
          titulo="Nenhum aluno cadastrado"
          descricao="Cadastre os alunos para registrar a entrega de uniformes a cada um. Todo aluno pertence a uma turma."
          acao={novoAluno}
        />
      )}

      {!semCadastro && !isError && (
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
              <SelectItens
                className="w-36"
                aria-label="Filtrar por situação"
                items={itensSituacao}
                value={situacao}
                onValueChange={(valor) => atualizar({ situacao: valor === 'ativos' || !valor ? null : valor })}
              />
              {temFiltro && (
                <Button variant="ghost" onClick={limparFiltros}>
                  <X aria-hidden />
                  Limpar filtros
                </Button>
              )}
            </div>

            {isLoading && <EstadoCarregando rotulo="Carregando alunos" colunas={2} />}

            {data && data.totalElements === 0 && (
              <EstadoVazio
                icone={<Search />}
                titulo={tituloVazio()}
                descricao={
                  turmaId !== TODAS_AS_TURMAS
                    ? `Confira a grafia ou procure em todas as turmas. O filtro atual é “${nomeTurma}”.`
                    : situacao === 'ativos'
                      ? 'Confira a grafia, procure também entre os alunos que saíram ou cadastre o aluno.'
                      : 'Confira a grafia ou cadastre o aluno.'
                }
                acao={
                  <>
                    {turmaId !== TODAS_AS_TURMAS && (
                      <Button variant="outline" onClick={() => atualizar({ turma: null })}>
                        Buscar em todas as turmas
                      </Button>
                    )}
                    {situacao !== 'todos' && (
                      <Button variant="outline" onClick={() => atualizar({ situacao: 'todos' })}>
                        Incluir {situacao === 'ativos' ? 'desligados' : 'ativos'}
                      </Button>
                    )}
                    <Button variant="outline" onClick={limparFiltros}>
                      Limpar filtros
                    </Button>
                  </>
                }
              />
            )}

            {alunos.length > 0 && (
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
                    {alunos.map((aluno) => (
                      <TableRow key={aluno.id} data-state={aluno.id === verId ? 'selected' : undefined}>
                        <TableCell className="font-medium">
                          <div className="flex flex-wrap items-center gap-2">
                            <Link
                              to={`?${new URLSearchParams({ ...Object.fromEntries(searchParams), ver: aluno.id })}`}
                              aria-current={aluno.id === verId ? 'true' : undefined}
                              className={
                                aluno.ativo
                                  ? 'underline-offset-4 hover:underline'
                                  : 'text-muted-foreground underline-offset-4 hover:underline'
                              }
                            >
                              {aluno.nome}
                            </Link>
                            <BadgeSaida aluno={aluno} />
                          </div>
                        </TableCell>
                        <TableCell className="text-muted-foreground">{aluno.turmaNome}</TableCell>
                        <TableCell className="text-right">
                          <AcoesLinha rotulo={aluno.nome} acoes={acoesDoAluno(aluno)} />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}

            {data && (
              <Paginacao
                pagina={data.number}
                totalPaginas={data.totalPages}
                totalItens={data.totalElements}
                porPagina={POR_PAGINA}
                rotuloItens={['aluno', 'alunos']}
                onChange={(proxima) => atualizar({ pagina: String(proxima) }, false)}
              />
            )}
          </section>

          {alunoSelecionado ? (
            <PainelAluno
              aluno={alunoSelecionado}
              onFechar={() => atualizar({ ver: null }, false)}
              onRegistrarSaida={() => setSaindo(alunoSelecionado)}
              onReativar={() => reativar(alunoSelecionado)}
              reativando={reativarMutation.isPending}
            />
          ) : (
            <div className="hidden rounded-xl border border-dashed border-border p-6 text-center text-sm text-muted-foreground lg:block">
              {verId && selecionado.isLoading
                ? 'Carregando a ficha do aluno…'
                : 'Escolha um aluno na lista para ver a ficha e o histórico de entregas.'}
            </div>
          )}
        </div>
      )}

      <ConfirmarExclusao
        aberto={Boolean(excluindo)}
        titulo="Excluir aluno"
        descricao={`Excluir “${excluindo?.nome ?? ''}”? Use só para cadastro feito por engano: alunos que já receberam uniforme não podem ser excluídos. Essa ação não pode ser desfeita.`}
        carregando={deleteMutation.isPending}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
      />

      <ConfirmarExclusao
        aberto={Boolean(saindo)}
        titulo="Registrar saída do aluno"
        descricao={`Registrar a saída de “${saindo?.nome ?? ''}”? O histórico de uniformes entregues continua guardado e aparece nas entregas e nos relatórios, mas o aluno deixa de aparecer para novas entregas. Dá para reativar depois.`}
        rotuloConfirmar="Registrar saída"
        rotuloCarregando="Registrando…"
        destrutiva={false}
        carregando={saidaMutation.isPending}
        onConfirmar={confirmarSaida}
        onCancelar={() => setSaindo(null)}
      />
    </AppLayout>
  )
}
