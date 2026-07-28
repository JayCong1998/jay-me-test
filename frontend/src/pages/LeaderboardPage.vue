<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <div ref="scrollRoot" class="lb-page page-bg" @scroll="onScroll">
      <div class="bg-orb bg-orb--top"></div>
      <div class="bg-orb bg-orb--bottom"></div>

      <div class="lb-content">
        <!-- Header -->
        <section class="lb-header">
          <h1 class="lb-title text-gold">🏆 排行榜</h1>
        </section>

        <section v-if="authStore.isGuest" class="guest-state glass-card">
          <div class="guest-icon">🏆</div>
          <h2 class="guest-title">登录后查看排行榜</h2>
          <p class="guest-desc">保存你的答题成绩，和其他杰迷一起冲榜。</p>
          <div class="guest-actions">
            <button class="btn-login" @click="goLogin">登录</button>
            <button class="btn-register" @click="router.push('/register')">注册</button>
          </div>
        </section>

        <!-- Tabs -->
        <section v-if="authStore.isLoggedIn" class="lb-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="lb-tab"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >{{ tab.label }}</button>
        </section>

        <!-- Loading -->
        <div v-if="authStore.isLoggedIn && loading" class="lb-loading">
          <span class="loading-spinner"></span>
          <p>加载中...</p>
        </div>

        <!-- Error -->
        <div v-else-if="authStore.isLoggedIn && errorMsg" class="lb-error">
          <p>{{ errorMsg }}</p>
          <button class="btn-retry" @click="loadData">重试</button>
        </div>

        <!-- Leaderboard -->
        <template v-else-if="authStore.isLoggedIn && allEntries.length > 0">
          <!-- Top 3 podium -->
          <section v-if="allEntries.length >= 3" class="podium-section">
            <div class="podium">
              <!-- 2nd -->
              <div class="podium-item">
                <div class="podium-avatar silver">
                  <span>{{ allEntries[1].nickname.charAt(0) }}</span>
                </div>
                <div class="podium-card silver-card">
                  <span class="podium-rank">🥈</span>
                  <span class="podium-name">{{ allEntries[1].nickname }}</span>
                  <span class="podium-score">{{ formatScore(allEntries[1]) }}</span>
                  <span class="podium-detail">{{ formatDetail(allEntries[1]) }}</span>
                </div>
              </div>
              <!-- 1st -->
              <div class="podium-item first">
                <div class="podium-avatar gold">
                  <span>{{ allEntries[0].nickname.charAt(0) }}</span>
                </div>
                <div class="podium-card gold-card">
                  <span class="podium-rank">🥇</span>
                  <span class="podium-name">{{ allEntries[0].nickname }}</span>
                  <span class="podium-score">{{ formatScore(allEntries[0]) }}</span>
                  <span class="podium-detail">{{ formatDetail(allEntries[0]) }}</span>
                </div>
              </div>
              <!-- 3rd -->
              <div class="podium-item">
                <div class="podium-avatar bronze">
                  <span>{{ allEntries[2].nickname.charAt(0) }}</span>
                </div>
                <div class="podium-card bronze-card">
                  <span class="podium-rank">🥉</span>
                  <span class="podium-name">{{ allEntries[2].nickname }}</span>
                  <span class="podium-score">{{ formatScore(allEntries[2]) }}</span>
                  <span class="podium-detail">{{ formatDetail(allEntries[2]) }}</span>
                </div>
              </div>
            </div>
          </section>

          <!-- Rank list (4th+) -->
          <section class="rank-list glass-card" v-if="visibleRankList.length > 0">
            <div
              v-for="entry in visibleRankList"
              :key="entry.rank"
              class="rank-item"
            >
              <span class="rank-num">{{ entry.rank }}</span>
              <span class="rank-name">{{ entry.nickname }}</span>
              <span class="rank-level" :style="{ color: getLevelColor(entry) }">
                {{ formatDetail(entry) }}
              </span>
              <span class="rank-score">{{ formatScore(entry) }}</span>
              <span class="rank-time">{{ formatDuration(entry.totalAlbumTimeSecs ?? entry.timeSpentSecs) }}</span>
            </div>

            <!-- 加载更多 -->
            <div class="load-more-footer">
              <div v-if="loadingMore" class="load-more-loading">
                <span class="loading-spinner"></span>
                <span>加载中...</span>
              </div>
              <p v-else-if="!hasMore" class="load-more-end">— 没有更多了 —</p>
            </div>
          </section>

          <!-- My rank -->
          <section v-if="myRank" class="my-rank glass-card">
            <span class="my-rank-label">我的排名</span>
            <span class="my-rank-val text-gold">第 {{ myRank }} 名</span>
          </section>
          <section v-else-if="myRank === null" class="my-rank glass-card">
            <span class="my-rank-label">你还没有上榜成绩，快去答题吧！</span>
          </section>
        </template>

        <!-- Empty -->
        <div v-else-if="authStore.isLoggedIn" class="lb-empty">
          <p>暂无排行数据</p>
        </div>

        <div class="bottom-spacer"></div>
      </div>
    </div>
  </van-pull-refresh>
