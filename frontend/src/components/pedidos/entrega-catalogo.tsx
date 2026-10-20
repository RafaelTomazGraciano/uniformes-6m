import { useMemo, useState } from 'react'
import { Search, Shirt } from 'lucide-react'

import { QuantidadeStepper } from '@/components/shared/quantidade-stepper'
import { SeletorTamanho } from '@/components/shared/seletor-tamanho'
import { EstadoVazio } from '@/components/shared/estados-lista'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { normalizar } from '@/lib/alunos'
import { somarPorUniforme, type GrupoPeca, type LinhaEntrega } from '@/lib/entrega'
import { sexoLabels } from '@/lib/labels'
import type { Sexo } from '@/lib/types/uniforme'

type Filtro = 'TODOS' | Sexo

type EntregaCatalogoProps = {
  grupos: GrupoPeca[]
  linhas: LinhaEntrega[]
  onAdicionar: (linha: Pick<LinhaEntrega, 'grupo' | 'uniformeId' | 'quantidade'>, descricao: string) => void
}

export function EntregaCatalogo({ grupos, linhas, onAdicionar }: EntregaCatalogoProps) {
  const [busca, setBusca] = useState('')
  const [filtro, setFiltro] = useState<Filtro>('TODOS')
  const [escolhas, setEscolhas] = useState<Record<string, { uniformeId: string; quantidade: number }>>({})
  const naSacola = somarPorUniforme(linhas)

  const visiveis = useMemo(() => {
    const termo = normalizar(busca.trim())
    return grupos.filter(
      (grupo) => (filtro === 'TODOS' || grupo.sexo === filtro) && (!termo || normalizar(grupo.tipo).includes(termo)),
    )
  }, [grupos, busca, filtro])

  return (
    <section aria-label="Catálogo de peças" className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <InputGroup className="h-9 max-w-sm min-w-56 flex-1">
          <InputGroupAddon>
            <Search aria-hidden />
          </InputGroupAddon>
          <InputGroupInput
            type="search"
            aria-label="Buscar peça"
            placeholder="Buscar peça"
            value={busca}
            onChange={(event) => setBusca(event.target.value)}
          />
        </InputGroup>
        <ToggleGroup
          aria-label="Filtrar por modelo"
          variant="outline"
          spacing={1}
          value={[filtro]}
          onValueChange={(valores: string[]) => {
            const proximo = valores[valores.length - 1] as Filtro | undefined
            if (proximo) setFiltro(proximo)
          }}
        >
          {(['TODOS', 'FEMININO', 'MASCULINO'] as const).map((opcao) => (
            <ToggleGroupItem
              key={opcao}
              value={opcao}
              className="h-9 rounded-full px-4 data-pressed:border-foreground data-pressed:bg-foreground data-pressed:text-background"
            >
              {opcao === 'TODOS' ? 'Todos' : sexoLabels[opcao]}
            </ToggleGroupItem>
          ))}
        </ToggleGroup>
      </div>

      {visiveis.length === 0 ? (
        <EstadoVazio
          icone={<Search />}
          titulo="Nenhuma peça encontrada"
          descricao={grupos.length === 0 ? 'Ainda não há peças em estoque.' : 'Tente outro nome ou limpe o filtro.'}
          acao={
            grupos.length > 0 && (
              <Button
                variant="outline"
                onClick={() => {
                  setBusca('')
                  setFiltro('TODOS')
                }}
              >
                Limpar busca
              </Button>
            )
          }
        />
      ) : (
        <ul className="grid gap-4 [grid-template-columns:repeat(auto-fill,minmax(16rem,1fr))]">
          {visiveis.map((grupo) => {
            const escolha = escolhas[grupo.chave] ?? { uniformeId: '', quantidade: 1 }
            const uniforme = grupo.uniformes.find((candidato) => candidato.id === escolha.uniformeId)
            const livre = uniforme ? uniforme.quantidade - (naSacola.get(uniforme.id)?.quantidade ?? 0) : 0
            const bloqueado = !uniforme || livre < escolha.quantidade
            const titulo = `${grupo.tipo} ${sexoLabels[grupo.sexo]}`
            const definir = (parcial: Partial<typeof escolha>) =>
              setEscolhas((atual) => ({ ...atual, [grupo.chave]: { ...escolha, ...parcial } }))

            return (
              <li key={grupo.chave}>
                <Card className="h-full">
                  <CardContent className="flex h-full flex-col gap-4">
                    <div className="flex h-24 items-center justify-center rounded-lg bg-muted/70" aria-hidden>
                      <Shirt className="size-10 text-primary" strokeWidth={1.4} />
                    </div>
                    <div>
                      <h3 className="text-[0.9375rem] font-semibold">{grupo.tipo}</h3>
                      <p className="text-sm text-muted-foreground" data-numeric>
                        {sexoLabels[grupo.sexo]} · {grupo.total} em estoque
                      </p>
                    </div>
                    <fieldset className="flex flex-col gap-1.5">
                      <legend className="mb-1.5 text-xs font-semibold text-muted-foreground">Tamanho</legend>
                      <SeletorTamanho
                        rotulo={`Tamanho de ${titulo}`}
                        valor={escolha.uniformeId}
                        onChange={(uniformeId) => definir({ uniformeId, quantidade: 1 })}
                        opcoes={grupo.uniformes.map((candidato) => ({
                          valor: candidato.id,
                          tamanho: candidato.tamanho,
                          estoque: candidato.quantidade,
                        }))}
                      />
                    </fieldset>
                    <div className="mt-auto flex items-center gap-2">
                      <QuantidadeStepper
                        rotulo={`Quantidade de ${titulo}`}
                        valor={escolha.quantidade}
                        maximo={uniforme ? Math.max(1, livre) : undefined}
                        disabled={!uniforme}
                        onChange={(quantidade) => definir({ quantidade })}
                      />
                      <Button
                        size="lg"
                        className="flex-1"
                        disabled={bloqueado}
                        onClick={() => {
                          if (!uniforme || bloqueado) return
                          onAdicionar(
                            { grupo: grupo.chave, uniformeId: uniforme.id, quantidade: escolha.quantidade },
                            `${grupo.tipo} ${uniforme.tamanho} × ${escolha.quantidade}`,
                          )
                          definir({ quantidade: 1 })
                        }}
                      >
                        {!uniforme ? 'Escolha o tamanho' : livre < escolha.quantidade ? 'Sem estoque livre' : 'Adicionar'}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
