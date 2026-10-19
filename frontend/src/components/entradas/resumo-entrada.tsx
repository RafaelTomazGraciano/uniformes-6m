import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { SEXOS, somarCelulas, TAMANHOS, type LinhaEntrada } from '@/lib/entrada'
import { sexoLabels } from '@/lib/labels'
import type { SelectItemData } from '@/lib/select-items'

export function ResumoEntrada({ linhas, itensTipo }: { linhas: LinhaEntrada[]; itensTipo: SelectItemData[] }) {
  const preenchidas = linhas.filter((linha) => linha.tipoUniformeId && linha.quantidade > 0)
  const somas = somarCelulas(preenchidas)
  const total = preenchidas.reduce((soma, linha) => soma + linha.quantidade, 0)
  const combinacoes = [...new Set(preenchidas.map((linha) => `${linha.tipoUniformeId}|${linha.sexo}`))].map((chave) => {
    const [tipoId, sexo] = chave.split('|') as [string, (typeof SEXOS)[number]]
    return { chave, tipoId, sexo, nome: itensTipo.find((item) => item.value === tipoId)?.label ?? '—' }
  })

  return (
    <Card className="lg:sticky lg:top-20">
      <CardHeader className="border-b border-border">
        <CardTitle>Resumo da nota</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-3">
        {combinacoes.length === 0 ? (
          <p className="py-4 text-center text-sm text-muted-foreground">Os itens aparecem aqui somados por tamanho.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-[0.8125rem] tabular-nums">
              <caption className="pb-2 text-left text-xs text-muted-foreground">Peças somadas por tamanho</caption>
              <thead>
                <tr className="text-xs text-muted-foreground">
                  <th scope="col" className="py-1 text-left font-medium">
                    Peça
                  </th>
                  {TAMANHOS.map((tamanho) => (
                    <th key={tamanho} scope="col" className="px-1 py-1 text-center font-medium">
                      {tamanho}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {combinacoes.map((combinacao) => (
                  <tr key={combinacao.chave} className="border-t border-border">
                    <th scope="row" className="py-1.5 pr-2 text-left font-medium">
                      {combinacao.nome} · {sexoLabels[combinacao.sexo].charAt(0)}
                    </th>
                    {TAMANHOS.map((tamanho) => {
                      const valor = somas.de(combinacao.tipoId, combinacao.sexo, tamanho)?.quantidade
                      return (
                        <td key={tamanho} className="px-1 py-1.5 text-center">
                          {valor ? <strong>{valor}</strong> : <span className="text-muted-foreground">—</span>}
                        </td>
                      )
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <p aria-live="polite" className="flex justify-between border-t border-border pt-3 text-base font-semibold" data-numeric>
          <span>Total</span>
          <span>
            {total} {total === 1 ? 'peça' : 'peças'}
          </span>
        </p>
        <p className="text-xs text-muted-foreground">Confira com a nota antes de salvar. O estoque sobe assim que a entrada é registrada.</p>
      </CardContent>
    </Card>
  )
}
