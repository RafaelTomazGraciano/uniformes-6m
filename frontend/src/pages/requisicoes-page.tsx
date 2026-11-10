import { useState } from 'react'
import { Ban, CircleCheck, ClipboardCheck, ClipboardList, Plus } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { AcoesLinha } from '@/components/shared/acoes-linha'
import { ButtonLink } from '@/components/shared/button-link'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Paginacao } from '@/components/shared/paginacao'
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
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { useAtenderRequisicaoMutation, useCancelarRequisicaoMutation, useRequisicoesQuery } from '@/hooks/use-requisicoes'
import { formatarData } from '@/lib/format'
import { sexoLabels, statusRequisicaoLabels } from '@/lib/labels'
import type { Requisicao, StatusRequisicao } from '@/lib/types/requisicao'

const POR_PAGINA = 20

type Situacao = 'andamento' | 'concluidas'

const FILTROS: Record<Situacao, { rotulo: string; status: StatusRequisicao[] }> = {
  andamento: { rotulo: 'Em andamento', status: ['PENDENTE'] },
  concluidas: { rotulo: 'Atendidas e canceladas', status: ['ATENDIDA', 'CANCELADA'] },
}

const VARIANTE_STATUS: Record<StatusRequisicao, 'low' | 'ok' | 'outline'> = {
  PENDENTE: 'low',
  ATENDIDA: 'ok',
  CANCELADA: 'outline',
}

function descreverPeca(requisicao: Requisicao) {
  return `${requisicao.tipoUniforme} ${sexoLabels[requisicao.sexo].toLowerCase()} ${requisicao.tamanho}`
}

