import { useMemo } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { ButtonLink } from '@/components/shared/button-link'
import { useSaidaProtegida } from '@/components/shared/confirmar-saida'
import { EstadoErro } from '@/components/shared/estados-lista'
import { QuantidadeStepper } from '@/components/shared/quantidade-stepper'
import { SelectItens } from '@/components/shared/select-itens'
import { SeletorTamanho } from '@/components/shared/seletor-tamanho'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Textarea } from '@/components/ui/textarea'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { useCreateRequisicaoMutation } from '@/hooks/use-requisicoes'
import { useTiposUniformeQuery } from '@/hooks/use-tipos-uniforme'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { nivelDoUniforme, TAMANHOS, type NivelEstoque } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'
import { requisicaoSchema, type RequisicaoFormInput, type RequisicaoFormValues } from '@/lib/schemas/requisicao'
import { toSelectItems } from '@/lib/select-items'
import type { Sexo, Tamanho } from '@/lib/types/uniforme'

const SEXOS: readonly Sexo[] = ['FEMININO', 'MASCULINO']
const VARIANTE_NIVEL: Record<NivelEstoque, 'out' | 'low' | 'ok'> = { esgotado: 'out', baixo: 'low', ok: 'ok' }

/* "Solicitar" na lista de reposição abre o formulário já com a peça (?tipo=&sexo=&tamanho=). */
function valoresIniciais(searchParams: URLSearchParams): Partial<RequisicaoFormInput> {
  return {
    tipoUniformeId: searchParams.get('tipo') ?? '',
    sexo: SEXOS.find((sexo) => sexo === searchParams.get('sexo')),
    tamanho: TAMANHOS.find((tamanho) => tamanho === searchParams.get('tamanho')),
    quantidade: 1,
    observacao: '',
  }
}

