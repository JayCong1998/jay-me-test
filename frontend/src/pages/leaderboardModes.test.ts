import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const pageSource = readFileSync(resolve(__dirname, 'LeaderboardPage.vue'), 'utf-8')
const apiSource = readFileSync(resolve(__dirname, '../api/leaderboardApi.ts'), 'utf-8')

describe('leaderboard mode tabs', () => {
  it('shows only the three game mode leaderboards', () => {
    expect(pageSource).toContain("key: 'classic'")
    expect(pageSource).toContain("key: 'album'")
    expect(pageSource).toContain("key: 'abyss'")
    expect(pageSource).not.toContain("key: 'total'")
    expect(pageSource).not.toContain("key: 'daily'")
    expect(pageSource).not.toContain("key: 'level'")
    expect(pageSource).not.toContain('level-picker')
  })

  it('formats each mode with its own primary score field', () => {
    expect(pageSource).toContain('correctCount}/10')
    expect(pageSource).toContain('通关 ${entry.completedAlbumCount}/15')
    expect(pageSource).toContain('${entry.streak ?? entry.correctCount} 连对')
  })

  it('renders one or two leaderboard rows without requiring a podium', () => {
    expect(pageSource).toContain('visibleRankList')
    expect(pageSource).toContain('allEntries.value.length >= 3 ? allEntries.value.slice(3) : allEntries.value')
  })

  it('loads leaderboard data when login state changes on the leaderboard page', () => {
    expect(pageSource).toContain('watch(')
    expect(pageSource).toContain('authStore.isLoggedIn')
    expect(pageSource).toContain('loadData()')
  })

  it('refreshes leaderboard data when the cached tab is re-entered', () => {
    expect(pageSource).toContain('onActivated')
    expect(pageSource).toContain('onActivated(() => {')
    expect(pageSource).toContain('loadData()')
  })

  it('narrows the api type to the three modes and exposes detail fields', () => {
    expect(apiSource).toContain("type: LeaderboardType = 'classic'")
    expect(apiSource).toContain("'classic' | 'album' | 'abyss'")
    expect(apiSource).toContain('completedAlbumCount?: number')
    expect(apiSource).toContain('summaryText?: string')
    expect(apiSource).not.toContain("'total' | 'daily' | 'level' | 'abyss'")
  })
})
