<template>
  <div class="app-container" :class="{ 'has-tabbar': showTabbar }">
    <div class="view-area">
      <router-view v-slot="{ Component, route }">
        <keep-alive :include="keepAliveNames">
          <component :is="Component" :key="route.path" />
        </keep-alive>
      </router-view>
    </div>
    <AppTabbar v-if="showTabbar" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppTabbar from '@/components/common/AppTabbar.vue'

const route = useRoute()
const showTabbar = computed(() => route.meta.showTabbar !== false)

// 需要缓存的 tab 页面组件名（与各页面 defineOptions name 一致）
const keepAliveNames = ['HomePage', 'LeaderboardPage', 'AlbumListPage', 'ProfilePage']
</script>

<style scoped>
.app-container {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  height: 100dvh;
  max-width: 480px;
  background: var(--app-bg-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.view-area {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
  background: var(--app-bg-gradient);
}
</style>
