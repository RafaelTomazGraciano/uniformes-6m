import { useEffect, useMemo } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { ButtonLink } from '@/components/shared/button-link'
import { SearchX } from 'lucide-react'
import { useForm, useWatch } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { useSaidaProtegida } from '@/components/shared/confirmar-saida'
import { EstadoCarregando, EstadoVazio } from '@/components/shared/estados-lista'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { useCreateTipoUniformeMutation, useTiposUniformeQuery, useUpdateTipoUniformeMutation } from '@/hooks/use-tipos-uniforme'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { agruparPorTipo, nivelEstoque, TAMANHOS } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'
import { tipoUniformeSchema, type TipoUniformeFormValues } from '@/lib/schemas/tipo-uniforme'
import { cn } from '@/lib/utils'

const LIMITE_PADRAO = 5

export function TipoUniformeFormPage() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navigate = useNavigate()
  const tipos = useTiposUniformeQuery()
  const uniformes = useAllUniformesQuery()
  const createMutation = useCreateTipoUniformeMutation()
  const updateMutation = useUpdateTipoUniformeMutation()
  const tipo = tipos.data?.find((candidato) => candidato.id === id)

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<TipoUniformeFormValues>({
    resolver: zodResolver(tipoUniformeSchema),
    defaultValues: { tipo: '', estoqueBaixo: LIMITE_PADRAO },
  })

  useEffect(() => {
    if (tipo) reset({ tipo: tipo.tipo, estoqueBaixo: tipo.estoqueBaixo })
  }, [tipo, reset])

  const limiteDigitado = useWatch({ control, name: 'estoqueBaixo' })
  const limite = Number.isFinite(limiteDigitado) && limiteDigitado >= 0 ? limiteDigitado : 0
  const grupo = useMemo(
    () => (id ? agruparPorTipo((uniformes.data ?? []).filter((uniforme) => uniforme.tipoUniformeId === id))[0] : undefined),
    [uniformes.data, id],
  )
  const celulas = grupo?.linhas.flatMap((linha) => TAMANHOS.map((tamanho) => linha.porTamanho[tamanho]).filter((valor) => valor !== undefined)) ?? []
  const emAlerta = celulas.filter((quantidade) => nivelEstoque(quantidade!, limite) !== 'ok').length
  const esgotadas = celulas.filter((quantidade) => quantidade === 0).length

  const saida = useSaidaProtegida(isDirty)
  const salvando = createMutation.isPending || updateMutation.isPending

  function onSubmit(values: TipoUniformeFormValues) {
    const requisicao = editando ? updateMutation.mutateAsync({ id: id!, payload: values }) : createMutation.mutateAsync(values)
    requisicao
      .then(() => {
        toast.success(editando ? 'Tipo atualizado.' : `Tipo “${values.tipo}” criado.`)
        saida.liberar()
        navigate('/tipos-uniforme', { replace: true })
      })
      .catch((error: Error) => toast.error(error.message))
  }

  const titulo = editando ? 'Editar tipo de uniforme' : 'Novo tipo de uniforme'

  if (editando && tipos.data && !tipo) {
    return (
      <AppLayout title="Tipo não encontrado" breadcrumb={[{ label: 'Tipos de uniforme', to: '/tipos-uniforme' }, { label: 'Não encontrado' }]}>
        <EstadoVazio
          icone={<SearchX />}
          titulo="Este tipo não existe mais"
          descricao="Ele pode ter sido excluído. Volte para a lista e escolha outro."
          acao={
            <ButtonLink to="/tipos-uniforme">
              Ver tipos de uniforme
            </ButtonLink>
          }
        />
      </AppLayout>
    )
  }

  return (
    <AppLayout
      title={titulo}
      description="O nome aparece nas entregas e nos relatórios. O limite decide quando um tamanho entra na lista de reposição."
      breadcrumb={[{ label: 'Tipos de uniforme', to: '/tipos-uniforme' }, { label: tipo?.tipo ?? titulo }]}
      footer={
        <div className="ml-auto flex gap-2">
          <ButtonLink variant="outline" size="lg" to="/tipos-uniforme">
            Cancelar
          </ButtonLink>
          <Button size="lg" type="submit" form="form-tipo" disabled={salvando}>
            {salvando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Criar tipo'}
          </Button>
        </div>
      }
    >
      {saida.dialogo}
      {editando && tipos.isLoading ? (
        <EstadoCarregando rotulo="Carregando tipo" linhas={2} />
      ) : (
        <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,26rem)_minmax(0,1fr)]">
          <Card>
            <CardContent>
              <form id="form-tipo" noValidate onSubmit={handleSubmit(onSubmit)}>
                <FieldGroup>
                  <Field data-invalid={Boolean(errors.tipo) || undefined}>
                    <FieldLabel htmlFor="tipo">Nome do tipo</FieldLabel>
                    <Input
                      id="tipo"
                      placeholder="Camiseta manga curta"
                      aria-invalid={Boolean(errors.tipo) || undefined}
                      aria-describedby={errors.tipo ? 'tipo-erro' : undefined}
                      {...register('tipo')}
                    />
                    <FieldError id="tipo-erro" errors={[errors.tipo]} />
                  </Field>
                  <Field data-invalid={Boolean(errors.estoqueBaixo) || undefined}>
                    <FieldLabel htmlFor="estoqueBaixo">Avisar quando restar</FieldLabel>
                    <div className="flex items-center gap-3">
                      <Input
                        id="estoqueBaixo"
                        type="number"
                        min={0}
                        inputMode="numeric"
                        className="w-24 text-right tabular-nums"
                        aria-invalid={Boolean(errors.estoqueBaixo) || undefined}
                        aria-describedby="estoqueBaixo-ajuda"
                        {...register('estoqueBaixo', { valueAsNumber: true })}
                      />
                      <span className="text-sm text-muted-foreground">peças ou menos, por tamanho e modelo</span>
                    </div>
                    <FieldDescription id="estoqueBaixo-ajuda">
                      Use 0 para avisar só quando esgotar. Peças muito entregues (camiseta) pedem um limite maior que as raras (jaqueta).
                    </FieldDescription>
                    <FieldError errors={[errors.estoqueBaixo]} />
                  </Field>
                </FieldGroup>
              </form>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Como fica com o limite {limite}</CardTitle>
              <CardDescription aria-live="polite">
                {!grupo
                  ? 'Este tipo ainda não tem estoque. A prévia aparece depois da primeira entrada.'
                  : `${emAlerta} de ${celulas.length} combinações ficam em alerta${esgotadas ? `, ${esgotadas} esgotada${esgotadas > 1 ? 's' : ''}` : ''}.`}
              </CardDescription>
            </CardHeader>
            {grupo && (
              <CardContent className="overflow-x-auto">
                <table className="w-full min-w-[22rem] border-collapse text-sm">
                  <caption className="sr-only">Estoque atual com o limite aplicado</caption>
                  <thead>
                    <tr className="text-xs text-muted-foreground">
                      <th scope="col" className="py-1.5 text-left font-medium">
                        Modelo
                      </th>
                      {TAMANHOS.map((tamanho) => (
                        <th key={tamanho} scope="col" className="py-1.5 text-center font-medium">
                          {tamanho}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {grupo.linhas.map((linha) => (
                      <tr key={linha.sexo}>
                        <th scope="row" className="py-1 text-left font-medium">
                          {sexoLabels[linha.sexo]}
                        </th>
                        {TAMANHOS.map((tamanho) => {
                          const quantidade = linha.porTamanho[tamanho]
                          const nivel = quantidade === undefined ? null : nivelEstoque(quantidade, limite)
                          return (
                            <td key={tamanho} className="px-1 py-1 text-center">
                              <span
                                className={cn(
                                  'mx-auto flex h-9 w-12 items-center justify-center rounded-md bg-muted/50 tabular-nums',
                                  nivel === 'baixo' && 'bg-signal-low-surface font-semibold text-signal-low ring-1 ring-signal-low/40',
                                  nivel === 'esgotado' && 'bg-signal-out-surface font-semibold text-signal-out ring-1 ring-signal-out/40',
                                  nivel === null && 'bg-transparent text-muted-foreground/50',
                                )}
                              >
                                {quantidade ?? '–'}
                                {nivel && nivel !== 'ok' && <span className="sr-only">, {nivel}</span>}
                              </span>
                            </td>
                          )
                        })}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </CardContent>
            )}
          </Card>
        </div>
      )}
    </AppLayout>
  )
}
