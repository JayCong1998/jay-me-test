export interface BottomLoadTriggerInput {
  scrollTop: number
  previousScrollTop: number
  clientHeight: number
  scrollHeight: number
  wasNearBottom: boolean
}

export interface BottomLoadTriggerState {
  shouldLoad: boolean
  isNearBottom: boolean
}

const BOTTOM_THRESHOLD_PX = 8

export function isPullRefreshEnabled(scrollTop: number): boolean {
  return scrollTop <= 0
}

/**
 * 仅在用户向下滚动、首次进入底部边缘时触发加载，避免触底附近的回弹和反向滑动重复请求。
 */
export function getBottomLoadState(input: BottomLoadTriggerInput): BottomLoadTriggerState {
  const distanceToBottom = input.scrollHeight - input.clientHeight - input.scrollTop
  const isNearBottom = distanceToBottom <= BOTTOM_THRESHOLD_PX
  const isScrollingDown = input.scrollTop > input.previousScrollTop

  return {
    shouldLoad: isNearBottom && isScrollingDown && !input.wasNearBottom,
    isNearBottom,
  }
}
