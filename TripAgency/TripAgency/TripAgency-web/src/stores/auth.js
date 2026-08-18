import { defineStore } from 'pinia'
import { api } from '@/api'

const TOKEN_KEY = 'trip_agency_token'
const USER_KEY = 'trip_agency_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    async login(form) {
      const res = await api.login(form)
      if (!res.ok) {
        throw new Error(res.message)
      }
      this.token = res.data.token
      this.user = res.data.user
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      return res.data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})