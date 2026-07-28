import axios from 'axios'
import type { ApiResponse } from '@/types'

const AUTH_KEY = 'jaymetest_admin_auth'

export const apiClient = axios.create({
  baseURL: '/api/admin',
  timeout: 10000,
})

apiClient.interceptors.request.use((config) => {
  const saved = localStorage.getItem(AUTH_KEY)
  if (saved) {
    const auth = JSON.parse(saved) as { token?: string }
    if (auth.token) {
      config.headers.Authorization = auth.token
    }
  }
  return config
})

apiClient.interceptors.response.use(
  (response): any => {
    const payload = response.data as ApiResponse<unknown>
    if (payload?.code && payload.code !== 200) {
      return Promise.reject(new Error(payload.msg || '请求失败'))
    }
    return payload.data
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem(AUTH_KEY)
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export { AUTH_KEY }
