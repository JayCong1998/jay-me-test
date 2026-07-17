<template>
  <div
    class="album-card"
    :class="{ 'album-card--locked': !album.unlocked, 'album-card--clickable': album.unlocked }"
    :style="{ '--card-gradient': gradient }"
    @click="handleClick"
  >
    <!-- 封面占位 -->
    <div class="album-cover" :style="{ background: gradient }">
      <div class="album-cover__inner">
        <span class="album-cover__year">{{ album.year }}</span>
        <!-- 锁定遮罩 -->
        <div v-if="!album.unlocked" class="album-cover__lock">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1s3.1 1.39 3.1 3.1v2z"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 信息栏 -->
    <div class="album-info">
      <h3 class="album-name">{{ album.displayName }}</h3>
      <div class="album-status">
        <template v-if="album.unlocked">
          <span v-if="album.bestScore > 0" class="album-score">
            <span class="score-num">{{ album.bestScore }}</span>
            <span class="score-sep">/</span>
            <span class="score-total">10</span>
          </span>
          <span v-else class="album-new">新关卡</span>
        </template>
        <span v-else class="album-locked-text">🔒 未解锁</span>
      </div>
      <div v-if="album.totalAttempts > 0" class="album-attempts">
        {{ album.totalAttempts }} 次挑战
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getAlbumInfo } from '@/utils/albums'
import type { AlbumDTO } from '@/api/albumApi'

const props = defineProps<{
  album: AlbumDTO
}>()

const emit = defineEmits<{
  click: [albumKey: string]
}>()

const gradient = computed(() => {
  return getAlbumInfo(props.album.albumKey)?.gradient || 'linear-gradient(135deg, #2c3e50, var(--app-text-on-accent))'
})

function handleClick() {
  if (props.album.unlocked) {
    emit('click', props.album.albumKey)
  }
}
</script>

<style scoped lang="scss">
.album-card {
  border-radius: 14px;
  overflow: hidden;
  background: var(--app-bg-card);
  border: 1px solid var(--app-border);
  transition: all 0.3s ease;
  cursor: default;

  &--clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      border-color: rgba(var(--app-accent-rgb), 0.3);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
    }

    &:active {
      transform: scale(0.97);
    }
  }

  &--locked {
    opacity: 0.55;
    filter: grayscale(0.6);
  }
}

.album-cover {
  position: relative;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;

  &__inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
  }

  &__year {
    font-family: 'Poppins', sans-serif;
    font-size: 28px;
    font-weight: 800;
    color: rgba(var(--app-surface-rgb), 0.85);
    letter-spacing: 2px;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  }

  &__lock {
    color: rgba(var(--app-surface-rgb), 0.7);

    svg {
      width: 26px;
      height: 26px;
    }
  }
}

.album-info {
  padding: 12px 14px 14px;
}

.album-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-status {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
}

.album-score {
  font-family: 'Poppins', sans-serif;

  .score-num {
    font-size: 18px;
    font-weight: 700;
    color: var(--app-gold);
  }

  .score-sep {
    font-size: 12px;
    color: var(--app-text-muted);
  }

  .score-total {
    font-size: 12px;
    color: var(--app-text-muted);
  }
}

.album-new {
  font-size: 12px;
  color: var(--app-gold);
  font-weight: 600;
}

.album-locked-text {
  font-size: 12px;
  color: var(--app-text-muted);
}

.album-attempts {
  font-size: 11px;
  color: var(--app-text-muted);
}
</style>
