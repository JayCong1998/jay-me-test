<template>
  <div class="result-page page-bg">
    <!-- 背景装饰 -->
    <div class="bg-orb bg-orb--top"></div>
    <div class="bg-orb bg-orb--bottom"></div>

    <div v-if="result" class="result-content">
      <!-- ===== 等级展示 ===== -->
      <section class="level-section">
        <div class="level-icon-wrap" :class="'icon-' + levelKey.toLowerCase()">
          <!-- 路人粉: 嫩芽 -->
          <svg v-if="levelKey === 'PASSERBY'" class="level-svg" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M40 72V40" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <path d="M40 40C40 28 28 20 20 24C28 16 40 16 40 8C40 16 52 16 60 24C52 20 40 28 40 40Z" fill="currentColor" opacity="0.8"/>
            <circle cx="40" cy="72" r="3" fill="currentColor"/>
          </svg>
          <!-- 初级杰迷: 麦克风 -->
          <svg v-else-if="levelKey === 'JUNIOR'" class="level-svg" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="32" y="12" width="16" height="32" rx="8" stroke="currentColor" stroke-width="3"/>
            <path d="M24 40C24 52 30 62 40 62C50 62 56 52 56 40" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <line x1="40" y1="62" x2="40" y2="74" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <line x1="30" y1="74" x2="50" y2="74" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
          </svg>
          <!-- 中级杰迷: 耳机 -->
          <svg v-else-if="levelKey === 'INTERMEDIATE'" class="level-svg" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M16 48V36C16 22.745 26.745 12 40 12C53.255 12 64 22.745 64 36V48" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <rect x="10" y="44" width="18" height="20" rx="6" stroke="currentColor" stroke-width="3"/>
            <rect x="52" y="44" width="18" height="20" rx="6" stroke="currentColor" stroke-width="3"/>
            <path d="M28 54C28 48 24 44 22 44" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <path d="M52 54C52 48 56 44 58 44" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
          </svg>
          <!-- 高级杰迷: 奖杯 -->
          <svg v-else-if="levelKey === 'SENIOR'" class="level-svg" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M24 16H56V28C56 38 48 44 40 44C32 44 24 38 24 28V16Z" stroke="currentColor" stroke-width="3"/>
            <path d="M24 20H16V28C16 34 22 38 26 38" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <path d="M56 20H64V28C64 34 58 38 54 38" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <rect x="30" y="44" width="20" height="8" rx="2" fill="currentColor"/>
            <rect x="26" y="52" width="28" height="5" rx="2.5" fill="currentColor" opacity="0.6"/>
            <path d="M36 57L33 68H47L44 57" stroke="currentColor" stroke-width="3" stroke-linejoin="round"/>
            <circle cx="40" cy="70" r="2" fill="currentColor" opacity="0.4"/>
          </svg>
          <!-- 终极杰迷: 皇冠 -->
          <svg v-else class="level-svg" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M16 52L24 20L40 32L56 20L64 52" stroke="currentColor" stroke-width="3" stroke-linejoin="round"/>
            <path d="M16 52H64V62H16V52Z" stroke="currentColor" stroke-width="3"/>
            <line x1="24" y1="62" x2="24" y2="68" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <line x1="56" y1="62" x2="56" y2="68" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            <rect x="20" y="56" width="40" height="3" rx="1.5" fill="currentColor" opacity="0.5"/>
            <circle cx="24" cy="20" r="4" fill="currentColor" opacity="0.4"/>
            <circle cx="40" cy="32" r="4" fill="currentColor" opacity="0.4"/>
            <circle cx="56" cy="20" r="4" fill="currentColor" opacity="0.4"/>
          </svg>
        </div>
        <h1 class="level-title" :style="{ color: levelColor }">
          {{ result.levelTitle }}
        </h1>
        <p class="level-desc">
          {{ result.levelDescription }}
        </p>
      </section>

      <!-- ===== 得分圆环 ===== -->
      <section class="score-section">
        <div class="score-ring">
          <svg class="ring-svg" viewBox="0 0 160 160">
            <defs>
              <linearGradient id="ring-grad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#c9a84c" />
                <stop offset="50%" stop-color="#e0cc8e" />
                <stop offset="100%" stop-color="#b8973b" />
              </linearGradient>
            </defs>
            <!-- 背景轨道 -->
            <circle cx="80" cy="80" r="66" fill="none" stroke="currentColor"
              stroke-width="6" opacity="0.08" />
            <!-- 得分弧 -->
            <circle cx="80" cy="80" r="66" fill="none" stroke="url(#ring-grad)"
              stroke-width="6" stroke-linecap="round"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="dashOffset"
              class="ring-arc" />
          </svg>
          <div class="score-inner">
            <span class="score-number">{{ result.correctCount }}</span>
            <span class="score-divider">/</span>
            <span class="score-total">{{ result.totalQuestions }}</span>
          </div>
        </div>
        <p class="accuracy-text">
          <template v-if="isAbyss">
            深渊深度 <strong>{{ result.correctCount }}</strong> 层
          </template>
          <template v-else>
            正确率
            <strong>{{ (result.accuracy * 100).toFixed(0) }}%</strong>
          </template>
          <template v-if="result.beatPercentage > 0">
            · 击败
            <strong class="beat-highlight">{{ result.beatPercentage }}%</strong>
            的杰迷
          </template>
        </p>
      </section>

      <!-- ===== 数据面板 ===== -->
      <section class="stats-section">
        <div class="stats-grid">
          <div class="stat-card glass-card">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
            </svg>
            <span class="stat-val">{{ result.correctCount }}<small>{{ isAbyss ? '层' : '题' }}</small></span>
            <span class="stat-lbl">{{ isAbyss ? '深渊深度' : '答对题数' }}</span>
          </div>
          <div class="stat-card glass-card">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            <span class="stat-val">{{ formatTime(result.timeSpentSecs) }}</span>
            <span class="stat-lbl">用时</span>
          </div>
          <div class="stat-card glass-card">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />
            </svg>
            <span class="stat-val stat-val--gold">{{ result.beatPercentage }}%</span>
            <span class="stat-lbl">击败率</span>
          </div>
          <div class="stat-card glass-card">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
            </svg>
            <span class="stat-val">{{ formatNumber(result.totalPlayers) }}</span>
            <span class="stat-lbl">总玩家</span>
          </div>
        </div>

        <div v-if="usedRevival" class="revival-tag glass-card">
          <svg class="revival-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 4v6h6" />
            <path d="M3.51 15a9 9 0 102.13-9.36L1 10" />
          </svg>
          <span>{{ isAbyss ? '本局使用了深渊续命' : '本轮使用了复活机会' }}</span>
        </div>
      </section>

      <!-- ===== 专辑闯关结果 ===== -->
      <section v-if="result.albumResult" class="album-result-section glass-card">
        <h3 class="album-result-title">
          {{ result.albumResult.albumDisplayName }}
        </h3>
        <div class="album-result-detail">
          <p v-if="result.albumResult.passed" class="album-passed">
            ✅ 通关成功！本局答对 {{ result.correctCount }} 题
          </p>
          <p v-else class="album-failed">
            ❌ 本局未达到通关要求，再挑战一次吧
          </p>
          <p v-if="result.albumResult.unlockedNext" class="album-next">
            🎉 已解锁下一专辑：<strong>{{ result.albumResult.nextAlbumDisplayName }}</strong>
          </p>
          <p v-if="result.albumResult.isNewRecord" class="album-new-record">
            🏆 刷新个人最佳纪录！
          </p>
        </div>
        <div class="album-result-stats">
          <span class="album-best">历史最佳：<strong>答对 {{ result.albumResult.albumBestScore }} 题</strong></span>
        </div>
      </section>

      <!-- ===== 操作按钮 ===== -->
      <section class="action-section">
        <!-- 游客：登录引导 -->
        <div v-if="authStore.isGuest" class="login-prompt glass-card">
          <p class="prompt-text">🔒 登录后查看排行榜</p>
          <p class="prompt-sub">看看你的成绩能排第几！</p>
          <button class="btn-login-prompt" @click="router.push('/login')">登录 / 注册</button>
        </div>

        <button class="btn-cert" @click="goCertificate">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 15a7 7 0 100-14 7 7 0 000 14z" />
            <path d="M8.21 13.89L7 23l5-3 5 3-1.21-9.12" />
          </svg>
          查看证书
        </button>
        <!-- 登录用户：查看排行榜 -->
        <button v-if="authStore.isLoggedIn" class="btn-leaderboard" @click="goLeaderboard">
          🏆 查看排行榜
        </button>
        <!-- 专辑模式专属按钮 -->
        <template v-if="result.albumResult">
          <button
            v-if="result.albumResult.unlockedNext"
            class="btn-next-album"
            @click="handleNextAlbum"
          >
            🎵 挑战下一专辑：{{ result.albumResult.nextAlbumDisplayName }}
          </button>
          <button class="btn-retry-album" @click="handleRetryAlbum">
            🔄 重试本专辑
          </button>
          <button class="btn-back-albums" @click="router.push('/albums')">
            📀 返回专辑列表
          </button>
        </template>
        <button v-if="!result.albumResult" class="btn-retry" :disabled="retrying" @click="handleRetry">
          <svg v-if="!retrying" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 4v6h6" />
            <path d="M3.51 15a9 9 0 102.13-9.36L1 10" />
          </svg>
          <span v-if="!retrying">{{ isAbyss ? '再次挑战深渊' : '再来一局' }}</span>
          <span v-else class="retry-spinner"></span>
        </button>
        <button class="btn-home" @click="goHome">返回首页</button>
      </section>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round"
          d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9 5.25h.008v.008H12v-.008z" />
      </svg>
      <p class="empty-text">还没有游戏结果</p>
      <button class="btn-cert" @click="goHome">去挑战</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import { useQuiz } from '@/composables/useQuiz'
