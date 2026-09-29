import { useEffect, useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Minus, Plus, Trash2 } from 'lucide-react'
import { Controller, useFieldArray, useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'

import { AlunoPicker } from '@/components/pedidos/aluno-picker'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useAllAlunosQuery } from '@/hooks/use-alunos'
import { useCreatePedidoMutation } from '@/hooks/use-pedidos'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { TAMANHOS } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'
import { pedidoSchema, type PedidoFormValues } from '@/lib/schemas/pedido'
import type { Sexo, Uniforme } from '@/lib/types/uniforme'

const VALORES_INICIAIS: PedidoFormValues = { alunoId: '', itens: [{ uniformeId: '', quantidade: 1 }] }

type Grupo = { chave: string; tipo: string; sexo: Sexo; uniformes: Uniforme[] }

function chaveGrupo(uniforme: Uniforme) {
  return `${uniforme.tipoUniformeNome}|${uniforme.sexo}`
}

function agruparUniformes(uniformes: Uniforme[]): Grupo[] {
  const mapa = new Map<string, Grupo>()

  for (const uniforme of uniformes) {
    const chave = chaveGrupo(uniforme)
    const grupo = mapa.get(chave) ?? { chave, tipo: uniforme.tipoUniformeNome, sexo: uniforme.sexo, uniformes: [] }
    grupo.uniformes.push(uniforme)
    mapa.set(chave, grupo)
  }

  return [...mapa.values()]
    .map((grupo) => ({
      ...grupo,
      uniformes: [...grupo.uniformes].sort((a, b) => TAMANHOS.indexOf(a.tamanho) - TAMANHOS.indexOf(b.tamanho)),
    }))
    .sort((a, b) => a.tipo.localeCompare(b.tipo, 'pt-BR') || a.sexo.localeCompare(b.sexo))
}

type NovaEntregaDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function NovaEntregaDialog({ open, onOpenChange }: NovaEntregaDialogProps) {
  const { data: alunos, isLoading: carregandoAlunos, isError: erroAlunos } = useAllAlunosQuery()
  const { data: uniformes, isLoading: carregandoUniformes, isError: erroUniformes } = useAllUniformesQuery()
  const createMutation = useCreatePedidoMutation()

  /* O uniforme é escolhido em dois toques: primeiro o tipo, depois o tamanho.
     O tipo sozinho não vai no payload, então vive fora do formulário. */
  const [tiposEscolhidos, setTiposEscolhidos] = useState<Record<string, string>>({})

  const {
    control,
    handleSubmit,
    setError,
    setValue,
    reset,
    formState: { errors },
  } = useForm<PedidoFormValues>({
    resolver: zodResolver(pedidoSchema),
    defaultValues: VALORES_INICIAIS,
  })

  const { fields, append, remove } = useFieldArray({ control, name: 'itens' })
  const itens = useWatch({ control, name: 'itens' }) ?? []

  useEffect(() => {
    if (open) {
      reset(VALORES_INICIAIS)
      setTiposEscolhidos({})
    }
  }, [open, reset])

  const grupos = useMemo(() => agruparUniformes(uniformes ?? []), [uniformes])
  const porId = useMemo(() => new Map((uniformes ?? []).map((uniforme) => [uniforme.id, uniforme])), [uniformes])

  const totalPecas = itens.reduce(
    (soma, item) => soma + (item?.uniformeId ? Number(item.quantidade) || 0 : 0),
    0,
  )

  const onSubmit = (values: PedidoFormValues) => {
    let estoqueInsuficiente = false

    values.itens.forEach((item, index) => {
      const uniforme = porId.get(item.uniformeId)
      if (uniforme && item.quantidade > uniforme.quantidade) {
        setError(`itens.${index}.quantidade`, { message: `Só há ${uniforme.quantidade} em estoque.` })
        estoqueInsuficiente = true
      }
    })

    if (estoqueInsuficiente) return

    createMutation.mutate(values, {
      onSuccess: () => {
        const aluno = alunos?.find((candidato) => candidato.id === values.alunoId)
        toast.success(aluno ? `Entrega registrada para ${aluno.nome}.` : 'Entrega registrada.')
        onOpenChange(false)
      },
      onError: (error: Error) => toast.error(error.message),
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[88vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Nova entrega</DialogTitle>
          <DialogDescription>
            Escolha o aluno e as peças que saem do estoque. A quantidade para no que há disponível.
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
          <Controller
            control={control}
            name="alunoId"
            render={({ field }) => (
              <AlunoPicker
                alunos={alunos}
                isLoading={carregandoAlunos}
                isError={erroAlunos}
                value={field.value}
                onChange={field.onChange}
                errorMessage={errors.alunoId?.message}
              />
            )}
          />

          <div className="space-y-3">
            <Label>Itens</Label>

            {erroUniformes && (
              <p className="text-sm text-destructive">
                Não foi possível carregar o estoque. Recarregue a página para tentar de novo.
              </p>
            )}

            {fields.map((field, index) => {
              const uniformeId = itens[index]?.uniformeId ?? ''
              const selecionado = uniformeId ? porId.get(uniformeId) : undefined
              const chave = selecionado ? chaveGrupo(selecionado) : (tiposEscolhidos[field.id] ?? '')
              const grupo = grupos.find((candidato) => candidato.chave === chave)
              const usadosPorOutros = new Set(
                itens.filter((_, outro) => outro !== index).map((item) => item?.uniformeId),
              )

              return (
                <div key={field.id} className="rounded-lg border border-border p-3">
                  <div className="flex items-start gap-2">
                    <div className="flex min-w-0 flex-1 flex-col gap-3">
                      <Select
                        value={chave}
                        onValueChange={(valor) => {
                          setTiposEscolhidos((atual) => ({ ...atual, [field.id]: String(valor) }))
                          setValue(`itens.${index}.uniformeId`, '')
                          setValue(`itens.${index}.quantidade`, 1)
                        }}
                      >
                        <SelectTrigger className="w-full" aria-label={`Uniforme do item ${index + 1}`}>
                          {/* O valor guardado é "tipo|sexo"; quem lê a tela vê o nome. */}
                          <SelectValue>
                            {(valor: string | null) => {
                              const escolhido = grupos.find((candidato) => candidato.chave === valor)
                              if (escolhido) return `${escolhido.tipo} — ${sexoLabels[escolhido.sexo]}`
                              return (
                                <span className="text-muted-foreground">
                                  {carregandoUniformes ? 'Carregando…' : 'Escolha o uniforme'}
                                </span>
                              )
                            }}
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent>
                          {grupos.map((candidato) => (
                            <SelectItem key={candidato.chave} value={candidato.chave}>
                              {candidato.tipo} — {sexoLabels[candidato.sexo]}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>

                      {grupo && (
                        <div
                          role="group"
                          aria-label={`Tamanho do item ${index + 1}`}
                          className="flex flex-wrap gap-1.5"
                        >
                          {grupo.uniformes.map((uniforme) => {
                            const esgotado = uniforme.quantidade <= 0
                            const jaUsado = usadosPorOutros.has(uniforme.id)
                            const ativo = uniformeId === uniforme.id

                            return (
                              <button
                                key={uniforme.id}
                                type="button"
                                aria-pressed={ativo}
                                disabled={esgotado || jaUsado}
                                aria-label={`Tamanho ${uniforme.tamanho}, ${uniforme.quantidade} em estoque${
                                  jaUsado ? ', já adicionado nesta entrega' : ''
                                }`}
                                onClick={() => {
                                  setValue(`itens.${index}.uniformeId`, uniforme.id, { shouldValidate: true })
                                  setValue(
                                    `itens.${index}.quantidade`,
                                    Math.min(Number(itens[index]?.quantidade) || 1, uniforme.quantidade),
                                  )
                                }}
                                className={`flex w-14 flex-col items-center rounded-md border px-1 py-1.5 text-center disabled:cursor-not-allowed disabled:opacity-45 ${
                                  ativo
                                    ? 'border-primary bg-primary text-primary-foreground'
                                    : 'border-border hover:border-primary/60'
                                }`}
                              >
                                <span aria-hidden className="text-sm font-medium">
                                  {uniforme.tamanho}
                                </span>
                                <span
                                  aria-hidden
                                  data-numeric
                                  className={`text-[0.6875rem] ${ativo ? 'opacity-80' : 'text-muted-foreground'}`}
                                >
                                  {esgotado ? 'esgotado' : uniforme.quantidade}
                                </span>
                              </button>
                            )
                          })}
                        </div>
                      )}

                      {errors.itens?.[index]?.uniformeId && (
                        <p className="text-sm text-destructive">{errors.itens[index]?.uniformeId?.message}</p>
                      )}
                    </div>

                    <Controller
                      control={control}
                      name={`itens.${index}.quantidade`}
                      render={({ field: campo }) => {
                        const disponivel = selecionado?.quantidade ?? 1
                        const valor = Number(campo.value) || 1

                        return (
                          <div className="flex items-center gap-1">
                            <Button
                              type="button"
                              variant="outline"
                              size="icon-sm"
                              disabled={valor <= 1}
                              aria-label="Diminuir quantidade"
                              onClick={() => campo.onChange(Math.max(1, valor - 1))}
                            >
                              <Minus className="size-3.5" />
                            </Button>
                            <input
                              type="number"
                              inputMode="numeric"
                              min={1}
                              max={disponivel}
                              aria-label={`Quantidade do item ${index + 1}`}
                              aria-invalid={Boolean(errors.itens?.[index]?.quantidade)}
                              className="h-8 w-12 rounded-md border border-input bg-background text-center text-sm tabular-nums outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 aria-invalid:border-destructive"
                              value={campo.value ?? 1}
                              onChange={(event) => campo.onChange(event.target.valueAsNumber || 1)}
                            />
                            <Button
                              type="button"
                              variant="outline"
                              size="icon-sm"
                              disabled={!selecionado || valor >= disponivel}
                              aria-label="Aumentar quantidade"
                              onClick={() => campo.onChange(Math.min(disponivel, valor + 1))}
                            >
                              <Plus className="size-3.5" />
                            </Button>
                          </div>
                        )
                      }}
                    />

                    <Button
                      type="button"
                      variant="ghost"
                      size="icon-sm"
                      disabled={fields.length === 1}
                      aria-label={`Remover item ${index + 1}`}
                      onClick={() => remove(index)}
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  </div>

                  {errors.itens?.[index]?.quantidade && (
                    <p className="mt-2 text-sm text-destructive">{errors.itens[index]?.quantidade?.message}</p>
                  )}
                </div>
              )
            })}

            <Button type="button" variant="outline" size="sm" onClick={() => append({ uniformeId: '', quantidade: 1 })}>
              <Plus className="size-4" />
              Adicionar item
            </Button>

            {errors.itens?.root?.message && <p className="text-sm text-destructive">{errors.itens.root.message}</p>}
          </div>

          <DialogFooter className="items-center sm:justify-between">
            <p className="text-sm text-muted-foreground">
              {totalPecas === 0
                ? 'Nenhuma peça selecionada'
                : `${totalPecas} ${totalPecas === 1 ? 'peça' : 'peças'} nesta entrega`}
            </p>
            <div className="flex gap-2">
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Registrando…' : 'Registrar entrega'}
              </Button>
            </div>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
