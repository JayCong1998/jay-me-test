<template>
  <van-pull-refresh v-model="refreshing" :disabled="!isPullRefreshEnabled" @refresh="onRefresh">
    <div ref="scrollRoot" class="profile-page page-bg" @scroll="onScroll">
      <!-- 背景装饰光斑 -->
      <div class="bg-orb bg-orb--top"></div>
      <div class="bg-orb bg-orb--bottom"></div>

      <div class="profile-content">
        <!-- ===== 用户身份卡片 ===== -->
        <section class="profile-section">
          <!-- 登录用户 -->
          <div v-if="authStore.isLoggedIn" class="user-card glass-card">
            <div class="user-avatar has-name">
              <span class="avatar-text">{{ authStore.user!.nickname.charAt(0) }}</span>
            </div>
            <div class="user-info">
              <span class="user-name">{{ authStore.user!.nickname }}</span>
              <span class="user-status">已登录</span>
            </div>
            <button class="btn-logout" @click="handleLogout">退出登录</button>
          </div>
          <!-- 游客 -->
          <div v-else class="guest-panel glass-card">
            <div class="guest-main">
              <div class="user-avatar">
                <svg viewBox="0 0 24 24" class="avatar-icon" fill="none" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                    d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
                </svg>
              </div>
              <div class="user-info">
                <span class="user-name">游客模式</span>
                <span class="user-status">登录后保存成绩、查看记录、参与排行</span>
              </div>
            </div>
            <div class="auth-actions">
              <button class="btn-auth btn-login" @click="router.push('/login?redirect=/profile')">
                登录
              </button>
              <button class="btn-auth btn-register" @click="router.push('/register')">
                注册
              </button>
            </div>
          </div>
        </section>

        <!-- ===== 界面风格（主题切换）===== -->
        <section v-if="authStore.isLoggedIn" class="profile-section">
          <div class="section-header">
            <h3 class="section-title">界面风格</h3>
          </div>
          <div class="theme-card glass-card">
            <ThemeSwitcher />
          </div>
        </section>

        <!-- ===== 游戏记录 ===== -->
        <section class="profile-section">
          <div class="section-header">
            <h3 class="section-title">游戏记录</h3>
            <span v-if="authStore.isLoggedIn && recordStore.records.length > 0" class="section-count">
              {{ recordStore.records.length }}+ 场
            </span>
          </div>

          <!-- 已登录：加载中 -->
          <div v-if="authStore.isLoggedIn && recordStore.loading" class="state-card glass-card">
            <div class="loading-spinner">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="10" stroke-opacity="0.15" />
                <path d="M12 2a10 10 0 019.95 9" stroke-linecap="round" />
              </svg>
            </div>
            <p class="state-text">加载中...</p>
          </div>

          <!-- 已登录：加载失败 -->
          <div v-else-if="authStore.isLoggedIn && recordStore.error" class="state-card glass-card">
            <p class="state-text error-text">{{ recordStore.error }}</p>
            <button class="btn-retry" @click="recordStore.fetchMyRecords()">重试</button>
          </div>

          <!-- 已登录：记录列表 -->
          <div v-else-if="authStore.isLoggedIn && recordStore.records.length > 0" class="history-list glass-card">
            <div
              v-for="item in historyItems"
              :key="item.record.roundId"
              class="history-item"
            >
              <div class="history-left">
                <span
                  class="history-mode"
                  :class="`history-mode--${item.presentation.modeTone}`"
                >
                  {{ item.presentation.modeLabel }}
                </span>
                <span class="history-date">{{ formatDate(item.record.createdAt) }}</span>
                <span class="history-meta">
                  {{ formatDuration(item.record.timeSpentSecs) }}
                  <template v-if="item.record.usedRevival"> · 已复活</template>
                </span>
              </div>
              <div class="history-right">
                <span
                  class="history-score"
                  :class="{ 'history-score--abyss': item.record.mode === 'ABYSS' }"
                >
                  {{ item.presentation.scoreText }}
                </span>
                <span class="history-level" :style="{ color: item.presentation.levelColor }">
                  {{ item.presentation.levelTitle }}
                </span>
              </div>
            </div>

            <!-- 加载更多状态 -->
            <div class="load-more-footer">
              <div v-if="recordStore.loadingMore" class="load-more-loading">
                <span class="loading-spinner"></span>
                <span>加载中...</span>
              </div>
              <p v-else-if="!recordStore.hasMore" class="load-more-end">— 没有更多了 —</p>
            </div>
          </div>

          <!-- 已登录：空记录 -->
          <div v-else-if="authStore.isLoggedIn" class="state-card glass-card">
            <p class="state-text">暂无游戏记录，快去挑战吧！</p>
          </div>

          <div v-else class="state-card glass-card">
            <p class="state-text">登录后可查看你的游戏记录</p>
            <button class="btn-retry" @click="router.push('/login?redirect=/profile')">去登录</button>
          </div>

        </section>

        <!-- ===== App 信息 ===== -->
        <section class="profile-section app-info">
          <p class="app-name">杰迷试炼</p>
        </section>

        <!-- 底部间距 -->
        <div class="bottom-spacer"></div>
      </div>
    </div>
  </van-pull-refresh>
