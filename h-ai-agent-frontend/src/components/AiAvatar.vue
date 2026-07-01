<template>
  <div class="ai-avatar" :class="[type, size]" :title="title">
    <img v-if="type === 'shop-mate'" src="@/assets/avatars/shop-mate.svg" alt="店小二 AI" />
    <img v-else-if="type === 'manus'" src="@/assets/avatars/manus.svg" alt="HManus AI" />
    <img v-else src="@/assets/avatars/user.svg" alt="用户" />
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'user',
    validator: (v) => ['shop-mate', 'manus', 'user'].includes(v),
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md', 'lg'].includes(v),
  },
  title: { type: String, default: '' },
})
</script>

<style scoped>
.ai-avatar {
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-surface);
  border: 2px solid var(--color-border);
  box-shadow: var(--shadow-glow);
}

.ai-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.ai-avatar.sm {
  width: 32px;
  height: 32px;
}

.ai-avatar.md {
  width: 40px;
  height: 40px;
}

.ai-avatar.lg {
  width: 48px;
  height: 48px;
}

.ai-avatar.shop-mate {
  border-color: rgba(99, 102, 241, 0.45);
  box-shadow: 0 0 16px rgba(99, 102, 241, 0.25);
}

.ai-avatar.manus {
  border-color: rgba(236, 72, 153, 0.45);
  box-shadow: 0 0 16px rgba(236, 72, 153, 0.25);
}

.ai-avatar.user {
  border-color: rgba(0, 212, 255, 0.35);
}
</style>
