import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Download } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import { FiltroPeriodo } from '@/components/relatorios/filtro-periodo'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { ApiError } from '@/lib/api-client'
import { periodoSchema, type PeriodoFormValues } from '@/lib/schemas/relatorio'
import {
  baixarRelatorioComPeriodo,
  baixarRelatorioEstoque,
  salvarArquivo,
  type RelatorioComPeriodo,
} from '@/services/relatorio-service'

type Relatorio = {
  chave: RelatorioComPeriodo | 'estoque'
  titulo: string
  descricao: string
  usaPeriodo: boolean
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
    chave: 'transacoes',
    titulo: 'Transações',
    descricao: 'Entradas e saídas lado a lado, com o saldo do período.',
    usaPeriodo: true,
  },
]

export function RelatoriosPage() {
  const [baixando, setBaixando] = useState<string | null>(null)

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
      const blob =
        relatorio.chave === 'estoque'
          ? await baixarRelatorioEstoque()
          : await baixarRelatorioComPeriodo(relatorio.chave, periodo!)

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

              <CardContent className="mt-auto pb-6">
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
