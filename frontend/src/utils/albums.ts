/**
 * 专辑闯关常量
 */

export interface AlbumInfo {
  key: string
  displayName: string
  year: number
  /** 封面占位渐变 */
  gradient: string
}

/** 16 张录音室专辑（按发行时间排序） */
export const ALL_ALBUMS: AlbumInfo[] = [
  { key: 'Jay', displayName: 'Jay', year: 2000, gradient: 'linear-gradient(135deg, #2c3e50, #1a1a2e)' },
  { key: '范特西', displayName: '范特西', year: 2001, gradient: 'linear-gradient(135deg, #c0392b, #8b0000)' },
  { key: '八度空间', displayName: '八度空间', year: 2002, gradient: 'linear-gradient(135deg, #2c3e50, #34495e)' },
  { key: '叶惠美', displayName: '叶惠美', year: 2003, gradient: 'linear-gradient(135deg, #8e44ad, #4a235a)' },
  { key: '七里香', displayName: '七里香', year: 2004, gradient: 'linear-gradient(135deg, #27ae60, #1e8449)' },
  { key: '十一月的肖邦', displayName: '十一月的肖邦', year: 2005, gradient: 'linear-gradient(135deg, #d4a017, #996515)' },
  { key: '依然范特西', displayName: '依然范特西', year: 2006, gradient: 'linear-gradient(135deg, #e74c3c, #c0392b)' },
  { key: '我很忙', displayName: '我很忙', year: 2007, gradient: 'linear-gradient(135deg, #2980b9, #1a5276)' },
  { key: '魔杰座', displayName: '魔杰座', year: 2008, gradient: 'linear-gradient(135deg, #2c3e50, #1c2833)' },
  { key: '跨时代', displayName: '跨时代', year: 2010, gradient: 'linear-gradient(135deg, #7d3c98, #512e5f)' },
  { key: '惊叹号', displayName: '惊叹号', year: 2011, gradient: 'linear-gradient(135deg, #e67e22, #c0392b)' },
  { key: '12新作', displayName: '12新作', year: 2012, gradient: 'linear-gradient(135deg, #1abc9c, #148f77)' },
  { key: '哎呦不错哦', displayName: '哎呦不错哦', year: 2014, gradient: 'linear-gradient(135deg, #f39c12, #d68910)' },
  { key: '周杰伦的床边故事', displayName: '周杰伦的床边故事', year: 2016, gradient: 'linear-gradient(135deg, #3498db, #1f618d)' },
  { key: '最伟大的作品', displayName: '最伟大的作品', year: 2022, gradient: 'linear-gradient(135deg, #c9a84c, #8b7355)' },
  { key: '太阳之子', displayName: '太阳之子', year: 2026, gradient: 'linear-gradient(135deg, #f59e0b, #dc2626)' },
]

/** 通关门槛：答对 8/10 解锁下一关 */
export const UNLOCK_THRESHOLD = 8

/** 根据 key 查找专辑信息 */
export function getAlbumInfo(key: string): AlbumInfo | undefined {
  return ALL_ALBUMS.find(a => a.key === key)
}
