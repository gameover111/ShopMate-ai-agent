import { reactive, computed } from 'vue'
import http from '@/api/config'

const TOKEN_KEY = 'shopmate_token'
const USER_KEY = 'shopmate_user'

/** 从 localStorage 恢复 */
const savedToken = localStorage.getItem(TOKEN_KEY)
const savedUser = JSON.parse(localStorage.getItem(USER_KEY) || 'null')

const state = reactive({
  token: savedToken || '',
  user: savedUser,
})

/**
 * 认证状态 store — 响应式、localStorage 持久化
 */
export function useAuth() {
  const isLoggedIn = computed(() => !!state.token)
  const isAdmin = computed(() => state.user?.role === 'ADMIN')
  const currentUser = computed(() => state.user)
  const authToken = computed(() => state.token)

  async function login(email, password) {
    const res = await http.post('/auth/login', { email, password })
    state.token = res.token
    state.user = res.user
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(USER_KEY, JSON.stringify(res.user))
    return res
  }

  async function register(username, password, email) {
    const res = await http.post('/auth/register', { username, password, email })
    state.token = res.token
    state.user = res.user
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(USER_KEY, JSON.stringify(res.user))
    return res
  }

  function logout() {
    state.token = ''
    state.user = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return {
    state,
    isLoggedIn,
    isAdmin,
    currentUser,
    authToken,
    login,
    register,
    logout,
  }
}
