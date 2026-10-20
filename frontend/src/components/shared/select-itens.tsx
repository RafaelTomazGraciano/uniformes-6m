import type { ComponentProps } from 'react'

import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { SelectItemData } from '@/lib/select-items'
import { cn } from '@/lib/utils'

type SelectItensProps = {
  items: SelectItemData[]
  value: string
  onValueChange: (valor: string) => void
  placeholder?: string
  /** Rótulo acessível quando não há <label for> apontando para o gatilho. */
  'aria-label'?: string
  id?: string
  invalido?: boolean
  descritoPor?: string
  className?: string
  disabled?: boolean
} & Pick<ComponentProps<typeof SelectTrigger>, 'size'>

/**
 * O único Select de opções dinâmicas do app. O gatilho sempre mostra o rótulo:
 * passa `items` ao Base UI e ainda resolve o texto à mão, porque o valor pode
 * chegar antes da lista (link "Repor", edição) e aí o Base UI exibiria o UUID.
 */
export function SelectItens({
  items,
  value,
  onValueChange,
  placeholder = 'Selecione',
  id,
  invalido,
  descritoPor,
  className,
  disabled,
  size,
  ...rest
}: SelectItensProps) {
  const rotulo = items.find((item) => item.value === value)?.label

  return (
    <Select items={items} value={value || null} disabled={disabled} onValueChange={(valor) => onValueChange(valor ? String(valor) : '')}>
      <SelectTrigger
        id={id}
        size={size}
        aria-label={rest['aria-label']}
        aria-invalid={invalido || undefined}
        aria-describedby={descritoPor}
        className={cn('h-9 w-full', className)}
      >
        <SelectValue>
          {() => (rotulo ? rotulo : <span className="text-muted-foreground">{value && !items.length ? 'Carregando…' : placeholder}</span>)}
        </SelectValue>
      </SelectTrigger>
      <SelectContent>
        {items.map((item) => (
          <SelectItem key={item.value} value={item.value}>
            {item.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
