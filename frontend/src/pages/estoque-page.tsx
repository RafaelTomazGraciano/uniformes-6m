import { useState } from 'react'

import { AppLayout } from '@/components/layout/app-layout'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useUniformesQuery } from '@/hooks/use-uniformes'
import { nivelEstoque } from '@/lib/estoque'
import { sexoLabels } from '@/lib/labels'

const situacao = {
  esgotado: { variant: 'out', texto: 'Esgotado' },
  baixo: { variant: 'low', texto: 'Baixo' },
  ok: { variant: 'ok', texto: 'Disponível' },
} as const

export function EstoquePage() {
  const [page, setPage] = useState(0)
  const { data, isLoading, isError } = useUniformesQuery({ page })

  return (
    <AppLayout title="Estoque" description="Cada combinação de tipo, tamanho e sexo, com o que resta de cada uma.">
      <div className="rounded-xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Tipo de uniforme</TableHead>
              <TableHead>Tamanho</TableHead>
              <TableHead>Sexo</TableHead>
              <TableHead className="text-right">Quantidade</TableHead>
              <TableHead className="w-32 text-right">Situação</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading &&
              Array.from({ length: 5 }).map((_, index) => (
                <TableRow key={index}>
                  <TableCell colSpan={5}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))}

            {isError && (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-destructive">
                  Não foi possível carregar o estoque. Tente novamente mais tarde.
                </TableCell>
              </TableRow>
            )}

            {!isLoading && !isError && data?.content.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} className="py-10 text-center text-sm text-muted-foreground">
                  Nenhum uniforme em estoque ainda. Quando um lote der entrada, os tamanhos aparecem aqui.
                </TableCell>
              </TableRow>
            )}

            {data?.content.map((uniforme) => {
              const nivel = situacao[nivelEstoque(uniforme.quantidade)]

              return (
                <TableRow key={uniforme.id}>
                  <TableCell className="font-medium">{uniforme.tipoUniformeNome}</TableCell>
                  <TableCell>{uniforme.tamanho}</TableCell>
                  <TableCell>{sexoLabels[uniforme.sexo]}</TableCell>
                  <TableCell className="text-right">{uniforme.quantidade}</TableCell>
                  <TableCell className="text-right">
                    <Badge variant={nivel.variant} className="ml-auto">
                      {nivel.texto}
                    </Badge>
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
          <span>
            Página {data.number + 1} de {data.totalPages}
          </span>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled={data.first} onClick={() => setPage((p) => p - 1)}>
              Anterior
            </Button>
            <Button variant="outline" size="sm" disabled={data.last} onClick={() => setPage((p) => p + 1)}>
              Próxima
            </Button>
          </div>
        </div>
      )}
    </AppLayout>
  )
}
