import { Copy, Plus, Trash2 } from 'lucide-react'

import { SeletorTamanho } from '@/components/shared/seletor-tamanho'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { SelectItens } from '@/components/shared/select-itens'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { novaLinhaEntrada, somarCelulas, TAMANHOS, type LinhaEntrada } from '@/lib/entrada'
import { sexoLabels } from '@/lib/labels'
import type { SelectItemData } from '@/lib/select-items'
import type { Sexo, Tamanho } from '@/lib/types/uniforme'

type EntradaLinhasProps = {
  linhas: LinhaEntrada[]
  itensTipo: SelectItemData[]
  carregandoTipos: boolean
  erros: Record<string, string>
  onChange: (linhas: LinhaEntrada[]) => void
}

export function EntradaLinhas({ linhas, itensTipo, carregandoTipos, erros, onChange }: EntradaLinhasProps) {
  const somas = somarCelulas(linhas)
  const atualizar = (chave: string, parcial: Partial<LinhaEntrada>) =>
    onChange(linhas.map((linha) => (linha.chave === chave ? { ...linha, ...parcial } : linha)))
  const ultima = linhas[linhas.length - 1]

  return (
    <div className="flex flex-col gap-3">
      <ol className="flex flex-col gap-3" aria-label="Itens recebidos">
        {linhas.map((linha, index) => {
          const numero = index + 1
          const erro = erros[linha.chave]
          const soma = linha.tipoUniformeId ? somas.de(linha.tipoUniformeId, linha.sexo, linha.tamanho) : undefined
          const nomeTipo = itensTipo.find((item) => item.value === linha.tipoUniformeId)?.label
          const idAjuda = `entrada-${linha.chave}-ajuda`

          return (
            <li
              key={linha.chave}
              className="grid gap-3 rounded-xl border border-border bg-card p-4 xl:grid-cols-[minmax(11rem,1.4fr)_auto_auto_6rem_auto] xl:items-center"
            >
              <SelectItens
                aria-label={`Tipo da linha ${numero}`}
                items={itensTipo}
                value={linha.tipoUniformeId}
                onValueChange={(valor) => atualizar(linha.chave, { tipoUniformeId: valor })}
                placeholder={carregandoTipos ? 'Carregando…' : 'Escolha o tipo'}
                invalido={Boolean(erro && !linha.tipoUniformeId)}
                descritoPor={idAjuda}
              />

              <ToggleGroup
                aria-label={`Modelo da linha ${numero}`}
                variant="outline"
                spacing={0}
                value={[linha.sexo]}
                onValueChange={(valores: string[]) => {
                  const proximo = valores[valores.length - 1] as Sexo | undefined
                  if (proximo) atualizar(linha.chave, { sexo: proximo })
                }}
              >
                {(['FEMININO', 'MASCULINO'] as const).map((sexo) => (
                  <ToggleGroupItem
                    key={sexo}
                    value={sexo}
                    className="h-9 px-3 data-pressed:border-primary data-pressed:bg-primary data-pressed:text-primary-foreground"
                  >
                    {sexoLabels[sexo]}
                  </ToggleGroupItem>
                ))}
              </ToggleGroup>

              <SeletorTamanho
                rotulo={`Tamanho da linha ${numero}`}
                valor={linha.tamanho}
                onChange={(tamanho) => atualizar(linha.chave, { tamanho: tamanho as Tamanho })}
                opcoes={TAMANHOS.map((tamanho) => ({ valor: tamanho, tamanho }))}
              />

              <Input
                inputMode="numeric"
                className="h-9 text-right tabular-nums"
                aria-label={`Quantidade da linha ${numero}`}
                aria-invalid={Boolean(erro && !(linha.quantidade >= 1)) || undefined}
                aria-describedby={idAjuda}
                value={linha.quantidade || ''}
                onChange={(event) => {
                  const numeroDigitado = Number.parseInt(event.target.value.replace(/\D/g, ''), 10)
                  atualizar(linha.chave, { quantidade: Number.isNaN(numeroDigitado) ? 0 : numeroDigitado })
                }}
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

              <p
                id={idAjuda}
                role={erro ? 'alert' : undefined}
                className={`xl:col-span-5 ${erro ? 'text-sm text-destructive' : 'text-xs text-signal-low'}`}
              >
                {erro ??
                  (soma && soma.linhas > 1
                    ? `Mesma peça em ${soma.linhas} linhas: serão somadas, ${soma.quantidade} ${nomeTipo ?? ''} ${sexoLabels[linha.sexo].toLowerCase()} ${linha.tamanho}.`
                    : '')}
              </p>
            </li>
          )
        })}
      </ol>

      <div className="flex flex-wrap gap-2">
        <Button variant="outline" onClick={() => onChange([...linhas, novaLinhaEntrada()])}>
          <Plus aria-hidden />
          Adicionar linha
        </Button>
        <Button
          variant="outline"
          disabled={!ultima?.tipoUniformeId}
          onClick={() => {
            if (!ultima) return
            const { chave: _chave, ...copia } = ultima
            onChange([...linhas, novaLinhaEntrada(copia)])
          }}
        >
          <Copy aria-hidden />
          Duplicar última
        </Button>
      </div>
    </div>
  )
}
