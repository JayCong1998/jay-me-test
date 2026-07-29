<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
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
                  随机题目 · 游客可玩
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
                专辑挑战 · 通关解锁下一关<template v-if="authStore.isGuest"> · 需登录</template>
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
      </div>
    </div>
  </van-pull-refresh>
</template>

<script setup lang="ts">
defineOptions({ name: 'HomePage' })

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useAuthStore } from '@/stores/authStore'
import { useQuiz } from '@/composables/useQuiz'
import { fetchOverview } from '@/api/statsApi'
import { showFailToast } from 'vant'
import type { StatsOverview } from '@/api/statsApi'
import { generateGuestNickname } from '@/utils/nickname'

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()
const { startNewRound, startAbyssRound } = useQuiz()

// --- 状态 ---
const loading = ref(false)
const overview = ref<StatsOverview | null>(null)
const refreshing = ref(false)

// --- 生命周期 ---
onMounted(async () => {
  await loadOverview()
})

async function loadOverview() {
  try {
    overview.value = await fetchOverview()
  } catch {
    // 统计加载失败不阻塞
  }
}

// --- 下拉刷新 ---
async function onRefresh() {
  refreshing.value = true
  try {
    await loadOverview()
  } finally {
    refreshing.value = false
  }
}

// --- 方法 ---
async function handleStart() {
  if (!authStore.isLoggedIn) {
    userStore.setNickname(generateGuestNickname())
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

</script>

<style scoped lang="scss">
/* ======== 背景装饰 ======== */
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

/* ======== 页面容器 ======== */
.home-page {
  position: relative;
  padding: 12px 16px 0;
  display: flex;
  flex-direction: column;
}

.home-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

/* ======== Hero 区域 ======== */
.hero-section {
  text-align: center;
  padding: 20px 0 16px;
  animation: fade-in-up 0.7s ease-out;
}

.logo-wrapper {
  margin-bottom: 12px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  animation: float 3s ease-in-out infinite;
}

.app-title {
  font-family: var(--app-font-heading), 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 2px;
  margin-bottom: 6px;
  background-size: 200% auto;
  animation: shimmer 4s linear infinite;
}

.app-subtitle {
  font-size: 13px;
  color: var(--app-text-secondary);
  letter-spacing: 1px;
}

/* ======== 模式选择 ======== */
.mode-section {
  margin-bottom: 20px;
  animation: fade-in-up 0.7s ease-out 0.2s both;
}

.mode-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;

  .mode-card:nth-child(3):last-child {
    grid-column: 1 / -1;
  }
}

.mode-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18px 12px 14px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
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
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;

  svg {
    width: 20px;
    height: 20px;
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
  font-size: 14px;
  font-weight: 700;
  color: var(--app-text-primary);
  margin-bottom: 4px;
}

.mode-desc {
  font-size: 11px;
  color: var(--app-text-muted);
  line-height: 1.4;
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

/* ======== 全局统计 ======== */
.stats-section {
  animation: fade-in-up 0.7s ease-out 0.3s both;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.stat-card {
  padding: 14px 10px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-number {
  font-family: var(--app-font-display), sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text-primary);

  &--accent {
    color: var(--app-gold);
  }
}

.stat-label {
  font-size: 11px;
  color: var(--app-text-muted);
}</style>
