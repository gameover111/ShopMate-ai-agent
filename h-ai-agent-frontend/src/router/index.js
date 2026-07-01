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
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.afterEach((to) => {
  applyPageSEO(to.meta)
})

export default router
