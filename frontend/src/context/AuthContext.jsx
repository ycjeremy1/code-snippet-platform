import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import * as authApi from '../api/auth'
import { tokenStorage } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function bootstrap() {
      if (tokenStorage.getAccessToken()) {
        try {
          const current = await authApi.fetchCurrentUser()
          setUser(current)
        } catch {
          tokenStorage.clear()
        }
      }
      setLoading(false)
    }
    bootstrap()
  }, [])

  const login = useCallback(async (usernameOrEmail, password) => {
    const loggedInUser = await authApi.login(usernameOrEmail, password)
    setUser(loggedInUser)
    return loggedInUser
  }, [])

  const register = useCallback(async (username, email, password) => {
    const registeredUser = await authApi.register(username, email, password)
    setUser(registeredUser)
    return registeredUser
  }, [])

  const logout = useCallback(async () => {
    await authApi.logout()
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, setUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
