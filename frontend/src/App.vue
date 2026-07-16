<template>
  <div class="app-container">
    <router-view v-slot="{ Component, route }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
// App 根组件
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  background: var(--app-bg-color);
  position: relative;
}

/* 页面切换动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>
