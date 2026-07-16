import axios from 'axios'
import type { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'

const client: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 添加 token（登录用户）
    try {
      const saved = localStorage.getItem('jaymetest_auth')
      if (saved) {
        const auth = JSON.parse(saved)
        if (auth.token) {
          config.headers.Authorization = auth.token
        }
      }
    } catch {
      // ignore
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
client.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code && data.code !== 200) {
      // 业务错误
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
    return Promise.reject(error)
  }
)

export default client
