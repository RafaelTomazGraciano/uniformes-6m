import type { ReactNode } from 'react'
import { AlertTriangle, RotateCw } from 'lucide-react'

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'

type EstadoVazioProps = {
  icone: ReactNode
  titulo: string
  descricao: ReactNode
  acao?: ReactNode
  /** "positivo" para vazios que são boa notícia ("Tudo em dia"). */
  tom?: 'neutro' | 'positivo'
  className?: string
}

/** Todo vazio responde: o que é esta tela, por que está vazia e qual o próximo passo. */
export function EstadoVazio({ icone, titulo, descricao, acao, tom = 'neutro', className }: EstadoVazioProps) {
  return (
    <Empty className={cn('rounded-xl border border-border bg-card py-12', className)}>
      <EmptyHeader>
        <EmptyMedia
          variant="icon"
          className={cn('size-12 rounded-xl [&_svg]:size-6', tom === 'positivo' && 'bg-signal-ok-surface text-signal-ok')}
        >
          {icone}
        </EmptyMedia>
        <EmptyTitle className="text-base">{titulo}</EmptyTitle>
        <EmptyDescription>{descricao}</EmptyDescription>
      </EmptyHeader>
      {acao && <EmptyContent className="flex-row flex-wrap justify-center">{acao}</EmptyContent>}
    </Empty>
  )
}

export function EstadoErro({ titulo, onRetry }: { titulo: string; onRetry?: () => void }) {
  return (
    <Alert variant="destructive" className="items-start">
      <AlertTriangle aria-hidden />
      <AlertTitle>{titulo}</AlertTitle>
      <AlertDescription className="flex flex-col items-start gap-3">
        <span>O servidor não respondeu. Confira a conexão e tente de novo.</span>
        {onRetry && (
          <Button variant="outline" size="sm" onClick={onRetry}>
            <RotateCw aria-hidden />
            Tentar de novo
          </Button>
        )}
      </AlertDescription>
    </Alert>
  )
}

/** Skeleton no formato das colunas reais, para a página não "pular" quando os dados chegam. */
export function EstadoCarregando({ linhas = 5, colunas = 3, rotulo }: { linhas?: number; colunas?: number; rotulo: string }) {
  return (
    <div className="overflow-hidden rounded-xl border border-border bg-card" aria-busy="true" role="status">
      <span className="sr-only">{rotulo}</span>
      {Array.from({ length: linhas }).map((_, linha) => (
        <div
          key={linha}
          className="grid gap-4 border-b border-border px-4 py-4 last:border-0"
          style={{ gridTemplateColumns: `2fr repeat(${Math.max(colunas - 1, 0)}, 1fr)` }}
        >
          {Array.from({ length: colunas }).map((__, coluna) => (
            <Skeleton key={coluna} className="h-4" style={{ width: `${55 + ((linha * 7 + coluna * 13) % 35)}%` }} />
          ))}
        </div>
      ))}
    </div>
  )
}
