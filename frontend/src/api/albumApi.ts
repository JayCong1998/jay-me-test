import client from './client'
import type { R } from '@/utils/constants'

export interface AlbumDTO {
  albumKey: string
  displayName: string
  year: number
  unlocked: boolean
  bestScore: number
  totalAttempts: number
  isFirst: boolean
  isLast: boolean
}

/**
 * 获取专辑列表及解锁状态（需登录）
 */
export async function fetchAlbumList(): Promise<AlbumDTO[]> {
  const res = await client.get<R<AlbumDTO[]>>('/albums/list')
  return res.data.data
}

export interface RoundData {
  roundId: string
  questions: {
    id: number
    category: string
    difficulty: string
    questionText: string
    options: string[]
  }[]
}

/**
 * 获取专辑关卡题目（需登录 + 已解锁）
 */
export async function fetchAlbumRound(albumKey: string, count = 10): Promise<RoundData> {
  const res = await client.get<R<RoundData>>('/albums/round', {
    params: { albumKey, count },
  })
  return res.data.data
}
