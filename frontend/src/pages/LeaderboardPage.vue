<template>
  <div class="lb-page page-bg">
    <div class="bg-orb bg-orb--top"></div>
    <div class="bg-orb bg-orb--bottom"></div>

    <div class="lb-content">
      <!-- Header -->
      <section class="lb-header">
        <button class="btn-back" @click="$router.push('/')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h1 class="lb-title text-gold">🏆 排行榜</h1>
        <div style="width:36px"></div>
      </section>

      <!-- Tabs -->
      <section class="lb-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="lb-tab"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >{{ tab.label }}</button>
      </section>

      <!-- Level picker (for 等级榜) -->
      <section v-if="activeTab === 'level'" class="level-picker">
        <button
          v-for="lvl in LEVELS"
          :key="lvl.key"
          class="level-btn"
          :class="{ active: selectedLevel === lvl.key }"
          :style="selectedLevel === lvl.key ? { borderColor: lvl.color, color: lvl.color } : {}"
          @click="selectLevel(lvl.key)"
        >{{ lvl.title }}</button>
      </section>

      <!-- Loading -->
      <div v-if="loading" class="lb-loading">
        <span class="loading-spinner"></span>
        <p>加载中...</p>
      </div>

      <!-- Error -->
      <div v-else-if="errorMsg" class="lb-error">
        <p>{{ errorMsg }}</p>
        <button class="btn-retry" @click="loadData">重试</button>
      </div>

      <!-- Leaderboard -->
      <template v-else-if="data">
        <!-- Top 3 podium -->
        <section v-if="data.list.length >= 3" class="podium-section">
          <div class="podium">
            <!-- 2nd -->
            <div class="podium-item" @click="() => {}">
              <div class="podium-avatar silver">
                <span>{{ data.list[1].nickname.charAt(0) }}</span>
              </div>
              <div class="podium-card silver-card">
                <span class="podium-rank">🥈</span>
                <span class="podium-name">{{ data.list[1].nickname }}</span>
                <span class="podium-score">{{ isAbyssTab ? data.list[1].correctCount + '连对' : data.list[1].correctCount + '/10' }}</span>
              </div>
            </div>
            <!-- 1st -->
            <div class="podium-item first">
              <div class="podium-avatar gold">
                <span>{{ data.list[0].nickname.charAt(0) }}</span>
              </div>
              <div class="podium-card gold-card">
                <span class="podium-rank">🥇</span>
                <span class="podium-name">{{ data.list[0].nickname }}</span>
                <span class="podium-score">{{ isAbyssTab ? data.list[0].correctCount + '连对' : data.list[0].correctCount + '/10' }}</span>
              </div>
            </div>
            <!-- 3rd -->
            <div class="podium-item">
              <div class="podium-avatar bronze">
                <span>{{ data.list[2].nickname.charAt(0) }}</span>
              </div>
              <div class="podium-card bronze-card">
                <span class="podium-rank">🥉</span>
                <span class="podium-name">{{ data.list[2].nickname }}</span>
                <span class="podium-score">{{ isAbyssTab ? data.list[2].correctCount + '连对' : data.list[2].correctCount + '/10' }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Rank list (4th+) -->
        <section class="rank-list glass-card">
          <div
            v-for="entry in listAfterPodium"
            :key="entry.rank"
            class="rank-item"
          >
            <span class="rank-num">{{ entry.rank }}</span>
            <span class="rank-name">{{ entry.nickname }}</span>
            <span class="rank-level" :style="{ color: getLevelColor(entry.correctCount) }">
              {{ entry.levelTitle }}
            </span>
            <span class="rank-score">{{ isAbyssTab ? entry.correctCount + '连对' : entry.correctCount + '/10' }}</span>
            <span class="rank-time">{{ formatDuration(entry.timeSpentSecs) }}</span>
          </div>

          <p v-if="listAfterPodium.length === 0 && data.list.length <= 3" class="empty-hint">
            暂无更多排名
          </p>
        </section>

        <!-- My rank -->
        <section v-if="data.myRank" class="my-rank glass-card">
          <span class="my-rank-label">我的排名</span>
          <span class="my-rank-val text-gold">第 {{ data.myRank }} 名</span>
        </section>
        <section v-else-if="data.myRank === null && activeTab !== 'level'" class="my-rank glass-card">
          <span class="my-rank-label">你还没有上榜成绩，快去答题吧！</span>
        </section>
      </template>

      <!-- Empty -->
      <div v-else class="lb-empty">
        <p>暂无排行数据</p>
      </div>

      <div class="bottom-spacer"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import * as leaderboardApi from '@/api/leaderboardApi'
import type { LeaderboardResult } from '@/api/leaderboardApi'
import { LEVELS, ABYSS_LEVELS } from '@/utils/constants'
import { getAbyssLevelByStreak } from '@/utils/levels'
import { showFailToast } from 'vant'

const router = useRouter()
const authStore = useAuthStore()

const tabs = [
  { key: 'total' as const, label: '🏆 总分榜' },
  { key: 'daily' as const, label: '📅 今日榜' },
  { key: 'level' as const, label: '🎖 等级榜' },
  { key: 'abyss' as const, label: '🕳️ 深渊榜' },
]

const activeTab = ref<'total' | 'daily' | 'level' | 'abyss'>('total')
const selectedLevel = ref('ULTIMATE')

const isAbyssTab = computed(() => activeTab.value === 'abyss')
const data = ref<LeaderboardResult | null>(null)
const loading = ref(false)
const errorMsg = ref('')

const listAfterPodium = computed(() => {
  if (!data.value) return []
  return data.value.list.slice(3)
})

function switchTab(tab: 'total' | 'daily' | 'level' | 'abyss') {
  activeTab.value = tab
  loadData()
}

function selectLevel(key: string) {
  selectedLevel.value = key
  loadData()
}

async function loadData() {
  // 未登录重定向
  if (!authStore.isLoggedIn) {
    router.push('/login?redirect=leaderboard')
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    if (activeTab.value === 'level') {
      data.value = await leaderboardApi.fetchLeaderboard('level', 50, selectedLevel.value)
    } else {
      data.value = await leaderboardApi.fetchLeaderboard(activeTab.value, 50)
    }
  } catch (e: any) {
    if (e.response?.status === 401 || e.message?.includes('登录')) {
      router.push('/login?redirect=leaderboard')
      return
    }
    errorMsg.value = e.message || '加载排行榜失败'
  } finally {
    loading.value = false
  }
}

function getLevelColor(score: number): string {
  if (isAbyssTab.value) {
    return getAbyssLevelByStreak(score).color
  }
  const level = LEVELS.find(l => score >= l.minScore && score <= l.maxScore)
  return level?.color || 'var(--app-text-muted)'
}

function formatDuration(secs: number): string {
  const m = Math.floor(secs / 60)
  const s = secs % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

onMounted(() => {
  loadData()
})
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
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
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
  justify-content: space-between;
  margin-bottom: 16px;
}

.btn-back {
  width: 36px; height: 36px;
  border: none; background: rgba(var(--app-surface-rgb),0.06);
  border-radius: 50%;
  color: var(--app-text-secondary);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;

  svg { width: 20px; height: 20px; }
  &:hover { background: rgba(var(--app-surface-rgb),0.12); color: var(--app-gold); }
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

/* ======== Level Picker ======== */
.level-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
  animation: fade-in-up 0.5s ease-out 0.05s;
}

.level-btn {
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid rgba(var(--app-surface-rgb),0.1);
  border-radius: 20px;
  background: rgba(var(--app-surface-rgb),0.03);
  color: var(--app-text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;

  &.active {
    background: rgba(var(--app-accent-rgb),0.1);
  }

  &:hover { border-color: rgba(var(--app-surface-rgb),0.2); }
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

.empty-hint {
  text-align: center;
  padding: 20px;
  color: var(--app-text-muted);
  font-size: 13px;
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
  padding: 60px 20px;
  color: var(--app-text-muted);
  animation: fade-in-up 0.5s ease-out;
}

.loading-spinner {
  display: inline-block;
  width: 32px; height: 32px;
  border: 2px solid rgba(var(--app-accent-rgb),0.2);
  border-top-color: var(--app-gold);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin-bottom: 12px;
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
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
