import { createContext, useContext, useMemo, useState } from 'react'
import { AUTH_KEY } from '@/api/client'
import * as authApi from '@/api/authApi'
import type { AdminProfile } from '@/types'

interface AuthState {
  token: string | null
  admin: AdminProfile | null
}

interface AuthContextValue extends AuthState {
  signIn: (username: string, password: string) => Promise<void>
  signOut: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

function loadSavedAuth(): AuthState {
  const saved = localStorage.getItem(AUTH_KEY)
  if (!saved) {
    return { token: null, admin: null }
  }
  try {
    const parsed = JSON.parse(saved) as AuthState
    return { token: parsed.token || null, admin: parsed.admin || null }
  } catch {
    localStorage.removeItem(AUTH_KEY)
    return { token: null, admin: null }
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [auth, setAuth] = useState<AuthState>(() => loadSavedAuth())

  const value = useMemo<AuthContextValue>(() => ({
    ...auth,
    async signIn(username, password) {
      const response = await authApi.login(username, password)
      const next = { token: response.token, admin: response.admin }
      localStorage.setItem(AUTH_KEY, JSON.stringify(next))
      setAuth(next)
    },
    async signOut() {
      try {
        await authApi.logout()
      } finally {
        localStorage.removeItem(AUTH_KEY)
        setAuth({ token: null, admin: null })
      }
    },
  }), [auth])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return value
}
