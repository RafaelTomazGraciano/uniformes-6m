import { useMemo, useState } from 'react'
import { Boxes, PackagePlus, Pencil, Plus, Shirt, Trash2 } from 'lucide-react'
import { ButtonLink } from '@/components/shared/button-link'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { AcoesLinha } from '@/components/shared/acoes-linha'
import { ConfirmarExclusao } from '@/components/shared/confirmar-exclusao'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useDeleteTipoUniformeMutation, useTiposUniformeQuery } from '@/hooks/use-tipos-uniforme'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { nivelDoUniforme, TAMANHOS } from '@/lib/estoque'
import type { TipoUniforme } from '@/lib/types/tipo-uniforme'
import { cn } from '@/lib/utils'

type ResumoTipo = { pecas: number; baixos: number; esgotados: number; porTamanho: number[]; alertaPorTamanho: boolean[] }

export function TiposUniformePage() {
  const navigate = useNavigate()
  const tipos = useTiposUniformeQuery()
  const uniformes = useAllUniformesQuery()
  const deleteMutation = useDeleteTipoUniformeMutation()
  const [excluindo, setExcluindo] = useState<TipoUniforme | null>(null)

  const resumo = useMemo(() => {
    const porTipo = new Map<string, ResumoTipo>()
    for (const uniforme of uniformes.data ?? []) {
      const atual = porTipo.get(uniforme.tipoUniformeId) ?? {
        pecas: 0,
        baixos: 0,
        esgotados: 0,
        porTamanho: TAMANHOS.map(() => 0),
        alertaPorTamanho: TAMANHOS.map(() => false),
      }
      const nivel = nivelDoUniforme(uniforme)
      const indice = TAMANHOS.indexOf(uniforme.tamanho)
      atual.pecas += uniforme.quantidade
      atual.porTamanho[indice] += uniforme.quantidade
      if (nivel !== 'ok') atual.alertaPorTamanho[indice] = true
      if (nivel === 'baixo') atual.baixos += 1
      if (nivel === 'esgotado') atual.esgotados += 1
      porTipo.set(uniforme.tipoUniformeId, atual)
    }
    return porTipo
  }, [uniformes.data])

  const maiorTamanho = Math.max(1, ...[...resumo.values()].flatMap((item) => item.porTamanho))

  const novoTipo = (
    <ButtonLink size="lg" to="/tipos-uniforme/novo">
      <Plus aria-hidden />
      Novo tipo
    </ButtonLink>
  )

  function confirmarExclusao() {
    if (!excluindo) return
    deleteMutation.mutate(excluindo.id, {
      onSuccess: () => {
        toast.success(`Tipo “${excluindo.tipo}” excluído.`)
        setExcluindo(null)
      },
      onError: (error: Error) => toast.error(error.message),
    })
  }

  return (
    <AppLayout
      title="Tipos de uniforme"
      description="As peças que a escola distribui e o limite de reposição de cada uma."
      count={tipos.data?.length}
      headerActions={novoTipo}
    >
      {tipos.isLoading && <EstadoCarregando rotulo="Carregando tipos de uniforme" colunas={5} />}
      {tipos.isError && <EstadoErro titulo="Não foi possível carregar os tipos" onRetry={() => tipos.refetch()} />}

      {tipos.data && tipos.data.length === 0 && (
        <EstadoVazio
          icone={<Shirt />}
          titulo="Nenhum tipo cadastrado"
          descricao="Sem um tipo não é possível dar entrada em estoque. Comece pela peça mais usada, como a camiseta."
          acao={novoTipo}
        />
      )}

      {tipos.data && tipos.data.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-border bg-card">
          <Table className="min-w-[48rem]">
            <TableCaption className="sr-only">Tipos de uniforme com limite e situação do estoque</TableCaption>
            <TableHeader>
              <TableRow>
                <TableHead scope="col">Tipo</TableHead>
                <TableHead scope="col" className="text-right">
                  Avisar com
                </TableHead>
                <TableHead scope="col" className="text-right">
                  Em estoque
                </TableHead>
                <TableHead scope="col">Por tamanho (PP → GG)</TableHead>
                <TableHead scope="col">Situação</TableHead>
                <TableHead scope="col" className="w-14">
                  <span className="sr-only">Ações</span>
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {tipos.data.map((tipo) => {
                const dados = resumo.get(tipo.id)
                return (
                  <TableRow key={tipo.id}>
                    <TableCell className="font-medium">
                      <Link to={`/tipos-uniforme/${tipo.id}/editar`} className="underline-offset-4 hover:underline">
                        {tipo.tipo}
                      </Link>
                    </TableCell>
                    <TableCell className="text-right" data-numeric>
                      ≤ {tipo.estoqueBaixo}
                    </TableCell>
                    <TableCell className="text-right font-medium" data-numeric>
                      {dados?.pecas ?? 0}
                    </TableCell>
                    <TableCell>
                      {dados ? (
                        <div className="flex h-7 items-end gap-1" aria-hidden>
                          {dados.porTamanho.map((quantidade, indice) => (
                            <span
                              key={TAMANHOS[indice]}
                              title={`${TAMANHOS[indice]}: ${quantidade}`}
                              className={cn(
                                'w-3.5 rounded-sm bg-primary/35',
                                dados.alertaPorTamanho[indice] && 'bg-signal-low',
                                quantidade === 0 && 'bg-signal-out',
                              )}
                              style={{ height: `${Math.max(12, (quantidade / maiorTamanho) * 100)}%` }}
                            />
                          ))}
                        </div>
                      ) : (
                        <span className="text-sm text-muted-foreground">Nenhuma entrada ainda</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {!dados && <Badge variant="outline">Sem estoque</Badge>}
                        {dados && dados.baixos === 0 && dados.esgotados === 0 && <Badge variant="ok">Em dia</Badge>}
                        {dados && dados.baixos > 0 && (
                          <Badge variant="low">
                            {dados.baixos} {dados.baixos === 1 ? 'baixo' : 'baixos'}
                          </Badge>
                        )}
                        {dados && dados.esgotados > 0 && (
                          <Badge variant="out">
                            {dados.esgotados} {dados.esgotados === 1 ? 'esgotado' : 'esgotados'}
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="text-right">
                      <AcoesLinha
                        rotulo={tipo.tipo}
                        acoes={[
                          { label: 'Editar', icone: <Pencil aria-hidden />, onSelect: () => navigate(`/tipos-uniforme/${tipo.id}/editar`) },
                          { label: 'Ver no estoque', icone: <Boxes aria-hidden />, onSelect: () => navigate('/estoque') },
                          {
                            label: 'Nova entrada deste tipo',
                            icone: <PackagePlus aria-hidden />,
                            onSelect: () => navigate(`/entradas/nova?tipo=${tipo.id}`),
                          },
                          { label: 'Excluir', icone: <Trash2 aria-hidden />, destrutiva: true, onSelect: () => setExcluindo(tipo) },
                        ]}
                      />
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </div>
      )}

      <ConfirmarExclusao
        aberto={Boolean(excluindo)}
        titulo="Excluir tipo de uniforme"
        descricao={`Excluir “${excluindo?.tipo ?? ''}”? Não é possível excluir um tipo que já tem estoque ou entregas.`}
        carregando={deleteMutation.isPending}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
      />
    </AppLayout>
  )
}
