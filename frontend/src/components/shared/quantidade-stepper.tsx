import { Minus, Plus } from 'lucide-react'

import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from '@/components/ui/input-group'
import { cn } from '@/lib/utils'

type QuantidadeStepperProps = {
  valor: number
  onChange: (valor: number) => void
  /** Sem máximo, o + não trava (entrada de estoque). */
  maximo?: number
  rotulo: string
  invalido?: boolean
  descritoPor?: string
  disabled?: boolean
  className?: string
}

export function QuantidadeStepper({
  valor,
  onChange,
  maximo,
  rotulo,
  invalido,
  descritoPor,
  disabled,
  className,
}: QuantidadeStepperProps) {
  const limitar = (numero: number) => Math.max(1, maximo === undefined ? numero : Math.min(maximo, numero))

  return (
    <InputGroup className={cn('h-9 w-32', className)} data-disabled={disabled || undefined}>
      <InputGroupAddon>
        <InputGroupButton
          size="icon-sm"
          aria-label={`Diminuir ${rotulo.toLowerCase()}`}
          disabled={disabled || valor <= 1}
          onClick={() => onChange(limitar(valor - 1))}
        >
          <Minus aria-hidden />
        </InputGroupButton>
      </InputGroupAddon>
      <InputGroupInput
        inputMode="numeric"
        aria-label={rotulo}
        aria-invalid={invalido || undefined}
        aria-describedby={descritoPor}
        disabled={disabled}
        className="text-center tabular-nums"
        value={valor || ''}
        onChange={(event) => {
          /* Vazio é permitido enquanto se digita; ao sair do campo volta a 1. */
          const numero = Number.parseInt(event.target.value.replace(/\D/g, ''), 10)
          onChange(Number.isNaN(numero) ? 0 : limitar(numero))
        }}
        onBlur={() => {
          if (!valor) onChange(1)
        }}
      />
      <InputGroupAddon align="inline-end">
        <InputGroupButton
          size="icon-sm"
          aria-label={`Aumentar ${rotulo.toLowerCase()}`}
          disabled={disabled || (maximo !== undefined && valor >= maximo)}
          onClick={() => onChange(limitar(valor + 1))}
        >
          <Plus aria-hidden />
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>
  )
}
