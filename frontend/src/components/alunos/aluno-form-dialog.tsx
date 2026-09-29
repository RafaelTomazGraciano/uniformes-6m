import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect } from 'react'
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
import { useCreateAlunoMutation, useUpdateAlunoMutation } from '@/hooks/use-alunos'
import { useTurmasQuery } from '@/hooks/use-turmas'
import { turnoLabels } from '@/lib/labels'
import { alunoSchema, type AlunoFormValues } from '@/lib/schemas/aluno'
import type { Aluno } from '@/lib/types/aluno'

type AlunoFormDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  aluno?: Aluno
}

export function AlunoFormDialog({ open, onOpenChange, aluno }: AlunoFormDialogProps) {
  const isEditMode = Boolean(aluno)
  const { data: turmas, isLoading: isLoadingTurmas } = useTurmasQuery()
  const createMutation = useCreateAlunoMutation()
  const updateMutation = useUpdateAlunoMutation()

  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AlunoFormValues>({
    resolver: zodResolver(alunoSchema),
    defaultValues: { nome: '', turmaId: '' },
  })

  useEffect(() => {
    if (open) {
      reset({ nome: aluno?.nome ?? '', turmaId: aluno?.turmaId ?? '' })
    }
  }, [open, aluno, reset])

  const isPending = createMutation.isPending || updateMutation.isPending

  const onSubmit = (values: AlunoFormValues) => {
    const mutation = isEditMode
      ? updateMutation.mutateAsync({ id: aluno!.id, payload: values })
      : createMutation.mutateAsync(values)

    mutation
      .then(() => {
        toast.success(isEditMode ? 'Aluno atualizado com sucesso.' : 'Aluno criado com sucesso.')
        onOpenChange(false)
      })
      .catch((error: Error) => {
        toast.error(error.message)
      })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditMode ? 'Editar aluno' : 'Novo aluno'}</DialogTitle>
          <DialogDescription>
            {isEditMode ? 'Atualize os dados do aluno.' : 'Cadastre um novo aluno vinculado a uma turma.'}
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="flex flex-col gap-2">
            <Label htmlFor="nome">Nome</Label>
            <Input id="nome" placeholder="Nome do aluno" {...register('nome')} aria-invalid={Boolean(errors.nome)} />
            {errors.nome && <p className="text-sm text-destructive">{errors.nome.message}</p>}
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="turmaId">Turma</Label>
            <Controller
              control={control}
              name="turmaId"
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger id="turmaId" className="w-full">
                    <SelectValue placeholder={isLoadingTurmas ? 'Carregando turmas...' : 'Selecione a turma'} />
                  </SelectTrigger>
                  <SelectContent>
                    {turmas?.map((turma) => (
                      <SelectItem key={turma.id} value={turma.id}>
                        {turma.nome} — {turnoLabels[turma.turno]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.turmaId && <p className="text-sm text-destructive">{errors.turmaId.message}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? 'Salvando...' : 'Salvar'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
