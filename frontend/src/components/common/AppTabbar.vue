<template>
  <van-tabbar
    v-model="active"
    route
    safe-area-inset-bottom
    :active-color="'var(--app-gold)'"
    :inactive-color="'var(--app-text-muted)'"
  >
    <van-tabbar-item to="/" icon="home-o">
      <template #icon>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
          stroke-linecap="round" stroke-linejoin="round" class="tab-icon">
          <path d="M3 9.5L12 3l9 6.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" />
          <polyline points="9 21 9 12 15 12 15 21" />
        </svg>
      </template>
      首页
    </van-tabbar-item>

    <van-tabbar-item to="/leaderboard" icon="chart-trending-o">
      <template #icon>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
          stroke-linecap="round" stroke-linejoin="round" class="tab-icon">
          <rect x="2" y="16" width="5" height="6" rx="1" />
          <rect x="9.5" y="10" width="5" height="12" rx="1" />
          <rect x="17" y="4" width="5" height="18" rx="1" />
        </svg>
      </template>
      排行
    </van-tabbar-item>

    <van-tabbar-item to="/profile" icon="user-o">
      <template #icon>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
          stroke-linecap="round" stroke-linejoin="round" class="tab-icon">
          <circle cx="12" cy="8" r="4" />
          <path d="M4 21c0-4.418 3.582-8 8-8s8 3.582 8 8" />
        </svg>
      </template>
      我的
    </van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const active = ref(0)
</script>

<style scoped>
.tab-icon {
  width: 22px;
  height: 22px;
}

/* 毛玻璃 tabbar — 进入文档流 */
:deep(.van-tabbar) {
  position: relative !important;
  flex-shrink: 0;
  background: var(--app-bg-card) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--app-border);
}
</style>
