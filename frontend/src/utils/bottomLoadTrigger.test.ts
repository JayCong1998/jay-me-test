import { describe, expect, it } from 'vitest'
import { getBottomLoadState, isPullRefreshEnabled } from './bottomLoadTrigger'

describe('bottom load trigger', () => {
  it('triggers once when scrolling down into the bottom edge', () => {
    expect(getBottomLoadState({
      scrollTop: 502,
      previousScrollTop: 470,
      clientHeight: 500,
      scrollHeight: 1000,
      wasNearBottom: false,
    })).toEqual({ shouldLoad: true, isNearBottom: true })
  })

  it('does not trigger while reversing or staying in the bottom edge', () => {
    expect(getBottomLoadState({
      scrollTop: 500,
      previousScrollTop: 510,
      clientHeight: 500,
      scrollHeight: 1000,
      wasNearBottom: true,
    })).toEqual({ shouldLoad: false, isNearBottom: true })
  })

  it('resets after the user scrolls away from the bottom edge', () => {
    expect(getBottomLoadState({
      scrollTop: 430,
      previousScrollTop: 500,
      clientHeight: 500,
      scrollHeight: 1000,
      wasNearBottom: true,
    })).toEqual({ shouldLoad: false, isNearBottom: false })
  })

  it('enables pull-to-refresh only when the actual scroll container is at its top', () => {
    expect(isPullRefreshEnabled(0)).toBe(true)
    expect(isPullRefreshEnabled(1)).toBe(false)
  })
})
