import type { ReactNode } from 'react'
import { MoreHorizontal } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'

export type AcaoLinha = {
  label: string
  icone?: ReactNode
  onSelect: () => void
  destrutiva?: boolean
}

/** As ações secundárias de uma linha ficam num menu “…” com nome acessível próprio. */
export function AcoesLinha({ rotulo, acoes }: { rotulo: string; acoes: AcaoLinha[] }) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger render={<Button variant="ghost" size="icon" aria-label={`Mais ações para ${rotulo}`} />}>
        <MoreHorizontal aria-hidden />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-48">
        {acoes.map((acao) => (
          <DropdownMenuItem
            key={acao.label}
            variant={acao.destrutiva ? 'destructive' : 'default'}
            onClick={acao.onSelect}
          >
            {acao.icone}
            {acao.label}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
