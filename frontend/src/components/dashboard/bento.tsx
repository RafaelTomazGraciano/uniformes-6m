import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

import { Skeleton } from '@/components/ui/skeleton'
import { formatarNumero } from '@/lib/format'
import { cn } from '@/lib/utils'

/**
 * Doze colunas no desktop, seis no tablet, duas no telefone. Cada nicho tem o
 * tamanho do peso do que guarda — como as prateleiras de um almoxarifado.
 */
export function BentoGrid({ className, children }: { className?: string; children: ReactNode }) {
  return (
    <div className={cn('grid grid-cols-2 gap-4 md:grid-cols-6 lg:grid-cols-12', className)}>{children}</div>
  )
}

/** Um nicho: peça solta, com borda própria e canto arredondado. */
export function BentoTile({ className, children }: { className?: string; children: ReactNode }) {
  return (
    <div
      className={cn(
        'col-span-2 flex min-w-0 flex-col overflow-hidden rounded-xl border border-border bg-card',
        className,
      )}
    >
      {children}
    </div>
  )
}

type BentoFiguraProps = {
  valor: number
  rotulo: string
  para: string
  isLoading: boolean
  /** Tinge o nicho quando há algo a resolver. Só isso justifica cor aqui. */
  alerta?: boolean
  className?: string
}

export function BentoFigura({ valor, rotulo, para, isLoading, alerta, className }: BentoFiguraProps) {
  const emAlerta = alerta === true && valor > 0

  return (
    <BentoTile
      className={cn('col-span-1', emAlerta && 'border-signal-out/35 bg-signal-out-surface', className)}
    >
      <Link
        to={para}
        className="group flex h-full flex-col justify-center px-5 py-4 outline-none focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-ring"
      >
        {isLoading ? (
          <Skeleton className="h-7 w-16" />
        ) : (
          <p
            data-numeric
            className={cn(
              'text-[1.75rem] leading-none font-semibold tracking-tight',
              emAlerta ? 'text-signal-out' : 'text-foreground',
            )}
          >
            {formatarNumero(valor)}
          </p>
        )}
        <p
          className={cn(
            'mt-2 text-sm underline-offset-4 group-hover:underline',
            emAlerta ? 'text-signal-out' : 'text-muted-foreground group-hover:text-foreground',
          )}
        >
          {rotulo}
        </p>
      </Link>
    </BentoTile>
  )
}
