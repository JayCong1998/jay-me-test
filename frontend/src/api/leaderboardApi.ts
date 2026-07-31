import client from './client'
import type { R } from '@/utils/constants'

export type LeaderboardType = 'classic' | 'album' | 'abyss'

export interface LeaderboardEntry {
  rank: number
  nickname: string
  correctCount?: number
  timeSpentSecs: number
  levelTitle?: string
  createdAt?: string
  summaryText?: string
  detailText?: string
  scoreText?: string
  completedAlbumCount?: number
  totalAlbumTimeSecs?: number
  bestAlbumKey?: string
  bestAlbumName?: string
  streak?: number
}

export interface LeaderboardResult {
  list: LeaderboardEntry[]
  myRank: number | null
}

/**
 * 获取排行榜（分页）
 */
export async function fetchLeaderboard(
  type: LeaderboardType = 'classic',
  page: number = 1,
  size: number = 20
): Promise<LeaderboardResult> {
  const params: Record<string, any> = { type, page, size }
  const res = await client.get<R<LeaderboardResult>>('/leaderboard', { params })
  return res.data.data
}