</template>

<script setup lang="ts">
defineOptions({ name: 'LeaderboardPage' })

import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import * as leaderboardApi from '@/api/leaderboardApi'
import type { LeaderboardResult, LeaderboardEntry, LeaderboardType } from '@/api/leaderboardApi'
import { LEVELS } from '@/utils/constants'
import { getAbyssLevelByStreak } from '@/utils/levels'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'

const router = useRouter()
const authStore = useAuthStore()

const tabs = [
  { key: 'classic' as const, label: '经典模式' },
  { key: 'album' as const, label: '专辑闯关' },
  { key: 'abyss' as const, label: '无尽深渊' },
]

const activeTab = ref<LeaderboardType>('classic')
const isAbyssTab = computed(() => activeTab.value === 'abyss')

const myRank = ref<number | null>(null)
const scrollRoot = ref<HTMLElement | null>(null)

const {
  items: allEntries,
  loading,
  error: errorMsg,
  loadingMore,
  hasMore,
  refreshing,
  loadFirstPage,
  loadMore,
  onRefresh,
  reset,
} = useInfiniteScroll<LeaderboardEntry>({
  pageSize: 10,
  fetchPage: async (page) => {
    const result: LeaderboardResult = await leaderboardApi.fetchLeaderboard(activeTab.value, 10, page, 10)
    if (page === 1) {
      myRank.value = result.myRank
    }
    return { items: result.list }
  },
})

const visibleRankList = computed(() =>
  allEntries.value.length >= 3 ? allEntries.value.slice(3) : allEntries.value
)

function formatScore(entry: LeaderboardEntry): string {
  if (activeTab.value === 'album') {
    return `通关 ${entry.completedAlbumCount}/15`
  }
  if (activeTab.value === 'abyss') {
    return `${entry.streak ?? entry.correctCount} 连对`
  }
  return `${entry.correctCount}/10`
}

function formatDetail(entry: LeaderboardEntry): string {
  if (activeTab.value === 'album') {
    return entry.bestAlbumName ? `最近 ${entry.bestAlbumName}` : '专辑闯关'
  }
  return entry.levelTitle || entry.detailText || ''
}

function switchTab(tab: LeaderboardType) {
  activeTab.value = tab
  loadData()
}

async function loadData() {
  if (!authStore.isLoggedIn) {
    return
  }
  await loadFirstPage()
}

function goLogin() {
  router.push('/login?redirect=' + encodeURIComponent('/leaderboard'))
}

// 滚动触底 - rAF 节流
let scrollPending = false
function onScroll() {
  if (scrollPending) return
  scrollPending = true
  requestAnimationFrame(() => {
    scrollPending = false
    const el = scrollRoot.value
    if (!el) return
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 100) {
      loadMore()
    }
  })
}

function getLevelColor(entry: LeaderboardEntry): string {
  if (isAbyssTab.value) {
    const score = entry.streak ?? entry.correctCount ?? 0
    return getAbyssLevelByStreak(score).color
  }
  if (activeTab.value === 'album') {
    return 'var(--app-gold)'
  }
  const score = entry.correctCount ?? 0
  const level = LEVELS.find(l => score >= l.minScore && score <= l.maxScore)
  return level?.color || 'var(--app-text-muted)'
}

function formatDuration(secs: number): string {
  const m = Math.floor(secs / 60)
  const s = secs % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

watch(
  () => authStore.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn) {
      loadData()
    } else {
      reset()
      myRank.value = null
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
/* ======== 背景 ======== */
.bg-orb {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;

  &--top {
    width: 240px; height: 240px; top: -60px; right: -60px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.1) 0%, transparent 70%);
  }
  &--bottom {
    width: 200px; height: 200px; bottom: 10%; left: -60px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.06) 0%, transparent 70%);
  }
}

.lb-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.lb-content {
  width: 100%;
  max-width: 420px;
  padding: 16px 20px;
  position: relative;
  z-index: 1;
}

/* ======== Header ======== */
.lb-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.lb-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
}

/* ======== Tabs ======== */
.lb-tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid rgba(var(--app-surface-rgb),0.08);
  margin-bottom: 16px;
  animation: fade-in-up 0.5s ease-out;
}

