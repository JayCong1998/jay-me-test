<template>
  <div class="home-page page-bg">
    <!-- 背景装饰光斑 -->
    <div class="bg-orb bg-orb--top"></div>
    <div class="bg-orb bg-orb--bottom"></div>

    <div class="home-content">
      <!-- ===== Hero 区域 ===== -->
      <section class="hero-section">
        <div class="logo-wrapper">
          <svg class="logo-icon" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="gold-grad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#c9a84c" />
                <stop offset="50%" stop-color="#e0cc8e" />
                <stop offset="100%" stop-color="#b8973b" />
              </linearGradient>
            </defs>
            <circle cx="40" cy="40" r="38" stroke="url(#gold-grad)" stroke-width="1.5" fill="none" opacity="0.3" />
            <circle cx="40" cy="40" r="30" stroke="url(#gold-grad)" stroke-width="1" fill="none" opacity="0.15" />
            <!-- 音符 -->
            <g transform="translate(26, 20)" fill="url(#gold-grad)">
              <ellipse cx="10" cy="32" rx="8" ry="6" />
              <rect x="17" y="4" width="3" height="28" rx="1.5" />
              <path d="M20 4 Q28 8 24 18 Q22 22 20 24" fill="url(#gold-grad)" />
            </g>
          </svg>
        </div>
        <h1 class="app-title text-gold">杰迷结业考试</h1>
        <p class="app-subtitle">测试你的杰伦知识储备，解锁专属杰迷等级</p>
      </section>

      <!-- ===== 用户身份卡片 ===== -->
      <!-- 登录用户 -->
      <section v-if="authStore.isLoggedIn" class="user-card glass-card">
        <div class="user-avatar has-name">
          <span class="avatar-text">{{ authStore.user!.nickname.charAt(0) }}</span>
        </div>
        <div class="user-info">
          <span class="user-name">Hi, {{ authStore.user!.nickname }}</span>
          <span class="user-hint">已登录 · 可参与排行榜</span>
        </div>
        <button class="btn-logout" @click="handleLogout">退出</button>
        <!-- 历史最佳标签 -->
        <van-tag
          v-if="userStore.bestScore > 0"
          class="best-badge"
          type="primary"
          round
        >
          <span class="badge-label">最佳</span>
          <span class="badge-score">{{ userStore.bestScore }}/10</span>
          <span class="badge-divider">·</span>
          <span class="badge-level">{{ getLevelByScore(userStore.bestScore).title }}</span>
        </van-tag>
      </section>
      <!-- 游客 -->
      <section v-else class="user-card glass-card guest-card">
        <div class="user-avatar">
          <svg viewBox="0 0 24 24" class="avatar-icon" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
          </svg>
        </div>
        <div class="user-info">
          <span class="user-name">游客模式</span>
          <span class="user-hint">登录后可编辑昵称，参与排行榜</span>
        </div>
      </section>

      <!-- ===== 模式选择 ===== -->
      <section class="mode-section">
        <div class="mode-cards">
          <!-- 经典模式 -->
          <button class="mode-card glass-card" :disabled="loading" @click="handleStart">
            <div class="mode-icon mode-icon--classic">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M4 6h16v2H4zm0 5h12v2H4zm0 5h8v2H4z"/>
              </svg>
            </div>
            <h3 class="mode-title">经典模式</h3>
            <p class="mode-desc">
              <template v-if="loading">
                <span class="loading-dot"></span> 加载中...
              </template>
              <template v-else>
                随机 10 题 · 游客可玩
              </template>
            </p>
          </button>

          <!-- 专辑闯关 -->
          <button class="mode-card glass-card mode-card--album" @click="handleAlbumMode">
            <div class="mode-icon mode-icon--album">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <circle cx="12" cy="12" r="10" opacity="0.15"/>
                <circle cx="12" cy="12" r="5"/>
                <circle cx="12" cy="4" r="1.5"/>
              </svg>
            </div>
            <h3 class="mode-title">专辑闯关</h3>
            <p class="mode-desc">
              15 张专辑 · 8/10 解锁下一关<template v-if="authStore.isGuest"> · 需登录</template>
            </p>
          </button>

          <!-- 无尽深渊 -->
          <button class="mode-card glass-card mode-card--abyss" :disabled="loading" @click="handleAbyssStart">
            <div class="mode-icon mode-icon--abyss">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L2 22h20L12 2zm0 4l7 14H5l7-14z"/>
              </svg>
            </div>
            <h3 class="mode-title">无尽深渊</h3>
            <p class="mode-desc">
              一错即坠 · 无限挑战<template v-if="authStore.isGuest"> · 需登录</template>
            </p>
          </button>
        </div>
      </section>

      <!-- ===== 全局统计 ===== -->
      <section class="stats-section" v-if="overview">
        <div class="stats-grid">
          <div class="stat-card glass-card">
            <span class="stat-number">{{ formatNumber(overview.totalGames) }}</span>
            <span class="stat-label">累计考试</span>
          </div>
          <div class="stat-card glass-card">
            <span class="stat-number">{{ formatNumber(overview.totalPlayers) }}</span>
            <span class="stat-label">考生人数</span>
          </div>
          <div class="stat-card glass-card">
            <span class="stat-number stat-number--accent">{{ overview.averageScore }}</span>
            <span class="stat-label">平均分</span>
          </div>
        </div>
      </section>

      <!-- ===== 主题切换 ===== -->
      <section class="theme-section">
        <ThemeSwitcher />
      </section>

      <!-- ===== 考试记录 ===== -->
      <section class="history-section" v-if="userStore.gameHistory.length > 0">
        <div class="section-header">
          <h3 class="section-title">考试记录</h3>
          <span class="section-count">{{ userStore.gameHistory.length }} 场</span>
        </div>
        <div class="history-list glass-card">
          <div
            v-for="(record, idx) in userStore.gameHistory.slice(0, 8)"
            :key="idx"
            class="history-item"
          >
            <div class="history-left">
              <span class="history-date">{{ record.date }}</span>
              <span class="history-meta">
                {{ formatDuration(record.timeSpentSecs) }}
                <template v-if="record.usedRevival"> · 已复活</template>
              </span>
            </div>
            <div class="history-right">
              <span class="history-score">{{ record.correctCount }}<small>/10</small></span>
              <span class="history-level" :style="{ color: getLevelColor(record.correctCount) }">
                {{ getLevelByScore(record.correctCount).title }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部间距 -->
      <div class="bottom-spacer"></div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useAuthStore } from '@/stores/authStore'
