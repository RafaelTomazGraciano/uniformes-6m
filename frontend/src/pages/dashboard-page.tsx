import { AppSidebar } from '@/components/app-sidebar'
import { Button } from '@/components/ui/button'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { useAuth } from '@/hooks/use-auth'

export function DashboardPage() {
  const { session, logout } = useAuth()

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-14 items-center gap-3 border-b border-border px-4">
          <SidebarTrigger />
          <h1 className="text-sm font-semibold text-foreground">Dashboard</h1>
          <div className="ml-auto flex items-center gap-3">
            <span className="text-sm text-muted-foreground">{session?.user.email}</span>
            <Button variant="outline" onClick={logout}>
              Sair
            </Button>
          </div>
        </header>

        <main className="m-4 rounded-xl border border-border bg-card p-6 text-card-foreground shadow-sm">
          <h2 className="text-2xl font-semibold">Bem-vindo ao painel</h2>
          <p className="mt-2 text-muted-foreground">
            Autenticação pronta para evoluir com recursos reais do sistema.
          </p>
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}
