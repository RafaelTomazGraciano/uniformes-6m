import { ShoppingBag, Trash2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { somarPorUniforme, type LinhaEntrega } from '@/lib/entrega'
import { sexoLabels } from '@/lib/labels'
import type { Uniforme } from '@/lib/types/uniforme'
import { cn } from '@/lib/utils'

type SacolaEntregaProps = {
  linhas: LinhaEntrega[]
  porId: Map<string, Uniforme>
  /** No catálogo cada linha aparece com botão de remover; na lista, só o resumo somado. */
  mostrarLinhas: boolean
  onRemover: (chave: string) => void
}

export function SacolaEntrega({ linhas, porId, mostrarLinhas, onRemover }: SacolaEntregaProps) {
  const preenchidas = linhas.filter((linha) => linha.uniformeId && porId.has(linha.uniformeId))
  const somados = [...somarPorUniforme(preenchidas).entries()]
  const total = preenchidas.reduce((soma, linha) => soma + linha.quantidade, 0)

  return (
    <Card className="lg:sticky lg:top-20">
      <CardHeader className="border-b border-border">
        <CardTitle className="flex items-center justify-between gap-2">
          <span>{mostrarLinhas ? 'Sacola da entrega' : 'Resumo'}</span>
          <span className="text-sm font-normal text-muted-foreground" data-numeric>
            {preenchidas.length} {preenchidas.length === 1 ? 'linha' : 'linhas'}
          </span>
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {preenchidas.length === 0 && (
          <div className="flex flex-col items-center gap-2 rounded-lg border border-dashed border-border px-4 py-6 text-center text-sm text-muted-foreground">
            <ShoppingBag aria-hidden className="size-6" />
            Nenhuma peça ainda. Escolha o tamanho e adicione.
          </div>
        )}

        {mostrarLinhas && preenchidas.length > 0 && (
          <ul className="flex flex-col divide-y divide-border">
            {preenchidas.map((linha, index) => {
              const uniforme = porId.get(linha.uniformeId)!
              return (
                <li key={linha.chave} className="flex items-center gap-2 py-2.5">
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">{uniforme.tipoUniformeNome}</p>
                    <p className="text-xs text-muted-foreground">
                      {sexoLabels[uniforme.sexo]} · {uniforme.tamanho}
                    </p>
                  </div>
                  <span className="text-sm font-semibold" data-numeric>
                    × {linha.quantidade}
                  </span>
                  <Button
                    variant="ghost"
                    size="icon"
                    aria-label={`Remover linha ${index + 1}: ${uniforme.tipoUniformeNome} ${uniforme.tamanho}`}
                    onClick={() => onRemover(linha.chave)}
                  >
                    <Trash2 aria-hidden />
                  </Button>
                </li>
              )
            })}
          </ul>
        )}

        {somados.length > 0 && (
          <div className={cn('flex flex-col gap-1.5', mostrarLinhas && 'rounded-lg bg-muted/60 p-3')}>
            {mostrarLinhas && <p className="text-xs font-semibold tracking-wide text-muted-foreground uppercase">Somado por peça</p>}
            <ul className="flex flex-col gap-1.5 text-sm">
              {somados.map(([uniformeId, soma]) => {
                const uniforme = porId.get(uniformeId)!
                const excede = soma.quantidade > uniforme.quantidade
                return (
                  <li key={uniformeId} className={cn('flex justify-between gap-3', excede && 'text-destructive')}>
                    <span className="min-w-0">
                      {uniforme.tipoUniformeNome} · {sexoLabels[uniforme.sexo].charAt(0)} · {uniforme.tamanho}
                      {soma.linhas > 1 && <span className="text-muted-foreground"> ({soma.linhas} linhas)</span>}
                    </span>
                    <span className="shrink-0 font-medium" data-numeric>
                      {soma.quantidade}
                      <span className="text-muted-foreground"> / {uniforme.quantidade}</span>
                    </span>
                  </li>
                )
              })}
            </ul>
          </div>
        )}

        <p
          aria-live="polite"
          className="flex justify-between border-t border-border pt-3 text-base font-semibold"
          data-numeric
        >
          <span>Total</span>
          <span>
            {total} {total === 1 ? 'peça' : 'peças'}
          </span>
        </p>
      </CardContent>
    </Card>
  )
}
