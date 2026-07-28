import type { GameRecordDTO } from '@/api/statsApi'
import { ABYSS_LEVELS, LEVELS } from './constants'
import { getAlbumInfo } from './albums'

export interface RecordPresentation {
  modeLabel: string
  modeTone: 'classic' | 'album' | 'abyss'
  scoreText: string
  levelTitle: string
  levelColor: string
}

export function getRecordPresentation(record: GameRecordDTO): RecordPresentation {
  const levelColor = getLevelColor(record)

  switch (record.mode) {
    case 'CLASSIC':
      return {
        modeLabel: '经典模式',
        modeTone: 'classic',
        scoreText: `${record.correctCount}/${record.totalQuestions}`,
        levelTitle: record.levelTitle,
        levelColor,
      }
    case 'ALBUM': {
      const albumName = record.albumKey
        ? getAlbumInfo(record.albumKey)?.displayName
        : undefined
      return {
        modeLabel: `专辑闯关 · ${albumName || '数据异常'}`,
        modeTone: 'album',
        scoreText: `${record.correctCount}/${record.totalQuestions}`,
        levelTitle: record.levelTitle,
        levelColor,
      }
    }
    case 'ABYSS':
      return {
        modeLabel: '无尽深渊',
        modeTone: 'abyss',
        scoreText: `连续答对 ${record.correctCount} 题`,
        levelTitle: record.levelTitle,
        levelColor,
      }
  }
}

function getLevelColor(record: GameRecordDTO): string {
  if (record.mode === 'ABYSS') {
    return ABYSS_LEVELS.find(level => level.key === record.level)?.color
      || 'var(--app-text-muted)'
  }
  return LEVELS.find(level => level.key === record.level)?.color
    || 'var(--app-text-muted)'
}
