import { ClipboardList, Plus } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { ButtonLink } from '@/components/shared/button-link'

import { AppLayout } from '@/components/layout/app-layout'
import { DetalheEntregaSheet } from '@/components/pedidos/detalhe-entrega-sheet'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Paginacao } from '@/components/shared/paginacao'
import { ResumoItens } from '@/components/shared/resumo-itens'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { usePedidosQuery } from '@/hooks/use-pedidos'
import { LimiteErro } from '@/components/shared/limite-erro'
import { formatarDataHora } from '@/lib/format'
import type { Pedido } from '@/lib/types/pedido'

const POR_PAGINA = 20

export function PedidosPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Number(searchParams.get('pagina') ?? 0) || 0
  const verId = searchParams.get('ver')

  const { data, isLoading, isError, refetch } = usePedidosQuery({ page, size: POR_PAGINA })

  function atualizarParams(mudar: (params: URLSearchParams) => void) {
    setSearchParams((atual) => {
      const params = new URLSearchParams(atual)
      mudar(params)
      return params
    })
  }

  const novaEntrega = (
    <ButtonLink size="lg" to="/pedidos/nova">
      <Plus aria-hidden />
      Nova entrega
    </ButtonLink>
  )

  return (
    <AppLayout
      title="Entregas"
      description="Todo uniforme que saiu do almoxarifado, do mais recente ao mais antigo."
      count={data?.totalElements}
      headerActions={novaEntrega}
    >
      {isLoading && <EstadoCarregando rotulo="Carregando entregas" colunas={4} />}
      {isError && <EstadoErro titulo="Não foi possível carregar as entregas" onRetry={() => refetch()} />}

      {data && data.totalElements === 0 && (
        <EstadoVazio
          icone={<ClipboardList />}
          titulo="Nenhuma entrega registrada"
          descricao="Cada uniforme entregue a um aluno aparece aqui, com quem registrou e quando."
          acao={novaEntrega}
        />
      )}

      {data && data.totalElements > 0 && (
        <>
          <div className="overflow-hidden rounded-xl border border-border bg-card">
            <Table>
              <TableCaption className="sr-only">Entregas de uniforme, da mais recente para a mais antiga</TableCaption>
              <TableHeader>
                <TableRow>
                  <TableHead scope="col">Aluno</TableHead>
                  <TableHead scope="col">Peças</TableHead>
                  <TableHead scope="col">Responsável</TableHead>
                  <TableHead scope="col">Data</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(data.content ?? []).map((pedido, indice) => (
                  <LimiteErro
                    key={pedido?.id ?? `linha-${indice}`}
                    fallback={
                      <TableRow>
                        <TableCell colSpan={4} className="text-sm text-muted-foreground">
                          Não foi possível exibir esta entrega.
                        </TableCell>
                      </TableRow>
                    }
                  >
                    <LinhaEntrega pedido={pedido} selecionado={pedido?.id === verId} searchParams={searchParams} />
                  </LimiteErro>
                ))}
              </TableBody>
            </Table>
          </div>

          <Paginacao
            pagina={data.number}
            totalPaginas={data.totalPages}
            totalItens={data.totalElements}
            porPagina={POR_PAGINA}
            rotuloItens={['entrega', 'entregas']}
            onChange={(proxima) => atualizarParams((params) => params.set('pagina', String(proxima)))}
          />
        </>
      )}

      <DetalheEntregaSheet pedidoId={verId} onClose={() => atualizarParams((params) => params.delete('ver'))} />
    </AppLayout>
  )
}

/* Cada linha se defende sozinha: um campo faltando vira "—" em vez de derrubar a lista. */
function LinhaEntrega({
  pedido,
  selecionado,
  searchParams,
}: {
  pedido: Pedido
  selecionado: boolean
  searchParams: URLSearchParams
}) {
  const itens = pedido.itens ?? []

  return (
    <TableRow data-state={selecionado ? 'selected' : undefined}>
      <TableCell className="font-medium">
        <div className="flex flex-wrap items-center gap-2">
          <Link
            to={`?${new URLSearchParams({ ...Object.fromEntries(searchParams), ver: pedido.id })}`}
            className="underline-offset-4 hover:underline"
          >
            {pedido.alunoNome || 'Aluno não identificado'}
          </Link>
          {pedido.alunoAtivo === false && <Badge variant="outline">Saiu</Badge>}
        </div>
      </TableCell>
      <TableCell>
        <ResumoItens
          itens={itens.map((item) => ({
            chave: item.id,
            nome: item.uniformeTipoUniformeNome ?? '—',
            tamanho: item.uniformeTamanho,
            quantidade: item.quantidade ?? 0,
          }))}
        />
      </TableCell>
      <TableCell className="text-muted-foreground">{pedido.usuarioNome || '—'}</TableCell>
      <TableCell className="whitespace-nowrap" data-numeric>
        {formatarDataHora(pedido.dataEfetivada)}
      </TableCell>
    </TableRow>
  )
}
