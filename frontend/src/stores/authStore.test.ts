import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from './authStore'

describe('authStore session expiry', () => {
  const setItem = vi.fn()
  const removeItem = vi.fn()

  beforeEach(() => {
    setActivePinia(createPinia())
    setItem.mockReset()
    removeItem.mockReset()
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => null),
      setItem,
      removeItem,
    })
  })

  it('clears in-memory and persisted login state when the session expires', () => {
    const store = useAuthStore()
    store.setAuth('expired-token', {
      id: 1,
      email: 'jay@example.com',
      nickname: 'JayCong',
    })

    store.expireSession()

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(store.sessionExpired).toBe(true)
    expect(removeItem).toHaveBeenCalledWith('jaymetest_auth')
  })
})