import { showFailToast } from 'vant'
import { formatTime } from '@/utils/format'
import { LEVELS, ABYSS_LEVELS } from '@/utils/constants'
import { getAbyssLevelByStreak } from '@/utils/levels'
import type { GameMode, GameResult } from '@/stores/gameStore'

const router = useRouter()
const gameStore = useGameStore()
const authStore = useAuthStore()
const { startNewRound, startAlbumRound, startAbyssRound } = useQuiz()

const retrying = ref(false)

const isAbyss = computed(() => gameStore.mode === 'ABYSS')

// 得分圆环
const circumference = 2 * Math.PI * 66 // r=66
const dashOffset = computed(() => {
  if (!result.value) return circumference
  const ratio = result.value.correctCount / result.value.totalQuestions
  return circumference * (1 - ratio)
})

// 结果数据
const result = computed<GameResult | null>(() => {
  return gameStore.lastGameResult
})

const levelKey = computed(() => {
  if (!result.value) return 'PASSERBY'
  if (isAbyss.value) {
    return getAbyssLevelByStreak(result.value.correctCount).key
  }
  return LEVELS.find(
    l => result.value!.correctCount >= l.minScore && result.value!.correctCount <= l.maxScore
  )?.key || 'PASSERBY'
})

const levelColor = computed(() => {
  if (isAbyss.value) {
    return getAbyssLevelByStreak(result.value?.correctCount || 0).color
  }
  return LEVELS.find(l => l.key === levelKey.value)?.color || 'var(--app-text-muted)'
})

