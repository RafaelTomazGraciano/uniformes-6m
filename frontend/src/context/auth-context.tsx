import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'

import {
  type AuthSession,
  type AuthUser,
  clearStoredSession,
  getStoredSession,
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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => getStoredSession())

  useEffect(() => {
    const syncSession = () => {
      setSession(getStoredSession())
    }

    window.addEventListener('storage', syncSession)
    return () => window.removeEventListener('storage', syncSession)
  }, [])

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
