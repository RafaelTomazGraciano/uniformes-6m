import { Activity, Box, Clipboard, LayoutDashboard, LogOut, Shirt, SquarePlus, Users } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'
import { useAuth } from '@/hooks/use-auth'

const navItems = [
  {
    title: 'Dashboard',
    url: '/dashboard',
    icon: LayoutDashboard,
  },
  {
    title: 'Nova requisição',
    url: '/requisicoes/nova',
    icon: SquarePlus,
  },
  {
    title: 'Estoque',
    url: '/estoque',
    icon: Box,
  },
  {
    title: 'Pedidos',
    url: '/pedidos',
    icon: Clipboard,
  },
  {
    title: 'Alunos',
    url: '/alunos',
    icon: Users,
  },
  {
    title: 'Relatório',
    url: '/relatorio',
    icon: Activity,
  },
]

export function AppSidebar() {
  const { logout } = useAuth()
  const location = useLocation()

  return (
    <Sidebar>
      <SidebarHeader>
        <div className="flex items-center gap-2 px-2 py-1.5">
          <Shirt className="size-5 text-sidebar-primary" />
          <span className="text-sm font-semibold text-sidebar-foreground">Uniformes</span>
        </div>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {navItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton
                    render={<Link to={item.url} />}
                    isActive={location.pathname === item.url}
                    className="data-active:font-semibold"
                  >
                    <item.icon />
                    <span>{item.title}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter>
        <SidebarMenu>
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
