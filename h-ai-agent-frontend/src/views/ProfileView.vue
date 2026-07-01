<template>
  <div class="profile-page">
    <div class="profile-card">
      <h1 class="page-title">账户设置</h1>

      <form class="profile-form" @submit.prevent="updateProfile">
        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" disabled class="input-disabled" />
          <span class="field-hint">用户名不可修改</span>
        </div>
        <div class="field">
          <label>邮箱</label>
          <input v-model="form.email" type="email" placeholder="your@email.com" />
        </div>
        <p v-if="profileMsg" class="success-msg">{{ profileMsg }}</p>
        <button type="submit" class="btn-submit" :disabled="profileLoading">
          {{ profileLoading ? '保存中…' : '保存修改' }}
        </button>
      </form>

      <hr class="divider" />

      <h2 class="section-title">修改密码</h2>
      <form class="profile-form" @submit.prevent="changePassword">
        <div class="field">
          <label>当前密码</label>
          <input v-model="pwdForm.oldPassword" type="password" required />
        </div>
        <div class="field">
          <label>新密码</label>
          <input v-model="pwdForm.newPassword" type="password" required />
        </div>
        <p v-if="pwdMsg" class="success-msg">{{ pwdMsg }}</p>
        <p v-if="pwdError" class="error-msg">{{ pwdError }}</p>
        <button type="submit" class="btn-submit" :disabled="pwdLoading">
          {{ pwdLoading ? '修改中…' : '修改密码' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import http from '@/api/config'
import { useAuth } from '@/stores/auth'

const { currentUser } = useAuth()

const form = reactive({ username: '', email: '' })
const profileLoading = ref(false)
const profileMsg = ref('')

const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const pwdLoading = ref(false)
const pwdMsg = ref('')
const pwdError = ref('')

onMounted(() => {
  const user = currentUser.value
  if (user) {
    form.username = user.username || ''
    form.email = user.email || ''
  }
})

async function updateProfile() {
  profileLoading.value = true
  profileMsg.value = ''
  try {
    await http.put('/user/profile', { email: form.email })
    profileMsg.value = '个人信息已更新'
  } catch (e) {
    profileMsg.value = '更新失败: ' + (e.response?.data?.message || e.message)
  } finally {
    profileLoading.value = false
  }
}

async function changePassword() {
  pwdLoading.value = true
  pwdMsg.value = ''
  pwdError.value = ''
  try {
    await http.put('/user/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    pwdMsg.value = '密码修改成功'
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch (e) {
    pwdError.value = e.response?.data?.message || '密码修改失败'
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 32px 16px;
  background: var(--color-bg);
}

.profile-card {
  width: 100%;
  max-width: 500px;
  padding: 32px 28px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 24px;
  color: var(--color-text);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--color-text-muted);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
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

.input-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.field-hint {
  font-size: 11px;
  color: var(--color-text-dim);
}

.btn-submit {
  padding: 10px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  color: #fff;
}

.btn-submit:disabled {
  opacity: 0.5;
}

.btn-submit:not(:disabled):hover {
  filter: brightness(1.08);
}

.divider {
  border: none;
  border-top: 1px solid var(--color-border);
  margin: 28px 0;
}

.success-msg {
  color: var(--color-success);
  font-size: 13px;
  text-align: center;
}

.error-msg {
  color: var(--color-danger);
  font-size: 13px;
  text-align: center;
}
</style>
