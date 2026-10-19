import { Plus, Trash2 } from 'lucide-react'

import { QuantidadeStepper } from '@/components/shared/quantidade-stepper'
import { SeletorTamanho } from '@/components/shared/seletor-tamanho'
import { Button } from '@/components/ui/button'
import { SelectItens } from '@/components/shared/select-itens'
import { novaLinha, somarPorUniforme, type GrupoPeca, type LinhaEntrega } from '@/lib/entrega'
import { sexoLabels } from '@/lib/labels'
import { toSelectItems } from '@/lib/select-items'
import type { Uniforme } from '@/lib/types/uniforme'

type EntregaListaProps = {
  linhas: LinhaEntrega[]
  grupos: GrupoPeca[]
  porId: Map<string, Uniforme>
  carregando: boolean
  erros: Record<string, string>
  onChange: (linhas: LinhaEntrega[]) => void
}

export function EntregaLista({ linhas, grupos, porId, carregando, erros, onChange }: EntregaListaProps) {
  const itensGrupo = toSelectItems(grupos, (grupo) => grupo.chave, (grupo) => `${grupo.tipo} — ${sexoLabels[grupo.sexo]}`)
  const somas = somarPorUniforme(linhas)

  const atualizar = (chave: string, parcial: Partial<LinhaEntrega>) =>
    onChange(linhas.map((linha) => (linha.chave === chave ? { ...linha, ...parcial } : linha)))

  return (
    <div className="flex flex-col gap-3">
      <ol className="flex flex-col gap-3" aria-label="Linhas da entrega">
        {linhas.map((linha, index) => {
          const numero = index + 1
          const grupo = grupos.find((candidato) => candidato.chave === linha.grupo)
          const uniforme = linha.uniformeId ? porId.get(linha.uniformeId) : undefined
          const soma = linha.uniformeId ? somas.get(linha.uniformeId) : undefined
          const erro = erros[linha.chave]
          const idAjuda = `linha-${linha.chave}-ajuda`

          return (
            <li
              key={linha.chave}
              className="grid gap-3 rounded-xl border border-border bg-card p-4 md:grid-cols-[minmax(12rem,1.2fr)_minmax(14rem,1.5fr)_auto_auto] md:items-start"
            >
              <SelectItens
                aria-label={`Peça da linha ${numero}`}
                items={itensGrupo}
                value={linha.grupo}
                onValueChange={(valor) => atualizar(linha.chave, { grupo: valor, uniformeId: '', quantidade: 1 })}
                placeholder={carregando ? 'Carregando…' : 'Escolha a peça'}
              />

              <div className="flex flex-col gap-1.5">
                {grupo ? (
                  <SeletorTamanho
                    rotulo={`Tamanho da linha ${numero}`}
                    invalido={Boolean(erro)}
                    valor={linha.uniformeId}
                    onChange={(uniformeId) => atualizar(linha.chave, { uniformeId, quantidade: 1 })}
                    opcoes={grupo.uniformes.map((candidato) => ({
                      valor: candidato.id,
                      tamanho: candidato.tamanho,
                      estoque: candidato.quantidade,
                    }))}
                  />
                ) : (
                  <p className="flex h-9 items-center text-sm text-muted-foreground">Escolha a peça para ver os tamanhos.</p>
                )}
                <p id={idAjuda} className={erro ? 'text-sm text-destructive' : 'text-xs text-muted-foreground'} role={erro ? 'alert' : undefined}>
                  {erro ??
                    (uniforme && soma && soma.linhas > 1
                      ? `Também em outra linha: no total ${soma.quantidade} de ${uniforme.quantidade} em estoque.`
                      : uniforme
                        ? `${uniforme.quantidade} em estoque`
                        : ' ')}
                </p>
              </div>

              <QuantidadeStepper
                rotulo={`Quantidade da linha ${numero}`}
                valor={linha.quantidade}
                maximo={uniforme?.quantidade}
                disabled={!uniforme}
                invalido={Boolean(erro)}
                descritoPor={idAjuda}
                onChange={(quantidade) => atualizar(linha.chave, { quantidade })}
              />

              <Button
                variant="ghost"
                size="icon"
                className="justify-self-end"
                disabled={linhas.length === 1}
                aria-label={`Remover linha ${numero}`}
                onClick={() => onChange(linhas.filter((outra) => outra.chave !== linha.chave))}
              >
                <Trash2 aria-hidden />
              </Button>
            </li>
          )
        })}
      </ol>

      <Button variant="outline" className="self-start" onClick={() => onChange([...linhas, novaLinha()])}>
        <Plus aria-hidden />
        Adicionar linha
      </Button>
    </div>
  )
}