.lb-tab {
  flex: 1;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text-muted);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;

  &.active {
    color: var(--app-gold);
    border-bottom-color: var(--app-gold);
  }
}

/* ======== Guest State ======== */
.guest-state {
  padding: 28px 22px;
  text-align: center;
  animation: fade-in-up 0.5s ease-out;
}

.guest-icon {
  font-size: 42px;
  margin-bottom: 12px;
}

.guest-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text-primary);
}

.guest-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--app-text-secondary);
}

.guest-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
}

.btn-login,
.btn-register {
  flex: 1;
  min-height: 44px;
  border: 0;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}

.btn-login {
  color: var(--app-text-on-accent);
  background: linear-gradient(135deg, var(--app-gold), var(--app-accent));
}

.btn-register {
  color: var(--app-gold);
  background: rgba(var(--app-accent-rgb),0.08);
  border: 1px solid rgba(var(--app-accent-rgb),0.22);
}

/* ======== Podium ======== */
.podium-section {
  margin-bottom: 16px;
  animation: fade-in-up 0.5s ease-out 0.1s both;
}

.podium {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 10px;
}

.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;

  &.first { margin-bottom: 8px; }
}

.podium-avatar {
  width: 44px; height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text-on-accent);

  &.gold { background: linear-gradient(135deg, #ffd700, #ffaa00); }
  &.silver { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); }
  &.bronze { background: linear-gradient(135deg, #cd7f32, #a0522d); }
}

.podium-card {
  text-align: center;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(var(--app-surface-rgb),0.04);
  border: 1px solid rgba(var(--app-surface-rgb),0.06);
  min-width: 80px;

  &.gold-card { border-color: rgba(255,215,0,0.2); background: rgba(255,215,0,0.05); }
  &.silver-card { border-color: rgba(192,192,192,0.2); background: rgba(192,192,192,0.04); }
  &.bronze-card { border-color: rgba(205,127,50,0.2); background: rgba(205,127,50,0.04); }
}

.podium-rank { font-size: 20px; display: block; }
.podium-name { font-size: 13px; font-weight: 600; color: var(--app-text-primary); display: block; margin: 2px 0; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.podium-score { font-size: 13px; font-weight: 700; color: var(--app-gold); font-family: 'Poppins', sans-serif; }
.podium-detail { display: block; max-width: 88px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 10px; color: var(--app-text-muted); }

/* ======== Rank List ======== */
.rank-list {
  overflow: hidden;
  margin-bottom: 16px;
  animation: fade-in-up 0.5s ease-out 0.15s both;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(var(--app-surface-rgb),0.04);
  transition: background 0.2s;

  &:last-child { border-bottom: none; }
  &:hover { background: rgba(var(--app-surface-rgb),0.03); }
}

.rank-num {
  width: 28px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text-muted);
  text-align: center;
}

.rank-name {
  flex: 1;
  font-size: 14px;
  color: var(--app-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-level {
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

.rank-score {
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 700;
  color: var(--app-gold);
  min-width: 40px;
  text-align: right;
}

.rank-time {
  font-size: 11px;
  color: var(--app-text-muted);
  font-family: 'Poppins', sans-serif;
  min-width: 36px;
  text-align: right;
}

/* ======== Load More ======== */
.load-more-footer {
  padding: 16px;
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

/* ======== My Rank ======== */
.my-rank {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border: 1px solid rgba(var(--app-accent-rgb),0.2);
  background: rgba(var(--app-accent-rgb),0.06);
  animation: fade-in-up 0.5s ease-out 0.2s both;
}

.my-rank-label {
  font-size: 14px;
  color: var(--app-text-secondary);
}

.my-rank-val {
  font-size: 18px;
  font-weight: 700;
}

/* ======== Loading / Error / Empty ======== */
.lb-loading, .lb-error, .lb-empty {
  text-align: center;
  padding: 40px 20px;
  color: var(--app-text-muted);
  animation: fade-in-up 0.5s ease-out;
}

.loading-spinner {
  display: inline-block;
  width: 24px; height: 24px;
  border: 2px solid rgba(var(--app-accent-rgb),0.2);
  border-top-color: var(--app-gold);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.btn-retry {
  margin-top: 12px;
  padding: 8px 24px;
  border: 1px solid rgba(var(--app-accent-rgb),0.3);
  border-radius: 10px;
  background: rgba(var(--app-accent-rgb),0.06);
  color: var(--app-gold);
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
}

.bottom-spacer { height: 40px; }

/* ======== 动画 ======== */

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
