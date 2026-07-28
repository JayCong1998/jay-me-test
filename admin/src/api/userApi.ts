import { apiClient } from '@/api/client'
import type { PageResponse, UserRecord } from '@/types'

export function fetchUsers(params: { keyword?: string; page?: number; size?: number }) {
  return apiClient.get<never, PageResponse<UserRecord>>('/users', { params })
}
