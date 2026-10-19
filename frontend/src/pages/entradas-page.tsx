import { PackagePlus, Plus } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { ButtonLink } from '@/components/shared/button-link'

import { DetalheEntradaSheet } from '@/components/entradas/detalhe-entrada-sheet'
import { AppLayout } from '@/components/layout/app-layout'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Paginacao } from '@/components/shared/paginacao'
import { ResumoItens } from '@/components/shared/resumo-itens'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useLotesQuery } from '@/hooks/use-lotes'
import { formatarChaveAcesso, formatarData } from '@/lib/format'
import type { Lote } from '@/lib/types/lote'

const POR_PAGINA = 20

function contarPecas(lote: Lote) {
  return lote.itens.reduce((total, item) => total + item.quantidade, 0)
}

export function EntradasPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Number(searchParams.get('pagina') ?? 0) || 0
  const verId = searchParams.get('ver')
  const { data, isLoading, isError, refetch } = useLotesQuery({ page, size: POR_PAGINA })

  function atualizarParams(mudar: (params: URLSearchParams) => void) {
    setSearchParams((atual) => {
      const params = new URLSearchParams(atual)
      mudar(params)
      return params
    })
  }

  const novaEntrada = (
    <ButtonLink size="lg" to="/entradas/nova">
      <Plus aria-hidden />
      Nova entrada
    </ButtonLink>
  )

  return (
    <AppLayout
      title="Entradas"
      description="Os lotes recebidos dos fornecedores. É por aqui que o estoque sobe."
      count={data?.totalElements}
      headerActions={novaEntrada}
    >
      {isLoading && <EstadoCarregando rotulo="Carregando entradas" colunas={4} />}
      {isError && <EstadoErro titulo="Não foi possível carregar as entradas" onRetry={() => refetch()} />}

      {data && data.totalElements === 0 && (
        <EstadoVazio
          icone={<PackagePlus />}
          titulo="Nenhuma entrada registrada"
          descricao="Sem entrada não há estoque para entregar. Registre o primeiro lote recebido do fornecedor."
          acao={novaEntrada}
        />
      )}

      {data && data.totalElements > 0 && (
        <>
          <div className="overflow-hidden rounded-xl border border-border bg-card">
            <Table>
              <TableCaption className="sr-only">Entradas de estoque, da mais recente para a mais antiga</TableCaption>
              <TableHeader>
                <TableRow>
                  <TableHead scope="col">Fornecedor</TableHead>
                  <TableHead scope="col">Itens</TableHead>
                  <TableHead scope="col">Nota fiscal</TableHead>
                  <TableHead scope="col">Entrega</TableHead>
                  <TableHead scope="col" className="text-right">
                    Peças
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.content.map((lote) => (
                  <TableRow key={lote.id} data-state={lote.id === verId ? 'selected' : undefined}>
                    <TableCell className="font-medium">
                      <Link
                        to={`?${new URLSearchParams({ ...Object.fromEntries(searchParams), ver: lote.id })}`}
                        className="underline-offset-4 hover:underline"
                      >
                        {lote.fornecedor}
                      </Link>
                    </TableCell>
                    <TableCell>
                      <ResumoItens
                        itens={lote.itens.map((item) => ({
                          chave: item.id,
                          nome: item.tipoUniformeNome,
                          tamanho: item.tamanho,
                          quantidade: item.quantidade,
                        }))}
                      />
                    </TableCell>
                    <TableCell className="max-w-56 truncate font-mono text-xs text-muted-foreground" title={lote.notaFiscalChaveAcesso}>
                      {formatarChaveAcesso(lote.notaFiscalChaveAcesso)}
                    </TableCell>
                    <TableCell className="whitespace-nowrap">{formatarData(lote.dataEntrega)}</TableCell>
                    <TableCell className="text-right font-medium" data-numeric>
                      {contarPecas(lote)}
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
            rotuloItens={['entrada', 'entradas']}
            onChange={(proxima) => atualizarParams((params) => params.set('pagina', String(proxima)))}
          />
        </>
      )}

      <DetalheEntradaSheet loteId={verId} onClose={() => atualizarParams((params) => params.delete('ver'))} />
    </AppLayout>
  )
}
