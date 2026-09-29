import { zodResolver } from '@hookform/resolvers/zod'
import { Plus, Trash2 } from 'lucide-react'
import { Controller, useFieldArray, useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useAllAlunosQuery } from '@/hooks/use-alunos'
import { useCreatePedidoMutation } from '@/hooks/use-pedidos'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { sexoLabels } from '@/lib/labels'
import { pedidoSchema, type PedidoFormValues } from '@/lib/schemas/pedido'

export function NovaEntregaPage() {
  const navigate = useNavigate()
  const { data: alunos, isLoading: isLoadingAlunos, isError: isAlunosError } = useAllAlunosQuery()
  const { data: uniformes, isLoading: isLoadingUniformes, isError: isUniformesError } = useAllUniformesQuery()
  const createMutation = useCreatePedidoMutation()

  const {
    control,
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<PedidoFormValues>({
    resolver: zodResolver(pedidoSchema),
    defaultValues: { alunoId: '', itens: [{ uniformeId: '', quantidade: 1 }] },
  })

  const { fields, append, remove } = useFieldArray({ control, name: 'itens' })

  const onSubmit = (values: PedidoFormValues) => {
    let hasStockError = false

    values.itens.forEach((item, index) => {
      const uniforme = uniformes?.find((candidate) => candidate.id === item.uniformeId)
      if (uniforme && item.quantidade > uniforme.quantidade) {
        setError(`itens.${index}.quantidade`, {
          message: `Estoque insuficiente (disponível: ${uniforme.quantidade}).`,
        })
        hasStockError = true
      }
    })

    if (hasStockError) return

    createMutation.mutate(values, {
      onSuccess: () => {
        toast.success('Entrega registrada com sucesso.')
        navigate('/pedidos')
      },
      onError: (error: Error) => toast.error(error.message),
    })
  }

  return (
    <AppLayout title="Nova entrega">
      <form
        className="max-w-2xl space-y-6 rounded-xl border border-border bg-card p-6"
        onSubmit={handleSubmit(onSubmit)}
        noValidate
      >
        <div className="space-y-2">
          <Label htmlFor="alunoId">Aluno</Label>
          <Controller
            control={control}
            name="alunoId"
            render={({ field }) => (
              <Select value={field.value} onValueChange={field.onChange}>
                <SelectTrigger id="alunoId" className="w-full">
                  <SelectValue
                    placeholder={
                      isAlunosError
                        ? 'Erro ao carregar alunos'
                        : isLoadingAlunos
                          ? 'Carregando alunos...'
                          : 'Selecione o aluno'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {alunos?.map((aluno) => (
                    <SelectItem key={aluno.id} value={aluno.id}>
                      {aluno.nome} — {aluno.turmaNome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          />
          {errors.alunoId && <p className="text-sm text-destructive">{errors.alunoId.message}</p>}
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <Label>Itens</Label>
            <Button type="button" variant="outline" size="sm" onClick={() => append({ uniformeId: '', quantidade: 1 })}>
              <Plus className="size-4" />
              Adicionar item
            </Button>
          </div>

          {fields.map((field, index) => (
            <div key={field.id} className="flex items-start gap-2">
              <div className="flex-1 space-y-1">
                <Controller
                  control={control}
                  name={`itens.${index}.uniformeId`}
                  render={({ field: selectField }) => (
                    <Select value={selectField.value} onValueChange={selectField.onChange}>
                      <SelectTrigger className="w-full">
                        <SelectValue
                          placeholder={
                            isUniformesError
                              ? 'Erro ao carregar itens'
                              : isLoadingUniformes
                                ? 'Carregando...'
                                : 'Selecione o item'
                          }
                        />
                      </SelectTrigger>
                      <SelectContent>
                        {uniformes?.map((uniforme) => (
                          <SelectItem key={uniforme.id} value={uniforme.id}>
                            {uniforme.tipoUniformeNome} — {uniforme.tamanho} — {sexoLabels[uniforme.sexo]} (estoque:{' '}
                            {uniforme.quantidade})
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.itens?.[index]?.uniformeId && (
                  <p className="text-sm text-destructive">{errors.itens[index]?.uniformeId?.message}</p>
                )}
              </div>

              <div className="w-24 space-y-1">
                <Input
                  type="number"
                  min={1}
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
                <span className="sr-only">Remover item</span>
              </Button>
            </div>
          ))}

          {errors.itens?.root?.message && <p className="text-sm text-destructive">{errors.itens.root.message}</p>}
        </div>

        <Button type="submit" disabled={createMutation.isPending}>
          {createMutation.isPending ? 'Registrando...' : 'Registrar entrega'}
        </Button>
      </form>
    </AppLayout>
  )
}
