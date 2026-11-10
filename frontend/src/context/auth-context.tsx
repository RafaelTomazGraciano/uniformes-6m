import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { toast } from 'sonner'

import {
  type AuthSession,
  type AuthUser,
  MENSAGEM_SESSAO_EXPIRADA,
  SESSION_CLEARED_EVENT,
  clearStoredSession,
  expireSession,
  getStoredSession,
  getTokenExpiration,
  loginUser,
  setStoredSession,
} from '@/lib/auth'

type AuthContextValue = {
  session: AuthSession | null
  isAuthenticated: boolean
  login: (email: string, senha: string) => Promise<void>
  logout: () => void
  atualizarUsuario: (usuario: AuthUser) => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

// setTimeout estoura acima de ~24,8 dias; o token dura horas, mas o limite evita surpresa.
const MAIOR_ATRASO_MS = 2_147_483_647

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => getStoredSession())
  const sessionRef = useRef(session)

  useEffect(() => {
    sessionRef.current = session
  }, [session])

  useEffect(() => {
    const syncSession = () => {
      setSession(getStoredSession())
    }

    /* Vários 401 simultâneos (o dashboard dispara várias consultas) geram um só toast,
       pelo id fixo; e só avisa quem estava logado, para não incomodar no /login. */
    const encerrarSessao = () => {
      if (sessionRef.current) {
        toast.error(MENSAGEM_SESSAO_EXPIRADA, { id: 'sessao-expirada' })
      }
      sessionRef.current = null
      setSession(null)
    }

    window.addEventListener('storage', syncSession)
    window.addEventListener(SESSION_CLEARED_EVENT, encerrarSessao)
    return () => {
      window.removeEventListener('storage', syncSession)
      window.removeEventListener(SESSION_CLEARED_EVENT, encerrarSessao)
    }
  }, [])

  // Sai sozinho quando o token vence, sem esperar a próxima chamada à API.
  useEffect(() => {
    if (!session) return

    const exp = getTokenExpiration(session.token)
    if (exp === null) return

    const atraso = Math.min(Math.max(exp * 1000 - Date.now(), 0), MAIOR_ATRASO_MS)
    const timer = window.setTimeout(expireSession, atraso)
    return () => window.clearTimeout(timer)
  }, [session])

  const login = useCallback(async (email: string, senha: string) => {
    const nextSession = await loginUser({ email, senha })
    setStoredSession(nextSession)
    setSession(nextSession)
  }, [])

  const logout = useCallback(() => {
    clearStoredSession()
    setSession(null)
  }, [])

  /* O token continua válido depois de editar o perfil; só os dados exibidos
     mudam. Regravar a sessão evita exigir um novo login por causa do nome. */
  const atualizarUsuario = useCallback((usuario: AuthUser) => {
    setSession((atual) => {
      if (!atual) return atual

      const proxima = { ...atual, user: usuario }
      setStoredSession(proxima)
      return proxima
    })
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: Boolean(session),
      login,
      logout,
      atualizarUsuario,
    }),
    [atualizarUsuario, login, logout, session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
