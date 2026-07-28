import { describe, expect, it } from 'vitest'
import { getRecordPresentation } from './recordPresentation'

const baseRecord = {
  roundId: 'round-1',
  albumKey: null,
  score: 80,
  correctCount: 8,
  totalQuestions: 10,
  timeSpentSecs: 90,
  usedRevival: false,
  createdAt: '2026-07-20T18:00:00',
  level: 'SENIOR',
  levelTitle: '资深杰迷',
}

describe('getRecordPresentation', () => {
  it('presents a classic score as correct answers out of total questions', () => {
    const result = getRecordPresentation({ ...baseRecord, mode: 'CLASSIC' })

    expect(result.modeLabel).toBe('经典模式')
    expect(result.modeTone).toBe('classic')
    expect(result.scoreText).toBe('8/10')
    expect(result.levelTitle).toBe('资深杰迷')
  })

  it('includes the album name in album mode', () => {
    const result = getRecordPresentation({
      ...baseRecord,
      mode: 'ALBUM',
      albumKey: '叶惠美',
    })

    expect(result.modeLabel).toBe('专辑闯关 · 叶惠美')
    expect(result.modeTone).toBe('album')
    expect(result.scoreText).toBe('8/10')
  })

  it('presents an abyss streak without a ten-question denominator', () => {
    const result = getRecordPresentation({
      ...baseRecord,
      mode: 'ABYSS',
      score: 12,
      correctCount: 12,
      totalQuestions: 13,
      level: 'ABYSS_KNIGHT',
      levelTitle: '深渊骑士',
    })

    expect(result.modeLabel).toBe('无尽深渊')
    expect(result.modeTone).toBe('abyss')
    expect(result.scoreText).toBe('连续答对 12 题')
    expect(result.levelTitle).toBe('深渊骑士')
  })
})
