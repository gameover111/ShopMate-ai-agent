import { createRouter, createWebHistory } from 'vue-router'
import { applyPageSEO } from '@/utils/seo'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: {
      title: 'AI 应用中心',
      description:
        'Shopmate 店小二 AI 应用中心，一站式接入电商智能客服与 HManus 超级智能体，支持 SSE 流式对话与多轮会话。',
      keywords:
        'Shopmate,店小二,AI应用,电商客服,智能客服,HManus,超级智能体,Spring AI,Vue3',
    },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { title: '账户设置', requiresAuth: true },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('@/views/AdminUsersView.vue'),
    meta: { title: '用户管理 - 管理员', requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/shop-mate',
    name: 'shop-mate',
    component: () => import('@/views/ShopMateChatView.vue'),
    meta: {
      title: 'AI 智能客服 - 店小二',
      description:
        'Shopmate 店小二 AI 智能客服，专注售前咨询、售后纠纷、差评投诉话术优化，支持多轮会话记忆与 SSE 实时流式回复。',
      keywords:
        '店小二,AI客服,电商客服,话术优化,售前,售后,差评,SSE,多轮对话',
    },
  },
  {
    path: '/manus',
    name: 'manus',
    component: () => import('@/views/ManusChatView.vue'),
    meta: {
      title: 'AI 超级智能体 - HManus',
      description:
        'HManus AI 超级智能体，自主规划任务、调用工具逐步执行，每个 Agent 步骤独立展示，适合复杂自动化场景。',
      keywords:
        'HManus,超级智能体,AI Agent,自主规划,工具调用,SSE,步骤执行',
    },
  },
  {
    path: '/orchestrator',
    name: 'orchestrator',
    component: () => import('@/views/OrchestratorChatView.vue'),
    meta: {
      title: '多 Agent 编排器 - Orchestrator',
      description:
        '多 Agent 编排器，自动分析用户需求并分派给 HManus 或店小二，统一入口完成复杂任务。',
      keywords:
        'Orchestrator,多Agent,编排器,AI,智能路由,任务分派',
    },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 导航守卫：需登录/需管理员的页面
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('shopmate_token')
  const user = JSON.parse(localStorage.getItem('shopmate_user') || 'null')

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && user?.role !== 'ADMIN') {
    next('/')
    return
  }

  next()
})

router.afterEach((to) => {
  applyPageSEO(to.meta)
})

export default router
