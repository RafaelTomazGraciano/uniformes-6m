import { Fragment, useEffect, useRef, type ReactNode } from 'react'
import { Link } from 'react-router-dom'

import { AppSidebar } from '@/components/app-sidebar'
import { ThemeToggle } from '@/components/theme-toggle'
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { usePageTitle } from '@/hooks/use-page-title'
import { formatarNumero } from '@/lib/format'

export type Migalha = { label: string; to?: string }

type AppLayoutProps = {
  title: string
  description?: string
  /** Total de registros, mostrado ao lado do título das listas ("Alunos · 248"). */
  count?: number
  breadcrumb?: Migalha[]
  headerActions?: ReactNode
  /** Barra fixa no rodapé com as ações do formulário (FormPage). */
  footer?: ReactNode
  children: ReactNode
}

export function AppLayout({ title, description, count, breadcrumb, headerActions, footer, children }: AppLayoutProps) {
  const tituloRef = useRef<HTMLHeadingElement>(null)
  usePageTitle(title)

  /* WCAG 2.4.3: numa SPA a troca de rota não move o foco; levá-lo ao h1 faz o
     leitor de tela anunciar a página nova em vez de continuar no menu. */
  useEffect(() => {
    tituloRef.current?.focus({ preventScroll: true })
  }, [title])

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="sticky top-0 z-10 flex h-14 shrink-0 items-center gap-2 border-b border-border bg-background/85 px-4 backdrop-blur-sm">
          <SidebarTrigger />
          <span className="text-sm font-semibold md:hidden">Uniformes 6M</span>
          <div className="ml-auto">
            <ThemeToggle />
          </div>
        </header>

        <div id="conteudo" className="mx-auto flex w-full max-w-[88rem] flex-1 flex-col px-4 py-6 md:px-6 md:py-8">
          {breadcrumb && breadcrumb.length > 0 && (
            <Breadcrumb className="mb-3">
              <BreadcrumbList>
                {breadcrumb.map((migalha, index) => (
                  <Fragment key={migalha.label}>
                    {index > 0 && <BreadcrumbSeparator />}
                    <BreadcrumbItem>
                      {migalha.to ? (
                        <BreadcrumbLink render={<Link to={migalha.to} />}>{migalha.label}</BreadcrumbLink>
                      ) : (
                        <BreadcrumbPage>{migalha.label}</BreadcrumbPage>
                      )}
                    </BreadcrumbItem>
                  </Fragment>
                ))}
              </BreadcrumbList>
            </Breadcrumb>
          )}

          <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
            <div className="min-w-0">
              <h1
                ref={tituloRef}
                tabIndex={-1}
                className="text-2xl font-semibold tracking-tight text-foreground outline-none"
              >
                {title}
                {count !== undefined && (
                  <span className="font-normal text-muted-foreground" data-numeric>
                    {' '}
                    · {formatarNumero(count)}
                  </span>
                )}
              </h1>
              {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
            </div>
            {headerActions && <div className="flex flex-wrap items-center gap-2">{headerActions}</div>}
          </div>

          {children}
        </div>

        {footer && (
          <div className="sticky bottom-0 z-10 border-t border-border bg-background/95 backdrop-blur-sm">
            <div className="mx-auto flex w-full max-w-[88rem] flex-wrap items-center justify-between gap-3 px-4 py-3 md:px-6">
              {footer}
            </div>
          </div>
        )}
      </SidebarInset>
    </SidebarProvider>
  )
}
