import { apiClient } from '@/api/client'
import type { DashboardOverview } from '@/types'

export function fetchDashboardOverview() {
  return apiClient.get<never, DashboardOverview>('/dashboard/overview')
}