</template>

<script setup lang="ts">
defineOptions({ name: 'ProfilePage' })

import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useRecordStore } from '@/stores/recordStore'
import { useUserStore } from '@/stores/userStore'
import { formatDate } from '@/utils/format'
import { getRecordPresentation } from '@/utils/recordPresentation'
import { getBottomLoadState, isPullRefreshEnabled as getPullRefreshEnabled } from '@/utils/bottomLoadTrigger'
import ThemeSwitcher from '@/components/common/ThemeSwitcher.vue'

const router = useRouter()
const authStore = useAuthStore()
const recordStore = useRecordStore()
const userStore = useUserStore()

const scrollRoot = ref<HTMLElement | null>(null)
const refreshing = ref(false)
const isPullRefreshEnabled = ref(true)
let previousScrollTop = 0
let wasNearBottom = false

const historyItems = computed(() =>
  recordStore.records.map(record => ({
    record,
    presentation: getRecordPresentation(record),
  }))
)

watch(
  () => authStore.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn) {
      recordStore.fetchMyRecords()
    } else {
      recordStore.clear()
    }
  },
  { immediate: true }
)

// keep-alive 激活时不自动刷新，保留缓存数据，用户手动下拉刷新

// 下拉刷新
async function onRefresh() {
  refreshing.value = true
  try {
    if (authStore.isLoggedIn) {
      await recordStore.fetchMyRecords()
    }
  } finally {
    refreshing.value = false
  }
}

// 滚动触底检测
function onScroll() {
  const el = scrollRoot.value
  if (!el) return
  isPullRefreshEnabled.value = getPullRefreshEnabled(el.scrollTop)
  const state = getBottomLoadState({
    scrollTop: el.scrollTop,
    previousScrollTop,
    clientHeight: el.clientHeight,
    scrollHeight: el.scrollHeight,
    wasNearBottom,
  })
  previousScrollTop = el.scrollTop
  wasNearBottom = state.isNearBottom
  if (state.shouldLoad) {
    recordStore.loadMore()
  }
}

function handleLogout() {
  authStore.logout()
  recordStore.clear()
  userStore.reset()
}

function formatDuration(secs: number): string {
  const m = Math.floor(secs / 60)
  const s = secs % 60
  return `${m}:${s.toString().padStart(2, '0')}`
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
    width: 260px;
    height: 260px;
    top: -60px;
    right: -60px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.1) 0%, transparent 70%);
  }

  &--bottom {
    width: 220px;
    height: 220px;
    bottom: 15%;
    left: -70px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.06) 0%, transparent 70%);
  }
}

.profile-page {
  position: relative;
  padding: 20px 20px 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.profile-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

/* ======== 区块 ======== */
.profile-section {
  margin-bottom: 24px;
  animation: fade-in-up 0.6s ease-out both;

  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.1s; }
  &:nth-child(3) { animation-delay: 0.2s; }
  &:nth-child(4) { animation-delay: 0.3s; }
}

