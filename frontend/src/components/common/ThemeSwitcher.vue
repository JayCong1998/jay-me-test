<template>
  <div class="theme-switcher">
    <div class="theme-label">界面风格</div>
    <div class="theme-grid">
      <button
        v-for="t in themeStore.themes"
        :key="t.key"
        :class="['theme-option', { active: themeStore.activeTheme === t.key }]"
        @click="themeStore.setTheme(t.key)"
      >
        <span class="theme-icon">{{ t.icon }}</span>
        <span class="theme-name">{{ t.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/themeStore'

const themeStore = useThemeStore()
</script>

<style scoped lang="scss">
.theme-switcher {
  width: 100%;
}

.theme-label {
  font-size: 13px;
  color: var(--app-text-secondary);
  margin-bottom: 10px;
  font-weight: 500;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  border-radius: var(--app-radius-mode);
  border: 1.5px solid var(--app-border);
  background: var(--app-bg-card);
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;

  &:hover {
    border-color: var(--app-border-accent);
    background: var(--app-bg-card-hover);
  }

  &.active {
    border-color: var(--app-accent);
    background: rgba(var(--app-accent-rgb), 0.08);
  }
}

.theme-icon {
  font-size: 20px;
  line-height: 1;
}

.theme-name {
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-primary);
  white-space: nowrap;
}
</style>
