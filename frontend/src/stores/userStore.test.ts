import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from './userStore'

describe('userStore', () => {
  const setItem = vi.fn()

  beforeEach(() => {
    setActivePinia(createPinia())
    setItem.mockReset()
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => JSON.stringify({
        nickname: '旧昵称',
        gameHistory: [{ roundId: 'legacy-local-record' }],
        bestScore: 10,
      })),
      setItem,
    })
  })

  it('persists only the nickname and never local game history', () => {
    const store = useUserStore()

    store.setNickname('新昵称')

    const lastCall = setItem.mock.calls[setItem.mock.calls.length - 1]
    const persisted = JSON.parse(lastCall?.[1])
    expect(persisted).toEqual({ nickname: '新昵称' })
    expect('gameHistory' in store).toBe(false)
    expect('bestScore' in store).toBe(false)
  })
})
