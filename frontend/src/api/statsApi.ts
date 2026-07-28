import client from './client'
import type { R } from '@/utils/constants'

export type GameMode = 'CLASSIC' | 'ALBUM' | 'ABYSS'

export interface AlbumResult {
  albumKey: string
  albumDisplayName: string
  passed: boolean
  albumBestScore: number
  isNewRecord: boolean
  unlockedNext: boolean
  nextAlbumKey?: string
  nextAlbumDisplayName?: string
}

export interface GameSubmitRequest {
  roundId: string
  correctCount: number
  timeSpentSecs: number
  usedRevival: number
  nickname?: string
  mode: GameMode
  albumKey?: string
  totalQuestions?: number
}

export interface GameResult {
  roundId: string
  mode: GameMode
  albumKey: string | null
  score: number
  correctCount: number
  totalQuestions: number
  accuracy: number
  timeSpentSecs: number
  usedRevival: boolean
  createdAt: string
  level: string
  levelTitle: string
  levelDescription: string
  beatPercentage: number
  totalPlayers: number
  albumResult?: AlbumResult
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
  mode: GameMode
  albumKey: string | null
  score: number
  correctCount: number
  totalQuestions: number
  timeSpentSecs: number
  usedRevival: boolean
  createdAt: string
  level: string
  levelTitle: string
}

/**
 * 获取当前登录用户的考试记录（分页）
 */
export async function fetchMyRecords(page: number = 1, size: number = 10): Promise<GameRecordDTO[]> {
  const res = await client.get<R<GameRecordDTO[]>>('/stats/my-records', {
    params: { page, size }
  })
  return res.data.data
}
