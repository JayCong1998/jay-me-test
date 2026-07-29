import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import * as questionApi from '@/api/questionApi'
import * as statsApi from '@/api/statsApi'
import * as albumApi from '@/api/albumApi'
import { getLevelByScore, getAbyssLevelByStreak, calcScore } from '@/utils/levels'
import { generateGuestNickname } from '@/utils/nickname'

/**
 * 答题流程编排 composable
 */
export function useQuiz() {
  const gameStore = useGameStore()
  const authStore = useAuthStore()

  /**
   * 开始新游戏
   */
  async function startNewRound() {
    try {
      const data = await questionApi.fetchRound()
      gameStore.startGame(data.roundId, data.questions)
      return true
    } catch (e: any) {
      throw new Error(e.message || '获取题目失败')
    }
  }

  /**
   * 开始专辑闯关
   */
  async function startAlbumRound(albumKey: string) {
    try {
      const data = await albumApi.fetchAlbumRound(albumKey)
      gameStore.startGame(data.roundId, data.questions, albumKey)
      return true
    } catch (e: any) {
      throw new Error(e.message || '获取专辑题目失败')
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

  // ============================================================
  // 无尽深渊模式
  // ============================================================

  /**
   * 开始深渊挑战
   */
  async function startAbyssRound() {
    try {
      const data = await questionApi.startAbyss()
      gameStore.startGame(data.roundId, data.questions, undefined, 'ABYSS')
      gameStore.setAbyssStreak(0)
      return true
    } catch (e: any) {
      throw new Error(e.message || '开始深渊挑战失败')
    }
  }

  /**
   * 深渊模式校验答案（调用专属接口，答对自动累加 streak）
   */
  async function submitAbyssAnswer(selectedOption: string) {
    const q = gameStore.currentQuestion
    if (!q || !gameStore.roundId) return null

    try {
      const result = await questionApi.checkAbyssAnswer({
        roundId: gameStore.roundId,
        questionId: q.id,
        selectedOption,
      })
      gameStore.submitAnswer(gameStore.currentIndex, selectedOption, result.correct)
      // 答对时本地同步递增 streak，避免等待预加载才更新导致显示滞后
      if (result.correct) {
        gameStore.setAbyssStreak(gameStore.abyssStreak + 1)
      }
      return result
    } catch (e: any) {
      throw new Error(e.message || '校验答案失败')
    }
  }

  /**
   * 静默预加载下一批深渊题目（当前批次倒数第 2 题时触发）
   */
  async function prefetchAbyssBatch() {
    if (gameStore.prefetching || !gameStore.roundId) return
    gameStore.prefetching = true
    try {
      const data = await questionApi.fetchAbyssBatch(gameStore.roundId)
      gameStore.appendQuestions(data.questions)
      // 后端返回的是生成新批次前的 streak，用它校准本地状态，避免多端或重试造成显示偏差。
      gameStore.setAbyssStreak(data.streak)
    } catch (e: any) {
      // 预加载失败不打断当前答题；真正无题时会在进入下一批时由接口错误兜底提示。
      console.warn('预加载深渊题目失败:', e.message)
    } finally {
      gameStore.prefetching = false
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

    const isAbyss = gameStore.mode === 'ABYSS'

    try {
      const nickname = authStore.isLoggedIn
        ? authStore.user!.nickname
        : generateGuestNickname()

      const result = await statsApi.submitResult({
        roundId: gameStore.roundId!,
        timeSpentSecs: gameStore.elapsedSeconds,
        nickname,
      })

      // 保存 API 结果到 gameStore，供结果页使用
      gameStore.setLastGameResult(result)

      return result
    } catch (e: any) {
      // 结果页是分享链路的终点；提交失败时仍用本地数据生成结果，避免用户丢失本局体验。
      if (isAbyss) {
        const abyssLevel = getAbyssLevelByStreak(gameStore.correctCount)
        const fallback = {
          roundId: gameStore.roundId!,
          mode: gameStore.mode,
          albumKey: gameStore.albumKey,
          score: gameStore.correctCount,
          correctCount: gameStore.correctCount,
          totalQuestions: gameStore.answers.size || (gameStore.correctCount + 1),
          accuracy: gameStore.answers.size > 0 ? gameStore.correctCount / gameStore.answers.size : 0,
          timeSpentSecs: gameStore.elapsedSeconds,
          usedRevival: false,
          createdAt: new Date().toISOString(),
          level: abyssLevel.key,
          levelTitle: abyssLevel.title,
          levelDescription: abyssLevel.description,
          beatPercentage: 0,
          totalPlayers: 0,
        }
        gameStore.setLastGameResult(fallback)
        return fallback
      }

      const level = getLevelByScore(gameStore.correctCount)
      const fallback = {
        roundId: gameStore.roundId!,
        mode: gameStore.mode,
        albumKey: gameStore.albumKey,
        score: calcScore(gameStore.correctCount),
        correctCount: gameStore.correctCount,
        totalQuestions: gameStore.totalQuestions,
        accuracy: gameStore.totalQuestions > 0 ? gameStore.correctCount / gameStore.totalQuestions : 0,
        timeSpentSecs: gameStore.elapsedSeconds,
        usedRevival: gameStore.revivalRemaining === 0,
        createdAt: new Date().toISOString(),
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
    startAlbumRound,
    startAbyssRound,
    submitAnswer,
    submitAbyssAnswer,
    reviveCurrent,
    finishAndSubmit,
    prefetchAbyssBatch,
  }
}
