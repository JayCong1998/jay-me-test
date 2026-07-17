<template>
  <div class="feedback-bar" :class="correct ? 'feedback-correct' : 'feedback-wrong'">
    <!-- 图标动画 -->
    <div class="feedback-icon-wrap">
      <div class="feedback-icon-circle" :class="correct ? 'icon-correct' : 'icon-wrong'">
        <!-- 正确勾号 -->
        <svg v-if="correct" class="feedback-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="20 6 9 17 4 12" />
        </svg>
        <!-- 错误叉号 -->
        <svg v-else class="feedback-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </div>
    </div>

    <!-- 结果文字 -->
    <div class="feedback-info">
      <p class="feedback-result">{{ correct ? '回答正确！' : '回答错误' }}</p>

      <div v-if="!correct" class="feedback-answer-block">
        <span class="answer-label">正确答案</span>
        <span class="answer-value">{{ correctOption }}</span>
      </div>

      <p v-if="explanation" class="feedback-explain">
        <svg class="explain-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="16" x2="12" y2="12" />
          <line x1="12" y1="8" x2="12.01" y2="8" />
        </svg>
        {{ explanation }}
      </p>
    </div>

    <!-- 操作按钮 -->
    <div class="feedback-actions">
      <!-- 复活按钮（深渊模式不显示） -->
      <button
        v-if="canRevive && !correct && !isAbyssMode"
        class="btn-revive"
        @click="$emit('revive')"
      >
        <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round">
          <path d="M1 4v6h6" />
          <path d="M3.51 15a9 9 0 102.13-9.36L1 10" />
        </svg>
        使用复活（{{ 1 }}次）
      </button>

      <!-- 下一题 / 深渊模式按钮 -->
      <button
        class="btn-next"
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
</script>

<style scoped lang="scss">
/* ======== 容器 ======== */
.feedback-bar {
  margin-top: auto;
  padding: 28px 20px;
  border-radius: 20px;
  animation: feedback-enter 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);

  &.feedback-correct {
    background: rgba(34, 197, 94, 0.06);
    border: 1px solid rgba(34, 197, 94, 0.15);
  }

  &.feedback-wrong {
    background: rgba(239, 68, 68, 0.06);
    border: 1px solid rgba(239, 68, 68, 0.15);
  }
}

/* ======== 图标 ======== */
.feedback-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.feedback-icon-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: icon-pop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) 0.1s both;

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
  width: 28px;
  height: 28px;
}

/* ======== 文字 ======== */
.feedback-info {
  text-align: center;
  margin-bottom: 24px;
}

.feedback-result {
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text-primary);
  margin-bottom: 12px;
}

.feedback-answer-block {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 8px;
  background: rgba(var(--app-accent-rgb), 0.1);
  border: 1px solid rgba(var(--app-accent-rgb), 0.2);
  margin-bottom: 12px;

  .answer-label {
    font-size: 12px;
    color: var(--app-text-muted);
  }

  .answer-value {
    font-family: var(--app-font-display), sans-serif;
    font-size: 18px;
    font-weight: 700;
    color: var(--app-gold);
  }
}

.feedback-explain {
  font-size: 13px;
  color: var(--app-text-secondary);
  line-height: 1.65;
  padding: 0 8px;
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

/* ======== 按钮 ======== */
.feedback-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-revive {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 44px;
  border: 1.5px solid rgba(234, 179, 8, 0.3);
  border-radius: 12px;
  background: rgba(234, 179, 8, 0.08);
  color: var(--app-warning);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.25s ease;

  .btn-icon {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: rgba(234, 179, 8, 0.15);
    border-color: rgba(234, 179, 8, 0.5);
  }

  &:active {
    transform: scale(0.97);
  }
}

.btn-next {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: var(--app-gold-gradient);
  color: var(--app-text-on-accent);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  box-shadow: 0 4px 16px rgba(var(--app-accent-rgb), 0.3);
  transition: all 0.25s ease;

  svg {
    width: 18px;
    height: 18px;
    transition: transform 0.25s ease;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 24px rgba(var(--app-accent-rgb), 0.45);

    svg {
      transform: translateX(3px);
    }
  }

  &:active {
    transform: scale(0.97);
  }

  /* 深渊模式答错：堕入深渊按钮 */
  &.btn-abyss-fall {
    background: linear-gradient(135deg, #7c3aed 0%, #dc2626 100%);
    color: #fff;
    box-shadow: 0 4px 20px rgba(124, 58, 237, 0.4);

    &:hover {
      box-shadow: 0 6px 28px rgba(220, 38, 38, 0.5);
    }
  }
}

/* ======== 动画 ======== */
@keyframes feedback-enter {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes icon-pop {
  from {
    opacity: 0;
    transform: scale(0);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
