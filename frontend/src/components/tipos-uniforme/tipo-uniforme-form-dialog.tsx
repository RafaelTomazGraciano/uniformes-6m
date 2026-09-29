import { useEffect } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
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
import { useCreateTipoUniformeMutation, useUpdateTipoUniformeMutation } from '@/hooks/use-tipos-uniforme'
import { tipoUniformeSchema, type TipoUniformeFormValues } from '@/lib/schemas/tipo-uniforme'
import type { TipoUniforme } from '@/lib/types/tipo-uniforme'

type TipoUniformeFormDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  tipoUniforme?: TipoUniforme
}

export function TipoUniformeFormDialog({ open, onOpenChange, tipoUniforme }: TipoUniformeFormDialogProps) {
  const editando = Boolean(tipoUniforme)
  const createMutation = useCreateTipoUniformeMutation()
  const updateMutation = useUpdateTipoUniformeMutation()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TipoUniformeFormValues>({
    resolver: zodResolver(tipoUniformeSchema),
    defaultValues: { tipo: '' },
  })

  useEffect(() => {
    if (open) reset({ tipo: tipoUniforme?.tipo ?? '' })
  }, [open, tipoUniforme, reset])

  const salvando = createMutation.isPending || updateMutation.isPending

  function onSubmit(values: TipoUniformeFormValues) {
    const salvar = editando
      ? updateMutation.mutateAsync({ id: tipoUniforme!.id, payload: values })
      : createMutation.mutateAsync(values)

    salvar
      .then(() => {
        toast.success(editando ? 'Tipo de uniforme atualizado.' : 'Tipo de uniforme criado.')
        onOpenChange(false)
      })
      .catch((error: Error) => toast.error(error.message))
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{editando ? 'Editar tipo de uniforme' : 'Novo tipo de uniforme'}</DialogTitle>
          <DialogDescription>
            {editando
              ? 'O novo nome passa a valer em todo o estoque.'
              : 'A peça em si — camiseta, calça, agasalho. Tamanho e sexo vêm depois, na entrada do lote.'}
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="flex flex-col gap-2">
            <Label htmlFor="tipo">Tipo</Label>
            <Input id="tipo" placeholder="Camiseta" {...register('tipo')} aria-invalid={Boolean(errors.tipo)} />
            {errors.tipo && <p className="text-sm text-destructive">{errors.tipo.message}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={salvando}>
              {salvando ? 'Salvando...' : 'Salvar'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
