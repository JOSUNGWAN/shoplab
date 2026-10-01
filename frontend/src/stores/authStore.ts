import { create } from 'zustand'

type AuthState = {
  accessToken: string | null
  isInitialized: boolean
  setAccessToken: (token: string) => void
  setInitialized: () => void
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  isInitialized: false,
  setAccessToken: (accessToken) => set({ accessToken }),
  setInitialized: () => set({ isInitialized: true }),
  logout: () => set({ accessToken: null }),
}))