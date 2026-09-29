import { useEffect } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Plus, Trash2 } from 'lucide-react'
import { Controller, useFieldArray, useForm } from 'react-hook-form'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useCreateLoteMutation, useUpdateLoteMutation } from '@/hooks/use-lotes'
import { useTiposUniformeQuery } from '@/hooks/use-tipos-uniforme'
import { TAMANHOS } from '@/lib/estoque'
import { dataParaInput, hojeParaInput, inputParaLocalDateTime } from '@/lib/format'
import { sexoLabels } from '@/lib/labels'
import { loteSchema, type ItemEntradaFormValues, type LoteFormValues } from '@/lib/schemas/lote'
import type { Lote } from '@/lib/types/lote'
import type { Sexo, Tamanho } from '@/lib/types/uniforme'

const ITEM_VAZIO: ItemEntradaFormValues = {
  tipoUniformeId: '',
  tamanho: 'M',
  sexo: 'MASCULINO',
  quantidade: 1,
}

type NovaEntradaDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  lote?: Lote
}

export function NovaEntradaDialog({ open, onOpenChange, lote }: NovaEntradaDialogProps) {
  const editando = Boolean(lote)
  const { data: tipos, isLoading: carregandoTipos, isError: erroTipos } = useTiposUniformeQuery()
  const createMutation = useCreateLoteMutation()
  const updateMutation = useUpdateLoteMutation()

  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<LoteFormValues>({
    resolver: zodResolver(loteSchema),
    defaultValues: { chaveAcesso: '', fornecedor: '', dataEntrega: hojeParaInput(), itens: [ITEM_VAZIO] },
  })

  const { fields, append, remove } = useFieldArray({ control, name: 'itens' })

  useEffect(() => {
    if (!open) return

    reset({
      chaveAcesso: lote?.notaFiscalChaveAcesso ?? '',
      fornecedor: lote?.fornecedor ?? '',
      dataEntrega: dataParaInput(lote?.dataEntrega) || hojeParaInput(),
      itens: lote?.itens.length
        ? lote.itens.map((item) => ({
            tipoUniformeId: item.tipoUniformeId,
            tamanho: item.tamanho,
            sexo: item.sexo,
            quantidade: item.quantidade,
          }))
        : [ITEM_VAZIO],
    })
  }, [open, lote, reset])

  const salvando = createMutation.isPending || updateMutation.isPending

  function onSubmit(values: LoteFormValues) {
    const payload = { ...values, dataEntrega: inputParaLocalDateTime(values.dataEntrega) }

    const salvar = editando
      ? updateMutation.mutateAsync({ id: lote!.id, payload })
      : createMutation.mutateAsync(payload)

    salvar
      .then(() => {
        toast.success(editando ? 'Entrada atualizada. O estoque foi ajustado.' : 'Entrada registrada no estoque.')
        onOpenChange(false)
      })
      .catch((error: Error) => toast.error(error.message))
  }

  const placeholderTipo = erroTipos
    ? 'Erro ao carregar tipos'
    : carregandoTipos
      ? 'Carregando...'
      : 'Selecione o tipo'

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{editando ? 'Editar entrada' : 'Nova entrada'}</DialogTitle>
          <DialogDescription>
            As peças recebidas do fornecedor. O saldo de cada tamanho sobe assim que a entrada é salva.
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="flex flex-col gap-2">
              <Label htmlFor="fornecedor">Fornecedor</Label>
              <Input
                id="fornecedor"
                placeholder="Confecções Aurora"
                {...register('fornecedor')}
                aria-invalid={Boolean(errors.fornecedor)}
              />
              {errors.fornecedor && <p className="text-sm text-destructive">{errors.fornecedor.message}</p>}
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="dataEntrega">Data de entrega</Label>
              <Input
                id="dataEntrega"
                type="date"
                {...register('dataEntrega')}
                aria-invalid={Boolean(errors.dataEntrega)}
              />
              {errors.dataEntrega && <p className="text-sm text-destructive">{errors.dataEntrega.message}</p>}
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="chaveAcesso">Chave de acesso da nota fiscal</Label>
            <Input
              id="chaveAcesso"
              inputMode="numeric"
              placeholder="Os 44 dígitos da NF-e"
              {...register('chaveAcesso')}
              aria-invalid={Boolean(errors.chaveAcesso)}
            />
            {errors.chaveAcesso && <p className="text-sm text-destructive">{errors.chaveAcesso.message}</p>}
          </div>

          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <Label>Itens recebidos</Label>
              <Button type="button" variant="outline" size="sm" onClick={() => append(ITEM_VAZIO)}>
                <Plus className="size-4" />
                Adicionar item
              </Button>
            </div>

            {fields.map((field, index) => (
              <div key={field.id} className="flex flex-wrap items-start gap-2 rounded-md border border-border p-3">
                <div className="min-w-40 flex-1 space-y-1">
                  <Controller
                    control={control}
                    name={`itens.${index}.tipoUniformeId`}
                    render={({ field: campo }) => (
                      <Select value={campo.value} onValueChange={(valor) => campo.onChange(valor ?? '')}>
                        <SelectTrigger className="w-full" aria-label={`Tipo do item ${index + 1}`}>
                          <SelectValue placeholder={placeholderTipo} />
                        </SelectTrigger>
                        <SelectContent>
                          {tipos?.map((tipo) => (
                            <SelectItem key={tipo.id} value={tipo.id}>
                              {tipo.tipo}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                  {errors.itens?.[index]?.tipoUniformeId && (
                    <p className="text-sm text-destructive">{errors.itens[index]?.tipoUniformeId?.message}</p>
                  )}
                </div>

                <Controller
                  control={control}
                  name={`itens.${index}.tamanho`}
                  render={({ field: campo }) => (
                    <Select value={campo.value} onValueChange={(valor) => campo.onChange(valor as Tamanho)}>
                      <SelectTrigger className="w-24" aria-label={`Tamanho do item ${index + 1}`}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {TAMANHOS.map((tamanho) => (
                          <SelectItem key={tamanho} value={tamanho}>
                            {tamanho}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />

                <Controller
                  control={control}
                  name={`itens.${index}.sexo`}
                  render={({ field: campo }) => (
                    <Select value={campo.value} onValueChange={(valor) => campo.onChange(valor as Sexo)}>
                      <SelectTrigger className="w-36" aria-label={`Sexo do item ${index + 1}`}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.entries(sexoLabels).map(([valor, rotulo]) => (
                          <SelectItem key={valor} value={valor}>
                            {rotulo}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />

                <div className="w-24 space-y-1">
                  <Input
                    type="number"
                    min={1}
                    aria-label={`Quantidade do item ${index + 1}`}
                    {...register(`itens.${index}.quantidade`, { valueAsNumber: true })}
                    aria-invalid={Boolean(errors.itens?.[index]?.quantidade)}
                  />
                  {errors.itens?.[index]?.quantidade && (
                    <p className="text-sm text-destructive">{errors.itens[index]?.quantidade?.message}</p>
                  )}
                </div>

                <Button
                  type="button"
                  variant="ghost"
                  size="icon-sm"
                  disabled={fields.length === 1}
                  onClick={() => remove(index)}
                >
                  <Trash2 className="size-4" />
                  <span className="sr-only">Remover item {index + 1}</span>
                </Button>
              </div>
            ))}

            {errors.itens?.root?.message && <p className="text-sm text-destructive">{errors.itens.root.message}</p>}
            {errors.itens?.message && <p className="text-sm text-destructive">{errors.itens.message}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={salvando}>
              {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Registrar entrada'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
