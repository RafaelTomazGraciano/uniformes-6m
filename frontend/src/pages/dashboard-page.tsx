import { AppLayout } from '@/components/layout/app-layout'

export function DashboardPage() {
  return (
    <AppLayout title="Dashboard">
      <div className="rounded-xl border border-border bg-card p-6 text-card-foreground shadow-sm">
        <h2 className="text-2xl font-semibold">Bem-vindo ao painel</h2>
        <p className="mt-2 text-muted-foreground">
          Autenticação pronta para evoluir com recursos reais do sistema.
        </p>
      </div>
    </AppLayout>
  )
}