import { useQuiz } from '@/composables/useQuiz'
import { fetchOverview, fetchMyRecords } from '@/api/statsApi'
import { showFailToast } from 'vant'
import { getLevelByScore } from '@/utils/levels'
import { LEVELS } from '@/utils/constants'
import ThemeSwitcher from '@/components/common/ThemeSwitcher.vue'
import type { StatsOverview } from '@/api/statsApi'

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()
const { startNewRound, startAbyssRound } = useQuiz()

// --- 状态 ---
const loading = ref(false)
const overview = ref<StatsOverview | null>(null)

// --- 生命周期 ---
onMounted(async () => {
  // 加载全局统计
  try {
    overview.value = await fetchOverview()
  } catch {
    // 统计加载失败不阻塞
  }

  // 登录用户从服务端同步考试记录
  if (authStore.isLoggedIn) {
    try {
      const serverRecords = await fetchMyRecords()
      if (serverRecords.length > 0) {
        userStore.syncFromServer(serverRecords)
      }
    } catch {
      // 同步失败不阻塞，使用本地记录
    }
  }
})

// --- 方法 ---
async function handleStart() {
  // 游客自动生成唯一昵称
  if (!authStore.isLoggedIn) {
    const ts = Date.now().toString(36).toUpperCase()
    userStore.setNickname(`游客${ts}`)
  }

  loading.value = true
  try {
    await startNewRound()
    router.push('/quiz')
  } catch (e: any) {
    showFailToast(e.message || '加载题目失败，请重试')
  } finally {
    loading.value = false
  }
}

function formatNumber(n: number): string {
  if (n >= 10000) {
    return (n / 10000).toFixed(1) + '万'
  }
  return n.toLocaleString()
}

