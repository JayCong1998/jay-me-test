import client from './client'
import type { R } from '@/utils/constants'

export interface LeaderboardEntry {
  rank: number
  nickname: string
  correctCount: number
  timeSpentSecs: number
  levelTitle: string
}

export interface LeaderboardResult {
  list: LeaderboardEntry[]
  myRank: number | null
}

/**
 * 获取排行榜
 */
export async function fetchLeaderboard(
  type: 'total' | 'daily' | 'level' = 'total',
  limit: number = 50,
  level?: string
): Promise<LeaderboardResult> {
  const params: Record<string, any> = { type, limit }
  if (level) params.level = level
  const res = await client.get<R<LeaderboardResult>>('/leaderboard', { params })
  return res.data.data
}
