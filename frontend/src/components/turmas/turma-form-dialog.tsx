import { useEffect } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm } from 'react-hook-form'
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
import { useCreateTurmaMutation, useUpdateTurmaMutation } from '@/hooks/use-turmas'
import { ensinoLabels, turnoLabels } from '@/lib/labels'
import { turmaSchema, type TurmaFormValues } from '@/lib/schemas/turma'
import type { Ensino, Turma, Turno } from '@/lib/types/turma'

type TurmaFormDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  turma?: Turma
}

export function TurmaFormDialog({ open, onOpenChange, turma }: TurmaFormDialogProps) {
  const editando = Boolean(turma)
  const createMutation = useCreateTurmaMutation()
  const updateMutation = useUpdateTurmaMutation()

  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TurmaFormValues>({
    resolver: zodResolver(turmaSchema),
    defaultValues: { nome: '', turno: 'DIURNO', ensino: 'FUNDAMENTAL' },
  })

  useEffect(() => {
    if (open) {
      reset({
        nome: turma?.nome ?? '',
        turno: turma?.turno ?? 'DIURNO',
        ensino: turma?.ensino ?? 'FUNDAMENTAL',
      })
    }
  }, [open, turma, reset])

  const salvando = createMutation.isPending || updateMutation.isPending

  function onSubmit(values: TurmaFormValues) {
    const salvar = editando
      ? updateMutation.mutateAsync({ id: turma!.id, payload: values })
      : createMutation.mutateAsync(values)

    salvar
      .then(() => {
        toast.success(editando ? 'Turma atualizada.' : 'Turma criada.')
        onOpenChange(false)
      })
      .catch((error: Error) => toast.error(error.message))
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{editando ? 'Editar turma' : 'Nova turma'}</DialogTitle>
          <DialogDescription>
            {editando ? 'O novo nome aparece na ficha de cada aluno.' : 'Turmas agrupam os alunos que recebem uniforme.'}
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="flex flex-col gap-2">
            <Label htmlFor="nome">Nome</Label>
            <Input id="nome" placeholder="9º Ano A" {...register('nome')} aria-invalid={Boolean(errors.nome)} />
            {errors.nome && <p className="text-sm text-destructive">{errors.nome.message}</p>}
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="turno">Turno</Label>
            <Controller
              control={control}
              name="turno"
              render={({ field }) => (
                <Select value={field.value} onValueChange={(valor) => field.onChange(valor as Turno)}>
                  <SelectTrigger id="turno" className="w-full">
                    <SelectValue placeholder="Selecione o turno" />
                  </SelectTrigger>
                  <SelectContent>
                    {Object.entries(turnoLabels).map(([valor, rotulo]) => (
                      <SelectItem key={valor} value={valor}>
                        {rotulo}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.turno && <p className="text-sm text-destructive">{errors.turno.message}</p>}
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="ensino">Ensino</Label>
            <Controller
              control={control}
              name="ensino"
              render={({ field }) => (
                <Select value={field.value} onValueChange={(valor) => field.onChange(valor as Ensino)}>
                  <SelectTrigger id="ensino" className="w-full">
                    <SelectValue placeholder="Selecione o ensino" />
                  </SelectTrigger>
                  <SelectContent>
                    {Object.entries(ensinoLabels).map(([valor, rotulo]) => (
                      <SelectItem key={valor} value={valor}>
                        {rotulo}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.ensino && <p className="text-sm text-destructive">{errors.ensino.message}</p>}
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