function getLevelColor(score: number): string {
  const level = LEVELS.find(l => score >= l.minScore && score <= l.maxScore)
  return level?.color || 'var(--app-text-muted)'
}

function handleAlbumMode() {
  if (authStore.isGuest) {
    router.push('/login?redirect=' + encodeURIComponent('/albums'))
    return
  }
  router.push('/albums')
}

async function handleAbyssStart() {
  if (authStore.isGuest) {
    router.push('/login?redirect=' + encodeURIComponent('/quiz') + '&mode=abyss')
    return
  }

  loading.value = true
  try {
    await startAbyssRound()
    router.push('/quiz')
  } catch (e: any) {
    showFailToast(e.message || '加载题目失败，请重试')
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  authStore.logout()
  userStore.reset()
}

function formatDuration(secs: number): string {
  const m = Math.floor(secs / 60)
  const s = secs % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
/* ========================================
   背景装饰
   ======================================== */
.bg-orb {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;

  &--top {
    width: 280px;
    height: 280px;
    top: -80px;
    right: -60px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.12) 0%, transparent 70%);
  }

  &--bottom {
    width: 240px;
    height: 240px;
    bottom: 10%;
    left: -80px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.08) 0%, transparent 70%);
  }
}

/* ========================================
   页面容器
   ======================================== */
.home-page {
  position: relative;
  padding: 20px 20px 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.home-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

/* ========================================
   Hero 区域
   ======================================== */
.hero-section {
  text-align: center;
  padding: 48px 0 36px;
  animation: fade-in-up 0.7s ease-out;
}

.logo-wrapper {
  margin-bottom: 20px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  animation: float 3s ease-in-out infinite;
}

.app-title {
  font-family: var(--app-font-heading), 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 3px;
  margin-bottom: 10px;
  background-size: 200% auto;
  animation: shimmer 4s linear infinite;
}

.app-subtitle {
  font-size: 15px;
  color: var(--app-text-secondary);
  letter-spacing: 1px;
}

/* ========================================
   用户卡片
   ======================================== */
.user-card {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  margin-bottom: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fade-in-up 0.7s ease-out 0.1s both;

  &:hover {
    background: var(--app-bg-card-hover);
    border-color: rgba(var(--app-accent-rgb), 0.2);
  }

  &:active {
    transform: scale(0.985);
  }

  &.guest-card {
    cursor: default;

    &:hover {
      background: none;
      border-color: transparent;
    }

    &:active {
      transform: none;
    }
  }
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(var(--app-accent-rgb), 0.12);
  border: 1.5px solid rgba(var(--app-accent-rgb), 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 14px;
  transition: all 0.3s ease;

  &.has-name {
    background: var(--app-gold-gradient);
    border-color: transparent;
  }

  .avatar-text {
    font-size: 20px;
    font-weight: 700;
    color: var(--app-text-on-accent);
    line-height: 1;
  }

  .avatar-icon {
    width: 24px;
    height: 24px;
    color: var(--app-text-muted);
  }
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--app-text-primary);
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-hint {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-top: 2px;
  display: block;
}

.best-badge {
  flex-shrink: 0;
  margin-left: auto;
  font-weight: 600;

  .badge-label {
    font-size: 11px;
    opacity: 0.8;
    margin-right: 2px;
  }

  .badge-score {
    font-family: var(--app-font-display), sans-serif;
    font-size: 13px;
  }

  .badge-divider {
    opacity: 0.4;
    margin: 0 2px;
  }

  .badge-level {
    font-size: 12px;
  }
}

/* ========================================
   模式选择
   ======================================== */
.mode-section {
  margin-bottom: 32px;
  animation: fade-in-up 0.7s ease-out 0.2s both;
}

.mode-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  /* 第三个卡片（深渊）独占一行 */
  .mode-card:nth-child(3):last-child {
    grid-column: 1 / -1;
  }
}

.mode-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px 20px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-bg-card);
  cursor: pointer;
  font-family: inherit;
  color: inherit;
  transition: all 0.3s ease;
  text-align: center;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    border-color: rgba(var(--app-accent-rgb), 0.25);
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.2);
  }

  &:active:not(:disabled) {
    transform: scale(0.97);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &--album {
    position: relative;
    overflow: hidden;

    &::before {
      content: 'NEW';
      position: absolute;
      top: 8px;
      right: -24px;
      padding: 2px 28px;
      background: var(--app-gold-gradient);
      color: var(--app-text-on-accent);
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 1px;
      transform: rotate(45deg);
    }
  }

  &--abyss {
    position: relative;
    overflow: hidden;
    border-color: rgba(124, 58, 237, 0.2);

    &::before {
      content: 'NEW';
      position: absolute;
      top: 8px;
      right: -24px;
      padding: 2px 28px;
      background: linear-gradient(135deg, #7c3aed, #dc2626);
      color: var(--app-text-on-accent);
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 1px;
      transform: rotate(45deg);
      z-index: 1;
    }

    &:hover:not(:disabled) {
      border-color: rgba(124, 58, 237, 0.4);
      box-shadow: 0 6px 24px rgba(124, 58, 237, 0.15);
    }
  }
}

.mode-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;

  svg {
    width: 24px;
    height: 24px;
  }

  &--classic {
    background: rgba(64, 158, 255, 0.12);
    color: var(--app-info);
  }

  &--album {
    background: rgba(var(--app-accent-rgb), 0.12);
    color: var(--app-gold);
  }

  &--abyss {
    background: rgba(124, 58, 237, 0.12);
    color: var(--app-info);
  }
}

