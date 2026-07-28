import { describe, expect, it, vi } from 'vitest'

vi.mock('vue-router', () => ({
  createRouter: vi.fn(() => ({ beforeEach: vi.fn() })),
  createWebHashHistory: vi.fn(() => ({})),
}))

describe('router auth access', () => {
  it('allows guests to open the leaderboard tab', async () => {
    const { routes } = await import('./index')
    const leaderboard = routes.find(route => route.path === '/leaderboard')

    expect(leaderboard?.meta?.requiresAuth).toBeFalsy()
  })
})
