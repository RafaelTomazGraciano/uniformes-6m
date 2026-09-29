import type { ReactElement, ReactNode } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'

import { AuthProvider } from '@/context/auth-context'

export function renderWithProviders(ui: ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <AuthProvider>{children}</AuthProvider>
        </MemoryRouter>
      </QueryClientProvider>
    )
  }

  return render(ui, { wrapper: Wrapper })
}
