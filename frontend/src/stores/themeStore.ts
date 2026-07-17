import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export type ThemeName = 'chinese' | 'playful' | 'cyberpunk' | 'ios'

const STORAGE_KEY = 'jaymetest_theme'
const THEME_CLASS_PREFIX = 'theme-'

const THEMES: { key: ThemeName; label: string; icon: string }[] = [
  { key: 'chinese', label: '新中式典雅', icon: '🏮' },
  { key: 'playful', label: '潮玩卡牌', icon: '🃏' },
  { key: 'cyberpunk', label: '赛博摩登', icon: '🌃' },
  { key: 'ios', label: 'iOS 原生', icon: '🍎' },
]

function loadFromStorage(): ThemeName {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw && THEMES.some(t => t.key === raw)) return raw as ThemeName
  } catch { /* ignore */ }
  return 'chinese'
}

function persist(theme: ThemeName) {
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch { /* ignore */ }
}

function applyTheme(theme: ThemeName) {
  // 清除所有 theme-* class，然后添加当前主题 class
  document.documentElement.classList.forEach(c => {
    if (c.startsWith(THEME_CLASS_PREFIX)) document.documentElement.classList.remove(c)
  })
  if (theme !== 'chinese') {
    document.documentElement.classList.add(`${THEME_CLASS_PREFIX}${theme}`)
  }
  // chinese 是默认主题，不需要 class（:root 即生效）
}

export const useThemeStore = defineStore('theme', () => {
  const activeTheme = ref<ThemeName>(loadFromStorage())

  // 初始化时应用主题
  applyTheme(activeTheme.value)

  function setTheme(theme: ThemeName) {
    activeTheme.value = theme
    applyTheme(theme)
    persist(theme)
  }

  return {
    activeTheme,
    setTheme,
    themes: THEMES,
  }
})
