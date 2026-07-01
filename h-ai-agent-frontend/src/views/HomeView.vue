<template>
  <div class="home-page">
    <div class="home-bg" aria-hidden="true">
      <div class="grid-lines"></div>
      <div class="glow glow-1"></div>
      <div class="glow glow-2"></div>
    </div>

    <header class="home-hero">
      <p class="hero-badge">
        <span class="badge-dot"></span>
        SHOPMATE · AI HUB
      </p>
      <h1 class="brand">
        <span class="brand-main">Shopmate</span>
        <span class="brand-sub">店小二</span>
      </h1>
      <p class="subtitle">
        电商智能客服 × 超级智能体 × 多 Agent 编排 · 三引擎驱动 · 流式对话即刻响应
      </p>
    </header>

    <main class="app-grid">
      <router-link
        v-for="app in apps"
        :key="app.path"
        :to="app.path"
        class="app-card"
        :data-theme="app.theme"
      >
        <div class="card-head">
          <AiAvatar :type="app.avatarType" size="lg" :title="app.title" />
          <span class="card-tag">{{ app.tag }}</span>
        </div>
        <h2 class="card-title">{{ app.title }}</h2>
        <p class="card-desc">{{ app.description }}</p>
        <span class="card-action">
          进入应用
          <span class="arrow" aria-hidden="true">→</span>
        </span>
      </router-link>
    </main>

    <section class="home-features" aria-label="产品特性">
      <div v-for="feat in features" :key="feat.label" class="feat-item">
        <span class="feat-icon">{{ feat.icon }}</span>
        <span class="feat-label">{{ feat.label }}</span>
      </div>
    </section>

    <SiteFooter />
  </div>
</template>

<script setup>
import AiAvatar from '@/components/AiAvatar.vue'
import SiteFooter from '@/components/SiteFooter.vue'

const apps = [
  {
    path: '/shop-mate',
    theme: 'shop-mate',
    avatarType: 'shop-mate',
    tag: 'SSE · 多轮记忆',
    title: 'AI 智能客服 · 店小二',
    description:
      '深耕电商客服场景，售前咨询、售后纠纷、差评投诉话术优化，支持 chatId 多轮会话记忆与流式输出。',
  },
  {
    path: '/manus',
    theme: 'manus',
    avatarType: 'manus',
    tag: 'Agent · 分步执行',
    title: 'AI 超级智能体 · HManus',
    description:
      '全能型 AI 助手，自主规划并调用工具逐步完成任务，每个执行步骤独立展示，清晰可追踪。',
  },
  {
    path: '/orchestrator',
    theme: 'manus',
    avatarType: 'manus',
    tag: 'Harness · 多 Agent',
    title: '多 Agent 编排器 · Orchestrator',
    description:
      '统一入口，自动分析需求并分派给最合适的 Agent。HManus 处理复杂任务，店小二专注电商客服。',
  },
]

const features = [
  { icon: '⚡', label: 'SSE 流式' },
  { icon: '🧠', label: 'Spring AI' },
  { icon: '🔗', label: 'Agent Harness' },
  { icon: '🎯', label: '多 Agent 编排' },
]
</script>

<style scoped>
.home-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  min-height: 0;
  overflow-x: hidden;
}

.home-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.grid-lines {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.04) 1px, transparent 1px);
  background-size: 40px 40px;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
}

.glow-1 {
  width: 400px;
  height: 400px;
  top: -120px;
  left: -80px;
  background: #6366f1;
}

.glow-2 {
  width: 360px;
  height: 360px;
  bottom: 80px;
  right: -60px;
  background: #ec4899;
}

.home-hero {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: clamp(32px, 8vw, 64px) var(--safe-x) clamp(24px, 5vw, 40px);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.2em;
  color: var(--color-accent);
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: rgba(0, 212, 255, 0.06);
  margin-bottom: 20px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-success);
  box-shadow: 0 0 8px var(--color-success);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  50% {
    opacity: 0.5;
  }
}

.brand {
  line-height: 1.15;
  margin-bottom: 14px;
}

.brand-main {
  display: block;
  font-size: clamp(36px, 9vw, 52px);
  font-weight: 800;
  background: linear-gradient(135deg, #00d4ff 0%, #a78bfa 45%, #f472b6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.brand-sub {
  display: block;
  font-size: clamp(18px, 4vw, 24px);
  font-weight: 600;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.subtitle {
  max-width: 520px;
  margin: 0 auto;
  font-size: clamp(14px, 3vw, 16px);
  color: var(--color-text-muted);
  line-height: 1.7;
}

.app-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 300px), 1fr));
  gap: clamp(16px, 3vw, 24px);
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 var(--safe-x) 32px;
}

.app-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition:
    transform 0.25s,
    border-color 0.25s,
    box-shadow 0.25s;
}

.app-card[data-theme='shop-mate']:hover {
  transform: translateY(-6px);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 16px 40px rgba(99, 102, 241, 0.2);
}

.app-card[data-theme='manus']:hover {
  transform: translateY(-6px);
  border-color: rgba(236, 72, 153, 0.5);
  box-shadow: 0 16px 40px rgba(236, 72, 153, 0.2);
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-tag {
  font-family: var(--font-mono);
  font-size: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  background: rgba(0, 212, 255, 0.08);
  color: var(--color-accent);
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 10px;
}

.card-desc {
  flex: 1;
  font-size: 14px;
  color: var(--color-text-muted);
  line-height: 1.65;
  margin-bottom: 18px;
  text-align: left;
}

.card-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-accent);
}

.app-card[data-theme='shop-mate'] .card-action {
  color: #a5b4fc;
}

.app-card[data-theme='manus'] .card-action {
  color: #f9a8d4;
}

.arrow {
  transition: transform 0.2s;
}

.app-card:hover .arrow {
  transform: translateX(4px);
}

.home-features {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: clamp(12px, 3vw, 28px);
  padding: 8px var(--safe-x) 40px;
  margin-top: auto;
}

.feat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-dim);
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--color-border);
}

.feat-icon {
  font-size: 16px;
}

@media (max-width: 480px) {
  .home-features {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}
</style>
