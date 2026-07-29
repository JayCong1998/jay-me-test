import { describe, expect, it } from 'vitest'
import { calcScore, getLevelByScore } from './levels'

describe('fallback level calculation', () => {
  it('uses the actual 20-question round size for score and level', () => {
    expect(calcScore(16, 20)).toBe(80)
    expect(getLevelByScore(16, 20).key).toBe('SENIOR')
  })
})
