import { Navigate } from 'react-router-dom'

import { RegisterForm } from '@/components/auth/register-form'
import { useAuth } from '@/hooks/use-auth'

export function RegisterPage() {
  const { isAuthenticated } = useAuth()

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  return (
    <div className="dark min-h-screen bg-background p-4 md:p-8">
      <div className="mx-auto flex min-h-[calc(100vh-2rem)] w-full max-w-3xl items-center justify-center md:min-h-[calc(100vh-4rem)]">
        <div className="relative w-full overflow-hidden rounded-3xl bg-background shadow-[0_30px_60px_rgba(0,0,0,0.2)]">
          <div className="m-4 rounded-xl bg-card md:m-8">
            <section className="flex items-center p-6 md:p-8">
              <div className="w-full px-1 md:px-2">
                <p className="mb-2 text-sm text-muted-foreground">Crie sua conta para acessar o sistema.</p>
                <h1 className="mb-8 text-4xl font-semibold text-foreground md:text-[40px]">Cadastro</h1>
                <RegisterForm />
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>
  )
}
