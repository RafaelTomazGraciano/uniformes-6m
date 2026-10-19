import { useMemo } from 'react'
import { Boxes, CircleCheck, Grid3x3, List, PackagePlus } from 'lucide-react'
import { ButtonLink } from '@/components/shared/button-link'
import { Link, useSearchParams } from 'react-router-dom'

import { AppLayout } from '@/components/layout/app-layout'
import { EstadoCarregando, EstadoErro, EstadoVazio } from '@/components/shared/estados-lista'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { agruparPorTipo, nivelDoUniforme, nivelEstoque, TAMANHOS, type NivelEstoque } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'
import type { Uniforme } from '@/lib/types/uniforme'
import { cn } from '@/lib/utils'

type Situacao = 'todos' | 'alerta' | 'esgotado'
type Visao = 'grade' | 'lista'

const rotuloNivel: Record<NivelEstoque, { texto: string; variant: 'out' | 'low' | 'ok' }> = {
  esgotado: { texto: 'Esgotado', variant: 'out' },
  baixo: { texto: 'Baixo', variant: 'low' },
  ok: { texto: 'Em dia', variant: 'ok' },
}

const ORDEM: Record<NivelEstoque, number > = { esgotado: 0, baixo: 1, ok: 2 }

function passa(nivel: NivelEstoque, situacao: Situacao) {
  if (situacao === 'todos') return true
  if (situacao === 'esgotado') return nivel === 'esgotado'
  return nivel !== 'ok'
}

function linkRepor(uniforme: Pick<Uniforme, 'tipoUniformeId' | 'sexo' | 'tamanho'>) {
  const params = new URLSearchParams({ tipo: uniforme.tipoUniformeId, sexo: uniforme.sexo, tamanho: uniforme.tamanho })
  return `/entradas/nova?${params}`
}

