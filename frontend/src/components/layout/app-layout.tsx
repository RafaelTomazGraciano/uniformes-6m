import type { ReactNode } from 'react'

import { AppSidebar } from '@/components/app-sidebar'
import { Button } from '@/components/ui/button'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { useAuth } from '@/hooks/use-auth'

type AppLayoutProps = {
  title: string
  headerActions?: ReactNode
  children: ReactNode
}

export function AppLayout({ title, headerActions, children }: AppLayoutProps) {
  const { session, logout } = useAuth()

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-14 items-center gap-3 border-b border-border px-4">
          <SidebarTrigger />
          <h1 className="text-sm font-semibold text-foreground">{title}</h1>
          <div className="ml-auto flex items-center gap-3">
            {headerActions}
            <span className="text-sm text-muted-foreground">{session?.user.email}</span>
            <Button variant="outline" onClick={logout}>
              Sair
            </Button>
          </div>
        </header>

        <main className="m-4">{children}</main>
      </SidebarInset>
    </SidebarProvider>
  )
}
