import { useState } from 'react'
import { Plus } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { SelectItens } from '@/components/shared/select-itens'
import { definirCelula, SEXOS, somarCelulas, TAMANHOS, tiposNaGrade, type LinhaEntrada } from '@/lib/entrada'
import { sexoLabels } from '@/lib/labels'
import type { SelectItemData } from '@/lib/select-items'
import { cn } from '@/lib/utils'

type EntradaGradeProps = {
  linhas: LinhaEntrada[]
  itensTipo: SelectItemData[]
  onChange: (linhas: LinhaEntrada[]) => void
}

/** Lançamento rápido: digita as quantidades direto na grade tamanho × modelo; Tab anda célula a célula. */
export function EntradaGrade({ linhas, itensTipo, onChange }: EntradaGradeProps) {
  const [extras, setExtras] = useState<string[]>([])
  const [paraAdicionar, setParaAdicionar] = useState<string | null>(null)
  const preenchidas = linhas.filter((linha) => linha.tipoUniformeId)
  const tipos = tiposNaGrade(preenchidas, extras)
  const somas = somarCelulas(preenchidas)
  const disponiveis = itensTipo.filter((item) => !tipos.includes(item.value))

  return (
    <div className="flex flex-col gap-4">
      {somas.repetidas > 0 && (
        <p className="text-sm text-muted-foreground">
          Algumas peças estão em mais de uma linha. Editar a célula junta essas linhas numa só.
        </p>
      )}

      {tipos.map((tipoId) => {
        const nome = itensTipo.find((item) => item.value === tipoId)?.label ?? 'Tipo'
        const totalTipo = SEXOS.reduce(
          (soma, sexo) => soma + TAMANHOS.reduce((parcial, tamanho) => parcial + (somas.de(tipoId, sexo, tamanho)?.quantidade ?? 0), 0),
          0,
        )

        return (
          <Card key={tipoId}>
            <CardHeader className="flex flex-row items-baseline justify-between gap-3">
              <CardTitle>{nome}</CardTitle>
              <span className="text-sm text-muted-foreground" data-numeric>
                {totalTipo} {totalTipo === 1 ? 'peça' : 'peças'}
              </span>
            </CardHeader>
            <CardContent className="overflow-x-auto px-0">
              <table className="w-full min-w-[30rem] border-collapse text-sm">
                <caption className="sr-only">Quantidades recebidas de {nome} por modelo e tamanho</caption>
                <thead>
                  <tr className="text-xs text-muted-foreground">
                    <th scope="col" className="py-2 pl-6 text-left font-medium">
                      Modelo
                    </th>
                    {TAMANHOS.map((tamanho) => (
                      <th key={tamanho} scope="col" className="px-1 py-2 text-center font-medium">
                        {tamanho}
                      </th>
                    ))}
                    <th scope="col" className="py-2 pr-6 text-right font-medium">
                      Total
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {SEXOS.map((sexo) => {
                    const totalLinha = TAMANHOS.reduce((soma, tamanho) => soma + (somas.de(tipoId, sexo, tamanho)?.quantidade ?? 0), 0)
                    return (
                      <tr key={sexo}>
                        <th scope="row" className="py-1.5 pl-6 text-left font-medium">
                          {sexoLabels[sexo]}
                        </th>
                        {TAMANHOS.map((tamanho) => {
                          const valor = somas.de(tipoId, sexo, tamanho)?.quantidade ?? 0
                          return (
                            <td key={tamanho} className="px-1 py-1.5 text-center">
                              <input
                                inputMode="numeric"
                                aria-label={`${nome} ${sexoLabels[sexo].toLowerCase()} tamanho ${tamanho}`}
                                placeholder="0"
                                value={valor || ''}
                                onChange={(event) => {
                                  const numero = Number.parseInt(event.target.value.replace(/\D/g, ''), 10)
                                  onChange(definirCelula(linhas, tipoId, sexo, tamanho, Number.isNaN(numero) ? 0 : numero))
                                }}
                                className={cn(
                                  'h-10 w-16 rounded-md border border-input bg-background text-center text-[0.9375rem] tabular-nums outline-none placeholder:text-muted-foreground/60 focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50',
                                  valor > 0 && 'border-primary bg-primary/5 font-semibold',
                                )}
                              />
                            </td>
                          )
                        })}
                        <td className="py-1.5 pr-6 text-right font-semibold" data-numeric>
                          {totalLinha}
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </CardContent>
          </Card>
        )
      })}

      <div className="flex flex-wrap items-center gap-2">
        <SelectItens
          className="w-60"
          aria-label="Tipo para adicionar à grade"
          items={disponiveis}
          value={paraAdicionar ?? ''}
          onValueChange={(valor) => setParaAdicionar(valor || null)}
          placeholder={disponiveis.length ? 'Escolha um tipo' : 'Todos os tipos já estão na grade'}
        />
        <Button
          variant="outline"
          disabled={!paraAdicionar}
          onClick={() => {
            if (!paraAdicionar) return
            setExtras((atual) => [...atual, paraAdicionar])
            setParaAdicionar(null)
          }}
        >
          <Plus aria-hidden />
          Adicionar tipo à grade
        </Button>
      </div>
    </div>
  )
}
