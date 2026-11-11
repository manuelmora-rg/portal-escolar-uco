import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import api, { setAccessToken, setUnauthorizedHandler } from './api'

const AuthContext = createContext(null)

export const ROLE_HOME = {
  ADMIN: '/app/admin',
  TEACHER: '/app/docente',
  STUDENT: '/app/estudiante',
  PARENT: '/app/padre',
}

export const ROLE_LABEL = {
  ADMIN: 'Administrador',
  TEACHER: 'Docente',
  STUDENT: 'Estudiante',
  PARENT: 'Acudiente',
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const clearSession = useCallback(() => {
    setAccessToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler({
      onRefresh: (refreshedUser) => setUser(refreshedUser),
      onLogout: () => clearSession(),
    })
  }, [clearSession])

  useEffect(() => {
    let active = true
    axios
      .post('/api/v1/auth/refresh', null, { withCredentials: true })
      .then((response) => {
        if (!active) return
        setAccessToken(response.data.accessToken)
        setUser(response.data.user)
      })
      .catch(() => {
        if (active) clearSession()
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [clearSession])

  const login = useCallback(async (email, password) => {
    const response = await api.post('/auth/login', { email, password })
    setAccessToken(response.data.accessToken)
    setUser(response.data.user)
    return response.data.user
  }, [])

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout')
    } catch {
      // La sesión local se cierra aunque el servidor no responda.
    }
    clearSession()
  }, [clearSession])

  const changePassword = useCallback(async (currentPassword, newPassword) => {
    const response = await api.post('/auth/change-password', { currentPassword, newPassword })
    clearSession()
    return response.data
  }, [clearSession])

  const value = useMemo(
    () => ({ user, loading, login, logout, changePassword }),
    [user, loading, login, logout, changePassword],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return context
}
