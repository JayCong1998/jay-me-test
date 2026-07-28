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

// Request interceptor: attach current user token when available.
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
      // ignore storage parsing failures
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error),
)

// Response interceptor: unwrap business errors and centralize auth expiry.
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
        window.setTimeout(() => {
          authExpiredToastShown = false
        }, 2000)
      }

      return Promise.reject(new AuthExpiredError())
    }

    return Promise.reject(error)
  },
)

export default client