export function EstoquePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const situacao = (['alerta', 'esgotado'].includes(searchParams.get('situacao') ?? '') ? searchParams.get('situacao') : 'todos') as Situacao
  const visao: Visao = searchParams.get('visao') === 'lista' ? 'lista' : 'grade'
  const { data: uniformes, isLoading, isError, refetch } = useAllUniformesQuery()

  const contagem = useMemo(() => {
    const niveis = (uniformes ?? []).map(nivelDoUniforme)
    return {
      todos: niveis.length,
      alerta: niveis.filter((nivel) => nivel !== 'ok').length,
      esgotado: niveis.filter((nivel) => nivel === 'esgotado').length,
    }
  }, [uniformes])

  const pecas = (uniformes ?? []).reduce((soma, uniforme) => soma + uniforme.quantidade, 0)
  const lista = useMemo(
    () =>
      (uniformes ?? [])
        .map((uniforme) => ({ uniforme, nivel: nivelDoUniforme(uniforme) }))
        .filter(({ nivel }) => passa(nivel, situacao))
        .sort(
          (a, b) =>
            ORDEM[a.nivel] - ORDEM[b.nivel] ||
            a.uniforme.quantidade - b.uniforme.quantidade ||
            a.uniforme.tipoUniformeNome.localeCompare(b.uniforme.tipoUniformeNome, 'pt-BR'),
        ),
    [uniformes, situacao],
  )
  const grupos = useMemo(
    () => agruparPorTipo((uniformes ?? []).filter((uniforme) => passa(nivelDoUniforme(uniforme), situacao))),
    [uniformes, situacao],
  )

  function definir(chave: string, valor: string, padrao: string) {
    setSearchParams(
      (atual) => {
        const params = new URLSearchParams(atual)
        if (valor === padrao) params.delete(chave)
        else params.set(chave, valor)
        return params
      },
      { replace: true },
    )
  }

  return (
    <AppLayout
      title="Estoque"
      description="Cada tipo avisa no próprio limite, definido em Tipos de uniforme."
      count={uniformes ? pecas : undefined}
      headerActions={
        <ToggleGroup
          aria-label="Visualização"
          variant="outline"
          spacing={0}
          value={[visao]}
          onValueChange={(valores: string[]) => {
            const proxima = valores[valores.length - 1]
            if (proxima) definir('visao', proxima, 'grade')
          }}
        >
          <ToggleGroupItem value="grade" className="h-9 px-3 data-pressed:bg-muted data-pressed:font-semibold">
            <Grid3x3 aria-hidden />
            Grade
          </ToggleGroupItem>
          <ToggleGroupItem value="lista" className="h-9 px-3 data-pressed:bg-muted data-pressed:font-semibold">
            <List aria-hidden />
            Lista
          </ToggleGroupItem>
        </ToggleGroup>
      }
    >
      {isLoading && <EstadoCarregando rotulo="Carregando estoque" colunas={6} />}
      {isError && <EstadoErro titulo="Não foi possível carregar o estoque" onRetry={() => refetch()} />}

      {uniformes && uniformes.length === 0 && (
        <EstadoVazio
          icone={<Boxes />}
          titulo="O estoque ainda está vazio"
          descricao="As peças aparecem aqui quando um lote do fornecedor for registrado em Entradas."
          acao={
            <ButtonLink size="lg" to="/entradas/nova">
              <PackagePlus aria-hidden />
              Registrar primeira entrada
            </ButtonLink>
          }
        />
      )}

      {uniformes && uniformes.length > 0 && (
        <div className="flex flex-col gap-5">
          <ToggleGroup
            aria-label="Filtrar por situação"
            variant="outline"
            spacing={2}
            className="flex-wrap"
            value={[situacao]}
            onValueChange={(valores: string[]) => {
              const proxima = valores[valores.length - 1]
              if (proxima) definir('situacao', proxima, 'todos')
            }}
          >
            {(
              [
                ['todos', 'Todos'],
                ['alerta', 'Repor (baixo ou esgotado)'],
                ['esgotado', 'Esgotado'],
              ] as const
            ).map(([valor, rotulo]) => (
              <ToggleGroupItem
                key={valor}
                value={valor}
                className="h-9 rounded-full px-4 data-pressed:border-foreground data-pressed:bg-foreground data-pressed:text-background"
              >
                {rotulo}
                <span className="opacity-70" data-numeric>
                  · {contagem[valor]}
                </span>
              </ToggleGroupItem>
            ))}
          </ToggleGroup>

          {lista.length === 0 && (
            <EstadoVazio
              tom="positivo"
              icone={<CircleCheck />}
              titulo="Tudo em dia"
              descricao="Nenhum tamanho está nesta situação agora."
              acao={
                <Button variant="outline" onClick={() => definir('situacao', 'todos', 'todos')}>
                  Ver todo o estoque
                </Button>
              }
            />
          )}

          {lista.length > 0 && visao === 'grade' && (
            <div className="grid gap-4 [grid-template-columns:repeat(auto-fill,minmax(min(100%,26rem),1fr))]">
              {grupos.map((grupo) => (
                <Card key={grupo.tipo}>
                  <CardHeader className="flex flex-row flex-wrap items-baseline justify-between gap-2">
                    <CardTitle>{grupo.tipo}</CardTitle>
                    <span className="text-xs text-muted-foreground">
                      avisa com {grupo.limite} ou menos ·{' '}
                      <Link to={`/tipos-uniforme/${grupo.tipoUniformeId}/editar`} className="text-primary underline underline-offset-4">
                        alterar
                      </Link>
                    </span>
                  </CardHeader>
                  <CardContent className="overflow-x-auto px-0">
                    <table className="w-full min-w-[22rem] border-collapse text-sm">
                      <caption className="sr-only">{grupo.tipo}: quantidade por modelo e tamanho</caption>
                      <thead>
                        <tr className="text-xs text-muted-foreground">
                          <th scope="col" className="py-1.5 pl-6 text-left font-medium">
                            Modelo
                          </th>
                          {TAMANHOS.map((tamanho) => (
                            <th key={tamanho} scope="col" className="px-1 py-1.5 text-center font-medium">
                              {tamanho}
                            </th>
                          ))}
                          <th scope="col" className="py-1.5 pr-6 text-right font-medium">
                            Total
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {grupo.linhas.map((linha) => (
                          <tr key={linha.sexo}>
                            <th scope="row" className="py-1 pl-6 text-left font-medium">
                              {sexoLabels[linha.sexo]}
                            </th>
                            {TAMANHOS.map((tamanho) => {
                              const quantidade = linha.porTamanho[tamanho]
                              if (quantidade === undefined) {
                                return (
                                  <td key={tamanho} className="px-1 py-1 text-center text-muted-foreground/50">
                                    <span aria-hidden>–</span>
                                    <span className="sr-only">não cadastrado</span>
                                  </td>
                                )
                              }
                              const nivel = nivelEstoque(quantidade, grupo.limite)
                              return (
                                <td key={tamanho} className="px-1 py-1 text-center">
                                  <span
                                    className={cn(
                                      'mx-auto flex h-9 w-12 items-center justify-center rounded-md bg-muted/50 tabular-nums',
                                      nivel === 'baixo' && 'bg-signal-low-surface font-semibold text-signal-low ring-1 ring-signal-low/40',
                                      nivel === 'esgotado' && 'bg-signal-out-surface font-semibold text-signal-out ring-1 ring-signal-out/40',
                                    )}
                                  >
                                    {quantidade}
                                    {nivel !== 'ok' && <span className="sr-only">, {rotuloNivel[nivel].texto.toLowerCase()}</span>}
                                  </span>
                                </td>
                              )
                            })}
                            <td className="py-1 pr-6 text-right font-semibold" data-numeric>
                              {linha.total}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {lista.length > 0 && visao === 'lista' && (
            <div className="overflow-x-auto rounded-xl border border-border bg-card">
              <Table className="min-w-[40rem]">
                <TableCaption className="sr-only">Estoque por item, mais críticos primeiro</TableCaption>
                <TableHeader>
                  <TableRow>
                    <TableHead scope="col">Peça</TableHead>
                    <TableHead scope="col">Modelo</TableHead>
                    <TableHead scope="col">Tamanho</TableHead>
                    <TableHead scope="col" className="text-right">
                      Quantidade
                    </TableHead>
                    <TableHead scope="col" className="text-right">
                      Limite
                    </TableHead>
                    <TableHead scope="col">Situação</TableHead>
                    <TableHead scope="col">
                      <span className="sr-only">Ações</span>
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {lista.map(({ uniforme, nivel }) => (
                    <TableRow key={uniforme.id}>
                      <TableCell className="font-medium">{uniforme.tipoUniformeNome}</TableCell>
                      <TableCell>{sexoLabels[uniforme.sexo]}</TableCell>
                      <TableCell>{uniforme.tamanho}</TableCell>
                      <TableCell className="text-right font-semibold" data-numeric>
                        {uniforme.quantidade}
                      </TableCell>
                      <TableCell className="text-right text-muted-foreground" data-numeric>
                        ≤ {uniforme.estoqueBaixo}
                      </TableCell>
                      <TableCell>
                        <Badge variant={rotuloNivel[nivel].variant}>{rotuloNivel[nivel].texto}</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Link
                          to={linkRepor(uniforme)}
                          className="text-sm text-primary underline-offset-4 hover:underline"
                          aria-label={`Repor ${uniforme.tipoUniformeNome} ${sexoLabels[uniforme.sexo].toLowerCase()} ${uniforme.tamanho}`}
                        >
                          Repor
                        </Link>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      )}
    </AppLayout>
  )
}
