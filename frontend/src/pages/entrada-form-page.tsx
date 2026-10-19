import { useEffect, useMemo, useState } from 'react'
import { Grid3x3, List } from 'lucide-react'
import { ButtonLink } from '@/components/shared/button-link'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'

import { EntradaGrade } from '@/components/entradas/entrada-grade'
import { EntradaLinhas } from '@/components/entradas/entrada-linhas'
import { ResumoEntrada } from '@/components/entradas/resumo-entrada'
import { AppLayout } from '@/components/layout/app-layout'
import { useSaidaProtegida } from '@/components/shared/confirmar-saida'
import { EstadoCarregando, EstadoErro } from '@/components/shared/estados-lista'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useCreateLoteMutation, useLoteQuery, useUpdateLoteMutation } from '@/hooks/use-lotes'
import { useTiposUniformeQuery } from '@/hooks/use-tipos-uniforme'
import { novaLinhaEntrada, TAMANHOS, type LinhaEntrada } from '@/lib/entrada'
import { dataParaInput, hojeParaInput, inputParaLocalDateTime } from '@/lib/format'
import { loteSchema } from '@/lib/schemas/lote'
import { toSelectItems } from '@/lib/select-items'

type Modo = 'linhas' | 'grade'
type Dados = { fornecedor: string; dataEntrega: string; chaveAcesso: string }
type Erros = Partial<Record<keyof Dados | 'itens', string>> & { linhas: Record<string, string > }

const SEM_ERROS: Erros = { linhas: {} }

