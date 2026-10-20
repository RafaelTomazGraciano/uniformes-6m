import { ClipboardList, Plus } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { ButtonLink } from '@/components/shared/button-link'

import { AppLayout } from '@/components/layout/app-layout'
import { DetalheEntregaSheet } from '@/components/pedidos/detalhe-entrega-sheet'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Paginacao } from '@/components/shared/paginacao'
import { ResumoItens } from '@/components/shared/resumo-itens'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { usePedidosQuery } from '@/hooks/use-pedidos'
import { formatarDataHora } from '@/lib/format'

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
                {data.content.map((pedido) => (
                  <TableRow key={pedido.id} data-state={pedido.id === verId ? 'selected' : undefined}>
                    <TableCell className="font-medium">
                      <Link
                        to={`?${new URLSearchParams({ ...Object.fromEntries(searchParams), ver: pedido.id })}`}
                        className="underline-offset-4 hover:underline"
                      >
                        {pedido.alunoNome}
                      </Link>
                    </TableCell>
                    <TableCell>
                      <ResumoItens
                        itens={pedido.itens.map((item) => ({
                          chave: item.id,
                          nome: item.uniformeTipoUniformeNome,
                          tamanho: item.uniformeTamanho,
                          quantidade: item.quantidade,
                        }))}
                      />
                    </TableCell>
                    <TableCell className="text-muted-foreground">{pedido.usuarioNome}</TableCell>
                    <TableCell className="whitespace-nowrap" data-numeric>
                      {formatarDataHora(pedido.dataEfetivada)}
                    </TableCell>
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
            rotuloItens={['entrega', 'entregas']}
            onChange={(proxima) => atualizarParams((params) => params.set('pagina', String(proxima)))}
          />
        </>
      )}

      <DetalheEntregaSheet pedidoId={verId} onClose={() => atualizarParams((params) => params.delete('ver'))} />
    </AppLayout>
  )
}
