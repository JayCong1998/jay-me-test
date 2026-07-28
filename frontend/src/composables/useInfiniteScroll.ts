import { ref, shallowRef } from 'vue'

function isAuthExpiredError(error: unknown): boolean {
  return error instanceof Error && error.name === 'AuthExpiredError'
}

export interface PageResult<T> {
  items: T[]
  /** API 返回的总数，用于精确判断 hasMore。不传则用 pageSize 推断 */
  total?: number
}

export interface UseInfiniteScrollOptions<T> {
  /** 每页条数，默认 10 */
  pageSize?: number
  /** 获取指定页数据 */
  fetchPage: (page: number) => Promise<PageResult<T>>
}

export function useInfiniteScroll<T>(options: UseInfiniteScrollOptions<T>) {
  const pageSize = options.pageSize ?? 10

  const items = shallowRef<T[]>([])
  const currentPage = ref(1)
  const hasMore = ref(true)
  const loading = ref(false)
  const loadingMore = ref(false)
  const error = ref<string | null>(null)
  const refreshing = ref(false)

  async function loadFirstPage() {
    loading.value = true
    error.value = null
    try {
      const result = await options.fetchPage(1)
      items.value = result.items
      currentPage.value = 1
      hasMore.value = resolveHasMore(result, 0)
    } catch (e: any) {
      items.value = []
      error.value = isAuthExpiredError(e) ? null : e.message || '加载失败'
    } finally {
      loading.value = false
    }
  }

  async function loadMore() {
    if (loadingMore.value || !hasMore.value) return
    loadingMore.value = true
    try {
      const result = await options.fetchPage(currentPage.value + 1)
      if (result.items.length > 0) {
        items.value = [...items.value, ...result.items]
        currentPage.value++
        hasMore.value = resolveHasMore(result, currentPage.value - 1)
      } else {
        hasMore.value = false
      }
    } catch {
      // 加载更多失败静默处理
    } finally {
      loadingMore.value = false
    }
  }

  async function onRefresh() {
    refreshing.value = true
    try {
      await loadFirstPage()
    } finally {
      refreshing.value = false
    }
  }

  function reset() {
    items.value = []
    currentPage.value = 1
    hasMore.value = true
    error.value = null
    loading.value = false
    loadingMore.value = false
  }

  /**
   * 判断是否还有更多数据。
   * 优先使用 API 返回的 total；否则用当前页返回条数 >= pageSize 推断。
   */
  function resolveHasMore(result: PageResult<T>, _page: number): boolean {
    if (result.total !== undefined) {
      return items.value.length < result.total
    }
    return result.items.length >= pageSize
  }

  return {
    items,
    currentPage,
    hasMore,
    loading,
    loadingMore,
    error,
    refreshing,
    loadFirstPage,
    loadMore,
    onRefresh,
    reset,
  }
}
