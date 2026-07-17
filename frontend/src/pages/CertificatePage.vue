<template>
  <div class="cert-page page-bg">
    <!-- 背景光斑 -->
    <div class="bg-orb bg-orb--top"></div>
    <div class="bg-orb bg-orb--bottom"></div>

    <div class="cert-content">
      <!-- 顶部导航 -->
      <header class="cert-header">
        <button class="header-back" @click="goBack" aria-label="返回结果">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6" />
          </svg>
          <span>返回</span>
        </button>
        <h2 class="header-title">我的证书</h2>
        <div class="header-spacer"></div>
      </header>

      <!-- ===== 证书预览 ===== -->
      <section class="cert-preview" ref="certPreviewRef">
        <div class="cert-card" ref="certCardRef">
          <div class="cert-inner">
            <!-- 水印 -->
            <span class="cert-watermark">JAY CHOU</span>

            <!-- 装饰线 + 音符 -->
            <div class="cert-deco-top">
              <span class="deco-line"></span>
              <svg class="deco-note" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="10" cy="30" rx="8" ry="6" fill="currentColor" />
                <rect x="17" y="0" width="3" height="30" rx="1.5" fill="currentColor" />
                <path d="M20 0 Q28 6 24 16 Q22 20 20 24" fill="currentColor" />
              </svg>
              <span class="deco-line"></span>
            </div>

            <!-- 标题 -->
            <h3 class="cert-title text-gold">杰迷结业考试</h3>
            <span class="cert-divider"></span>

            <!-- 正文 -->
            <p class="cert-subtitle">兹 证 明</p>
            <p class="cert-nickname">{{ userStore.nickname }}</p>
            <p class="cert-meta-text">在杰迷结业考试中获得</p>
            <p class="cert-level" :style="{ color: levelColor }">{{ levelConfig.title }}</p>

            <!-- 分数 -->
            <p class="cert-score">
              得分 {{ result.correctCount }}/{{ result.totalQuestions }}
              <span class="score-sep">·</span>
              正确率 {{ (result.accuracy * 100).toFixed(0) }}%
            </p>

            <!-- 装饰分隔 -->
            <div class="cert-sep">
              <span class="sep-line"></span>
              <svg class="sep-diamond" viewBox="0 0 16 16" fill="currentColor">
                <path d="M8 0L16 8L8 16L0 8Z" />
              </svg>
              <span class="sep-line"></span>
            </div>

            <!-- 日期 -->
            <p class="cert-date">考试日期：{{ examDate }}</p>

            <!-- 底部 -->
            <div class="cert-bottom">
              <p class="cert-org">杰迷结业考试 组委会</p>
              <p class="cert-brand">— JayMe Certificate —</p>
            </div>

            <!-- 底部装饰 -->
            <div class="cert-deco-bottom">
              <svg class="deco-note-sm" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="6" cy="18" rx="5" ry="4" fill="currentColor" />
                <rect x="10" y="0" width="2" height="18" rx="1" fill="currentColor" />
                <path d="M12 0 Q17 4 15 10 Q14 12 12 14" fill="currentColor" />
              </svg>
              <svg class="deco-note-sm" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="6" cy="18" rx="5" ry="4" fill="currentColor" />
                <rect x="10" y="0" width="2" height="18" rx="1" fill="currentColor" />
                <path d="M12 0 Q17 4 15 10 Q14 12 12 14" fill="currentColor" />
              </svg>
              <svg class="deco-note-sm" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="6" cy="18" rx="5" ry="4" fill="currentColor" />
                <rect x="10" y="0" width="2" height="18" rx="1" fill="currentColor" />
                <path d="M12 0 Q17 4 15 10 Q14 12 12 14" fill="currentColor" />
              </svg>
            </div>
          </div>
        </div>
      </section>

      <!-- ===== 操作按钮 ===== -->
      <section class="cert-actions">
        <button class="btn-save" :disabled="saving" @click="handleSaveImage">
          <svg v-if="!saving" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
            <polyline points="7 10 12 15 17 10" />
            <line x1="12" y1="15" x2="12" y2="3" />
          </svg>
          <span v-if="!saving">保存证书图片</span>
          <span v-else class="saving-spinner"></span>
        </button>
        <button class="btn-share" @click="handleShare">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <circle cx="18" cy="5" r="3" />
            <circle cx="6" cy="12" r="3" />
            <circle cx="18" cy="19" r="3" />
            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" />
            <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" />
          </svg>
          分享给朋友
        </button>
      </section>
    </div>

    <!-- ===== 分享引导蒙层 (Tier 3) ===== -->
    <Teleport to="body">
      <Transition name="dialog-fade">
        <div v-if="showShareGuide" class="share-overlay" @click="showShareGuide = false">
          <div class="share-guide" @click.stop>
            <div class="guide-arrow">
              <svg viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M45 10L15 45M45 10H25M45 10V30" stroke="#c9a84c" stroke-width="2.5"
                  stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </div>
            <p class="guide-title">点击右上角</p>
            <p class="guide-desc">选择「分享给朋友」或「发送给朋友」</p>
            <p class="guide-alt">或长按证书图片保存后分享</p>
            <button class="guide-btn" @click="showShareGuide = false">我知道了</button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'
