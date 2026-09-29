import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Download } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { FiltroPeriodo } from '@/components/relatorios/filtro-periodo'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useTurmasQuery } from '@/hooks/use-turmas'
import { ApiError } from '@/lib/api-client'
import { turnoLabels } from '@/lib/labels'
import { periodoSchema, type PeriodoFormValues } from '@/lib/schemas/relatorio'
import {
  baixarRelatorioComPeriodo,
  baixarRelatorioEstoque,
  salvarArquivo,
  type RelatorioComPeriodo,
} from '@/services/relatorio-service'

/* O Select não distingue "sem valor" de "valor vazio", então a opção de não
   filtrar precisa de um valor próprio. */
const TODAS_AS_TURMAS = 'todas'

type Relatorio = {
  chave: RelatorioComPeriodo | 'estoque'
  titulo: string
  descricao: string
  usaPeriodo: boolean
  usaTurma?: boolean
}

const RELATORIOS: Relatorio[] = [
  {
    chave: 'estoque',
    titulo: 'Estoque',
    descricao: 'A situação de cada tamanho agora. Não depende do período.',
    usaPeriodo: false,
  },
  {
    chave: 'entrada',
    titulo: 'Entradas',
    descricao: 'As peças recebidas dos fornecedores no período.',
    usaPeriodo: true,
  },
  {
    chave: 'saida',
    titulo: 'Saídas',
    descricao: 'Os uniformes entregues aos alunos no período.',
    usaPeriodo: true,
  },
  {
    chave: 'entregas-turma',
    titulo: 'Entregas por turma',
    descricao: 'Quem recebeu o quê, turma a turma, com o subtotal de cada uma.',
    usaPeriodo: true,
    usaTurma: true,
  },
  {
    chave: 'transacoes',
    titulo: 'Transações',
    descricao: 'Entradas e saídas lado a lado, com o saldo do período.',
    usaPeriodo: true,
  },
]

export function RelatoriosPage() {
  const [baixando, setBaixando] = useState<string | null>(null)
  const [turmaId, setTurmaId] = useState(TODAS_AS_TURMAS)

  const { data: turmas, isLoading: carregandoTurmas } = useTurmasQuery()

  const form = useForm<PeriodoFormValues>({
    resolver: zodResolver(periodoSchema),
    defaultValues: {
      tipo: 'MES',
      anoInicio: new Date().getFullYear(),
      mesInicio: new Date().getMonth() + 1,
    },
  })

  async function baixar(relatorio: Relatorio, periodo?: PeriodoFormValues) {
    setBaixando(relatorio.chave)

    try {
      const turma = relatorio.usaTurma && turmaId !== TODAS_AS_TURMAS ? turmaId : undefined
      const blob =
        relatorio.chave === 'estoque'
          ? await baixarRelatorioEstoque()
          : await baixarRelatorioComPeriodo(relatorio.chave, periodo!, turma)

      salvarArquivo(blob, `relatorio-${relatorio.chave}.pdf`)
      toast.success(`Relatório de ${relatorio.titulo.toLowerCase()} baixado.`)
    } catch (error) {
      /* 404 aqui não é falha: é o backend dizendo que não houve movimento no
         período. Tratar como erro vermelho faria o usuário procurar um defeito. */
      if (error instanceof ApiError && error.status === 404) {
        toast.info(error.message)
        return
      }

      toast.error(error instanceof Error ? error.message : 'Não foi possível gerar o relatório.')
    } finally {
      setBaixando(null)
    }
  }

  function aoClicar(relatorio: Relatorio) {
    if (!relatorio.usaPeriodo) {
      void baixar(relatorio)
      return
    }

    // Os campos de período são compartilhados: valida uma vez, na hora de usar.
    void form.handleSubmit((periodo) => baixar(relatorio, periodo))()
  }

  return (
    <AppLayout title="Relatórios" description="Documentos em PDF para prestação de contas e conferência.">
      <div className="space-y-4">
        <FiltroPeriodo form={form} />

        <div className="grid gap-4 sm:grid-cols-2">
          {RELATORIOS.map((relatorio) => (
            <Card key={relatorio.chave} className="flex flex-col">
              <CardHeader>
                <CardTitle>{relatorio.titulo}</CardTitle>
                <CardDescription>{relatorio.descricao}</CardDescription>
              </CardHeader>

              <CardContent className="mt-auto space-y-4 pb-6">
                {/* A turma é do relatório de entregas, não do período: fica dentro do card. */}
                {relatorio.usaTurma && (
                  <div className="flex max-w-xs flex-col gap-2">
                    <Label htmlFor="turma-relatorio">Turma</Label>
                    <Select value={turmaId} onValueChange={(valor) => setTurmaId(valor ?? TODAS_AS_TURMAS)}>
                      <SelectTrigger id="turma-relatorio" className="w-full">
                        <SelectValue placeholder={carregandoTurmas ? 'Carregando turmas...' : 'Todas as turmas'} />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value={TODAS_AS_TURMAS}>Todas as turmas</SelectItem>
                        {turmas?.map((turma) => (
                          <SelectItem key={turma.id} value={turma.id}>
                            {turma.nome} — {turnoLabels[turma.turno]}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                )}

                <Button
                  variant={relatorio.usaPeriodo ? 'default' : 'outline'}
                  disabled={baixando !== null}
                  onClick={() => aoClicar(relatorio)}
                >
                  <Download className="size-4" />
                  {baixando === relatorio.chave ? 'Gerando...' : 'Baixar PDF'}
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </AppLayout>
  )
}
