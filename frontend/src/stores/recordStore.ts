import { defineStore } from 'pinia'
import { fetchMyRecords as fetchMyRecordsApi } from '@/api/statsApi'
import type { GameRecordDTO } from '@/api/statsApi'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'

export const useRecordStore = defineStore('record', () => {
  // 个人记录页可能长列表滚动，分页状态集中在 composable 中，避免页面切换后重复实现加载边界。
  const {
    items: records,
    loading,
    error,
    loadingMore,
    hasMore,
    loadFirstPage: fetchMyRecords,
    loadMore,
    reset: _reset,
  } = useInfiniteScroll<GameRecordDTO>({
    pageSize: 10,
    fetchPage: async (page) => {
      const data = await fetchMyRecordsApi(page, 10)
      return { items: data }
    },
  })

  function clear() {
    _reset()
  }

  return {
    records,
    loading,
    error,
    loadingMore,
    hasMore,
    fetchMyRecords,
    loadMore,
    clear,
  }
})