import { useGameStore } from '@/stores/gameStore'
import { useUserStore } from '@/stores/userStore'
import { formatDate } from '@/utils/format'
import { LEVELS, ABYSS_LEVELS, SHARE_TEXT_TEMPLATE, ABYSS_SHARE_TEXT_TEMPLATE } from '@/utils/constants'
import { renderCertificate, downloadCertificate } from '@/utils/certificate'
import type { CertData } from '@/utils/certificate'

const router = useRouter()
const gameStore = useGameStore()
const userStore = useUserStore()

const certCardRef = ref<HTMLElement>()
const certPreviewRef = ref<HTMLElement>()
const saving = ref(false)
const showShareGuide = ref(false)

// --- 证书数据 ---
const result = computed(() => {
  const latest = userStore.latestRecord
  if (latest) {
    return {
      correctCount: latest.correctCount,
      totalQuestions: latest.totalQuestions,
      accuracy: latest.correctCount / latest.totalQuestions,
    }
  }
  return {
    correctCount: gameStore.correctCount,
    totalQuestions: gameStore.totalQuestions || 10,
    accuracy: gameStore.totalQuestions > 0
      ? gameStore.correctCount / gameStore.totalQuestions
      : 0,
  }
})

const levelConfig = computed(() => {
  // 深渊模式：优先用服务端返回的 level，降级用 ABYSS_LEVELS
  if (gameStore.mode === 'ABYSS') {
    const serverResult = gameStore.lastGameResult
    if (serverResult?.level) {
      const match = ABYSS_LEVELS.find(l => l.key === serverResult.level)
      if (match) return match
    }
    return ABYSS_LEVELS.find(
      l => gameStore.abyssStreak >= l.minStreak && gameStore.abyssStreak <= l.maxStreak
    ) || ABYSS_LEVELS[0]
  }

  // 经典/专辑模式：优先用服务端返回的 level，降级用 LEVELS
  const serverResult = gameStore.lastGameResult
  if (serverResult?.level) {
    const match = LEVELS.find(l => l.key === serverResult.level)
    if (match) return match
  }
  return LEVELS.find(
    l => result.value.correctCount >= l.minScore && result.value.correctCount <= l.maxScore
  ) || LEVELS[0]
})

const levelColor = computed(() => levelConfig.value.color)

const examDate = computed(() => {
  const latest = userStore.latestRecord
  return latest ? latest.date : formatDate()
})

// --- 方法 ---
function goBack() {
  router.push('/result')
}

