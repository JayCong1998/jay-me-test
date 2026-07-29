<template>
  <div class="feedback-bar" :class="correct ? 'feedback-correct' : 'feedback-wrong'">
    <div class="feedback-summary">
      <div class="feedback-icon-circle" :class="correct ? 'icon-correct' : 'icon-wrong'">
        <svg v-if="correct" class="feedback-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="20 6 9 17 4 12" />
        </svg>
        <svg v-else class="feedback-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </div>
      <div>
        <p class="feedback-result">{{ correct ? '回答正确' : '回答错误' }}</p>
        <p v-if="!correct" class="feedback-answer">正确答案 <strong>{{ correctOption }}</strong></p>
      </div>
    </div>

    <button
      v-if="explanation && !showExplanation"
      class="btn-explanation"
      type="button"
      @click="showExplanation = true"
    >
      查看解析
    </button>

    <p v-if="explanation && showExplanation" class="feedback-explain">
        <svg class="explain-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="16" x2="12" y2="12" />
          <line x1="12" y1="8" x2="12.01" y2="8" />
        </svg>
        {{ explanation }}
    </p>

    <div
      class="feedback-actions"
      :class="{ 'feedback-actions--split': canRevive && !correct && !isAbyssMode }"
    >
      <button
        v-if="canRevive && !correct && !isAbyssMode"
        class="btn-revive"
        type="button"
        @click="$emit('revive')"
      >
        <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round">
          <path d="M1 4v6h6" />
          <path d="M3.51 15a9 9 0 102.13-9.36L1 10" />
        </svg>
        使用复活（{{ 1 }}次）
      </button>

      <button
        class="btn-next"
        type="button"
        :class="{ 'btn-abyss-fall': isAbyssMode && !correct }"
        @click="$emit('next')"
      >
        <span v-if="isAbyssMode && !correct">堕入深渊</span>
        <span v-else-if="isAbyssMode && correct">继续深入</span>
        <span v-else>下一题</span>
        <svg v-if="!isAbyssMode || correct" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
          stroke-linecap="round" stroke-linejoin="round">
          <line x1="5" y1="12" x2="19" y2="12" />
          <polyline points="12 5 19 12 12 19" />
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
          stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19" />
          <polyline points="19 12 12 19 12 19" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  correct: boolean
  correctOption: string
  explanation: string
  canRevive: boolean
  isAbyssMode?: boolean
}>()

defineEmits<{
  revive: []
  next: []
}>()

const showExplanation = ref(false)
</script>

<style scoped lang="scss">
/* ======== 容器 ======== */
.feedback-bar {
  margin-top: auto;
  padding: 16px;
  border-radius: 14px;
  animation: feedback-fade-in 0.18s ease-out;

  &.feedback-correct {
    background: rgba(34, 197, 94, 0.06);
    border: 1px solid rgba(34, 197, 94, 0.15);
  }

  &.feedback-wrong {
    background: rgba(239, 68, 68, 0.06);
    border: 1px solid rgba(239, 68, 68, 0.15);
  }
}

.feedback-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 8px;
}

.feedback-summary > div:last-child {
  text-align: center;
}

.feedback-icon-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.icon-correct {
    background: rgba(34, 197, 94, 0.15);
    color: var(--app-success);
  }

  &.icon-wrong {
    background: rgba(239, 68, 68, 0.15);
    color: var(--app-error);
  }
}

.feedback-svg {
  width: 20px;
  height: 20px;
}

.feedback-result {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text-primary);
  line-height: 1.3;
}

.feedback-answer {
  margin-top: 2px;
  color: var(--app-text-secondary);
  font-size: 13px;
  line-height: 1.3;

  strong {
    color: var(--app-gold);
    font-family: var(--app-font-display), sans-serif;
    font-size: 16px;
  }
}

.btn-explanation {
  width: 100%;
  min-height: 30px;
  margin-bottom: 8px;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: var(--app-text-secondary);
  cursor: pointer;
  font-family: inherit;
  font-size: 12px;

  &:hover {
    background: rgba(var(--app-surface-rgb), 0.06);
  }
}

.feedback-explain {
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--app-text-secondary);
  line-height: 1.65;
  padding: 8px;
  border-radius: 8px;
  background: rgba(var(--app-surface-rgb), 0.04);
  display: flex;
  align-items: flex-start;
  gap: 6px;
  text-align: left;

  .explain-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    margin-top: 2px;
    color: var(--app-text-muted);
  }
}

.feedback-actions {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 8px;
}

.feedback-actions--split {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.btn-revive {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 0;
  height: 44px;
  border: 1.5px solid rgba(234, 179, 8, 0.3);
  border-radius: 12px;
  background: rgba(234, 179, 8, 0.08);
  color: var(--app-warning);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background-color 0.18s ease, border-color 0.18s ease;

  .btn-icon {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: rgba(234, 179, 8, 0.15);
    border-color: rgba(234, 179, 8, 0.5);
  }

}

.btn-next {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 0;
  height: 44px;
  border: none;
  border-radius: 12px;
  background: var(--app-gold-gradient);
  color: var(--app-text-on-accent);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  box-shadow: none;
  transition: filter 0.18s ease;

  svg {
    width: 18px;
    height: 18px;
    transition: none;
  }

  &:hover {
    filter: brightness(1.03);
  }

  /* 深渊模式答错：堕入深渊按钮 */
  &.btn-abyss-fall {
    background: linear-gradient(135deg, #7c3aed 0%, #dc2626 100%);
    color: #fff;
    box-shadow: none;

    &:hover {
      box-shadow: none;
    }
  }
}

@keyframes feedback-fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
