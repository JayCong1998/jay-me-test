import axios from 'axios'
import type { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/authStore'

export class AuthExpiredError extends Error {
  constructor(message = '登录已过期，请重新登录') {
    super(message)
    this.name = 'AuthExpiredError'
  }
}

let authExpiredToastShown = false

const client: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 直接从 localStorage 读取 token，避免 Pinia 尚未初始化时首个接口漏带登录态。
client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    try {
      const saved = localStorage.getItem('jaymetest_auth')
      if (saved) {
        const auth = JSON.parse(saved)
        if (auth.token) {
          config.headers.Authorization = auth.token
        }
      }
    } catch {
      // 本地缓存损坏时忽略本次 token 注入，响应拦截器会统一处理登录过期。
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error),
)

// 统一处理业务错误和登录过期，让页面代码只关心成功数据和明确异常。
client.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code && data.code !== 200) {
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return response
  },
  (error: AxiosError) => {
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('网络超时，请检查网络后重试'))
    }

    if (!error.response) {
      return Promise.reject(new Error('网络开小差了，请重试'))
    }

    if (error.response.status === 401) {
      try {
        useAuthStore().expireSession()
      } catch {
        localStorage.removeItem('jaymetest_auth')
      }

      if (!authExpiredToastShown) {
        authExpiredToastShown = true
        showToast('登录已过期，请重新登录')
        // 多个接口同时 401 时只提示一次，防止 toast 连续刷屏。
        window.setTimeout(() => {
          authExpiredToastShown = false
        }, 2000)
      }

      return Promise.reject(new AuthExpiredError())
    }

    const payload = error.response.data as { msg?: unknown; message?: unknown } | undefined
    const message = typeof payload?.msg === 'string'
      ? payload.msg
      : typeof payload?.message === 'string'
        ? payload.message
        : '请求失败，请稍后重试'
    return Promise.reject(new Error(message))
  },
)

export default client
