import { useEffect, useMemo } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { ButtonLink } from '@/components/shared/button-link'
import { Controller, useForm } from 'react-hook-form'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { useSaidaProtegida } from '@/components/shared/confirmar-saida'
import { EstadoCarregando, EstadoErro } from '@/components/shared/estados-lista'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { SelectItens } from '@/components/shared/select-itens'
import { useAlunoQuery, useCreateAlunoMutation, useUpdateAlunoMutation } from '@/hooks/use-alunos'
import { useTurmasQuery } from '@/hooks/use-turmas'
import { turnoLabels } from '@/lib/labels'
import { alunoSchema, type AlunoFormInput, type AlunoFormValues } from '@/lib/schemas/aluno'
import { formatarTelefone } from '@/lib/format'
import { toSelectItems } from '@/lib/select-items'

export function AlunoFormPage() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const aluno = useAlunoQuery(id)
  const turmas = useTurmasQuery()
  const createMutation = useCreateAlunoMutation()
  const updateMutation = useUpdateAlunoMutation()

  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<AlunoFormInput, unknown, AlunoFormValues>({
    resolver: zodResolver(alunoSchema),
    defaultValues: { nome: '', nomeResponsavel: '', telefoneResponsavel: '', turmaId: searchParams.get('turma') ?? '' },
  })

  useEffect(() => {
    if (aluno.data)
      reset({
        nome: aluno.data.nome,
        nomeResponsavel: aluno.data.nomeResponsavel ?? '',
        telefoneResponsavel: formatarTelefone(aluno.data.telefoneResponsavel),
        turmaId: aluno.data.turmaId,
      })
  }, [aluno.data, reset])

  const itensTurma = useMemo(
    () => toSelectItems(turmas.data, (turma) => turma.id, (turma) => `${turma.nome} — ${turnoLabels[turma.turno]}`),
    [turmas.data],
  )
  const saida = useSaidaProtegida(isDirty)
  const salvando = createMutation.isPending || updateMutation.isPending

  function onSubmit(valores: AlunoFormValues) {
    const values = {
      nome: valores.nome,
      turmaId: valores.turmaId,
      nomeResponsavel: valores.nomeResponsavel || undefined,
      telefoneResponsavel: valores.telefoneResponsavel || undefined,
    }
    const requisicao = editando ? updateMutation.mutateAsync({ id: id!, payload: values }) : createMutation.mutateAsync(values)
    requisicao
      .then((salvo) => {
        toast.success(editando ? 'Aluno atualizado.' : `${values.nome} cadastrado.`)
        saida.liberar()
        navigate(`/alunos?ver=${salvo.id}`, { replace: true })
      })
      .catch((error: Error) => toast.error(error.message))
  }

  const titulo = editando ? 'Editar aluno' : 'Novo aluno'

  return (
    <AppLayout
      title={titulo}
      description={editando ? 'Atualize o nome ou a turma do aluno.' : 'Todo aluno pertence a uma turma.'}
      breadcrumb={[{ label: 'Alunos', to: '/alunos' }, { label: aluno.data?.nome ?? titulo }]}
      footer={
        <div className="ml-auto flex gap-2">
          <ButtonLink variant="outline" size="lg" to="/alunos">
            Cancelar
          </ButtonLink>
          <Button size="lg" type="submit" form="form-aluno" disabled={salvando}>
            {salvando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Cadastrar aluno'}
          </Button>
        </div>
      }
    >
      {saida.dialogo}
      {editando && aluno.isLoading && <EstadoCarregando rotulo="Carregando aluno" linhas={2} />}
      {editando && aluno.isError && <EstadoErro titulo="Não foi possível carregar o aluno" onRetry={() => aluno.refetch()} />}

      {(!editando || aluno.data) && (
        <Card className="max-w-2xl">
          <CardContent>
            <form id="form-aluno" noValidate onSubmit={handleSubmit(onSubmit)}>
              <FieldGroup>
                <Field data-invalid={Boolean(errors.nome) || undefined}>
                  <FieldLabel htmlFor="nome">Nome completo</FieldLabel>
                  <Input
                    id="nome"
                    autoComplete="off"
                    aria-invalid={Boolean(errors.nome) || undefined}
                    aria-describedby={errors.nome ? 'nome-erro' : undefined}
                    {...register('nome')}
                  />
                  <FieldError id="nome-erro" errors={[errors.nome]} />
                </Field>
                <div className="grid gap-4 sm:grid-cols-2">
                  <Field data-invalid={Boolean(errors.nomeResponsavel) || undefined}>
                    <FieldLabel htmlFor="nomeResponsavel">Responsável (opcional)</FieldLabel>
                    <Input
                      id="nomeResponsavel"
                      autoComplete="off"
                      aria-invalid={Boolean(errors.nomeResponsavel) || undefined}
                      {...register('nomeResponsavel')}
                    />
                    <FieldError errors={[errors.nomeResponsavel]} />
                  </Field>
                  <Field data-invalid={Boolean(errors.telefoneResponsavel) || undefined}>
                    <FieldLabel htmlFor="telefoneResponsavel">Telefone do responsável (opcional)</FieldLabel>
                    <Input
                      id="telefoneResponsavel"
                      type="tel"
                      inputMode="tel"
                      autoComplete="off"
                      placeholder="(11) 98765-4321"
                      aria-invalid={Boolean(errors.telefoneResponsavel) || undefined}
                      aria-describedby="telefoneResponsavel-ajuda"
                      {...register('telefoneResponsavel')}
                    />
                    <FieldDescription id="telefoneResponsavel-ajuda">Com DDD. Usado para avisar quando o uniforme chegar.</FieldDescription>
                    <FieldError errors={[errors.telefoneResponsavel]} />
                  </Field>
                </div>
                <Field data-invalid={Boolean(errors.turmaId) || undefined}>
                  <FieldLabel htmlFor="turmaId">Turma</FieldLabel>
                  <Controller
                    control={control}
                    name="turmaId"
                    render={({ field }) => (
                      <SelectItens
                        id="turmaId"
                        items={itensTurma}
                        value={field.value}
                        onValueChange={field.onChange}
                        placeholder={turmas.isLoading ? 'Carregando turmas…' : 'Escolha a turma'}
                        invalido={Boolean(errors.turmaId)}
                        descritoPor="turmaId-ajuda"
                      />
                    )}
                  />
                  <FieldDescription id="turmaId-ajuda">
                    A turma não está na lista? <Link to="/turmas/nova">Cadastre a turma</Link> primeiro.
                  </FieldDescription>
                  <FieldError errors={[errors.turmaId]} />
                </Field>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>
      )}
    </AppLayout>
  )
}
