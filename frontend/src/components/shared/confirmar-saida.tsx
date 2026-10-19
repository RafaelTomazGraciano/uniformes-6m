import { useRef } from 'react'
import { useBlocker } from 'react-router-dom'

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

/**
 * Protege um formulário com alterações não salvas. `liberar()` precisa ser
 * chamado antes do `navigate` pós-salvamento: é síncrono (ref), enquanto um
 * setState só valeria no próximo render e o bloqueio pegaria o próprio redirect.
 */
export function useSaidaProtegida(alterado: boolean) {
  const liberado = useRef(false)
  const blocker = useBlocker(
    ({ currentLocation, nextLocation }) =>
      alterado && !liberado.current && currentLocation.pathname !== nextLocation.pathname,
  )

  return {
    liberar: () => {
      liberado.current = true
    },
    dialogo: (
      <AlertDialog open={blocker.state === 'blocked'} onOpenChange={(open) => !open && blocker.reset?.()}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Sair sem salvar?</AlertDialogTitle>
            <AlertDialogDescription>O que você preencheu nesta tela será perdido.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Continuar editando</AlertDialogCancel>
            <AlertDialogAction variant="destructive" onClick={() => blocker.proceed?.()}>
              Sair sem salvar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    ),
  }
}
