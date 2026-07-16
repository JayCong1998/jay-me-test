<template>
  <div class="auth-page page-bg">
    <div class="bg-orb bg-orb--top"></div>
    <div class="bg-orb bg-orb--bottom"></div>

    <div class="auth-content">
      <!-- Logo -->
      <section class="hero-section">
        <h1 class="app-title text-gold">杰迷结业考试</h1>
        <p class="app-subtitle">登录后参与排行榜，和全国杰迷一决高下</p>
      </section>

      <!-- 表单 -->
      <section class="form-section glass-card">
        <h2 class="form-title">登录</h2>

        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input
            v-model="form.email"
            type="email"
            class="form-input"
            placeholder="请输入邮箱"
            autocomplete="email"
          />
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </div>

        <p v-if="errorMsg" class="error-text">{{ errorMsg }}</p>

        <button
          class="btn-submit"
          :disabled="loading || !canSubmit"
          @click="handleLogin"
        >
          <span v-if="!loading">登录</span>
          <span v-else class="loading-spinner"></span>
        </button>

        <p class="switch-text">
          还没有账号？<router-link to="/register" class="switch-link">去注册</router-link>
        </p>
      </section>

      <!-- 游客入口 -->
      <section class="guest-section">
        <router-link to="/" class="guest-link">← 先不登录，以游客身份答题</router-link>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useUserStore } from '@/stores/userStore'
import * as authApi from '@/api/authApi'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const userStore = useUserStore()

const form = reactive({ email: '', password: '' })
const loading = ref(false)
const errorMsg = ref('')

const canSubmit = computed(() => {
  return form.email.trim() && form.password.trim()
})

async function handleLogin() {
  if (!canSubmit.value) return
  errorMsg.value = ''
  loading.value = true

  try {
    const res = await authApi.login({
      email: form.email.trim(),
      password: form.password,
    })
    authStore.setAuth(res.token, res.user)

    // 登录成功后清空游客数据，同步昵称
    userStore.reset()
    userStore.setNickname(res.user.nickname)

    // 重定向
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    errorMsg.value = e.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@use './auth-common.scss';
</style>
