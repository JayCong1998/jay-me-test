<template>
  <div class="auth-page page-bg">
    <div class="bg-orb bg-orb--top"></div>
    <div class="bg-orb bg-orb--bottom"></div>

    <div class="auth-content">
      <section class="hero-section">
        <span class="auth-badge">✦ 杰迷认证中心</span>
        <h1 class="app-title text-gold">杰迷试炼</h1>
        <p class="app-subtitle">注册账号，你的成绩将永久保留并参与排行榜</p>
      </section>

      <section class="form-section glass-card">
        <h2 class="form-title">注册</h2>
        <p class="form-intro">创建账号，保存你的每一次高光表现</p>

        <div class="form-group">
          <div class="field-label-row"><label class="form-label" for="register-nickname">昵称</label></div>
          <div class="input-wrap" :class="{ 'is-invalid': showNicknameError, 'is-valid': !nicknameError && form.nickname }">
            <span class="input-icon">♩</span>
            <input id="register-nickname" v-model="form.nickname" type="text" class="form-input" placeholder="给自己起个响亮的杰迷名号" maxlength="10" autocomplete="nickname" @blur="touched.nickname = true" />
          </div>
          <p v-if="showNicknameError" class="field-message is-error">{{ nicknameError }}</p>
        </div>

        <div class="form-group">
          <div class="field-label-row"><label class="form-label" for="register-email">邮箱</label></div>
          <div class="input-wrap" :class="{ 'is-invalid': showEmailError, 'is-valid': !emailError && form.email }">
            <span class="input-icon">@</span>
            <input id="register-email" v-model="form.email" type="email" class="form-input" placeholder="name@example.com" autocomplete="email" inputmode="email" @blur="touched.email = true" />
          </div>
          <p v-if="showEmailError" class="field-message is-error">{{ emailError }}</p>
        </div>

        <div class="form-group">
          <div class="field-label-row"><label class="form-label" for="register-password">密码</label></div>
          <div class="input-wrap input-wrap--password" :class="{ 'is-invalid': showPasswordError, 'is-valid': !passwordError && form.password }">
            <input id="register-password" v-model="form.password" :type="showPassword ? 'text' : 'password'" class="form-input" placeholder="设置 6–10 位密码" maxlength="10" autocomplete="new-password" @blur="touched.password = true" @keyup.enter="handleRegister" />
            <button class="password-toggle" type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
              <svg v-if="showPassword" class="password-toggle__icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M3 3l18 18" />
                <path d="M10.7 5.1A10.8 10.8 0 0 1 12 5c5 0 9 4.5 10 7a14.7 14.7 0 0 1-3.1 4.4" />
                <path d="M6.6 6.6A14.4 14.4 0 0 0 2 12c1 2.5 5 7 10 7a10.7 10.7 0 0 0 4.9-1.2" />
                <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
              </svg>
              <svg v-else class="password-toggle__icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            </button>
          </div>
          <p v-if="showPasswordError" class="field-message is-error">{{ passwordError }}</p>
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
import { validateEmail, validateNickname, validatePassword } from '@/utils/authValidation'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const form = reactive({ nickname: '', email: '', password: '' })
const loading = ref(false)
const errorMsg = ref('')
const showPassword = ref(false)
const touched = reactive({ nickname: false, email: false, password: false })
const nicknameError = computed(() => validateNickname(form.nickname))
const emailError = computed(() => validateEmail(form.email))
const passwordError = computed(() => validatePassword(form.password))
const showNicknameError = computed(() => touched.nickname && !!nicknameError.value)
const showEmailError = computed(() => touched.email && !!emailError.value)
const showPasswordError = computed(() => touched.password && !!passwordError.value)

const canSubmit = computed(() => {
  return !nicknameError.value && !emailError.value && !passwordError.value
})

async function handleRegister() {
  touched.nickname = true
  touched.email = true
  touched.password = true
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
