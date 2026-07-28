import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  id: number
  email: string
  nickname: string
}

const STORAGE_KEY = 'jaymetest_auth'

export const useAuthStore = defineStore('auth', () => {
  // 从 localStorage 恢复
  const saved = loadFromStorage()

  // --- State ---
  const token = ref<string | null>(saved.token || null)
  const user = ref<UserInfo | null>(saved.user || null)
  const sessionExpired = ref(false)

  // --- Getters ---
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isGuest = computed(() => !isLoggedIn.value)

  // --- Actions ---
  function setAuth(t: string, u: UserInfo) {
    token.value = t
    user.value = u
    sessionExpired.value = false
    persist()
  }

  function logout() {
    token.value = null
    user.value = null
    sessionExpired.value = false
    clearPersistedAuth()
  }

  function expireSession() {
    token.value = null
    user.value = null
    sessionExpired.value = true
    clearPersistedAuth()
  }

  function resetSessionExpired() {
    sessionExpired.value = false
  }

  function persist() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        token: token.value,
        user: user.value,
      }))
    } catch {
      // localStorage 不可用时静默失败
    }
  }

  function clearPersistedAuth() {
    try {
      localStorage.removeItem(STORAGE_KEY)
    } catch {
      // localStorage 不可用时静默失败
    }
  }

  function loadFromStorage() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) {
        return JSON.parse(raw)
      }
    } catch {
      // 解析失败忽略
    }
    return {}
  }

  return {
    token, user, sessionExpired,
    isLoggedIn, isGuest,
    setAuth, logout, expireSession, resetSessionExpired,
  }
})
