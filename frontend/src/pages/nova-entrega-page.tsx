import { useMemo, useState } from 'react'
import { LayoutGrid, List } from 'lucide-react'
import { ButtonLink } from '@/components/shared/button-link'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { AlunoPicker } from '@/components/pedidos/aluno-picker'
import { EntregaCatalogo } from '@/components/pedidos/entrega-catalogo'
import { EntregaLista } from '@/components/pedidos/entrega-lista'
import { SacolaEntrega } from '@/components/pedidos/sacola-entrega'
import { useSaidaProtegida } from '@/components/shared/confirmar-saida'
import { EstadoErro } from '@/components/shared/estados-lista'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useAllAlunosQuery } from '@/hooks/use-alunos'
import { useCreatePedidoMutation } from '@/hooks/use-pedidos'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { agruparPecas, novaLinha, temErros, validarEntrega, type ErrosEntrega, type LinhaEntrega } from '@/lib/entrega'

type Modo = 'lista' | 'catalogo'

export function NovaEntregaPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const modo: Modo = searchParams.get('modo') === 'catalogo' ? 'catalogo' : 'lista'

  const alunos = useAllAlunosQuery()
  const uniformes = useAllUniformesQuery()
  const createMutation = useCreatePedidoMutation()

  const [alunoId, setAlunoId] = useState(() => searchParams.get('aluno') ?? '')
  const [linhas, setLinhas] = useState<LinhaEntrega[]>(() => [novaLinha()])
  const [erros, setErros] = useState<ErrosEntrega | null>(null)
  const [aviso, setAviso] = useState('')

  const grupos = useMemo(() => agruparPecas(uniformes.data ?? []), [uniformes.data])
  const porId = useMemo(() => new Map((uniformes.data ?? []).map((uniforme) => [uniforme.id, uniforme])), [uniformes.data])

  const preenchidas = linhas.filter((linha) => linha.uniformeId)
  const total = preenchidas.reduce((soma, linha) => soma + linha.quantidade, 0)
  const alterado = Boolean(alunoId) || preenchidas.length > 0
  const aluno = alunos.data?.find((candidato) => candidato.id === alunoId)

  /* Depois da primeira tentativa de envio, os erros acompanham a edição. */
  function mudarLinhas(proximas: LinhaEntrega[]) {
    setLinhas(proximas.length ? proximas : [novaLinha()])
    if (erros) setErros(validarEntrega(alunoId, proximas, porId))
  }

  function mudarModo(proximo: Modo) {
    setSearchParams(
      (atual) => {
        const params = new URLSearchParams(atual)
        if (proximo === 'catalogo') params.set('modo', 'catalogo')
        else params.delete('modo')
        return params
      },
      { replace: true },
    )
  }

  const saida = useSaidaProtegida(alterado)
  function registrar() {
    /* Linhas totalmente em branco são ignoradas; peça sem tamanho vira erro na linha. */
    const limpas = linhas.filter((linha) => linha.uniformeId || linha.grupo)
    const resultado = validarEntrega(alunoId, limpas, porId)
    setErros(resultado)

    if (temErros(resultado)) {
      toast.error('Confira os campos destacados antes de registrar.')
      document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
      return
    }

    createMutation.mutate(
      { alunoId, itens: limpas.map(({ uniformeId, quantidade }) => ({ uniformeId, quantidade })) },
      {
        onSuccess: (pedido) => {
          toast.success(aluno ? `Entrega registrada para ${aluno.nome}.` : 'Entrega registrada.')
          saida.liberar()
        navigate(`/pedidos?ver=${pedido.id}`, { replace: true })
        },
        onError: (error: Error) => toast.error(error.message),
      },
    )
  }

  return (
    <AppLayout
      title="Nova entrega"
      description="Escolha o aluno e as peças. A mesma peça pode entrar mais de uma vez, com quantidades diferentes."
      breadcrumb={[{ label: 'Entregas', to: '/pedidos' }, { label: 'Nova entrega' }]}
      footer={
        <>
          <p className="text-sm text-muted-foreground" aria-live="polite" data-numeric>
            {total === 0 ? 'Nenhuma peça selecionada' : `${total} ${total === 1 ? 'peça' : 'peças'}`}
            {aluno && total > 0 && (
              <>
                {' '}
                para <strong className="font-semibold text-foreground">{aluno.nome}</strong>
              </>
            )}
          </p>
          <div className="flex gap-2">
            <ButtonLink variant="outline" size="lg" to="/pedidos">
              Cancelar
            </ButtonLink>
            <Button size="lg" disabled={createMutation.isPending} onClick={registrar}>
              {createMutation.isPending ? 'Registrando…' : 'Registrar entrega'}
            </Button>
          </div>
        </>
      }
    >
      {saida.dialogo}
      <p className="sr-only" aria-live="polite">
        {aviso}
      </p>

      {uniformes.isError && <EstadoErro titulo="Não foi possível carregar o estoque" onRetry={() => uniformes.refetch()} />}

      <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <div className="flex min-w-0 flex-col gap-6">
          <div className="max-w-xl">
            <AlunoPicker
              alunos={alunos.data}
              isLoading={alunos.isLoading}
              isError={alunos.isError}
              value={alunoId}
              onChange={(id) => {
                setAlunoId(id)
                if (erros) setErros(validarEntrega(id, linhas, porId))
              }}
              errorMessage={erros?.aluno}
            />
          </div>

          <Tabs value={modo} onValueChange={(valor) => mudarModo(valor as Modo)} className="gap-4">
            <TabsList aria-label="Forma de lançar">
              <TabsTrigger value="lista" className="px-3">
                <List aria-hidden />
                Lista
              </TabsTrigger>
              <TabsTrigger value="catalogo" className="px-3">
                <LayoutGrid aria-hidden />
                Catálogo
              </TabsTrigger>
            </TabsList>

            {erros?.geral && (
              <p role="alert" className="text-sm text-destructive">
                {erros.geral}
              </p>
            )}

            <TabsContent value="lista">
              <EntregaLista
                linhas={linhas}
                grupos={grupos}
                porId={porId}
                carregando={uniformes.isLoading}
                erros={erros?.linhas ?? {}}
                onChange={mudarLinhas}
              />
            </TabsContent>
            <TabsContent value="catalogo">
              <EntregaCatalogo
                grupos={grupos}
                linhas={linhas}
                onAdicionar={(linha, descricao) => {
                  const semVazias = linhas.filter((existente) => existente.uniformeId)
                  mudarLinhas([...semVazias, novaLinha(linha)])
                  setAviso(`Adicionado: ${descricao}.`)
                  toast.success(`Adicionado: ${descricao}`)
                }}
              />
            </TabsContent>
          </Tabs>
        </div>

        <SacolaEntrega
          linhas={linhas}
          porId={porId}
          mostrarLinhas={modo === 'catalogo'}
          onRemover={(chave) => {
            mudarLinhas(linhas.filter((linha) => linha.chave !== chave))
            setAviso('Linha removida.')
          }}
        />
      </div>
    </AppLayout>
  )
}
