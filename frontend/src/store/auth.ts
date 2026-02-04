import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import Cookies from 'js-cookie'
import { api } from '@/lib/api'
import type { LoginResponse } from '@/types'

interface AuthState {
  user: LoginResponse['user'] | null
  token: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  setAuth: (response: LoginResponse) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: async (email: string, password: string) => {
        const response = await api.post<LoginResponse>('/api/auth/login', {
          email,
          password,
        })

        const { accessToken, user } = response.data

        Cookies.set('token', accessToken, { expires: 1 })

        set({
          user,
          token: accessToken,
          isAuthenticated: true,
        })
      },

      logout: () => {
        Cookies.remove('token')
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        })
      },

      setAuth: (response: LoginResponse) => {
        Cookies.set('token', response.accessToken, { expires: 1 })
        set({
          user: response.user,
          token: response.accessToken,
          isAuthenticated: true,
        })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
