<template>
  <div class="app-shell">
    <header class="app-navbar" v-if="showNavbar">
      <router-link to="/" class="nav-brand">Shopmate</router-link>
      <nav class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link v-if="isLoggedIn" to="/profile" class="nav-link">账户</router-link>
        <router-link v-if="isAdmin" to="/admin/users" class="nav-link">管理</router-link>
      </nav>
      <div class="nav-auth">
        <template v-if="isLoggedIn">
          <span class="nav-user">{{ currentUser?.username }}</span>
          <button class="nav-btn" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-btn">登录</router-link>
          <router-link to="/register" class="nav-btn nav-btn-primary">注册</router-link>
        </template>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { isLoggedIn, isAdmin, currentUser, logout } = useAuth()

const showNavbar = computed(() => {
  const hiddenRoutes = ['login', 'register']
  return !hiddenRoutes.includes(route.name)
})

function handleLogout() {
  logout()
  router.push('/')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

.app-navbar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 var(--safe-x);
  height: 52px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  flex-shrink: 0;
  z-index: 10;
}

.nav-brand {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 700;
  color: var(--color-accent);
  letter-spacing: 0.05em;
  margin-right: auto;
}

.nav-links {
  display: flex;
  gap: 16px;
}

.nav-link {
  font-size: 13px;
  color: var(--color-text-muted);
  transition: color 0.2s;
}

.nav-link:hover {
  color: var(--color-text);
}

.nav-auth {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-user {
  font-size: 13px;
  color: var(--color-text);
  font-weight: 600;
}

.nav-btn {
  padding: 5px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--color-text-muted);
  background: transparent;
  transition: border-color 0.2s, color 0.2s;
}

.nav-btn:hover {
  border-color: var(--color-accent);
  color: var(--color-text);
}

.nav-btn-primary {
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  color: #fff;
  border: none;
}

.nav-btn-primary:hover {
  filter: brightness(1.08);
}

.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>
