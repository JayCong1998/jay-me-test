import { apiClient } from '@/api/client'
import type { GameRecord, PageResponse } from '@/types'

export function fetchRecords(params: {
  keyword?: string
  mode?: string
  startAt?: string
  endAt?: string
  page?: number
  size?: number
}) {
  return apiClient.get<never, PageResponse<GameRecord>>('/records', { params })
}
