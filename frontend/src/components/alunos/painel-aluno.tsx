import { Pencil, Plus, X } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ButtonLink } from '@/components/shared/button-link'

import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { usePedidosDoAlunoQuery } from '@/hooks/use-pedidos'
import { formatarData, formatarTelefone } from '@/lib/format'
import type { Aluno } from '@/lib/types/aluno'

function iniciais(nome: string) {
  const partes = nome.trim().split(/\s+/)
  return `${partes[0]?.[0] ?? ''}${partes.length > 1 ? partes[partes.length - 1][0] : ''}`.toUpperCase()
}

/** Ficha do aluno ao lado da lista: dados, histórico e o atalho para nova entrega. */
export function PainelAluno({ aluno, onFechar }: { aluno: Aluno; onFechar: () => void }) {
  const { data, isLoading, isError } = usePedidosDoAlunoQuery(aluno.id)
  const pedidos = data?.content ?? []
  const pecas = pedidos.reduce((soma, pedido) => soma + pedido.itens.reduce((parcial, item) => parcial + item.quantidade, 0), 0)

  return (
    <Card role="complementary" aria-labelledby="painel-aluno-titulo" className="lg:sticky lg:top-20">
      <CardContent className="flex flex-col gap-5">
        <div className="flex items-start gap-3">
          <span aria-hidden className="flex size-12 shrink-0 items-center justify-center rounded-full bg-primary font-semibold text-primary-foreground">
            {iniciais(aluno.nome)}
          </span>
          <div className="min-w-0 flex-1">
            <h2 id="painel-aluno-titulo" className="text-lg leading-tight font-semibold">
              {aluno.nome}
            </h2>
            <p className="text-sm text-muted-foreground">{aluno.turmaNome}</p>
          </div>
          <Button variant="ghost" size="icon" aria-label="Fechar ficha do aluno" onClick={onFechar}>
            <X aria-hidden />
          </Button>
        </div>

        <div className="flex flex-wrap gap-2">
          <ButtonLink size="lg" className="flex-1" to={`/pedidos/nova?aluno=${aluno.id}`}>
            <Plus aria-hidden />
            Nova entrega
          </ButtonLink>
          <ButtonLink variant="outline" size="lg" to={`/alunos/${aluno.id}/editar`}>
            <Pencil aria-hidden />
            Editar
          </ButtonLink>
        </div>

        {(aluno.nomeResponsavel || aluno.telefoneResponsavel) && (
          <dl className="grid gap-1 text-sm">
            <dt className="text-xs text-muted-foreground">Responsável</dt>
            <dd>
              {aluno.nomeResponsavel ?? '—'}
              {aluno.telefoneResponsavel && (
                <>
                  {' · '}
                  <a href={`tel:+55${aluno.telefoneResponsavel}`} className="text-primary underline-offset-4 hover:underline">
                    {formatarTelefone(aluno.telefoneResponsavel)}
                  </a>
                </>
              )}
            </dd>
          </dl>
        )}

        <dl className="grid grid-cols-2 gap-3">
          <div className="rounded-lg bg-muted/60 p-3">
            <dt className="text-xs text-muted-foreground">Peças recebidas</dt>
            <dd className="text-2xl font-semibold" data-numeric>
              {isLoading ? '…' : pecas}
            </dd>
          </div>
          <div className="rounded-lg bg-muted/60 p-3">
            <dt className="text-xs text-muted-foreground">Entregas</dt>
            <dd className="text-2xl font-semibold" data-numeric>
              {isLoading ? '…' : (data?.totalElements ?? 0)}
            </dd>
          </div>
        </dl>

        <section aria-labelledby="historico-titulo" className="flex flex-col gap-2">
          <h3 id="historico-titulo" className="text-sm font-semibold">
            Histórico
          </h3>
          {isLoading && <Skeleton className="h-16 w-full" />}
          {isError && <p className="text-sm text-destructive">Não foi possível carregar o histórico.</p>}
          {!isLoading && !isError && pedidos.length === 0 && (
            <p className="rounded-lg border border-dashed border-border p-4 text-center text-sm text-muted-foreground">
              Este aluno ainda não recebeu uniforme.
            </p>
          )}
          <ol className="flex flex-col">
            {pedidos.map((pedido) => (
              <li key={pedido.id} className="border-t border-border py-2.5 first:border-0">
                <Link to={`/pedidos?ver=${pedido.id}`} className="text-sm font-medium text-primary underline-offset-4 hover:underline">
                  {formatarData(pedido.dataEfetivada)}
                </Link>
                <p className="text-xs text-muted-foreground">
                  {pedido.itens.map((item) => `${item.uniformeTipoUniformeNome} ${item.uniformeTamanho} ×${item.quantidade}`).join(' · ')}
                </p>
              </li>
            ))}
          </ol>
        </section>
      </CardContent>
    </Card>
  )
}
