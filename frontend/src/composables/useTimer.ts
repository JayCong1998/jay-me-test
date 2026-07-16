import { ref, onUnmounted } from 'vue'

/**
 * 计时器 composable
 */
export function useTimer() {
  const elapsed = ref(0)
  let timerId: ReturnType<typeof setInterval> | null = null
  let startTimestamp = 0

  function start() {
    stop()
    elapsed.value = 0
    startTimestamp = Date.now()
    timerId = setInterval(() => {
      elapsed.value = Math.floor((Date.now() - startTimestamp) / 1000)
    }, 200)
  }

  function stop() {
    if (timerId !== null) {
      clearInterval(timerId)
      timerId = null
    }
  }

  function reset() {
    stop()
    elapsed.value = 0
  }

  onUnmounted(() => {
    stop()
  })

  return { elapsed, start, stop, reset }
}
