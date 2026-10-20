import type { ReactElement } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import { RouterProvider, createMemoryRouter } from 'react-router-dom'

import { AuthProvider } from '@/context/auth-context'

type Opcoes = {
  /** URL inicial, com query string (ex.: "/alunos?turma=t1"). */
  rota?: string
  /** Padrão de rota do elemento, para telas que leem `useParams` (ex.: "/alunos/:id/editar"). */
  caminho?: string
}

/* Data router (e não MemoryRouter): os formulários usam `useBlocker` para
   perguntar antes de sair com alterações não salvas. */
export function renderWithProviders(ui: ReactElement, { rota = '/', caminho = '*' }: Opcoes = {}) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })

  const router = createMemoryRouter(
    [
      { path: caminho, element: <AuthProvider>{ui}</AuthProvider> },
      { path: '*', element: <AuthProvider>{null}</AuthProvider> },
    ],
    { initialEntries: [rota] },
  )

  const resultado = render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
  return { ...resultado, router }
}
