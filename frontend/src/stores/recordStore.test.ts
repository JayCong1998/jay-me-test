import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { fetchMyRecords } from '@/api/statsApi'
import { useRecordStore } from './recordStore'

vi.mock('@/api/statsApi', () => ({
  fetchMyRecords: vi.fn(),
}))

const record = {
  roundId: 'round-1',
  mode: 'ABYSS' as const,
  albumKey: null,
  score: 12,
  correctCount: 12,
  totalQuestions: 13,
  timeSpentSecs: 90,
  usedRevival: false,
  createdAt: '2026-07-20T18:00:00',
  level: 'ABYSS_KNIGHT',
  levelTitle: '深渊骑士',
}

describe('recordStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('loads records from the server as the only history source', async () => {
    vi.mocked(fetchMyRecords).mockResolvedValue([record])
    const store = useRecordStore()

    await store.fetchMyRecords()

    expect(store.records).toEqual([record])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('clears records and errors on logout', async () => {
    vi.mocked(fetchMyRecords).mockResolvedValue([record])
    const store = useRecordStore()
    await store.fetchMyRecords()

    store.clear()

    expect(store.records).toEqual([])
    expect(store.error).toBeNull()
  })

  it('exposes a retryable error without restoring local history', async () => {
    vi.mocked(fetchMyRecords).mockRejectedValue(new Error('network down'))
    const store = useRecordStore()

    await store.fetchMyRecords()

    expect(store.records).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBe('network down')
  })

  it('does not expose session expiry as a retryable record error', async () => {
    const error = new Error('登录已过期，请重新登录')
    error.name = 'AuthExpiredError'
    vi.mocked(fetchMyRecords).mockRejectedValue(error)
    const store = useRecordStore()

    await store.fetchMyRecords()

    expect(store.records).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })
})
