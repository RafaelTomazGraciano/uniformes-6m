import { LogOut } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { formatarDataCompleta } from '@/lib/format'
import type { Aluno } from '@/lib/types/aluno'

/** "Saiu em 15/03/2026" para quem já teve a saída registrada; nada para alunos ativos. */
export function BadgeSaida({ aluno, className }: { aluno: Aluno; className?: string }) {
  if (aluno.ativo !== false) return null

  return (
    <Badge variant="outline" className={className}>
      <LogOut aria-hidden />
      {aluno.dataSaida ? `Saiu em ${formatarDataCompleta(aluno.dataSaida)}` : 'Saiu'}
    </Badge>
  )
}
