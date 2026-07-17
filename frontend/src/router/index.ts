import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/HomePage.vue'),
    meta: { title: '杰迷结业考试' },
  },
  {
    path: '/quiz',
    name: 'Quiz',
    component: () => import('@/pages/QuizPage.vue'),
    meta: { title: '答题中' },
  },
  {
    path: '/result',
    name: 'Result',
    component: () => import('@/pages/ResultPage.vue'),
    meta: { title: '考试结果' },
  },
  {
    path: '/certificate',
    name: 'Certificate',
    component: () => import('@/pages/CertificatePage.vue'),
    meta: { title: '我的证书' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/RegisterPage.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/leaderboard',
    name: 'Leaderboard',
    component: () => import('@/pages/LeaderboardPage.vue'),
    meta: { title: '排行榜', requiresAuth: true },
  },
  {
    path: '/albums',
    name: 'Albums',
    component: () => import('@/pages/AlbumListPage.vue'),
    meta: { title: '专辑闯关', requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 全局路由守卫 - 设置页面标题 + 登录校验
router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '杰迷结业考试'

  // 需要登录的页面，检查是否有有效 token
  if (to.meta.requiresAuth) {
    try {
      const saved = localStorage.getItem('jaymetest_auth')
      const auth = saved ? JSON.parse(saved) : null
      if (!auth?.token) {
        // 未登录，跳转到首页
        next({ name: 'Home' })
        return
      }
    } catch {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router
