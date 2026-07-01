<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">注册</h1>
      <p class="auth-subtitle">创建你的 Shopmate 账号</p>

      <form class="auth-form" @submit.prevent="handleRegister">
        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="至少 3 个字符" required />
        </div>
        <div class="field">
          <label>邮箱（登录账号）</label>
          <input v-model="form.email" type="email" placeholder="输入邮箱作为登录账号" required />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="至少 6 个字符" required />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '注册中…' : '注册' }}
        </button>
      </form>

      <p class="auth-switch">
        已有账号？
        <router-link to="/login">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const { register } = useAuth()

const form = reactive({ username: '', email: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await register(form.username, form.password, form.email)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  background: var(--color-bg);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 36px 28px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  color: var(--color-text);
  margin-bottom: 4px;
}

.auth-subtitle {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-muted);
  margin-bottom: 28px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.field input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  background: var(--color-surface);
  color: var(--color-text);
  outline: none;
  text-align: left;
}

.field input:focus {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.15);
}

.error-msg {
  color: var(--color-danger);
  font-size: 13px;
  text-align: center;
}

.btn-submit {
  padding: 11px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  color: #fff;
  transition: opacity 0.2s;
}

.btn-submit:disabled {
  opacity: 0.5;
}

.btn-submit:not(:disabled):hover {
  filter: brightness(1.08);
}

.auth-switch {
  text-align: center;
  font-size: 13px;
  color: var(--color-text-dim);
  margin-top: 20px;
}

.auth-switch a {
  color: var(--color-accent);
  font-weight: 600;
}
</style>
