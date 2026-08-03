import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/themeStore'
import './styles/global.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)

try {
  localStorage.removeItem('jaymetest_user')
} catch {
  // localStorage 不可用时不影响应用启动
}

// 在 mount 前初始化主题（恢复 localStorage 中的主题偏好）
useThemeStore()

app.mount('#app')
