import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface GameRecord {
  date: string
  score: number
  correctCount: number
  totalQuestions: number
  level: string
  levelTitle: string
  timeSpentSecs: number
  usedRevival: boolean
}

const STORAGE_KEY = 'jaymetest_user'
const MAX_HISTORY = 20

export const useUserStore = defineStore('user', () => {
  // --- 从 localStorage 恢复 ---
  const saved = loadFromStorage()

  // --- State ---
  const nickname = ref<string>(saved.nickname || '匿名杰迷')
  const gameHistory = ref<GameRecord[]>(saved.gameHistory || [])
  const totalGamesPlayed = ref<number>(saved.totalGamesPlayed || 0)
  const bestScore = ref<number>(saved.bestScore || 0)
  const bestLevel = ref<string>(saved.bestLevel || '')

  // --- Getters ---
  const hasSetNickname = computed(() => nickname.value !== '匿名杰迷')
  const latestRecord = computed(() =>
    gameHistory.value.length > 0 ? gameHistory.value[0] : null
  )

  // --- Actions ---
  function setNickname(name: string) {
    nickname.value = name
    persist()
  }

  function addGameRecord(record: GameRecord) {
    gameHistory.value.unshift(record)
    if (gameHistory.value.length > MAX_HISTORY) {
      gameHistory.value = gameHistory.value.slice(0, MAX_HISTORY)
    }
    totalGamesPlayed.value++

    if (record.correctCount > bestScore.value) {
      bestScore.value = record.correctCount
      bestLevel.value = record.level
    }

    persist()
  }

  /**
   * 从服务端同步考试记录（登录用户专用）
   * 以 roundId 去重，仅追加本地不存在的记录
   */
  function syncFromServer(serverRecords: Array<{
    roundId: string
    correctCount: number
    totalQuestions: number
    timeSpentSecs: number
    usedRevival: boolean
    createdAt: string
    level: string
    levelTitle: string
  }>) {
    const existingIds = new Set(gameHistory.value.map(g => g.date))
    let hasNew = false

    for (const rec of serverRecords) {
      if (!existingIds.has(rec.createdAt)) {
        gameHistory.value.unshift({
          date: rec.createdAt,
          score: rec.correctCount * 10,
          correctCount: rec.correctCount,
          totalQuestions: rec.totalQuestions,
          level: rec.level,
          levelTitle: rec.levelTitle,
          timeSpentSecs: rec.timeSpentSecs,
          usedRevival: rec.usedRevival,
        })
        existingIds.add(rec.createdAt)
        hasNew = true
      }
    }

    if (hasNew) {
      // 按日期降序排列（最新在前）
      gameHistory.value.sort((a, b) => b.date.localeCompare(a.date))
      // 截断到最大数量
      if (gameHistory.value.length > MAX_HISTORY) {
        gameHistory.value = gameHistory.value.slice(0, MAX_HISTORY)
      }
      // 重新计算统计
      totalGamesPlayed.value = gameHistory.value.length
      let maxScore = 0
      let maxLevel = ''
      for (const r of gameHistory.value) {
        if (r.correctCount > maxScore) {
          maxScore = r.correctCount
          maxLevel = r.level
        }
      }
      bestScore.value = maxScore
      bestLevel.value = maxLevel
      persist()
    }
  }

  function reset() {
    nickname.value = '匿名杰迷'
    gameHistory.value = []
    totalGamesPlayed.value = 0
    bestScore.value = 0
    bestLevel.value = ''
    persist()
  }

  function persist() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        nickname: nickname.value,
        gameHistory: gameHistory.value,
        totalGamesPlayed: totalGamesPlayed.value,
        bestScore: bestScore.value,
        bestLevel: bestLevel.value,
      }))
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
      // 解析失败时忽略
    }
    return {}
  }

  return {
    nickname, gameHistory, totalGamesPlayed, bestScore, bestLevel,
    hasSetNickname, latestRecord,
    setNickname, addGameRecord, syncFromServer, persist, reset,
  }
})