async function handleSaveImage() {
  saving.value = true
  try {
    const certData: CertData = {
      nickname: userStore.nickname,
      levelTitle: levelConfig.value.title,
      levelKey: levelConfig.value.key,
      correctCount: result.value.correctCount,
      totalQuestions: result.value.totalQuestions,
      accuracy: result.value.accuracy,
      examDate: examDate.value,
    }

    // 短暂延时让 loading 状态可见
    await new Promise(r => setTimeout(r, 200))

    const canvas = renderCertificate(certData)
    downloadCertificate(canvas, `杰迷证书_${levelConfig.value.title}.png`)
    showSuccessToast('证书图片已开始下载')
  } catch {
    showToast('生成失败，请尝试截图保存')
  } finally {
    saving.value = false
  }
}

async function handleShare() {
  const shareTemplate = gameStore.mode === 'ABYSS' ? ABYSS_SHARE_TEXT_TEMPLATE : SHARE_TEXT_TEMPLATE
  const shareText = shareTemplate
    .replace('{level}', levelConfig.value.title)
    .replace('{streak}', String(gameStore.abyssStreak))
  const shareUrl = window.location.origin + '/#/'

  // Tier 1: Web Share API
  if (navigator.share) {
    try {
      await navigator.share({
        title: '杰迷结业考试',
        text: shareText,
        url: shareUrl,
      })
      return
    } catch {
      // 用户取消或失败，继续降级
    }
  }

  // Tier 2: Clipboard API
  try {
    await navigator.clipboard.writeText(`${shareText} ${shareUrl}`)
    showSuccessToast('已复制分享文案，去粘贴给朋友吧')
    return
  } catch {
    // 继续降级
  }

  // Tier 3: 引导手动分享
  showShareGuide.value = true
}
</script>

<style scoped lang="scss">
/* ======== 背景 ======== */
.bg-orb {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;

  &--top {
    width: 200px;
    height: 200px;
    top: -40px;
    right: -60px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.1) 0%, transparent 70%);
  }

  &--bottom {
    width: 180px;
    height: 180px;
    bottom: 15%;
    left: -50px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.06) 0%, transparent 70%);
  }
}

/* ======== 页面 ======== */
.cert-page {
  min-height: 100vh;
  position: relative;
}

