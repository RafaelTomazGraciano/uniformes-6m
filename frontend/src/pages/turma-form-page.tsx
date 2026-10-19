import { useEffect } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { ButtonLink } from '@/components/shared/button-link'
import { Controller, useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { useSaidaProtegida } from '@/components/shared/confirmar-saida'
import { EstadoCarregando } from '@/components/shared/estados-lista'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Field, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { useCreateTurmaMutation, useTurmasQuery, useUpdateTurmaMutation } from '@/hooks/use-turmas'
import { ensinoLabels, turnoLabels } from '@/lib/labels'
import { turmaSchema, type TurmaFormValues } from '@/lib/schemas/turma'
import { labelsToSelectItems } from '@/lib/select-items'

const OPCOES_TURNO = labelsToSelectItems(turnoLabels)
const OPCOES_ENSINO = labelsToSelectItems(ensinoLabels)

function Escolha({
  legenda,
  opcoes,
  valor,
  onChange,
  erro,
}: {
  legenda: string
  opcoes: { value: string; label: string }[]
  valor: string
  onChange: (valor: string) => void
  erro?: string
}) {
  return (
    <FieldSet data-invalid={Boolean(erro) || undefined}>
      <FieldLegend variant="label">{legenda}</FieldLegend>
      <ToggleGroup
        aria-label={legenda}
        variant="outline"
        spacing={0}
        value={valor ? [valor] : []}
        onValueChange={(valores: string[]) => {
          const proximo = valores[valores.length - 1]
          if (proximo) onChange(proximo)
        }}
      >
        {opcoes.map((opcao) => (
          <ToggleGroupItem
            key={opcao.value}
            value={opcao.value}
            className="h-9 px-4 data-pressed:border-primary data-pressed:bg-primary data-pressed:text-primary-foreground"
          >
            {opcao.label}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>
      <FieldError>{erro}</FieldError>
    </FieldSet>
  )
}

export function TurmaFormPage() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navigate = useNavigate()
  const turmas = useTurmasQuery()
  const turma = turmas.data?.find((candidata) => candidata.id === id)
  const createMutation = useCreateTurmaMutation()
  const updateMutation = useUpdateTurmaMutation()

  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<TurmaFormValues>({
    resolver: zodResolver(turmaSchema),
    defaultValues: { nome: '', turno: 'DIURNO', ensino: 'FUNDAMENTAL' },
  })

  useEffect(() => {
    if (turma) reset({ nome: turma.nome, turno: turma.turno, ensino: turma.ensino })
  }, [turma, reset])

  const saida = useSaidaProtegida(isDirty)
  const salvando = createMutation.isPending || updateMutation.isPending

  function onSubmit(values: TurmaFormValues) {
    const requisicao = editando ? updateMutation.mutateAsync({ id: id!, payload: values }) : createMutation.mutateAsync(values)
    requisicao
      .then(() => {
        toast.success(editando ? 'Turma atualizada.' : `Turma ${values.nome} criada.`)
        saida.liberar()
        navigate('/turmas', { replace: true })
      })
      .catch((error: Error) => toast.error(error.message))
  }

  const titulo = editando ? 'Editar turma' : 'Nova turma'

  return (
    <AppLayout
      title={titulo}
      description={editando ? 'O novo nome aparece na ficha de cada aluno.' : 'Turmas agrupam os alunos que recebem uniforme.'}
      breadcrumb={[{ label: 'Turmas', to: '/turmas' }, { label: turma?.nome ?? titulo }]}
      footer={
        <div className="ml-auto flex gap-2">
          <ButtonLink variant="outline" size="lg" to="/turmas">
            Cancelar
          </ButtonLink>
          <Button size="lg" type="submit" form="form-turma" disabled={salvando}>
            {salvando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Criar turma'}
          </Button>
        </div>
      }
    >
      {saida.dialogo}
      {editando && turmas.isLoading ? (
        <EstadoCarregando rotulo="Carregando turma" linhas={2} />
      ) : (
        <Card className="max-w-2xl">
          <CardContent>
            <form id="form-turma" noValidate onSubmit={handleSubmit(onSubmit)}>
              <FieldGroup>
                <Field data-invalid={Boolean(errors.nome) || undefined}>
                  <FieldLabel htmlFor="nome">Nome da turma</FieldLabel>
                  <Input
                    id="nome"
                    placeholder="7º ano A"
                    aria-invalid={Boolean(errors.nome) || undefined}
                    aria-describedby={errors.nome ? 'nome-erro' : undefined}
                    {...register('nome')}
                  />
                  <FieldError id="nome-erro" errors={[errors.nome]} />
                </Field>
                <Controller
                  control={control}
                  name="turno"
                  render={({ field }) => (
                    <Escolha legenda="Turno" opcoes={OPCOES_TURNO} valor={field.value} onChange={field.onChange} erro={errors.turno?.message} />
                  )}
                />
                <Controller
                  control={control}
                  name="ensino"
                  render={({ field }) => (
                    <Escolha legenda="Ensino" opcoes={OPCOES_ENSINO} valor={field.value} onChange={field.onChange} erro={errors.ensino?.message} />
                  )}
                />
              </FieldGroup>
            </form>
          </CardContent>
        </Card>
      )}
    </AppLayout>
  )
}
