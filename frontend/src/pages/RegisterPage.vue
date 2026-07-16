<template>
  <div class="auth-page page-bg">
    <div class="bg-orb bg-orb--top"></div>
    <div class="bg-orb bg-orb--bottom"></div>

    <div class="auth-content">
      <!-- Logo -->
      <section class="hero-section">
        <h1 class="app-title text-gold">杰迷结业考试</h1>
        <p class="app-subtitle">注册账号，你的成绩将永久保留并参与排行榜</p>
      </section>

      <!-- 表单 -->
      <section class="form-section glass-card">
        <h2 class="form-title">注册</h2>

        <div class="form-group">
          <label class="form-label">昵称</label>
          <input
            v-model="form.nickname"
            type="text"
            class="form-input"
            placeholder="给自己起个响亮的杰迷名号"
            maxlength="20"
            autocomplete="off"
          />
        </div>

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
            placeholder="6-20位密码"
            maxlength="20"
            autocomplete="new-password"
            @keyup.enter="handleRegister"
          />
        </div>

        <p v-if="errorMsg" class="error-text">{{ errorMsg }}</p>

        <button
          class="btn-submit"
          :disabled="loading || !canSubmit"
          @click="handleRegister"
        >
          <span v-if="!loading">注册</span>
          <span v-else class="loading-spinner"></span>
        </button>

        <p class="switch-text">
          已有账号？<router-link to="/login" class="switch-link">去登录</router-link>
        </p>
      </section>

      <!-- 游客入口 -->
      <section class="guest-section">
        <router-link to="/" class="guest-link">← 先不注册，以游客身份答题</router-link>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useUserStore } from '@/stores/userStore'
import * as authApi from '@/api/authApi'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const form = reactive({ nickname: '', email: '', password: '' })
const loading = ref(false)
const errorMsg = ref('')

const canSubmit = computed(() => {
  return form.nickname.trim().length >= 1
    && form.nickname.trim().length <= 20
    && form.email.trim()
    && form.password.length >= 6
    && form.password.length <= 20
})

async function handleRegister() {
  if (!canSubmit.value) return
  errorMsg.value = ''
  loading.value = true

  try {
    const res = await authApi.register({
      email: form.email.trim(),
      password: form.password,
      nickname: form.nickname.trim(),
    })
    authStore.setAuth(res.token, res.user)

    // 注册成功后清空游客数据，同步昵称
    userStore.reset()
    userStore.setNickname(res.user.nickname)

    router.push('/')
  } catch (e: any) {
    errorMsg.value = e.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@use './auth-common.scss';
</style>
