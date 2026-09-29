import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Navigate, RouterProvider, createBrowserRouter } from 'react-router-dom'

import { ProtectedRoute } from '@/components/auth/protected-route'
import { Toaster } from '@/components/ui/sonner'
import { TooltipProvider } from '@/components/ui/tooltip'
import { AlunosPage } from '@/pages/alunos-page'
import { ContaPage } from '@/pages/conta-page'
import { DashboardPage } from '@/pages/dashboard-page'
import { EntradasPage } from '@/pages/entradas-page'
import { EstoquePage } from '@/pages/estoque-page'
import { LoginPage } from '@/pages/login-page'
import { NotFoundPage } from '@/pages/not-found-page'
import { PedidosPage } from '@/pages/pedidos-page'
import { RegisterPage } from '@/pages/register-page'
import { RelatoriosPage } from '@/pages/relatorios-page'
import { TrocarSenhaPage } from '@/pages/trocar-senha-page'
import { TiposUniformePage } from '@/pages/tipos-uniforme-page'
import { TurmasPage } from '@/pages/turmas-page'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      staleTime: 1000 * 60,
    },
  },
})

const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/login" replace />,
    errorElement: <NotFoundPage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
    errorElement: <NotFoundPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
    errorElement: <NotFoundPage />,
  },
  {
    path: '/dashboard',
    element: (
      <ProtectedRoute>
        <DashboardPage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    path: '/alunos',
    element: (
      <ProtectedRoute>
        <AlunosPage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    path: '/pedidos',
    element: (
      <ProtectedRoute>
        <PedidosPage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    // A entrega virou diálogo dentro de Entregas; o link antigo continua funcionando.
    path: '/requisicoes/nova',
    element: <Navigate to="/pedidos?nova=1" replace />,
    errorElement: <NotFoundPage />,
  },
  {
    path: '/estoque',
    element: (
      <ProtectedRoute>
        <EstoquePage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    path: '/entradas',
    element: (
      <ProtectedRoute>
        <EntradasPage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    path: '/turmas',
    element: (
      <ProtectedRoute>
        <TurmasPage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    path: '/tipos-uniforme',
    element: (
      <ProtectedRoute>
        <TiposUniformePage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    path: '/relatorios',
    element: (
      <ProtectedRoute>
        <RelatoriosPage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    path: '/conta',
    element: (
      <ProtectedRoute>
        <ContaPage />
      </ProtectedRoute>
    ),
    errorElement: <NotFoundPage />,
  },
  {
    // Pública: é também o caminho de quem esqueceu a senha e não consegue entrar.
    path: '/trocar-senha',
    element: <TrocarSenhaPage />,
    errorElement: <NotFoundPage />,
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
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
