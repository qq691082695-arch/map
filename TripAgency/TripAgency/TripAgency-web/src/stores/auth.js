import { defineStore } from 'pinia'
import { login as loginRequest } from '@/api/modules/auth'

const TOKEN_KEY = 'trip_agency_token'
const USER_KEY = 'trip_agency_user'

const readUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUser()
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    async login(form) {
      const data = await loginRequest(form)
      this.token = data.token
      this.user = data.user
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      return data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
