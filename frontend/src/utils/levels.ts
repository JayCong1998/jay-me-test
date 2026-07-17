import { LEVELS, ABYSS_LEVELS, type LevelConfig, type AbyssLevelConfig } from './constants'

/**
 * 根据答对题数获取等级配置
 */
export function getLevelByScore(score: number): LevelConfig {
  return LEVELS.find(l => score >= l.minScore && score <= l.maxScore) || LEVELS[0]
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
