<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <div class="album-page page-bg">
      <!-- 背景光斑 -->
      <div class="bg-orb bg-orb--top"></div>

      <div class="album-content">
        <!-- ===== 页面标题 ===== -->
        <header class="album-header">
          <div class="header-title">
            <h1 class="page-title">🎵 专辑闯关</h1>
            <p class="page-subtitle">
              达到通关要求，解锁下一张专辑
            </p>
          </div>
          <!-- 进度概览 -->
          <div v-if="albumStore.progressSummary.total > 0" class="progress-badge">
            {{ albumStore.progressSummary.unlocked }}/{{ albumStore.progressSummary.total }}
          </div>
        </header>

        <!-- ===== 加载态 ===== -->
        <div v-if="albumStore.loading" class="loading-state">
          <div class="loading-spinner">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10" stroke-opacity="0.15" />
              <path d="M12 2a10 10 0 019.95 9" stroke-linecap="round" />
            </svg>
          </div>
          <p>加载专辑列表中...</p>
        </div>

        <!-- ===== 错误态 ===== -->
        <div v-else-if="albumStore.error" class="error-state">
          <p class="error-text">{{ albumStore.error }}</p>
          <button class="btn-retry" @click="loadAlbums">重新加载</button>
        </div>

        <!-- ===== 空状态 ===== -->
        <div v-else-if="albumStore.albums.length === 0" class="empty-state">
          <p class="empty-text">暂无专辑数据</p>
          <button class="btn-retry" @click="loadAlbums">重新加载</button>
        </div>

        <!-- ===== 专辑网格 ===== -->
        <div v-else class="album-grid">
          <AlbumCard
            v-for="album in albumStore.albums"
            :key="album.albumKey"
            :album="album"
            @click="handleAlbumClick"
          />
        </div>

        <!-- ===== 底部说明 ===== -->
        <div class="album-footer">
          <p>🎧 不熟的专辑？去听听歌再来挑战吧！</p>
        </div>
      </div>
    </div>
  </van-pull-refresh>
</template>

<script setup lang="ts">
defineOptions({ name: 'AlbumListPage' })

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlbumStore } from '@/stores/albumStore'
import { useAuthStore } from '@/stores/authStore'
import { useQuiz } from '@/composables/useQuiz'
import { showFailToast } from 'vant'
import AlbumCard from '@/components/album/AlbumCard.vue'

const router = useRouter()
const albumStore = useAlbumStore()
const authStore = useAuthStore()
const { startAlbumRound } = useQuiz()

const refreshing = ref(false)

onMounted(async () => {
  // 未登录重定向
  if (authStore.isGuest) {
    router.replace(`/login?redirect=${encodeURIComponent('/albums')}`)
    return
  }
  await loadAlbums()
})

async function loadAlbums() {
  try {
    await albumStore.fetchAlbums()
  } catch {
    // error is already set in store
  }
}

// 下拉刷新
async function onRefresh() {
  refreshing.value = true
  try {
    await loadAlbums()
  } finally {
    refreshing.value = false
  }
}

async function handleAlbumClick(albumKey: string) {
  try {
    await startAlbumRound(albumKey)
    router.push('/quiz')
  } catch (e: any) {
    showFailToast(e.message || '获取题目失败')
  }
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
    width: 220px;
    height: 220px;
    top: -60px;
    right: -40px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.08) 0%, transparent 70%);
  }
}

.album-page {
  display: flex;
  flex-direction: column;
  position: relative;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.album-content {
  flex: 1;
  padding: 16px;
  position: relative;
  z-index: 1;
}

/* ======== 页面头 ======== */
.album-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 8px 0;
}

.header-title {
  flex: 1;
  min-width: 0;
  text-align: center;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--app-text-primary);
  margin-bottom: 4px;
}

.page-subtitle {
  font-size: 13px;
  color: var(--app-text-muted);

  strong {
    color: var(--app-gold);
    font-weight: 700;
  }
}

.progress-badge {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 700;
  color: var(--app-gold);
  background: rgba(var(--app-accent-rgb), 0.1);
  border: 1px solid rgba(var(--app-accent-rgb), 0.2);
  padding: 6px 12px;
  border-radius: 10px;
  flex-shrink: 0;
}

/* ======== 加载态 ======== */
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--app-text-secondary);
  font-size: 15px;
}

.loading-spinner {
  width: 44px;
  height: 44px;
  animation: spin 0.8s linear infinite;
  color: var(--app-gold);

  svg {
    width: 100%;
    height: 100%;
  }
}

/* ======== 错误态 ======== */
.error-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

/* ======== 空状态 ======== */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.empty-text {
  font-size: 15px;
  color: var(--app-text-secondary);
  text-align: center;
}

.error-text {
  font-size: 15px;
  color: var(--app-error);
  text-align: center;
}

.btn-retry {
  padding: 10px 28px;
  border: 1px solid rgba(var(--app-accent-rgb), 0.3);
  border-radius: 12px;
  background: rgba(var(--app-accent-rgb), 0.06);
  color: var(--app-gold);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;

  &:hover {
    background: rgba(var(--app-accent-rgb), 0.12);
  }
}

/* ======== 专辑网格 ======== */
.album-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

/* ======== 底部 ======== */
.album-footer {
  text-align: center;
  padding: 32px 0 20px;

  p {
    font-size: 13px;
    color: var(--app-text-muted);
    opacity: 0.8;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
