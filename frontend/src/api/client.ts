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
    // 401 表示登录已过期或 token 无效，清除本地登录态并跳转首页
    if (error.response.status === 401) {
      localStorage.removeItem('jaymetest_auth')
      // 如果当前不在首页，跳转到首页（游客模式）
      if (window.location.hash !== '#/') {
        window.location.hash = '#/'
      }
      // 延迟 reload 确保路由跳转生效，同时清除内存中的 store 状态
      window.location.reload()
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }
    return Promise.reject(error)
  }
)

export default client
