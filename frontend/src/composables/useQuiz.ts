import { useGameStore } from '@/stores/gameStore'
import { useUserStore } from '@/stores/userStore'
import { useAuthStore } from '@/stores/authStore'
import * as questionApi from '@/api/questionApi'
import * as statsApi from '@/api/statsApi'
import { getLevelByScore, calcScore } from '@/utils/levels'
import { formatDate } from '@/utils/format'
import type { GameRecord } from '@/stores/userStore'

/**
 * 生成游客昵称：游客 + 时间戳36进制（保证唯一）
 */
function generateGuestNickname(): string {
  const ts = Date.now().toString(36).toUpperCase()
  return `游客${ts}`
}

/**
 * 答题流程编排 composable
 */
export function useQuiz() {
  const gameStore = useGameStore()
  const userStore = useUserStore()
  const authStore = useAuthStore()

  /**
   * 开始新游戏
   */
  async function startNewRound() {
    try {
      const data = await questionApi.fetchRound(10)
      gameStore.startGame(data.roundId, data.questions)
      return true
    } catch (e: any) {
      throw new Error(e.message || '获取题目失败')
    }
  }

  /**
   * 提交当前题目的答案
   */
  async function submitAnswer(selectedOption: string) {
    const q = gameStore.currentQuestion
    if (!q || !gameStore.roundId) return null

    try {
      const result = await questionApi.checkAnswer({
        roundId: gameStore.roundId,
        questionId: q.id,
        selectedOption,
      })
      gameStore.submitAnswer(gameStore.currentIndex, selectedOption, result.correct)
      return result
    } catch (e: any) {
      throw new Error(e.message || '校验答案失败')
    }
  }

  /**
   * 使用复活机会
   */
  async function reviveCurrent() {
    const q = gameStore.currentQuestion
    if (!q || !gameStore.roundId) return false

    try {
      await questionApi.revive({
        roundId: gameStore.roundId,
        questionId: q.id,
      })
      return gameStore.useRevival()
    } catch {
      return false
    }
  }

  /**
   * 完成游戏并提交结果
   */
  async function finishAndSubmit() {
    gameStore.finishGame()

    try {
      const nickname = authStore.isLoggedIn
        ? authStore.user!.nickname
        : generateGuestNickname()

      const result = await statsApi.submitResult({
        roundId: gameStore.roundId!,
        correctCount: gameStore.correctCount,
        timeSpentSecs: gameStore.elapsedSeconds,
        usedRevival: gameStore.revivalRemaining === 0 ? 1 : 0,
        nickname,
      })

      // 保存 API 结果到 gameStore，供结果页使用
      gameStore.setLastGameResult(result)

      // 保存到用户历史
      const record: GameRecord = {
        date: formatDate(),
        score: result.score,
        correctCount: result.correctCount,
        totalQuestions: result.totalQuestions,
        level: result.level,
        levelTitle: result.levelTitle,
        timeSpentSecs: result.timeSpentSecs,
        usedRevival: gameStore.revivalRemaining === 0,
      }
      userStore.addGameRecord(record)

      return result
    } catch (e: any) {
      // 即使提交失败也要返回本地计算结果
      const level = getLevelByScore(gameStore.correctCount)
      const fallback = {
        score: calcScore(gameStore.correctCount),
        correctCount: gameStore.correctCount,
        totalQuestions: gameStore.totalQuestions,
        accuracy: gameStore.correctCount / gameStore.totalQuestions,
        timeSpentSecs: gameStore.elapsedSeconds,
        level: level.key,
        levelTitle: level.title,
        levelDescription: level.description,
        beatPercentage: 0,
        totalPlayers: 0,
      }
      gameStore.setLastGameResult(fallback)
      return fallback
    }
  }

  return {
    startNewRound,
    submitAnswer,
    reviveCurrent,
    finishAndSubmit,
  }
}
