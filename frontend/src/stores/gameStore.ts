import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { GameMode, GameResult } from '@/api/statsApi'

export type { GameMode, GameResult } from '@/api/statsApi'

export interface Question {
  id: number
  category: string
  difficulty: string
  questionText: string
  options: string[]
}

export type GamePhase = 'idle' | 'playing' | 'finished'

export const useGameStore = defineStore('game', () => {
  // 答题状态仅保存在内存中，刷新后必须重新开局，避免 roundId 与后端缓存生命周期错位。
  const roundId = ref<string | null>(null)
  const questions = ref<Question[]>([])
  const currentIndex = ref(0)
  const answers = ref<Map<number, string>>(new Map())
  const results = ref<Map<number, boolean>>(new Map())
  const revivalRemaining = ref(0)
  const revivalUsed = ref(false)
  const startTime = ref<number>(0)
  const endTime = ref<number | null>(null)
  const phase = ref<GamePhase>('idle')
  const lastGameResult = ref<GameResult | null>(null)
  const mode = ref<GameMode>('CLASSIC')
  const albumKey = ref<string | null>(null)

  // 深渊模式没有固定题量，需要记录 streak 和预加载状态来支撑“无限批次”体验。
  const abyssStreak = ref(0)
  const prefetching = ref(false)

  // --- Getters ---
  const totalQuestions = computed(() => questions.value.length)
  const currentQuestion = computed(() => questions.value[currentIndex.value])
  const progress = computed(() =>
    totalQuestions.value > 0
      ? Math.round(((currentIndex.value + 1) / totalQuestions.value) * 100)
      : 0
  )
  const correctCount = computed(() =>
    Array.from(results.value.values()).filter(Boolean).length
  )
  const isLastQuestion = computed(() =>
    currentIndex.value >= totalQuestions.value - 1
  )
  const elapsedSeconds = computed(() => {
    if (!startTime.value) return 0
    const end = endTime.value || Date.now()
    return Math.floor((end - startTime.value) / 1000)
  })

  // --- Actions ---
  function startGame(rid: string, qs: Question[], albumKeyParam?: string, gameMode?: GameMode, revivalCount = 0) {
    roundId.value = rid
    questions.value = qs
    currentIndex.value = 0
    answers.value = new Map()
    results.value = new Map()
    revivalRemaining.value = revivalCount
    revivalUsed.value = false
    startTime.value = Date.now()
    endTime.value = null
    phase.value = 'playing'
    abyssStreak.value = 0
    prefetching.value = false
    if (gameMode) {
      mode.value = gameMode
    } else if (albumKeyParam) {
      mode.value = 'ALBUM'
    } else {
      mode.value = 'CLASSIC'
    }
    albumKey.value = albumKeyParam || null
  }

  function submitAnswer(questionIndex: number, option: string, isCorrect: boolean) {
    answers.value.set(questionIndex, option)
    results.value.set(questionIndex, isCorrect)
  }

  function useRevival(remainingRevivals: number) {
    if (remainingRevivals >= 0) {
      revivalRemaining.value = remainingRevivals
      revivalUsed.value = true
      // 复活只允许重答当前题，清理本地记录即可；正确答案仍只存在后端 round 缓存里。
      answers.value.delete(currentIndex.value)
      results.value.delete(currentIndex.value)
      return true
    }
    return false
  }

  function nextQuestion() {
    if (currentIndex.value < totalQuestions.value - 1) {
      currentIndex.value++
    }
  }

  function finishGame() {
    endTime.value = Date.now()
    phase.value = 'finished'
  }

  function appendQuestions(qs: Question[]) {
    questions.value.push(...qs)
  }

  function setAbyssStreak(streak: number) {
    abyssStreak.value = streak
  }

  function resetGame() {
    roundId.value = null
    questions.value = []
    currentIndex.value = 0
    answers.value = new Map()
    results.value = new Map()
    revivalRemaining.value = 0
    revivalUsed.value = false
    startTime.value = 0
    endTime.value = null
    phase.value = 'idle'
    lastGameResult.value = null
    mode.value = 'CLASSIC'
    albumKey.value = null
    abyssStreak.value = 0
    prefetching.value = false
  }

  function setLastGameResult(result: GameResult) {
    lastGameResult.value = result
  }

  return {
    // state
    roundId, questions, currentIndex, answers, results,
    revivalRemaining, revivalUsed, startTime, endTime, phase, mode, albumKey,
    abyssStreak, prefetching,
    // getters + data
    totalQuestions, currentQuestion, progress, correctCount,
    isLastQuestion, elapsedSeconds, lastGameResult,
    // actions
    startGame, submitAnswer, useRevival, nextQuestion, finishGame, resetGame, setLastGameResult,
    appendQuestions, setAbyssStreak,
  }
})
