<template>
  <div class="quiz-page page-bg" :class="{ 'abyss-mode': gameStore.mode === 'ABYSS' }">
    <!-- 背景光斑 -->
    <div class="bg-orb bg-orb--top"></div>

    <!-- ===== 顶部信息栏 ===== -->
    <header class="quiz-header glass-card">
      <div class="header-row">
        <!-- 退出按钮 -->
        <button class="header-btn header-btn--exit" @click="handleExit" aria-label="退出答题">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>

        <!-- 进度文本 -->
        <!-- 经典/专辑模式：X/10 -->
        <span v-if="gameStore.mode !== 'ABYSS'" class="progress-text">
          <span class="progress-current">{{ gameStore.currentIndex + 1 }}</span>
          <span class="progress-sep">/</span>
          <span class="progress-total">{{ gameStore.totalQuestions }}</span>
        </span>
        <!-- 深渊模式：深渊第 N 题 -->
        <span v-else class="progress-text abyss-progress">
          <span class="abyss-label">深渊第</span>
          <span class="progress-current">{{ gameStore.currentIndex + 1 }}</span>
          <span class="abyss-label">题</span>
          <span class="abyss-streak-badge">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
              stroke-linecap="round" stroke-linejoin="round">
              <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
            </svg>
            {{ gameStore.abyssStreak || gameStore.correctCount }}
          </span>
        </span>

        <!-- 专辑标签 -->
        <span v-if="gameStore.mode === 'ALBUM'" class="album-badge">
          {{ albumDisplayName }}
        </span>

        <!-- 计时器 -->
        <div class="timer-display">
          <svg class="timer-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          <span class="timer-text" :class="{ 'timer-warn': elapsed > 120 }">{{ formatTime(elapsed) }}</span>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="progress-track">
        <div
          class="progress-fill"
          :class="{ 'progress-abyss': gameStore.mode === 'ABYSS' }"
          :style="{ width: gameStore.mode === 'ABYSS' ? '100%' : gameStore.progress + '%' }"
        ></div>
      </div>

      <!-- 复活标记（深渊模式不显示） -->
      <Transition name="slide-down">
        <div v-if="gameStore.revivalRemaining > 0 && gameStore.mode !== 'ABYSS'" class="revival-indicator">
          <svg class="revival-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 4v6h6" />
            <path d="M3.51 15a9 9 0 102.13-9.36L1 10" />
          </svg>
          <span>复活可用 {{ gameStore.revivalRemaining }} 次</span>
        </div>
      </Transition>
    </header>

    <!-- ===== 加载状态 ===== -->
    <div v-if="loading && !gameStore.currentQuestion" class="loading-state">
      <div class="loading-spinner">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="12" cy="12" r="10" stroke-opacity="0.15" />
          <path d="M12 2a10 10 0 019.95 9" stroke-linecap="round" />
        </svg>
      </div>
      <p class="loading-text">加载题目中...</p>
    </div>

    <!-- ===== 题目区域 ===== -->
    <div v-else-if="gameStore.currentQuestion" class="question-area">
      <Transition name="question-slide" mode="out-in">
        <div :key="gameStore.currentIndex" class="question-inner">
          <!-- 题目卡片 -->
          <QuestionCard
            :question="gameStore.currentQuestion"
            :selected-option="currentSelected"
            :disabled="showFeedback"
            @select="handleSelect"
          />

          <!-- 反馈栏 -->
          <Transition name="feedback-enter">
            <FeedbackBar
              v-if="showFeedback && lastResult"
              :correct="lastResult.correct"
              :correct-option="lastResult.correctOption"
              :explanation="lastResult.explanation"
              :can-revive="gameStore.revivalRemaining > 0 && !lastResult.correct"
              :is-abyss-mode="gameStore.mode === 'ABYSS'"
              @revive="handleRevive"
              @next="handleNext"
            />
          </Transition>

          <!-- 提交按钮 -->
          <Transition name="fade">
            <div v-if="!showFeedback" class="submit-area">
              <button
                class="submit-btn"
                :disabled="!currentSelected"
                :class="{ 'btn-ready': currentSelected }"
                @click="handleSubmit"
              >
                <template v-if="gameStore.mode === 'ABYSS'">
                  确认答案
                </template>
                <template v-else-if="gameStore.isLastQuestion">
                  提交答卷
                </template>
                <template v-else>
                  确认答案
                </template>
              </button>
            </div>
          </Transition>
        </div>
      </Transition>
    </div>

    <!-- ===== 空状态 ===== -->
    <div v-else class="empty-state">
      <p class="empty-text">题目数据为空，请返回首页重新开始</p>
      <button class="empty-btn" @click="router.replace('/')">返回首页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '@/stores/gameStore'