const usedRevival = computed(() => {
  return result.value?.usedRevival ?? gameStore.revivalUsed
})

onMounted(() => {
  if (!result.value) {
    // 无数据，稍后通过 watch 或保持空状态
  }
})

function formatNumber(n: number): string {
  if (!n || n === 0) return '--'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toLocaleString()
}

function goCertificate() {
  router.push('/certificate')
}

function getLeaderboardType(mode: GameMode) {
  if (mode === 'ALBUM') return 'album'
  if (mode === 'ABYSS') return 'abyss'
  return 'classic'
}

function goLeaderboard() {
  const mode = result.value?.mode || gameStore.mode
  router.push({ path: '/leaderboard', query: { type: getLeaderboardType(mode) } })
}

async function handleRetry() {
  retrying.value = true
  try {
    // 先记住当前模式，因为 resetGame() 会把 mode 重置为 CLASSIC
    const wasAbyss = isAbyss.value
    gameStore.resetGame()
    if (wasAbyss) {
      await startAbyssRound()
    } else {
      await startNewRound()
    }
    router.push('/quiz')
  } catch (e: any) {
    showFailToast(e.message || '加载失败，请重试')
  } finally {
    retrying.value = false
  }
}

async function handleNextAlbum() {
  const nextKey = result.value?.albumResult?.nextAlbumKey
  if (!nextKey) return
  retrying.value = true
  try {
    gameStore.resetGame()
    await startAlbumRound(nextKey)
    router.push('/quiz')
  } catch (e: any) {
    showFailToast(e.message || '加载失败')
  } finally {
    retrying.value = false
  }
}

