import { ChevronLeft, ChevronRight } from 'lucide-react'

import { Button } from '@/components/ui/button'

type PaginacaoProps = {
  /** Página atual, começando em 0 como no Spring. */
  pagina: number
  totalPaginas: number
  totalItens: number
  porPagina: number
  onChange: (pagina: number) => void
  rotuloItens?: [singular: string, plural: string]
}

export function Paginacao({
  pagina,
  totalPaginas,
  totalItens,
  porPagina,
  onChange,
  rotuloItens = ['item', 'itens'],
}: PaginacaoProps) {
  if (totalItens === 0) return null

  const inicio = pagina * porPagina + 1
  const fim = Math.min(totalItens, (pagina + 1) * porPagina)
  const paginas = paginasVisiveis(pagina, totalPaginas)

  return (
    <nav aria-label="Paginação" className="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm">
      <p className="text-muted-foreground" data-numeric>
        Mostrando {inicio}–{fim} de {totalItens} {totalItens === 1 ? rotuloItens[0] : rotuloItens[1]}
      </p>
      {totalPaginas > 1 && (
        <ul className="flex items-center gap-1">
          <li>
            <Button
              variant="ghost"
              size="icon"
              disabled={pagina === 0}
              onClick={() => onChange(pagina - 1)}
              aria-label="Página anterior"
            >
              <ChevronLeft aria-hidden />
            </Button>
          </li>
          {paginas.map((numero, index) =>
            numero === null ? (
              <li key={`reticencias-${index}`} aria-hidden className="px-1 text-muted-foreground">
                …
              </li>
            ) : (
              <li key={numero}>
                <Button
                  variant={numero === pagina ? 'outline' : 'ghost'}
                  size="icon"
                  aria-current={numero === pagina ? 'page' : undefined}
                  aria-label={`Página ${numero + 1}`}
                  onClick={() => onChange(numero)}
                  data-numeric
                >
                  {numero + 1}
                </Button>
              </li>
            ),
          )}
          <li>
            <Button
              variant="ghost"
              size="icon"
              disabled={pagina >= totalPaginas - 1}
              onClick={() => onChange(pagina + 1)}
              aria-label="Próxima página"
            >
              <ChevronRight aria-hidden />
            </Button>
          </li>
        </ul>
      )}
    </nav>
  )
}

/** Primeira, última e as vizinhas da atual; o resto vira reticências. */
function paginasVisiveis(atual: number, total: number): (number | null)[] {
  const resultado: (number | null)[] = []
  for (let numero = 0; numero < total; numero++) {
    if (numero === 0 || numero === total - 1 || Math.abs(numero - atual) <= 1) {
      resultado.push(numero)
    } else if (resultado[resultado.length - 1] !== null) {
      resultado.push(null)
    }
  }
  return resultado
}
