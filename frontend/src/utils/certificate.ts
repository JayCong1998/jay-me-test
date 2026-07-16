/**
 * 证书 Canvas 绘制与导出
 *
 * 规格：1080 × 1520px (9:16 竖版)
 * 主题：深色底 + 金色点缀
 */

export interface CertData {
  nickname: string
  levelTitle: string
  levelKey: string
  correctCount: number
  totalQuestions: number
  accuracy: number
  examDate: string
}

const W = 1080
const H = 1520

// 配色
const COLORS = {
  bgDeep: '#0a0a18',
  bgMid: '#111128',
  gold: '#c9a84c',
  goldLight: '#e0cc8e',
  goldDark: '#a08030',
  textPrimary: '#f0ead6',
  textSecondary: '#9ca3af',
  textMuted: '#6b7280',
  whiteFaint: 'rgba(255,255,255,0.04)',
  goldFaint: 'rgba(201,168,76,0.06)',
  border: 'rgba(201,168,76,0.25)',
}

/**
 * 绘制带圆角的矩形路径
 */
function roundRect(
  ctx: CanvasRenderingContext2D,
  x: number, y: number, w: number, h: number, r: number,
) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.lineTo(x + w - r, y)
  ctx.quadraticCurveTo(x + w, y, x + w, y + r)
  ctx.lineTo(x + w, y + h - r)
  ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h)
  ctx.lineTo(x + r, y + h)
  ctx.quadraticCurveTo(x, y + h, x, y + h - r)
  ctx.lineTo(x, y + r)
  ctx.quadraticCurveTo(x, y, x + r, y)
  ctx.closePath()
}

/**
 * 绘制音乐符号（简化八分音符）
 */
function drawMusicNote(ctx: CanvasRenderingContext2D, cx: number, cy: number, size: number, color: string) {
  ctx.save()
  ctx.fillStyle = color
  ctx.translate(cx, cy)

  const s = size / 40
  // 符头 (椭圆)
  ctx.beginPath()
  ctx.ellipse(0, 12 * s, 8 * s, 6 * s, 0, 0, Math.PI * 2)
  ctx.fill()
  // 符干
  ctx.fillRect(8 * s, -20 * s, 3 * s, 32 * s)
  // 符尾
  ctx.beginPath()
  ctx.moveTo(11 * s, -20 * s)
  ctx.quadraticCurveTo(22 * s, -12 * s, 17 * s, -2 * s)
  ctx.quadraticCurveTo(15 * s, 2 * s, 11 * s, 4 * s)
  ctx.fill()

  ctx.restore()
}

/**
 * 文本居中绘制辅助
 */
function textCenter(
  ctx: CanvasRenderingContext2D,
  text: string,
  y: number,
  font: string,
  color: string,
) {
  ctx.font = font
  ctx.fillStyle = color
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, W / 2, y)
}

/**
 * 生成证书 Canvas
 */