export function RequisicoesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const situacao: Situacao = searchParams.get('situacao') === 'concluidas' ? 'concluidas' : 'andamento'
  const page = Number(searchParams.get('pagina') ?? 0) || 0
  const { data, isLoading, isError, refetch } = useRequisicoesQuery(FILTROS[situacao].status, { page, size: POR_PAGINA })
  const atenderMutation = useAtenderRequisicaoMutation()
  const cancelarMutation = useCancelarRequisicaoMutation()
  const [cancelando, setCancelando] = useState<Requisicao | null>(null)

  function atualizarParams(mudar: (params: URLSearchParams) => void) {
    setSearchParams((atual) => {
      const params = new URLSearchParams(atual)
      mudar(params)
      return params
    })
  }

  function mudarSituacao(proxima: Situacao) {
    atualizarParams((params) => {
      params.delete('pagina')
      if (proxima === 'andamento') params.delete('situacao')
      else params.set('situacao', proxima)
    })
  }

  function atender(requisicao: Requisicao) {
    atenderMutation
      .mutateAsync(requisicao.id)
      .then(() => toast.success(`Requisição de ${descreverPeca(requisicao)} atendida. Registre o lote em Entradas para o estoque subir.`))
      .catch((error: Error) => toast.error(error.message))
  }

  function confirmarCancelamento() {
    if (!cancelando) return
    cancelarMutation
      .mutateAsync(cancelando.id)
      .then(() => {
        toast.success('Requisição cancelada.')
        setCancelando(null)
      })
      .catch((error: Error) => toast.error(error.message))
  }

  const novaRequisicao = (
    <ButtonLink size="lg" to="/requisicoes/nova">
      <Plus aria-hidden />
      Nova requisição
    </ButtonLink>
  )

  return (
    <AppLayout
      title="Requisições"
      description="Pedidos de novas peças ao fornecedor. Ficam em andamento até a entrega; o estoque sobe só com a entrada do lote."
      count={data?.totalElements}
      headerActions={novaRequisicao}
    >
      <div className="flex flex-col gap-5">
        <ToggleGroup
          aria-label="Filtrar por situação"
          variant="outline"
          spacing={2}
          className="flex-wrap"
          value={[situacao]}
          onValueChange={(valores: string[]) => {
            const proxima = valores[valores.length - 1] as Situacao | undefined
            if (proxima) mudarSituacao(proxima)
          }}
        >
          {(Object.keys(FILTROS) as Situacao[]).map((valor) => (
            <ToggleGroupItem
              key={valor}
              value={valor}
              className="h-9 rounded-full px-4 data-pressed:border-foreground data-pressed:bg-foreground data-pressed:text-background"
            >
              {FILTROS[valor].rotulo}
            </ToggleGroupItem>
          ))}
        </ToggleGroup>

        {isLoading && <EstadoCarregando rotulo="Carregando requisições" colunas={6} />}
        {isError && <EstadoErro titulo="Não foi possível carregar as requisições" onRetry={() => refetch()} />}

        {data && data.totalElements === 0 && situacao === 'andamento' && (
          <EstadoVazio
            tom="positivo"
            icone={<ClipboardCheck />}
            titulo="Nenhuma requisição em andamento"
            descricao="Nada esperando o fornecedor. Quando faltar peça, abra uma requisição e ela fica aqui até ser atendida."
            acao={novaRequisicao}
          />
        )}

        {data && data.totalElements === 0 && situacao === 'concluidas' && (
          <EstadoVazio
            icone={<ClipboardList />}
            titulo="Nenhuma requisição concluída"
            descricao="As requisições atendidas ou canceladas aparecem aqui."
          />
        )}

        {data && data.totalElements > 0 && (
          <>
            <div className="overflow-hidden rounded-xl border border-border bg-card">
              <Table>
                <TableCaption className="sr-only">
                  Requisições {FILTROS[situacao].rotulo.toLowerCase()}, da mais recente para a mais antiga
                </TableCaption>
                <TableHeader>
                  <TableRow>
                    <TableHead scope="col">Tipo</TableHead>
                    <TableHead scope="col">Sexo</TableHead>
                    <TableHead scope="col">Tamanho</TableHead>
                    <TableHead scope="col" className="text-right">
                      Quantidade
                    </TableHead>
                    <TableHead scope="col">Data</TableHead>
                    <TableHead scope="col">Solicitante</TableHead>
                    <TableHead scope="col">Status</TableHead>
                    {situacao === 'andamento' && (
                      <TableHead scope="col">
                        <span className="sr-only">Ações</span>
                      </TableHead>
                    )}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.content.map((requisicao) => (
                    <TableRow key={requisicao.id}>
                      <TableCell className="font-medium">
                        {requisicao.tipoUniforme}
                        {requisicao.observacao && (
                          <p className="max-w-64 truncate text-xs font-normal text-muted-foreground" title={requisicao.observacao}>
                            {requisicao.observacao}
                          </p>
                        )}
                      </TableCell>
                      <TableCell>{sexoLabels[requisicao.sexo]}</TableCell>
                      <TableCell>{requisicao.tamanho}</TableCell>
                      <TableCell className="text-right font-medium" data-numeric>
                        {requisicao.quantidade}
                      </TableCell>
                      <TableCell className="whitespace-nowrap">{formatarData(requisicao.dataCriacao)}</TableCell>
                      <TableCell>{requisicao.usuarioNome}</TableCell>
                      <TableCell>
                        <Badge variant={VARIANTE_STATUS[requisicao.status]}>{statusRequisicaoLabels[requisicao.status]}</Badge>
                      </TableCell>
                      {situacao === 'andamento' && (
                        <TableCell className="text-right">
                          {requisicao.status === 'PENDENTE' && (
                            <AcoesLinha
                              rotulo={`a requisição de ${descreverPeca(requisicao)}`}
                              acoes={[
                                { label: 'Marcar como atendida', icone: <CircleCheck aria-hidden />, onSelect: () => atender(requisicao) },
                                { label: 'Cancelar requisição', icone: <Ban aria-hidden />, onSelect: () => setCancelando(requisicao), destrutiva: true },
                              ]}
                            />
                          )}
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            <Paginacao
              pagina={data.number}
              totalPaginas={data.totalPages}
              totalItens={data.totalElements}
              porPagina={POR_PAGINA}
              rotuloItens={['requisição', 'requisições']}
              onChange={(proxima) => atualizarParams((params) => params.set('pagina', String(proxima)))}
            />
          </>
        )}
      </div>

      <AlertDialog open={Boolean(cancelando)} onOpenChange={(open) => !open && setCancelando(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancelar esta requisição?</AlertDialogTitle>
            <AlertDialogDescription>
              {cancelando
                ? `${cancelando.quantidade} × ${descreverPeca(cancelando)} sai dos pedidos em andamento. Não dá para reabrir depois.`
                : ''}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Voltar</AlertDialogCancel>
            <AlertDialogAction variant="destructive" disabled={cancelarMutation.isPending} onClick={confirmarCancelamento}>
              {cancelarMutation.isPending ? 'Cancelando…' : 'Cancelar requisição'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </AppLayout>
  )
}
