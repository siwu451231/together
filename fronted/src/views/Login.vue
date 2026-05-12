<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="brand-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M14.058 3.41c-4.074-.74-8.077.6-10.245 4.288-2.167 3.688-1.51 8.274 1.766 11.228l1.373 1.233-1.514 1.15a.75.75 0 0 0 .524 1.332l5.125-.33a.75.75 0 0 0 .618-.466l1.838-4.881a.75.75 0 0 0-1.428-.506l-.805 2.138-1.464-1.112c-2.434-2.193-2.92-5.59-1.31-8.324 1.611-2.734 4.582-3.73 7.608-3.18 3.027.55 5.513 3.084 5.952 6.064l1.492-.224c-.588-3.992-3.92-7.387-7.97-8.121z"/>
        </svg>
      </div>
      <h1>登录 Together</h1>
      <p class="subtitle">极简高效的文档协作工具</p>
      <form @submit.prevent="submit">
        <div class="input-group">
          <input v-model="form.username" placeholder="用户名或邮箱" required />
        </div>
        <div class="input-group">
          <input v-model="form.password" placeholder="密码" type="password" required />
        </div>
        <button :disabled="loading" class="primary-btn">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <p class="tips">还没有账号？ <router-link to="/register">立即注册</router-link></p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: '', password: '' })

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    await auth.login(form)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  padding: 24px;
}
.auth-card {
  width: 100%;
  max-width: 400px;
  background: #ffffff;
  border: 1px solid #ebebeb;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.02);
  text-align: center;
}
.brand {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.brand-icon {
  width: 32px;
  height: 32px;
  color: #111;
}
h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  letter-spacing: -0.5px;
  color: #111;
}
.subtitle {
  color: #777;
  font-size: 14px;
  margin: 8px 0 32px;
}
form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.input-group input {
  width: 100%;
  padding: 14px 16px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
  background: #fafafa;
}
.input-group input:focus {
  border-color: #111;
  background: #fff;
}
.primary-btn {
  margin-top: 8px;
  padding: 14px;
  border-radius: 8px;
  border: none;
  background: #111;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
}
.primary-btn:hover:not(:disabled) {
  background: #333;
}
.tips {
  margin-top: 24px;
  font-size: 14px;
  color: #777;
}
.tips a {
  color: #111;
  font-weight: 500;
}
.error {
  margin-top: 16px;
  color: #d32f2f;
  font-size: 14px;
}
</style>
