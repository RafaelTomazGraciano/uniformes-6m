import { Search, X } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useTurmasQuery } from '@/hooks/use-turmas'
import { turnoLabels } from '@/lib/labels'

/* O Select não distingue "sem valor" de "valor vazio", então a opção de não
   filtrar precisa de um valor próprio. */
export const TODAS_AS_TURMAS = 'todas'

type AlunosFiltrosProps = {
  busca: string
  onBuscaChange: (busca: string) => void
  turmaId: string
  onTurmaChange: (turmaId: string) => void
}

export function AlunosFiltros({ busca, onBuscaChange, turmaId, onTurmaChange }: AlunosFiltrosProps) {
  const { data: turmas, isLoading } = useTurmasQuery()
  const temFiltro = busca.trim() !== '' || turmaId !== TODAS_AS_TURMAS

  return (
    <div className="mb-4 flex flex-wrap items-end gap-3">
      <div className="flex min-w-56 flex-1 flex-col gap-2">
        <Label htmlFor="busca-aluno">Buscar</Label>
        <div className="relative">
          <Search
            aria-hidden
            className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground"
          />
          <Input
            id="busca-aluno"
            type="search"
            autoComplete="off"
            className="pl-9"
            placeholder="Nome do aluno ou turma"
            value={busca}
            onChange={(event) => onBuscaChange(event.target.value)}
          />
        </div>
      </div>

      <div className="flex w-52 flex-col gap-2">
        <Label htmlFor="filtro-turma">Turma</Label>
        <Select value={turmaId} onValueChange={(valor) => onTurmaChange(valor ?? TODAS_AS_TURMAS)}>
          <SelectTrigger id="filtro-turma" className="w-full">
            <SelectValue placeholder={isLoading ? 'Carregando turmas...' : 'Todas as turmas'} />
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

      {temFiltro && (
        <Button
          variant="ghost"
          onClick={() => {
            onBuscaChange('')
            onTurmaChange(TODAS_AS_TURMAS)
          }}
        >
          <X className="size-4" />
          Limpar
        </Button>
      )}
    </div>
  )
}
