import { defineStore } from 'pinia'
import { fetchMyRecords as fetchMyRecordsApi } from '@/api/statsApi'
import type { GameRecordDTO } from '@/api/statsApi'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'

export const useRecordStore = defineStore('record', () => {
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
