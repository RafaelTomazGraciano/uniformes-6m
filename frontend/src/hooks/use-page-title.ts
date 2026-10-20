import { useEffect } from 'react'

const APLICACAO = 'Uniformes 6M'

/** WCAG 2.4.2: cada rota anuncia o próprio título ao leitor de tela e na aba. */
export function usePageTitle(titulo: string) {
  useEffect(() => {
    document.title = titulo ? `${titulo} · ${APLICACAO}` : APLICACAO
  }, [titulo])
}
