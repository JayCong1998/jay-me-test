import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const STORAGE_KEY = 'jaymetest_user'
const DEFAULT_NICKNAME = '匿名杰迷'

export const useUserStore = defineStore('user', () => {
  const nickname = ref<string>(loadNickname())
  const hasSetNickname = computed(() => nickname.value !== DEFAULT_NICKNAME)

  function setNickname(name: string) {
    nickname.value = name
    persist()
  }

  function reset() {
    nickname.value = DEFAULT_NICKNAME
    persist()
  }

  function persist() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ nickname: nickname.value }))
    } catch {
      // localStorage 不可用时静默失败，答题流程仍可继续。
    }
  }

  function loadNickname(): string {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) {
        const parsed = JSON.parse(raw)
        if (typeof parsed.nickname === 'string' && parsed.nickname.trim()) {
          return parsed.nickname
        }
      }
    } catch {
      // 旧数据或损坏数据不影响启动。
    }
    return DEFAULT_NICKNAME
  }

  return {
    nickname,
    hasSetNickname,
    setNickname,
    reset,
  }
})