export function EntradaFormPage() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const modo: Modo = searchParams.get('modo') === 'grade' ? 'grade' : 'linhas'

  const lote = useLoteQuery(id)
  const tipos = useTiposUniformeQuery()
  const createMutation = useCreateLoteMutation()
  const updateMutation = useUpdateLoteMutation()

  const [dados, setDados] = useState<Dados>({ fornecedor: '', dataEntrega: hojeParaInput(), chaveAcesso: '' })
  /* "Repor" no Estoque abre a entrada já com a peça (?tipo=&sexo=&tamanho=). */
  const [linhas, setLinhas] = useState<LinhaEntrada[]>(() => {
    const tipo = searchParams.get('tipo')
    if (!tipo) return [novaLinhaEntrada()]
    const sexo = searchParams.get('sexo') === 'MASCULINO' ? 'MASCULINO' : 'FEMININO'
    const tamanho = TAMANHOS.find((candidato) => candidato === searchParams.get('tamanho')) ?? 'M'
    return [novaLinhaEntrada({ tipoUniformeId: tipo, sexo, tamanho })]
  })
  const [erros, setErros] = useState<Erros>(SEM_ERROS)
  const [alterado, setAlterado] = useState(false)
  const [carregadoId, setCarregadoId] = useState<string | null>(null)

  /* Na edição, o formulário parte do lote salvo (uma vez por id). */
  useEffect(() => {
    if (!lote.data || carregadoId === lote.data.id) return
    setDados({
      fornecedor: lote.data.fornecedor,
      dataEntrega: dataParaInput(lote.data.dataEntrega) || hojeParaInput(),
      chaveAcesso: lote.data.notaFiscalChaveAcesso,
    })
    setLinhas(
      lote.data.itens.length
        ? lote.data.itens.map((item) =>
            novaLinhaEntrada({ tipoUniformeId: item.tipoUniformeId, sexo: item.sexo, tamanho: item.tamanho, quantidade: item.quantidade }),
          )
        : [novaLinhaEntrada()],
    )
    setCarregadoId(lote.data.id)
  }, [lote.data, carregadoId])

  const itensTipo = useMemo(() => toSelectItems(tipos.data, (tipo) => tipo.id, (tipo) => tipo.tipo), [tipos.data])
  const digitos = dados.chaveAcesso.replace(/\D/g, '').length
  const saida = useSaidaProtegida(alterado)
  const salvando = createMutation.isPending || updateMutation.isPending

  function mudarDados(parcial: Partial<Dados>) {
    setDados((atual) => ({ ...atual, ...parcial }))
    setAlterado(true)
  }

  function mudarLinhas(proximas: LinhaEntrada[]) {
    setLinhas(proximas.length ? proximas : [novaLinhaEntrada()])
    setAlterado(true)
  }

  function mudarModo(proximo: Modo) {
    setSearchParams(
      (atual) => {
        const params = new URLSearchParams(atual)
        if (proximo === 'grade') params.set('modo', 'grade')
        else params.delete('modo')
        return params
      },
      { replace: true },
    )
  }

  function salvar() {
    const itens = linhas.filter((linha) => linha.tipoUniformeId || linha.quantidade > 1)
    const resultado = loteSchema.safeParse({ ...dados, itens: itens.map(({ chave: _chave, ...item }) => item) })

    if (!resultado.success) {
      const proximos: Erros = { linhas: {} }
      for (const issue of resultado.error.issues) {
        const [campo, indice] = issue.path
        if (campo === 'itens' && typeof indice === 'number') {
          proximos.linhas[itens[indice].chave] ??= issue.message
        } else if (campo === 'itens') {
          proximos.itens = issue.message
        } else if (typeof campo === 'string') {
          proximos[campo as keyof Dados] ??= issue.message
        }
      }
      setErros(proximos)
      toast.error('Confira os campos destacados antes de salvar.')
      requestAnimationFrame(() => document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus())
      return
    }

    setErros(SEM_ERROS)
    const payload = { ...resultado.data, dataEntrega: inputParaLocalDateTime(resultado.data.dataEntrega) }
    const requisicao = editando ? updateMutation.mutateAsync({ id: id!, payload }) : createMutation.mutateAsync(payload)

    requisicao
      .then((salvo) => {
        setAlterado(false)
        toast.success(editando ? 'Entrada atualizada. O estoque foi ajustado.' : 'Entrada registrada no estoque.')
        saida.liberar()
        navigate(`/entradas?ver=${salvo.id}`, { replace: true })
      })
      .catch((error: Error) => toast.error(error.message))
  }

  const titulo = editando ? 'Editar entrada' : 'Nova entrada'

  return (
    <AppLayout
      title={titulo}
      description="As peças recebidas do fornecedor. Uma linha por item da nota; a mesma peça pode se repetir."
      breadcrumb={[{ label: 'Entradas', to: '/entradas' }, { label: titulo }]}
      footer={
        <>
          <p className="text-sm text-muted-foreground">
            {dados.fornecedor ? (
              <>
                Nota de <strong className="font-semibold text-foreground">{dados.fornecedor}</strong>
              </>
            ) : (
              'Preencha os dados da nota e os itens'
            )}
          </p>
          <div className="flex gap-2">
            <ButtonLink variant="outline" size="lg" to="/entradas">
              Cancelar
            </ButtonLink>
            <Button size="lg" disabled={salvando || (editando && !lote.data)} onClick={salvar}>
              {salvando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Registrar entrada'}
            </Button>
          </div>
        </>
      }
    >
      {saida.dialogo}

      {editando && lote.isLoading && <EstadoCarregando rotulo="Carregando entrada" linhas={4} />}
      {editando && lote.isError && <EstadoErro titulo="Não foi possível carregar esta entrada" onRetry={() => lote.refetch()} />}
      {tipos.isError && <EstadoErro titulo="Não foi possível carregar os tipos de uniforme" onRetry={() => tipos.refetch()} />}

      {(!editando || lote.data) && (
        <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
          <div className="flex min-w-0 flex-col gap-6">
            <Card>
              <CardContent>
                <FieldSet>
                  <FieldLegend>Dados da nota</FieldLegend>
                  <FieldGroup className="grid gap-4 sm:grid-cols-2">
                    <Field data-invalid={Boolean(erros.fornecedor) || undefined}>
                      <FieldLabel htmlFor="fornecedor">Fornecedor</FieldLabel>
                      <Input
                        id="fornecedor"
                        autoComplete="organization"
                        value={dados.fornecedor}
                        aria-invalid={Boolean(erros.fornecedor) || undefined}
                        aria-describedby={erros.fornecedor ? 'fornecedor-erro' : undefined}
                        onChange={(event) => mudarDados({ fornecedor: event.target.value })}
                      />
                      <FieldError id="fornecedor-erro">{erros.fornecedor}</FieldError>
                    </Field>
                    <Field data-invalid={Boolean(erros.dataEntrega) || undefined}>
                      <FieldLabel htmlFor="dataEntrega">Data de entrega</FieldLabel>
                      <Input
                        id="dataEntrega"
                        type="date"
                        value={dados.dataEntrega}
                        aria-invalid={Boolean(erros.dataEntrega) || undefined}
                        onChange={(event) => mudarDados({ dataEntrega: event.target.value })}
                      />
                      <FieldError>{erros.dataEntrega}</FieldError>
                    </Field>
                    <Field className="sm:col-span-2" data-invalid={Boolean(erros.chaveAcesso) || undefined}>
                      <FieldLabel htmlFor="chaveAcesso">Chave de acesso da NF-e</FieldLabel>
                      <Input
                        id="chaveAcesso"
                        inputMode="numeric"
                        className="font-mono tracking-wide"
                        value={dados.chaveAcesso}
                        aria-invalid={Boolean(erros.chaveAcesso) || undefined}
                        aria-describedby="chaveAcesso-ajuda"
                        onChange={(event) => mudarDados({ chaveAcesso: event.target.value })}
                      />
                      <FieldDescription id="chaveAcesso-ajuda" className={digitos === 44 ? 'text-signal-ok' : undefined}>
                        A NF-e tem 44 dígitos. Digitados: {digitos} de 44.
                      </FieldDescription>
                      <FieldError>{erros.chaveAcesso}</FieldError>
                    </Field>
                  </FieldGroup>
                </FieldSet>
              </CardContent>
            </Card>

            <Tabs
              value={modo}
              onValueChange={(valor) => mudarModo(valor as Modo)}
              render={<section aria-labelledby="itens-titulo" />}
              className="flex flex-col gap-4"
            >
              <div className="flex flex-wrap items-center justify-between gap-3">
                <h2 id="itens-titulo" className="text-lg font-semibold">
                  Itens recebidos
                </h2>
                <TabsList aria-label="Forma de lançar">
                  <TabsTrigger value="linhas" className="px-3">
                    <List aria-hidden />
                    Linhas
                  </TabsTrigger>
                  <TabsTrigger value="grade" className="px-3">
                    <Grid3x3 aria-hidden />
                    Grade
                  </TabsTrigger>
                </TabsList>
              </div>
              {erros.itens && (
                <p role="alert" className="text-sm text-destructive">
                  {erros.itens}
                </p>
              )}
              <TabsContent value="linhas">
                <EntradaLinhas
                  linhas={linhas}
                  itensTipo={itensTipo}
                  carregandoTipos={tipos.isLoading}
                  erros={erros.linhas}
                  onChange={mudarLinhas}
                />
              </TabsContent>
              <TabsContent value="grade">
                <EntradaGrade linhas={linhas} itensTipo={itensTipo} onChange={mudarLinhas} />
              </TabsContent>
            </Tabs>
          </div>

          <ResumoEntrada linhas={linhas} itensTipo={itensTipo} />
        </div>
      )}
    </AppLayout>
  )
}