.cert-content {
  padding: 16px 16px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

/* ======== 顶部导航 ======== */
.cert-header {
  width: 100%;
  max-width: 400px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header-back {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  border: none;
  background: none;
  color: var(--app-gold);
  font-size: 15px;
  cursor: pointer;
  font-family: inherit;
  padding: 4px 0;
  border-radius: 6px;
  transition: opacity 0.2s;

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover { opacity: 0.7; }
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.header-spacer {
  width: 60px; // balance with back button
}

/* ======== 证书预览 ======== */
.cert-preview {
  width: 100%;
  max-width: 340px;
  margin-bottom: 28px;
  animation: fade-in-up 0.6s ease-out;
}

.cert-card {
  width: 100%;
  aspect-ratio: 340 / 478;
  background: linear-gradient(180deg, #0a0a18 0%, #111128 40%, #0f1a2e 70%, #0a0a18 100%);
  border: 2px solid rgba(var(--app-accent-rgb), 0.25);
  border-radius: 20px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4);
}

.cert-inner {
  position: absolute;
  inset: 16px;
  border: 1px solid rgba(var(--app-accent-rgb), 0.1);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 28px 20px 20px;
}

/* 水印 */
.cert-watermark {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Poppins', sans-serif;
  font-size: 52px;
  font-weight: 900;
  color: rgba(var(--app-accent-rgb), 0.04);
  transform: rotate(-25deg);
  pointer-events: none;
  white-space: nowrap;
  user-select: none;
}

/* 顶部装饰 */
.cert-deco-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.deco-line {
  width: 50px;
  height: 1px;
  background: var(--app-gold);
  opacity: 0.6;
}

.deco-note {
  width: 24px;
  height: 24px;
  color: var(--app-gold);
}

/* 标题 */
.cert-title {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 3px;
  margin-bottom: 10px;
}

.cert-divider {
  display: block;
  width: 50px;
  height: 1.5px;
  background: var(--app-gold);
  margin-bottom: 14px;
}

/* 正文 */
.cert-subtitle {
  font-size: 12px;
  color: var(--app-text-secondary);
  margin-bottom: 8px;
  letter-spacing: 4px;
}

.cert-nickname {
  font-size: 20px;
  font-weight: 700;
  color: var(--app-gold);
  margin-bottom: 10px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cert-meta-text {
  font-size: 11px;
  color: var(--app-text-muted);
  margin-bottom: 8px;
}

.cert-level {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.cert-score {
  font-size: 12px;
  color: #ccc;
  font-family: 'Poppins', sans-serif;

  .score-sep {
    color: var(--app-text-muted);
    margin: 0 4px;
  }
}

/* 装饰分隔 */
.cert-sep {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 14px 0;
  width: 70%;

  .sep-line {
    flex: 1;
    height: 1px;
    background: rgba(var(--app-accent-rgb), 0.2);
  }

  .sep-diamond {
    width: 10px;
    height: 10px;
    color: var(--app-gold);
    flex-shrink: 0;
  }
}

.cert-date {
  font-size: 11px;
  color: var(--app-text-secondary);
  margin-bottom: 16px;
}

/* 底部 */
.cert-bottom {
  margin-top: auto;

  .cert-org {
    font-size: 13px;
    font-weight: 600;
    color: var(--app-gold);
    letter-spacing: 1px;
    margin-bottom: 4px;
  }

  .cert-brand {
    font-family: 'Poppins', sans-serif;
    font-size: 10px;
    color: var(--app-text-muted);
    letter-spacing: 1px;
  }
}

/* 底部装饰音符 */
.cert-deco-bottom {
  display: flex;
  gap: 40px;
  margin-top: 10px;

  .deco-note-sm {
    width: 16px;
    height: 16px;
    color: rgba(var(--app-accent-rgb), 0.15);
  }
}

/* ======== 操作按钮 ======== */
.cert-actions {
  width: 100%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  animation: fade-in-up 0.6s ease-out 0.2s both;
}

.btn-save {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  font-family: inherit;
  color: var(--app-text-on-accent);
  background: var(--app-gold-gradient);
  box-shadow: 0 4px 20px rgba(var(--app-accent-rgb), 0.3);
  transition: all 0.3s ease;

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 28px rgba(var(--app-accent-rgb), 0.45);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-share {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  border: 1.5px solid rgba(var(--app-accent-rgb), 0.3);
  border-radius: 14px;
  background: rgba(var(--app-accent-rgb), 0.06);
  color: var(--app-gold);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.25s ease;

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: rgba(var(--app-accent-rgb), 0.12);
    border-color: rgba(var(--app-accent-rgb), 0.5);
  }
}

.saving-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(26, 26, 46, 0.2);
  border-top-color: var(--app-text-on-accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

/* ======== 分享引导蒙层 ======== */
.share-overlay {
  position: fixed;
  inset: 0;
  z-index: 1001;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 20px;
}

.share-guide {
  margin-top: 12px;
  background: var(--app-text-on-accent);
  border: 1px solid rgba(var(--app-accent-rgb), 0.2);
  border-radius: 20px;
  padding: 32px 28px 24px;
  text-align: center;
  max-width: 320px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.guide-arrow {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  animation: point-up 1.5s ease-in-out infinite;

  svg {
    width: 100%;
    height: 100%;
  }
}

.guide-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text-primary);
  margin-bottom: 8px;
}

.guide-desc {
  font-size: 14px;
  color: var(--app-text-secondary);
  margin-bottom: 6px;
}

.guide-alt {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-bottom: 24px;
}

.guide-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 12px;
  background: var(--app-gold-gradient);
  color: var(--app-text-on-accent);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-1px);
  }
}

/* ======== 过渡动画 ======== */
.dialog-fade-enter-active {
  transition: all 0.3s ease;

  .share-guide {
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
}

.dialog-fade-leave-active {
  transition: all 0.2s ease-in;

  .share-guide {
    transition: all 0.2s ease-in;
  }
}

.dialog-fade-enter-from {
  opacity: 0;

  .share-guide {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
}

.dialog-fade-leave-to {
  opacity: 0;

  .share-guide {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
}

/* ======== 动画 keyframes ======== */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes point-up {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
