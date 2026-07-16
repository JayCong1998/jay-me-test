/**
 * 统一响应格式
 */
export interface R<T> {
  code: number
  msg: string
  data: T
  timestamp: number
}

/**
 * 杰迷等级配置
 */
export interface LevelConfig {
  key: string
  title: string
  description: string
  minScore: number
  maxScore: number
  color: string
  emoji: string
}

export const LEVELS: LevelConfig[] = [
  {
    key: 'PASSERBY', title: '🌱 路人粉',
    description: '刚刚路过，杰伦的歌还等你发现',
    minScore: 0, maxScore: 2,
    color: '#909399', emoji: '🌱',
  },
  {
    key: 'JUNIOR', title: '🎤 初级杰迷',
    description: '入门粉丝，继续加油解锁更多杰伦冷知识',
    minScore: 3, maxScore: 4,
    color: '#67c23a', emoji: '🎤',
  },
  {
    key: 'INTERMEDIATE', title: '🎧 中级杰迷',
    description: '资深听友，离骨灰粉只差一张专辑的距离',
    minScore: 5, maxScore: 6,
    color: '#409eff', emoji: '🎧',
  },
  {
    key: 'SENIOR', title: '🏆 高级杰迷',
    description: '铁粉认证，演唱会前排选手就是你',
    minScore: 7, maxScore: 8,
    color: '#e6a23c', emoji: '🏆',
  },
  {
    key: 'ULTIMATE', title: '👑 终极杰迷',
    description: '你就是杰伦的"编外制作人"！无可挑剔',
    minScore: 9, maxScore: 10,
    color: '#f56c6c', emoji: '👑',
  },
]

/**
 * 默认分享文案模板
 */
export const SHARE_TEXT_TEMPLATE = '我在杰迷结业考试中获得了「{level}」称号！你也来测测你是几级杰迷？'

/**
 * 证书尺寸（1080 × 1520 px）
 */
export const CERT_WIDTH = 1080
export const CERT_HEIGHT = 1520