async function handleRetryAlbum() {
  const albumKey = gameStore.albumKey || result.value?.albumResult?.albumKey
  if (!albumKey) return
  retrying.value = true
  try {
    gameStore.resetGame()
    await startAlbumRound(albumKey)
    router.push('/quiz')
  } catch (e: any) {
    showFailToast(e.message || '加载失败')
  } finally {
    retrying.value = false
  }
}

function goHome() {
  gameStore.resetGame()
  router.push('/')
}
</script>

<style scoped lang="scss">
/* ======== 背景装饰 ======== */
.bg-orb {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;

  &--top {
    width: 260px;
    height: 260px;
    top: -60px;
    left: -80px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.1) 0%, transparent 70%);
  }

  &--bottom {
    width: 200px;
    height: 200px;
    bottom: 15%;
    right: -60px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.06) 0%, transparent 70%);
  }
}

/* ======== 页面 ======== */
.result-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.result-content {
  width: 100%;
  max-width: 400px;
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

/* ======== 等级展示 ======== */
.level-section {
  text-align: center;
  margin-bottom: 32px;
  animation: fade-in-up 0.6s ease-out both;
}

.level-icon-wrap {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float 3s ease-in-out infinite;

  .level-svg {
    width: 48px;
    height: 48px;
  }

  &.icon-passerby {
    background: rgba(144, 147, 153, 0.12);
    color: var(--app-text-muted);
  }
  &.icon-junior {
    background: rgba(103, 194, 58, 0.12);
    color: var(--app-success);
  }
  &.icon-intermediate {
    background: rgba(64, 158, 255, 0.12);
    color: var(--app-info);
  }
  &.icon-senior {
    background: rgba(230, 162, 60, 0.12);
    color: var(--app-warning);
  }
  &.icon-ultimate {
    background: rgba(245, 108, 108, 0.12);
    color: var(--app-error);
  }
}

.level-title {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.level-desc {
  font-size: 14px;
  color: var(--app-text-secondary);
  line-height: 1.6;
  max-width: 280px;
  margin: 0 auto;
}

/* ======== 得分圆环 ======== */
.score-section {
  text-align: center;
  margin-bottom: 28px;
  animation: fade-in-up 0.6s ease-out 0.15s both;
}

.score-ring {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 16px;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-arc {
  transition: stroke-dashoffset 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.score-inner {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.score-number {
  font-family: 'Poppins', sans-serif;
  font-size: 48px;
  font-weight: 800;
  color: var(--app-text-primary);
  line-height: 1;
}

.score-divider {
  font-size: 20px;
  color: var(--app-text-muted);
  margin: 0 2px;
}

.score-total {
  font-size: 20px;
  color: var(--app-text-muted);
}

.accuracy-text {
  font-size: 14px;
  color: var(--app-text-secondary);

  strong {
    color: var(--app-gold);
    font-weight: 700;
  }

  .beat-highlight {
    font-family: 'Poppins', sans-serif;
  }
}

/* ======== 数据面板 ======== */
.stats-section {
  width: 100%;
  margin-bottom: 28px;
  animation: fade-in-up 0.6s ease-out 0.3s both;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.stat-card {
  padding: 18px 16px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  width: 20px;
  height: 20px;
  color: var(--app-text-muted);
}

.stat-val {
  font-family: 'Poppins', sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: var(--app-text-primary);
  line-height: 1;

  small {
    font-size: 12px;
    font-weight: 500;
    color: var(--app-text-muted);
  }

  &--gold {
    color: var(--app-gold);
  }
}

.stat-lbl {
  font-size: 12px;
  color: var(--app-text-muted);
}

/* 复活标记 */
.revival-tag {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px 20px;
  color: var(--app-warning);
  font-size: 13px;
  font-weight: 500;
  border-color: rgba(234, 179, 8, 0.2);
  background: rgba(234, 179, 8, 0.06);
}

.revival-icon {
  width: 18px;
  height: 18px;
  color: var(--app-warning);
}

/* ======== 登录引导 ======== */
.login-prompt {
  text-align: center;
  padding: 18px 20px;
  border: 1px dashed rgba(var(--app-accent-rgb), 0.3);
  background: rgba(var(--app-accent-rgb), 0.06);
}

.prompt-text {
  font-size: 15px;
  color: var(--app-gold);
  font-weight: 600;
  margin-bottom: 4px;
}

.prompt-sub {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-bottom: 12px;
}

.btn-login-prompt {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 28px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  color: var(--app-text-on-accent);
  background: var(--app-gold-gradient);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(var(--app-accent-rgb), 0.3);
  }
}

.btn-leaderboard {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  height: 48px;
  border: 1.5px solid rgba(var(--app-accent-rgb), 0.3);
  border-radius: 14px;
  background: rgba(var(--app-accent-rgb), 0.08);
  color: var(--app-gold);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.25s ease;

  &:hover {
    background: rgba(var(--app-accent-rgb), 0.14);
    border-color: var(--app-gold);
  }
}

/* ======== 操作按钮 ======== */
.action-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  animation: fade-in-up 0.6s ease-out 0.45s both;
}

.btn-cert {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  font-size: 17px;
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

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 28px rgba(var(--app-accent-rgb), 0.45);
  }

  &:active {
    transform: scale(0.98);
  }
}

.btn-retry {
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

  &:hover:not(:disabled) {
    background: rgba(var(--app-accent-rgb), 0.12);
    border-color: rgba(var(--app-accent-rgb), 0.5);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* ======== 专辑闯关结果 ======== */
.album-result-section {
  width: 100%;
  padding: 20px;
  margin-bottom: 16px;
  text-align: center;
  border-color: rgba(var(--app-accent-rgb), 0.15);
  background: rgba(var(--app-accent-rgb), 0.04);
}

.album-result-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--app-gold);
  margin-bottom: 12px;
}

.album-result-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.album-passed {
  font-size: 15px;
  color: var(--app-success);
  font-weight: 600;
}

.album-failed {
  font-size: 14px;
  color: var(--app-error);
  font-weight: 500;
}

.album-next {
  font-size: 15px;
  color: var(--app-gold);
  font-weight: 600;

  strong {
    font-weight: 800;
  }
}

.album-new-record {
  font-size: 13px;
  color: var(--app-warning);
  font-weight: 600;
}

.album-result-stats {
  .album-best {
    font-size: 13px;
    color: var(--app-text-muted);

    strong {
      color: var(--app-gold);
      font-weight: 700;
    }
  }
}

.btn-next-album {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  color: var(--app-text-on-accent);
  background: var(--app-gold-gradient);
  box-shadow: 0 4px 20px rgba(var(--app-accent-rgb), 0.3);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 28px rgba(var(--app-accent-rgb), 0.45);
  }

  &:active {
    transform: scale(0.98);
  }
}

.btn-retry-album {
  display: inline-flex;
  align-items: center;
  justify-content: center;
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

  &:hover {
    background: rgba(var(--app-accent-rgb), 0.12);
    border-color: rgba(var(--app-accent-rgb), 0.5);
  }
}

.btn-back-albums {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 48px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  background: rgba(var(--app-surface-rgb), 0.03);
  color: var(--app-text-secondary);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;

  &:hover {
    background: rgba(var(--app-surface-rgb), 0.06);
    color: var(--app-text-primary);
  }
}

.btn-home {
  border: none;
  background: none;
  color: var(--app-text-muted);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    color: var(--app-text-secondary);
    background: rgba(var(--app-surface-rgb), 0.04);
  }
}

.retry-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(var(--app-accent-rgb), 0.2);
  border-top-color: var(--app-gold);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

/* ======== 空状态 ======== */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 60px 20px;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: var(--app-text-muted);
  margin-bottom: 8px;
}

.empty-text {
  font-size: 16px;
  color: var(--app-text-secondary);
}

/* ======== 动画 ======== */

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ======== 空状态下的按钮 ======== */
.empty-state .btn-cert {
  width: auto;
  padding: 12px 36px;
  height: auto;
}
</style>