export function renderCertificate(data: CertData): HTMLCanvasElement {
  const canvas = document.createElement('canvas')
  canvas.width = W
  canvas.height = H
  const ctx = canvas.getContext('2d')!

  // --- 1. 背景 ---
  const bgGrad = ctx.createLinearGradient(0, 0, 0, H)
  bgGrad.addColorStop(0, COLORS.bgDeep)
  bgGrad.addColorStop(0.4, '#0f0f20')
  bgGrad.addColorStop(0.7, COLORS.bgMid)
  bgGrad.addColorStop(1, COLORS.bgDeep)
  ctx.fillStyle = bgGrad
  ctx.fillRect(0, 0, W, H)

  // 顶部金色光晕
  const glowTop = ctx.createRadialGradient(W / 2, 100, 20, W / 2, 100, 500)
  glowTop.addColorStop(0, 'rgba(201,168,76,0.12)')
  glowTop.addColorStop(1, 'transparent')
  ctx.fillStyle = glowTop
  ctx.fillRect(0, 0, W, 500)

  // 底部光晕
  const glowBottom = ctx.createRadialGradient(W / 2, H - 100, 20, W / 2, H - 100, 400)
  glowBottom.addColorStop(0, 'rgba(201,168,76,0.08)')
  glowBottom.addColorStop(1, 'transparent')
  ctx.fillStyle = glowBottom
  ctx.fillRect(0, H - 400, W, 400)

  // --- 2. 装饰圆环 ---
  ctx.strokeStyle = COLORS.border
  ctx.lineWidth = 3
  roundRect(ctx, 60, 60, W - 120, H - 120, 24)
  ctx.stroke()

  ctx.strokeStyle = 'rgba(201,168,76,0.12)'
  ctx.lineWidth = 1
  roundRect(ctx, 80, 80, W - 160, H - 160, 20)
  ctx.stroke()

  // --- 3. 水印 ---
  ctx.save()
  ctx.globalAlpha = 0.04
  ctx.fillStyle = COLORS.gold
  ctx.font = 'bold 180px "Poppins", sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.translate(W / 2, H / 2)
  ctx.rotate(-25 * Math.PI / 180)
  ctx.fillText('JAY CHOU', 0, 0)
  ctx.restore()

  // 装饰线 (顶部)
  const deckY = 140
  ctx.strokeStyle = COLORS.gold
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.moveTo(W / 2 - 140, deckY)
  ctx.lineTo(W / 2 - 60, deckY)
  ctx.stroke()
  ctx.beginPath()
  ctx.moveTo(W / 2 + 60, deckY)
  ctx.lineTo(W / 2 + 140, deckY)
  ctx.stroke()

  // --- 4. 音符图标 ---
  drawMusicNote(ctx, W / 2, deckY, 36, COLORS.gold)

  // --- 5. 标题 ---
  textCenter(ctx, '杰迷结业考试', 210, 'bold 48px "Poppins","PingFang SC","Microsoft YaHei",sans-serif', COLORS.gold)

  // 装饰线 (标题下方)
  const titleLineY = 260
  ctx.strokeStyle = COLORS.gold
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.moveTo(W / 2 - 80, titleLineY)
  ctx.lineTo(W / 2 + 80, titleLineY)
  ctx.stroke()

  // --- 6. 证书正文 ---
  textCenter(ctx, '兹 证 明', 340, '400 22px "PingFang SC","Microsoft YaHei",sans-serif', COLORS.textSecondary)

  // 昵称
  textCenter(ctx, data.nickname, 430, 'bold 52px "Poppins","PingFang SC","Microsoft YaHei",sans-serif', COLORS.gold)

  // 小字
  textCenter(ctx, '在杰迷结业考试中获得', 500, '400 22px "PingFang SC","Microsoft YaHei",sans-serif', COLORS.textSecondary)

  // 等级称号（大字）
  textCenter(ctx, data.levelTitle, 590, 'bold 64px "Poppins","PingFang SC","Microsoft YaHei",sans-serif', COLORS.goldLight)

  // --- 7. 分数信息 ---
  textCenter(
    ctx,
    `得分 ${data.correctCount}/${data.totalQuestions}  ·  正确率 ${data.accuracy.toFixed(0)}%`,
    680,
    '400 24px "Poppins","PingFang SC","Microsoft YaHei",sans-serif',
    COLORS.textPrimary,
  )

  // --- 8. 装饰分隔 ---
  const sepY = 760
  ctx.strokeStyle = 'rgba(201,168,76,0.3)'
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(W / 2 - 120, sepY)
  ctx.lineTo(W / 2 + 120, sepY)
  ctx.stroke()

  // 中间菱形
  ctx.fillStyle = COLORS.gold
  ctx.beginPath()
  ctx.moveTo(W / 2, sepY - 8)
  ctx.lineTo(W / 2 + 8, sepY)
  ctx.lineTo(W / 2, sepY + 8)
  ctx.lineTo(W / 2 - 8, sepY)
  ctx.closePath()
  ctx.fill()

  // --- 9. 考试日期 ---
  textCenter(ctx, `考试日期：${data.examDate}`, 840, '400 20px "Poppins","PingFang SC","Microsoft YaHei",sans-serif', COLORS.textSecondary)

  // --- 10. 底部 ---
  textCenter(ctx, '杰迷结业考试 组委会', 920, 'bold 24px "Poppins","PingFang SC","Microsoft YaHei",sans-serif', COLORS.gold)

  textCenter(ctx, '— JayMe Certificate —', 970, '400 16px "Poppins",sans-serif', COLORS.textMuted)

  // --- 11. 底部装饰 ---
  const bottomDecoY = 1050
  ctx.strokeStyle = 'rgba(201,168,76,0.15)'
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(W / 2 - 200, bottomDecoY)
  ctx.lineTo(W / 2 + 200, bottomDecoY)
  ctx.stroke()

  // 音符装饰 × 3
  for (let i = -1; i <= 1; i++) {
    drawMusicNote(ctx, W / 2 + i * 200, bottomDecoY + 60, 24, 'rgba(201,168,76,0.2)')
  }

  return canvas
}

/**
 * 触发 Canvas 下载为 PNG
 */
export function downloadCertificate(canvas: HTMLCanvasElement, filename: string = '杰迷证书.png') {
  canvas.toBlob((blob) => {
    if (!blob) return
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }, 'image/png')
}