/* ======== 用户卡片 ======== */
.user-card {
  display: flex;
  align-items: center;
  padding: 20px;
  transition: all 0.3s ease;
}

.guest-panel {
  padding: 20px;
}

.guest-main {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(var(--app-accent-rgb), 0.12);
  border: 1.5px solid rgba(var(--app-accent-rgb), 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16px;

  &.has-name {
    background: var(--app-gold-gradient);
    border-color: transparent;
  }

  .avatar-text {
    font-size: 22px;
    font-weight: 700;
    color: var(--app-text-on-accent);
    line-height: 1;
  }

  .avatar-icon {
    width: 26px;
    height: 26px;
    color: var(--app-text-muted);
  }
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text-primary);
  display: block;
}

.user-status {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-top: 2px;
  display: block;
}

.btn-logout {
  flex-shrink: 0;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid rgba(var(--app-surface-rgb), 0.1);
  border-radius: 8px;
  background: rgba(var(--app-surface-rgb), 0.04);
  color: var(--app-text-muted);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
  margin-left: 12px;

  &:hover {
    color: var(--app-error);
    border-color: rgba(245, 108, 108, 0.3);
  }
}

/* ======== 游客操作按钮 ======== */
.auth-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.btn-auth {
  flex: 1;
  padding: 12px 0;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
  text-align: center;
}

.btn-login {
  background: var(--app-gold-gradient);
  color: var(--app-text-on-accent);
  border: none;

  &:hover {
    box-shadow: var(--app-shadow-btn);
  }
}

.btn-register {
  background: transparent;
  color: var(--app-gold);
  border: 1px solid rgba(var(--app-accent-rgb), 0.3);

  &:hover {
    background: rgba(var(--app-accent-rgb), 0.06);
  }
}

/* ======== Section Header ======== */
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

/* ======== 状态卡片 ======== */
.state-card {
  padding: 28px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.state-text {
  font-size: 14px;
  color: var(--app-text-muted);

  &.error-text {
    color: var(--app-error);
  }
}

.loading-spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 2px solid rgba(var(--app-accent-rgb), 0.2);
  border-top-color: var(--app-gold);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.btn-retry {
  padding: 8px 24px;
  border: 1px solid rgba(var(--app-accent-rgb), 0.3);
  border-radius: 10px;
  background: rgba(var(--app-accent-rgb), 0.06);
  color: var(--app-gold);
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;

  &:hover {
    background: rgba(var(--app-accent-rgb), 0.12);
  }
}

/* ======== 游戏记录列表 ======== */
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
  align-items: flex-start;
  gap: 3px;
  min-width: 0;
}

.history-mode {
  display: inline-flex;
  max-width: 100%;
  padding: 2px 7px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &--classic {
    color: #d6b65f;
    background: rgba(214, 182, 95, 0.12);
  }

  &--album {
    color: #a78bfa;
    background: rgba(139, 92, 246, 0.12);
  }

  &--abyss {
    color: #f87171;
    background: rgba(239, 68, 68, 0.12);
  }
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
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  text-align: right;
  flex-shrink: 0;
}

.history-score {
  font-family: var(--app-font-display), sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--app-gold);
  white-space: nowrap;

  &--abyss {
    font-size: 15px;
  }
}

.history-level {
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

/* ======== 加载更多 ======== */
.load-more-footer {
  padding: 16px 18px;
  text-align: center;
}

.load-more-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--app-text-muted);
  font-size: 13px;
}

.load-more-end {
  font-size: 12px;
  color: var(--app-text-muted);
  opacity: 0.6;
}

/* ======== 主题卡片 ======== */
.theme-card {
  padding: 20px;
}

/* ======== App 信息 ======== */
.app-info {
  text-align: center;
  padding: 16px 0;

  .app-name {
    font-family: var(--app-font-heading);
    font-size: 15px;
    font-weight: 600;
    color: var(--app-text-primary);
  }
}

.bottom-spacer {
  height: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
