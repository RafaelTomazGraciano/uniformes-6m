import {
  Boxes,
  ClipboardList,
  FileText,
  GraduationCap,
  LayoutDashboard,
  LogOut,
  PackagePlus,
  Shirt,
  UserCog,
  Users,
} from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'
import { useAuth } from '@/hooks/use-auth'

const grupos = [
  {
    label: null,
    itens: [{ title: 'Visão geral', url: '/dashboard', icon: LayoutDashboard }],
  },
  {
    label: 'Almoxarifado',
    itens: [
      // Na ordem do fluxo real: a peça entra, depois sai.
      { title: 'Entradas', url: '/entradas', icon: PackagePlus },
      { title: 'Entregas', url: '/pedidos', icon: ClipboardList },
      { title: 'Estoque', url: '/estoque', icon: Boxes },
    ],
  },
  {
    label: 'Cadastros',
    itens: [
      { title: 'Alunos', url: '/alunos', icon: Users },
      { title: 'Turmas', url: '/turmas', icon: GraduationCap },
      { title: 'Tipos de uniforme', url: '/tipos-uniforme', icon: Shirt },
    ],
  },
  {
    label: 'Prestação de contas',
    itens: [{ title: 'Relatórios', url: '/relatorios', icon: FileText }],
  },
]

export function AppSidebar() {
  const { session, logout } = useAuth()
  const location = useLocation()

  return (
    <Sidebar>
      {/* Mesma altura do header do conteúdo (h-14) para as duas linhas de borda casarem. */}
      <SidebarHeader className="h-14 shrink-0 justify-center border-b border-sidebar-border px-4 py-0">
        <div className="flex items-center gap-2.5">
          <span className="flex size-7 items-center justify-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground">
            <Shirt className="size-4" />
          </span>
          <span className="truncate text-sm font-semibold text-sidebar-foreground">Uniformes 6M</span>
        </div>
      </SidebarHeader>

      <SidebarContent>
        {grupos.map((grupo, index) => (
          <SidebarGroup key={grupo.label ?? index}>
            {grupo.label && <SidebarGroupLabel>{grupo.label}</SidebarGroupLabel>}
            <SidebarGroupContent>
              <SidebarMenu>
                {grupo.itens.map((item) => {
                  const ativo = location.pathname === item.url

                  return (
                    <SidebarMenuItem key={item.url}>
                      <SidebarMenuButton
                        render={<Link to={item.url} aria-current={ativo ? 'page' : undefined} />}
                        isActive={ativo}
                        className="data-active:font-medium"
                      >
                        <item.icon />
                        <span>{item.title}</span>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  )
                })}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        ))}
      </SidebarContent>

      <SidebarFooter className="border-t border-sidebar-border">
        {session?.user.email && (
          <p className="truncate px-2 pt-1 text-xs text-sidebar-foreground/70" title={session.user.email}>
            {session.user.email}
          </p>
        )}
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              render={<Link to="/conta" aria-current={location.pathname === '/conta' ? 'page' : undefined} />}
              isActive={location.pathname === '/conta'}
              className="data-active:font-medium"
            >
              <UserCog />
              <span>Minha conta</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
          <SidebarMenuItem>
            <SidebarMenuButton onClick={logout}>
              <LogOut />
              <span>Sair</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  )
}