import { useQuiz } from '@/composables/useQuiz'
import { useTimer } from '@/composables/useTimer'
import { formatTime } from '@/utils/format'
import { showSuccessToast, showFailToast, showToast, showConfirmDialog } from 'vant'
import { getAlbumInfo } from '@/utils/albums'
import QuestionCard from '@/components/quiz/QuestionCard.vue'
import FeedbackBar from '@/components/quiz/FeedbackBar.vue'

const router = useRouter()
const gameStore = useGameStore()
const { submitAnswer, submitAbyssAnswer, reviveCurrent, finishAndSubmit, prefetchAbyssBatch } = useQuiz()
const { elapsed, start: startTimer, stop: stopTimer } = useTimer()

// --- 专辑标签 ---
const albumDisplayName = computed(() => {
  if (!gameStore.albumKey) return ''
  return getAlbumInfo(gameStore.albumKey)?.displayName || gameStore.albumKey
})

// --- 本地状态 ---
const loading = ref(false)
const currentSelected = ref<string | null>(null)
const showFeedback = ref(false)
const lastResult = ref<{
  correct: boolean
  correctOption: string
  explanation: string
} | null>(null)

const isAbyss = computed(() => gameStore.mode === 'ABYSS')

// --- 生命周期 ---
onMounted(() => {
  if (gameStore.phase !== 'playing' || !gameStore.roundId) {
    router.replace('/')
    return
  }
  startTimer()
})

// 答题完成时停止计时器
watch(() => gameStore.phase, (val) => {
  if (val === 'finished') {
    stopTimer()
  }
})

// 深渊模式：自动预加载下一批题目（倒数第 2 题时触发）
watch(() => gameStore.currentIndex, (newVal) => {
  if (isAbyss.value && newVal >= gameStore.totalQuestions - 2 && !gameStore.prefetching) {
    prefetchAbyssBatch()
  }
})

// --- 选项交互 ---
function handleSelect(option: string) {
  if (!showFeedback.value) {
    currentSelected.value = option
  }
}

// --- 提交答案 ---
async function handleSubmit() {
  if (!currentSelected.value) return
  loading.value = true
  try {
    const result = isAbyss.value
      ? await submitAbyssAnswer(currentSelected.value)
      : await submitAnswer(currentSelected.value)
    lastResult.value = result
    showFeedback.value = true
  } catch (e: any) {
    showFailToast(e.message || '提交失败，请重试')
  } finally {
    loading.value = false
  }
}

// --- 复活（深渊模式不可用） ---
async function handleRevive() {
  loading.value = true
  try {
    const ok = await reviveCurrent()
    if (ok) {
      showFeedback.value = false
      currentSelected.value = null
      lastResult.value = null
      showSuccessToast('复活成功，请重新作答')
    } else {
      showToast('复活机会已用完')
    }
  } catch {
    showFailToast('复活失败')
  } finally {
    loading.value = false
  }
}

// --- 下一题 / 完成 / 堕入深渊 ---
async function handleNext() {
  // 深渊模式答错 → 游戏结束
  if (isAbyss.value && lastResult.value && !lastResult.value.correct) {
    loading.value = true
    try {
      await finishAndSubmit()
      router.replace('/result')
    } catch (e: any) {
      showFailToast(e.message || '提交失败')
    } finally {
      loading.value = false
    }
    return
  }

  // 经典/专辑模式最后一题 或 深渊模式答错（已在上方处理）
  if (!isAbyss.value && gameStore.isLastQuestion) {
    loading.value = true
    try {
      await finishAndSubmit()
      router.replace('/result')
    } catch (e: any) {
      showFailToast(e.message || '提交失败')
    } finally {
      loading.value = false
    }
  } else {
    // 下一题
    gameStore.nextQuestion()
    showFeedback.value = false
    currentSelected.value = null
    lastResult.value = null
  }
}