.mode-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text-primary);
  margin-bottom: 6px;
}

.mode-desc {
  font-size: 12px;
  color: var(--app-text-muted);
  line-height: 1.5;
}

.loading-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--app-gold);
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

/* ========================================
   全局统计
   ======================================== */
.stats-section {
  margin-bottom: 28px;
  animation: fade-in-up 0.7s ease-out 0.3s both;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-card {
  padding: 18px 12px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-family: var(--app-font-display), sans-serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--app-text-primary);

  &--accent {
    color: var(--app-gold);
  }
}

.stat-label {
  font-size: 12px;
  color: var(--app-text-muted);
}

/* ========================================
   考试记录
   ======================================== */
.theme-section {
  margin-bottom: 20px;
}

.history-section {
  margin-bottom: 20px;
  animation: fade-in-up 0.7s ease-out 0.4s both;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 0 4px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.section-count {
  font-family: var(--app-font-display), sans-serif;
  font-size: 12px;
  color: var(--app-text-muted);
}

.history-list {
  overflow: hidden;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--app-border);
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--app-bg-card-hover);
  }
}

.history-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.history-date {
  font-size: 14px;
  color: var(--app-text-primary);
}

.history-meta {
  font-size: 11px;
  color: var(--app-text-muted);
}

.history-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.history-score {
  font-family: var(--app-font-display), sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--app-gold);

  small {
    font-size: 12px;
    font-weight: 500;
    color: var(--app-text-muted);
  }
}

.history-level {
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.btn-logout {
  flex-shrink: 0;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid rgba(var(--app-surface-rgb),0.1);
  border-radius: 8px;
  background: rgba(var(--app-surface-rgb),0.04);
  color: var(--app-text-muted);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
  margin-right: 8px;

  &:hover {
    color: var(--app-error);
    border-color: rgba(245,108,108,0.3);
  }
}

.auth-links {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 14px;
}

.btn-secondary {
  padding: 10px 28px;
  border: 1px solid rgba(var(--app-accent-rgb),0.3);
  border-radius: 12px;
  background: rgba(var(--app-accent-rgb),0.06);
  color: var(--app-gold);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;

  &:hover {
    background: rgba(var(--app-accent-rgb),0.12);
    border-color: var(--app-gold);
  }
}

.login-hint {
  margin-top: 14px;
  font-size: 13px;
  color: var(--app-text-muted);
}

.login-link {
  color: var(--app-gold);
  text-decoration: none;
  font-weight: 600;

  &:hover { text-decoration: underline; }
}

.bottom-spacer {
  height: 40px;
}
</style>
