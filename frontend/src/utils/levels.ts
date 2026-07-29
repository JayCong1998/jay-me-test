import { LEVELS, ABYSS_LEVELS, type LevelConfig, type AbyssLevelConfig } from './constants'

/**
 * 根据本局正确率获取等级配置；等级常量按 10 题等价分数划分。
 */
export function getLevelByScore(correctCount: number, totalQuestions: number): LevelConfig {
  const normalizedScore = totalQuestions > 0 ? (correctCount * 10) / totalQuestions : 0
  return LEVELS.find(level => normalizedScore >= level.minScore && normalizedScore <= level.maxScore) || LEVELS[0]
}

/**
 * 根据深渊 streak 获取等级配置
 */
export function getAbyssLevelByStreak(streak: number): AbyssLevelConfig {
  return ABYSS_LEVELS.find(l => streak >= l.minStreak && streak <= l.maxStreak) || ABYSS_LEVELS[0]
}

/**
 * 计算得分（百分比）
 */
export function calcScore(correctCount: number, total: number = 10): number {
  return Math.round((correctCount / total) * 100)
}

/**
 * 计算正确率
 */
export function calcAccuracy(correctCount: number, total: number = 10): number {
  return Math.round((correctCount / total) * 1000) / 10
}