export function RequisicaoFormPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const tipos = useTiposUniformeQuery()
  const uniformes = useAllUniformesQuery()
  const createMutation = useCreateRequisicaoMutation()

  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isDirty },
  } = useForm<RequisicaoFormInput, unknown, RequisicaoFormValues>({
    resolver: zodResolver(requisicaoSchema),
    defaultValues: valoresIniciais(searchParams),
  })

  const [tipoUniformeId, sexo, tamanho, quantidade] = useWatch({ control, name: ['tipoUniformeId', 'sexo', 'tamanho', 'quantidade'] })
  const itensTipo = useMemo(() => toSelectItems(tipos.data, (tipo) => tipo.id, (tipo) => tipo.tipo), [tipos.data])
  const nomeTipo = tipos.data?.find((tipo) => tipo.id === tipoUniformeId)?.tipo
  const emEstoque = uniformes.data?.find(
    (uniforme) => uniforme.tipoUniformeId === tipoUniformeId && uniforme.sexo === sexo && uniforme.tamanho === tamanho,
  )
  const completo = Boolean(nomeTipo && sexo && tamanho)

  const saida = useSaidaProtegida(isDirty)

  function onSubmit(values: RequisicaoFormValues) {
    createMutation
      .mutateAsync({ ...values, observacao: values.observacao || undefined })
      .then(() => {
        toast.success('Requisição enviada. Ela fica em andamento até as peças chegarem.')
        saida.liberar()
        navigate('/requisicoes', { replace: true })
      })
      .catch((error: Error) => toast.error(error.message))
  }

  return (
    <AppLayout
      title="Nova requisição"
      description="Peça novas peças ao fornecedor quando o estoque não der conta. O estoque só sobe quando o lote chegar em Entradas."
      breadcrumb={[{ label: 'Requisições', to: '/requisicoes' }, { label: 'Nova requisição' }]}
      footer={
        <div className="ml-auto flex gap-2">
          <ButtonLink variant="outline" size="lg" to="/requisicoes">
            Cancelar
          </ButtonLink>
          <Button size="lg" type="submit" form="form-requisicao" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Enviando…' : 'Enviar requisição'}
          </Button>
        </div>
      }
    >
      {saida.dialogo}
      {tipos.isError && <EstadoErro titulo="Não foi possível carregar os tipos de uniforme" onRetry={() => tipos.refetch()} />}

      <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,32rem)_minmax(0,1fr)]">
        <Card>
          <CardContent>
            <form id="form-requisicao" noValidate onSubmit={handleSubmit(onSubmit)}>
              <FieldGroup>
                <Field data-invalid={Boolean(errors.tipoUniformeId) || undefined}>
                  <FieldLabel htmlFor="tipoUniformeId">Tipo de uniforme</FieldLabel>
                  <Controller
                    control={control}
                    name="tipoUniformeId"
                    render={({ field }) => (
                      <SelectItens
                        id="tipoUniformeId"
                        items={itensTipo}
                        value={field.value}
                        onValueChange={field.onChange}
                        placeholder={tipos.isLoading ? 'Carregando tipos…' : 'Escolha o tipo'}
                        invalido={Boolean(errors.tipoUniformeId)}
                        descritoPor="tipoUniformeId-ajuda"
                      />
                    )}
                  />
                  <FieldDescription id="tipoUniformeId-ajuda">
                    O tipo não está na lista? <Link to="/tipos-uniforme/novo">Cadastre o tipo</Link> primeiro.
                  </FieldDescription>
                  <FieldError errors={[errors.tipoUniformeId]} />
                </Field>

                <Field data-invalid={Boolean(errors.sexo) || undefined}>
                  <FieldLabel id="sexo-rotulo">Sexo</FieldLabel>
                  <Controller
                    control={control}
                    name="sexo"
                    render={({ field }) => (
                      <ToggleGroup
                        aria-labelledby="sexo-rotulo"
                        aria-invalid={Boolean(errors.sexo) || undefined}
                        variant="outline"
                        spacing={0}
                        value={field.value ? [field.value] : []}
                        onValueChange={(valores: string[]) => {
                          const proximo = valores[valores.length - 1] as Sexo | undefined
                          if (proximo) field.onChange(proximo)
                        }}
                      >
                        {SEXOS.map((opcao) => (
                          <ToggleGroupItem
                            key={opcao}
                            value={opcao}
                            className="h-9 px-4 data-pressed:border-primary data-pressed:bg-primary data-pressed:text-primary-foreground"
                          >
                            {sexoLabels[opcao]}
                          </ToggleGroupItem>
                        ))}
                      </ToggleGroup>
                    )}
                  />
                  <FieldError errors={[errors.sexo]} />
                </Field>

                <Field data-invalid={Boolean(errors.tamanho) || undefined}>
                  <FieldLabel id="tamanho-rotulo">Tamanho</FieldLabel>
                  <Controller
                    control={control}
                    name="tamanho"
                    render={({ field }) => (
                      <SeletorTamanho
                        rotulo="Tamanho"
                        valor={field.value ?? ''}
                        onChange={(valor) => field.onChange(valor as Tamanho)}
                        opcoes={TAMANHOS.map((opcao) => ({ valor: opcao, tamanho: opcao }))}
                        invalido={Boolean(errors.tamanho)}
                      />
                    )}
                  />
                  <FieldError errors={[errors.tamanho]} />
                </Field>

                <Field data-invalid={Boolean(errors.quantidade) || undefined}>
                  <FieldLabel id="quantidade-rotulo">Quantidade</FieldLabel>
                  <Controller
                    control={control}
                    name="quantidade"
                    render={({ field }) => (
                      <QuantidadeStepper
                        rotulo="Quantidade"
                        valor={field.value}
                        onChange={field.onChange}
                        invalido={Boolean(errors.quantidade)}
                      />
                    )}
                  />
                  <FieldError errors={[errors.quantidade]} />
                </Field>

                <Field data-invalid={Boolean(errors.observacao) || undefined}>
                  <FieldLabel htmlFor="observacao">Observação (opcional)</FieldLabel>
                  <Textarea
                    id="observacao"
                    rows={3}
                    placeholder="Ex.: chegaram alunos novos no 6º ano"
                    aria-invalid={Boolean(errors.observacao) || undefined}
                    {...register('observacao')}
                  />
                  <FieldError errors={[errors.observacao]} />
                </Field>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Resumo do pedido</CardTitle>
            <CardDescription aria-live="polite">
              {completo
                ? `${quantidade || 0} × ${nomeTipo} ${sexoLabels[sexo as Sexo].toLowerCase()} tamanho ${tamanho}`
                : 'Escolha o tipo, o sexo e o tamanho para ver o resumo.'}
            </CardDescription>
          </CardHeader>
          {completo && (
            <CardContent className="flex items-center gap-2 text-sm">
              <span className="text-muted-foreground">Em estoque agora:</span>
              {!emEstoque ? (
                <Badge variant="outline">nunca entrou</Badge>
              ) : (
                <Badge variant={VARIANTE_NIVEL[nivelDoUniforme(emEstoque)]} data-numeric>
                  {emEstoque.quantidade === 0 ? 'esgotado' : `${emEstoque.quantidade} ${emEstoque.quantidade === 1 ? 'peça' : 'peças'}`}
                </Badge>
              )}
            </CardContent>
          )}
        </Card>
      </div>
    </AppLayout>
  )
}