// --- 退出 ---
async function handleExit() {
  try {
    await showConfirmDialog({
      title: isAbyss.value ? '逃离深渊' : '确认退出',
      message: isAbyss.value
        ? '一旦逃离深渊，当前进度不会保存，确定要逃离吗？'
        : '当前进度不会保存，确定退出吗？',
      confirmButtonText: isAbyss.value ? '逃离深渊' : '确定退出',
      cancelButtonText: isAbyss.value ? '继续深入' : '继续答题',
    })
    gameStore.resetGame()
    router.replace('/')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
/* ======== 背景装饰 ======== */
.bg-orb {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;

  &--top {
    width: 200px;
    height: 200px;
    top: -40px;
    right: -40px;
    background: radial-gradient(circle, rgba(var(--app-accent-rgb), 0.08) 0%, transparent 70%);
  }
}

/* 深渊模式背景微调 */
.abyss-mode .bg-orb--top {
  background: radial-gradient(circle, rgba(124, 58, 237, 0.08) 0%, transparent 70%);
}

/* ======== 页面 ======== */
.quiz-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* ======== 顶部栏 ======== */
.quiz-header {
  margin: 12px 12px 0;
  padding: 12px 16px 10px;
  position: sticky;
  top: 12px;
  z-index: 20;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.header-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: rgba(var(--app-surface-rgb), 0.05);
  color: var(--app-text-secondary);
  cursor: pointer;
  transition: all 0.25s ease;

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: rgba(239, 68, 68, 0.15);
    color: var(--app-error);
  }

  &:active {
    transform: scale(0.9);
  }
}

.progress-text {
  font-family: var(--app-font-display), sans-serif;
  font-size: 16px;
  letter-spacing: 1px;
}

.progress-current {
  color: var(--app-gold);
  font-weight: 700;
  font-size: 20px;
}

.progress-sep {
  color: var(--app-text-muted);
  margin: 0 2px;
}

.progress-total {
  color: var(--app-text-secondary);
  font-weight: 500;
}

/* 深渊模式进度文本 */
.abyss-progress {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.abyss-label {
  font-size: 14px;
  color: var(--app-text-secondary);
  font-weight: 500;
  font-family: inherit;
}

.abyss-streak-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-left: 6px;
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(124, 58, 237, 0.15);
  border: 1px solid rgba(124, 58, 237, 0.25);
  color: var(--app-info);
  font-size: 13px;
  font-weight: 600;
  font-family: var(--app-font-display), sans-serif;

  svg {
    width: 14px;
    height: 14px;
  }
}

.album-badge {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-gold);
  background: rgba(var(--app-accent-rgb), 0.1);
  border: 1px solid rgba(var(--app-accent-rgb), 0.2);
  padding: 4px 10px;
  border-radius: 8px;
  white-space: nowrap;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.timer-display {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 64px;
  justify-content: flex-end;
}

.timer-icon {
  width: 16px;
  height: 16px;
  color: var(--app-text-muted);
}

.timer-text {
  font-family: var(--app-font-display), monospace;
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-secondary);
  font-variant-numeric: tabular-nums;
  transition: color 0.3s;

  &.timer-warn {
    color: var(--app-error);
  }
}

/* 进度条 */
.progress-track {
  height: 3px;
  background: rgba(var(--app-surface-rgb), 0.06);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--app-gold-gradient);
  border-radius: 2px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);

  &.progress-abyss {
    background: linear-gradient(90deg, #7c3aed, var(--app-info), #dc2626);
    animation: abyss-progress-pulse 2s ease-in-out infinite;
  }
}

/* 复活指示器 */
.revival-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 10px;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(234, 179, 8, 0.08);
  border: 1px solid rgba(234, 179, 8, 0.15);
  color: var(--app-warning);
  font-size: 12px;
  font-weight: 500;
}

.revival-icon {
  width: 14px;
  height: 14px;
  color: var(--app-warning);
}

/* ======== 加载状态 ======== */
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  animation: spin 0.8s linear infinite;
  color: var(--app-gold);

  svg {
    width: 100%;
    height: 100%;
  }
}

.loading-text {
  font-size: 15px;
  color: var(--app-text-secondary);
}

/* ======== 题目区域 ======== */
.question-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px 16px 24px;
  position: relative;
  z-index: 1;
}

.question-inner {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* ======== 提交按钮 ======== */
.submit-area {
  margin-top: auto;
  padding-top: 24px;
}

.submit-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 700;
  font-family: inherit;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--app-text-muted);
  background: rgba(var(--app-surface-rgb), 0.06);
  border: 1px solid var(--app-border);

  &.btn-ready {
    color: var(--app-text-on-accent);
    background: var(--app-gold-gradient);
    border-color: transparent;
    box-shadow: 0 4px 20px rgba(var(--app-accent-rgb), 0.25);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 28px rgba(var(--app-accent-rgb), 0.4);
    }

    &:active {
      transform: scale(0.98);
    }
  }

  &:disabled {
    cursor: not-allowed;
  }
}

/* ======== 空状态 ======== */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 20px;
}

.empty-text {
  font-size: 15px;
  color: var(--app-text-secondary);
  text-align: center;
}

.empty-btn {
  padding: 12px 32px;
  border: 1px solid var(--app-gold);
  border-radius: 12px;
  background: transparent;
  color: var(--app-gold);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.25s ease;

  &:hover {
    background: rgba(var(--app-accent-rgb), 0.1);
  }
}

/* ======== 过渡动画 ======== */
.question-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.question-slide-leave-active {
  transition: all 0.2s ease-in;
}

.question-slide-enter-from {
  opacity: 0;
  transform: translateX(40px);
}

.question-slide-leave-to {
  opacity: 0;
  transform: translateX(-40px);
}

.feedback-enter-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.feedback-enter-leave-active {
  transition: all 0.15s ease-in;
}

.feedback-enter-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.9);
}

.feedback-enter-leave-to {
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-down-enter-active {
  transition: all 0.3s ease;
}

.slide-down-leave-active {
  transition: all 0.2s ease-in;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes abyss-progress-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
</style>
