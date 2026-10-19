import type { ReactNode } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Navigate, RouterProvider, createBrowserRouter, type RouteObject } from 'react-router-dom'

import { ProtectedRoute } from '@/components/auth/protected-route'
import { Toaster } from '@/components/ui/sonner'
import { TooltipProvider } from '@/components/ui/tooltip'
import { AlunoFormPage } from '@/pages/aluno-form-page'
import { AlunosPage } from '@/pages/alunos-page'
import { ContaPage } from '@/pages/conta-page'
import { DashboardPage } from '@/pages/dashboard-page'
import { EntradaFormPage } from '@/pages/entrada-form-page'
import { EntradasPage } from '@/pages/entradas-page'
import { EstoquePage } from '@/pages/estoque-page'
import { LoginPage } from '@/pages/login-page'
import { NotFoundPage } from '@/pages/not-found-page'
import { NovaEntregaPage } from '@/pages/nova-entrega-page'
import { PedidosPage } from '@/pages/pedidos-page'
import { RegisterPage } from '@/pages/register-page'
import { RelatoriosPage } from '@/pages/relatorios-page'
import { TipoUniformeFormPage } from '@/pages/tipo-uniforme-form-page'
import { TiposUniformePage } from '@/pages/tipos-uniforme-page'
import { TrocarSenhaPage } from '@/pages/trocar-senha-page'
import { TurmaFormPage } from '@/pages/turma-form-page'
import { TurmasPage } from '@/pages/turmas-page'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      staleTime: 1000 * 60,
    },
  },
})

function protegida(path: string, element: ReactNode): RouteObject {
  return { path, element: <ProtectedRoute>{element}</ProtectedRoute>, errorElement: <NotFoundPage /> }
}

const router = createBrowserRouter([
  { path: '/', element: <Navigate to="/login" replace />, errorElement: <NotFoundPage /> },
  { path: '/login', element: <LoginPage />, errorElement: <NotFoundPage /> },
  { path: '/register', element: <RegisterPage />, errorElement: <NotFoundPage /> },
  // Pública: é também o caminho de quem esqueceu a senha e não consegue entrar.
  { path: '/trocar-senha', element: <TrocarSenhaPage />, errorElement: <NotFoundPage /> },

  protegida('/dashboard', <DashboardPage />),

  // Cadastros e edições são páginas inteiras; o detalhe abre num painel sobre a lista (?ver=id).
  protegida('/pedidos', <PedidosPage />),
  protegida('/pedidos/nova', <NovaEntregaPage />),
  protegida('/entradas', <EntradasPage />),
  protegida('/entradas/nova', <EntradaFormPage />),
  protegida('/entradas/:id/editar', <EntradaFormPage />),
  protegida('/estoque', <EstoquePage />),
  protegida('/alunos', <AlunosPage />),
  protegida('/alunos/novo', <AlunoFormPage />),
  protegida('/alunos/:id/editar', <AlunoFormPage />),
  protegida('/turmas', <TurmasPage />),
  protegida('/turmas/nova', <TurmaFormPage />),
  protegida('/turmas/:id/editar', <TurmaFormPage />),
  protegida('/tipos-uniforme', <TiposUniformePage />),
  protegida('/tipos-uniforme/novo', <TipoUniformeFormPage />),
  protegida('/tipos-uniforme/:id/editar', <TipoUniformeFormPage />),
  protegida('/relatorios', <RelatoriosPage />),
  protegida('/conta', <ContaPage />),

  // Links antigos continuam funcionando.
  { path: '/requisicoes/nova', element: <Navigate to="/pedidos/nova" replace /> },

  { path: '*', element: <NotFoundPage /> },
])

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <RouterProvider router={router} />
        <Toaster position="top-right" richColors closeButton />
      </TooltipProvider>
    </QueryClientProvider>
  )
}
