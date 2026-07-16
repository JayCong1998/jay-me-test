<template>
  <div class="question-card">
    <!-- 题目标签 -->
    <div class="question-meta">
      <span class="meta-tag meta-tag--difficulty" :class="'meta-tag--' + question.difficulty.toLowerCase()">
        <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path v-if="question.difficulty === 'EASY'" stroke-linecap="round" stroke-linejoin="round"
            d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
          <path v-else stroke-linecap="round" stroke-linejoin="round"
            d="M4.26 10.147a60.436 60.436 0 00-.491 6.347A48.627 48.627 0 0112 20.904a48.627 48.627 0 018.232-4.41 60.46 60.46 0 00-.491-6.347m-15.482 0a50.57 50.57 0 00-2.658-.813A59.905 59.905 0 0112 3.493a59.902 59.902 0 0110.399 5.84c-.896.248-1.783.52-2.658.814m-15.482 0A50.697 50.697 0 0112 13.489a50.702 50.702 0 017.74-3.342M6.75 15a.75.75 0 100-1.5.75.75 0 000 1.5zm0 0v-3.675A55.378 55.378 0 0112 8.443m-7.007 11.55A5.981 5.981 0 006.75 15.75v-1.5" />
        </svg>
        {{ difficultyLabel }}
      </span>
      <span class="meta-tag meta-tag--category">
        <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path v-if="question.category === 'LYRICS'" stroke-linecap="round" stroke-linejoin="round"
            d="M9 9l10.5-3m0 6.553v3.75a2.25 2.25 0 01-1.632 2.163l-1.32.377a1.803 1.803 0 11-.99-3.467l2.31-.66a2.25 2.25 0 001.632-2.163zm0 0V2.25L9 5.25v10.303m0 0v3.75a2.25 2.25 0 01-1.632 2.163l-1.32.377a1.803 1.803 0 01-.99-3.467l2.31-.66A2.25 2.25 0 009 15.553z" />
          <path v-else stroke-linecap="round" stroke-linejoin="round"
            d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
        </svg>
        {{ categoryLabel }}
      </span>
    </div>

    <!-- 题目正文 -->
    <div class="question-body">
      <h3 class="question-text">{{ question.questionText }}</h3>
    </div>

    <!-- 选项列表 -->
    <div class="options-list">
      <button
        v-for="(option, idx) in question.options"
        :key="idx"
        class="option-item glass-card"
        :class="{
          'option-selected': selectedOption === optionLetter(idx),
          'option-disabled': disabled,
        }"
        :aria-label="'选项 ' + optionLetter(idx)"
        :aria-pressed="selectedOption === optionLetter(idx)"
        @click="handleClick(optionLetter(idx))"
      >
        <span class="option-letter" :class="{ 'letter-active': selectedOption === optionLetter(idx) }">
          {{ optionLetter(idx) }}
        </span>
        <span class="option-text">{{ option.replace(/^[A-D]\.\s*/, '') }}</span>
        <span v-if="selectedOption === optionLetter(idx)" class="option-check">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Question } from '@/stores/gameStore'

const props = defineProps<{
  question: Question
  selectedOption: string | null
  disabled: boolean
}>()

const emit = defineEmits<{
  select: [option: string]
}>()

const difficultyLabel = props.question.difficulty === 'EASY' ? '简单' : '中等'
const categoryLabel = props.question.category === 'LYRICS' ? '歌词类' : '专辑归属'

function optionLetter(idx: number): string {
  return String.fromCharCode(65 + idx)
}

function handleClick(letter: string) {
  if (!props.disabled) {
    emit('select', letter)
  }
}
</script>

<style scoped lang="scss">
.question-card {
  display: flex;
  flex-direction: column;
}

/* ======== 标签行 ======== */
.question-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;

  &--difficulty {
    &.meta-tag--easy {
      background: rgba(34, 197, 94, 0.12);
      color: #4ade80;
      border: 1px solid rgba(34, 197, 94, 0.2);
    }

    &.meta-tag--medium {
      background: rgba(234, 179, 8, 0.12);
      color: #facc15;
      border: 1px solid rgba(234, 179, 8, 0.2);
    }
  }

  &--category {
    background: rgba(201, 168, 76, 0.1);
    color: var(--app-gold);
    border: 1px solid rgba(201, 168, 76, 0.18);
  }
}

.meta-icon {
  width: 14px;
  height: 14px;
}

/* ======== 题目正文 ======== */
.question-body {
  margin-bottom: 28px;
}

.question-text {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.75;
  color: var(--app-text-primary);
  letter-spacing: 0.3px;
}

/* ======== 选项列表 ======== */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 16px 18px;
  background: var(--app-bg-card);
  border: 1.5px solid var(--app-border);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: left;
  font-family: inherit;
  min-height: 56px;
  -webkit-tap-highlight-color: transparent;

  &:hover:not(.option-disabled) {
    background: var(--app-bg-card-hover);
    border-color: rgba(201, 168, 76, 0.25);
    transform: translateX(4px);
  }

  &:active:not(.option-disabled) {
    transform: scale(0.985);
  }

  &.option-selected {
    border-color: var(--app-gold);
    background: rgba(201, 168, 76, 0.1);
    box-shadow: 0 0 20px rgba(201, 168, 76, 0.1);

    &:hover {
      transform: translateX(4px);
    }
  }

  &.option-disabled {
    pointer-events: none;
    opacity: 0.55;
  }
}

.option-letter {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: rgba(201, 168, 76, 0.1);
  color: var(--app-gold);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
  font-family: 'Poppins', sans-serif;
  transition: all 0.25s ease;

  &.letter-active {
    background: var(--app-gold-gradient);
    color: #1a1a2e;
  }
}

.option-text {
  flex: 1;
  font-size: 15px;
  color: #d4d4d8;
  line-height: 1.5;
}

.option-check {
  width: 20px;
  height: 20px;
  color: var(--app-gold);
  flex-shrink: 0;
  animation: check-pop 0.25s ease;
}

@keyframes check-pop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.3); }
  100% { transform: scale(1); opacity: 1; }
}
</style>
