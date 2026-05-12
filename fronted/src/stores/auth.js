import { defineStore } from 'pinia'
import { getProfile, login, register } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    async register(payload) {
      const res = await register(payload)
      this.setSession(res.data)
      return res.data
    },
    async login(payload) {
      const res = await login(payload)
      this.setSession(res.data)
      return res.data
    },
    async fetchProfile() {
      if (!this.token) return null
      const res = await getProfile()
      this.user = res.data
      return this.user
    },
    setSession(authData) {
      if (!authData || !authData.token) {
        throw new Error('登录信息无效，请重试')
      }
      this.token = authData.token
      this.user = {
        id: authData.userId,
        username: authData.username,
        displayName: authData.displayName
      }
      localStorage.setItem('token', this.token)
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    }
  }
})
