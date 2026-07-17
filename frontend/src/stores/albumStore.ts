import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { fetchAlbumList, type AlbumDTO } from '@/api/albumApi'

export const useAlbumStore = defineStore('album', () => {
  const albums = ref<AlbumDTO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const unlockedAlbums = computed(() => albums.value.filter(a => a.unlocked))
  const lockedAlbums = computed(() => albums.value.filter(a => !a.unlocked))
  const progressSummary = computed(() => {
    const unlocked = unlockedAlbums.value.length
    const total = albums.value.length
    return { unlocked, total, percentage: total > 0 ? Math.round((unlocked / total) * 100) : 0 }
  })

  function albumByKey(key: string): AlbumDTO | undefined {
    return albums.value.find(a => a.albumKey === key)
  }

  async function fetchAlbums(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      albums.value = await fetchAlbumList()
    } catch (e: any) {
      error.value = e.message || '获取专辑列表失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  function reset() {
    albums.value = []
    loading.value = false
    error.value = null
  }

  return {
    albums, loading, error,
    unlockedAlbums, lockedAlbums, progressSummary,
    albumByKey,
    fetchAlbums, reset,
  }
})
