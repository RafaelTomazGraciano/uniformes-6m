import { Link } from 'react-router-dom'

import { BentoFigura, BentoGrid } from '@/components/dashboard/bento'
import { EntregasRecentes } from '@/components/dashboard/entregas-recentes'
import { ListaReposicao } from '@/components/dashboard/lista-reposicao'
import { MatrizEstoque } from '@/components/dashboard/matriz-estoque'
import { AppLayout } from '@/components/layout/app-layout'
import { Button } from '@/components/ui/button'
import { useAlunosQuery } from '@/hooks/use-alunos'
import { usePedidosQuery } from '@/hooks/use-pedidos'
import { useAllUniformesQuery } from '@/hooks/use-uniformes'
import { contarEntregasNoMes, resumoEstoque } from '@/lib/estoque'
import { nomeDoMes } from '@/lib/format'

export function DashboardPage() {
  const uniformes = useAllUniformesQuery()
  const pedidos = usePedidosQuery({ size: 50, sort: 'dataEfetivada,desc' })
  const alunos = useAlunosQuery({ size: 1, sort: 'nome,asc' })

  const resumo = resumoEstoque(uniformes.data ?? [])
  const entregasNoMes = contarEntregasNoMes(pedidos.data?.content ?? [])
  const carregandoFiguras = uniformes.isLoading || pedidos.isLoading

  return (
    <AppLayout
      title="Visão geral"
      description="O que há no almoxarifado, o que falta e o que saiu."
      headerActions={
        <Button asChild>
          <Link to="/pedidos?nova=1">Nova entrega</Link>
        </Button>
      }
    >
      {/*
        Doze colunas no desktop. O nicho maior é da matriz de tamanhos, que é o
        dado mais específico do almoxarifado. Os esgotados ficam na mesma vertical
        de "Repor primeiro", porque um é a contagem do outro.
      */}
      <BentoGrid>
        <BentoFigura
          valor={resumo.pecas}
          rotulo="peças em estoque"
          para="/estoque"
          isLoading={carregandoFiguras}
          className="md:col-span-3"
        />
        <BentoFigura
          valor={entregasNoMes}
          rotulo={`entregas em ${nomeDoMes()}`}
          para="/pedidos"
          isLoading={carregandoFiguras}
          className="md:col-span-3"
        />
        <BentoFigura
          valor={alunos.data?.totalElements ?? 0}
          rotulo="alunos cadastrados"
          para="/alunos"
          isLoading={alunos.isLoading}
          className="md:col-span-3"
        />
        <BentoFigura
          valor={resumo.esgotados}
          rotulo="tamanhos esgotados"
          para="/estoque"
          isLoading={carregandoFiguras}
          alerta
          className="md:col-span-3"
        />

        <MatrizEstoque
          uniformes={uniformes.data}
          isLoading={uniformes.isLoading}
          isError={uniformes.isError}
          className="md:col-span-6 lg:col-span-8"
        />

        <ListaReposicao
          uniformes={uniformes.data}
          isLoading={uniformes.isLoading}
          className="md:col-span-6 lg:col-span-4"
        />

        <EntregasRecentes
          pedidos={pedidos.data?.content}
          isLoading={pedidos.isLoading}
          className="md:col-span-6 lg:col-span-12"
        />
      </BentoGrid>
    </AppLayout>
  )
}
