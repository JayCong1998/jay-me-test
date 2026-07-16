import client from './client'
import type { R } from '@/utils/constants'

export interface GameSubmitRequest {
  roundId: string
  correctCount: number
  timeSpentSecs: number
  usedRevival: number
  nickname?: string
}

export interface GameResult {
  score: number
  correctCount: number
  totalQuestions: number
  accuracy: number
  timeSpentSecs: number
  level: string
  levelTitle: string
  levelDescription: string
  beatPercentage: number
  totalPlayers: number
}

export interface StatsOverview {
  totalPlayers: number
  totalGames: number
  averageScore: number
  levelDistribution: Record<string, number>
}

/**
 * 提交游戏结果
 */
export async function submitResult(req: GameSubmitRequest): Promise<GameResult> {
  const res = await client.post<R<GameResult>>('/stats/submit', req)
  return res.data.data
}

/**
 * 获取全局统计概览
 */
export async function fetchOverview(): Promise<StatsOverview> {
  const res = await client.get<R<StatsOverview>>('/stats/overview')
  return res.data.data
}

export interface GameRecordDTO {
  roundId: string
  correctCount: number
  totalQuestions: number
  timeSpentSecs: number
  usedRevival: boolean
  createdAt: string
  level: string
  levelTitle: string
}

/**
 * 获取当前登录用户的考试记录（需登录）
 */
export async function fetchMyRecords(): Promise<GameRecordDTO[]> {
  const res = await client.get<R<GameRecordDTO[]>>('/stats/my-records')
  return res.data.data
}
