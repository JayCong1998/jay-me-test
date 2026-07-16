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
    meta: { title: '排行榜' },
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 全局路由守卫 - 设置页面标题
router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '杰迷结业考试'
  next()
})

export default router
