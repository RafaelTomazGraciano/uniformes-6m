import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/use-auth'

export function DashboardPage() {
  const { session, logout } = useAuth()

  return (
    <div className="min-h-screen bg-slate-50 p-6 text-slate-900">
      <div className="mx-auto max-w-5xl">
        <header className="mb-8 flex items-center justify-between rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <div>
            <p className="text-sm font-medium uppercase tracking-[0.2em] text-slate-500">Uniformes</p>
            <h1 className="text-xl font-bold">Dashboard</h1>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-slate-600">{session?.user.email}</span>
            <Button variant="outline" onClick={logout}>
              Sair
            </Button>
          </div>
        </header>

        <main className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-2xl font-semibold">Bem-vindo ao painel</h2>
          <p className="mt-2 text-slate-600">
            Autenticação pronta para evoluir com recursos reais do sistema.
          </p>
        </main>
      </div>
    </div>
  )
}
