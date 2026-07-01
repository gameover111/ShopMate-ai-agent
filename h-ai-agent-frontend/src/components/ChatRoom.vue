<template>
  <div class="chat-page" :data-theme="theme">
    <div class="chat-room">
      <header class="chat-header">
        <router-link to="/" class="back-btn" title="返回主页" aria-label="返回主页">
          <span aria-hidden="true">←</span>
        </router-link>
        <div class="header-brand">
          <AiAvatar :type="aiAvatarType" size="sm" :title="title" />
          <div class="header-info">
            <h1 class="title">{{ title }}</h1>
            <p v-if="chatId" class="chat-id">
              <span class="label">SESSION</span> {{ shortChatId }}
            </p>
          </div>
        </div>
        <span v-if="loading" class="status-tag">
          <span class="pulse"></span>
          {{ streamMode === 'step' ? '执行中' : '生成中' }}
        </span>
      </header>

      <main ref="messageListRef" class="message-list" role="log" aria-live="polite">
        <div v-if="messages.length === 0" class="empty-hint">
          <AiAvatar :type="aiAvatarType" size="lg" />
          <p>{{ emptyHint }}</p>
        </div>

        <article
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="msg.role === 'user' ? 'row-user' : 'row-assistant'"
        >
          <AiAvatar
            :type="msg.role === 'user' ? 'user' : aiAvatarType"
            size="md"
            :title="msg.role === 'user' ? '我' : title"
          />
          <div class="bubble-wrap">
            <span v-if="msg.isStep" class="step-badge">STEP</span>
            <div class="bubble" :class="msg.role">
              <pre class="bubble-text">{{ msg.content }}<span
                v-if="showStreamCursor(msg)"
                class="cursor"
              >▋</span></pre>
            </div>
          </div>
        </article>
      </main>

      <p v-if="error" class="error-bar" role="alert">{{ error }}</p>

      <footer class="chat-footer">
        <textarea
          v-model="inputText"
          class="input-area"
          :placeholder="placeholder"
          :disabled="loading"
          rows="2"
          :aria-label="placeholder"
          @keydown.enter.exact.prevent="handleSend"
        />
        <div class="footer-actions">
          <button
            v-if="loading"
            type="button"
            class="btn btn-secondary"
            @click="stopGeneration"
          >
            停止
          </button>
          <button
            type="button"
            class="btn btn-primary"
            :disabled="loading || !inputText.trim()"
            @click="handleSend"
          >
            发送
          </button>
        </div>
      </footer>
    </div>
    <SiteFooter compact />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import AiAvatar from '@/components/AiAvatar.vue'
import SiteFooter from '@/components/SiteFooter.vue'

const props = defineProps({
  title: { type: String, required: true },
  theme: {
    type: String,
    default: 'shop-mate',
    validator: (v) => ['shop-mate', 'manus'].includes(v),
  },
  aiAvatarType: {
    type: String,
    default: 'shop-mate',
    validator: (v) => ['shop-mate', 'manus'].includes(v),
  },
  chatId: { type: String, default: '' },
  messages: { type: Array, required: true },
  inputText: { type: String, required: true },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  streamMode: {
    type: String,
    default: 'accumulate',
    validator: (v) => ['accumulate', 'step'].includes(v),
  },
  emptyHint: {
    type: String,
    default: '输入消息开始对话，AI 将实时流式回复。',
  },
  placeholder: { type: String, default: '输入消息，Enter 发送…' },
})

const emit = defineEmits(['update:inputText', 'send', 'stop'])

const messageListRef = ref(null)

const inputText = computed({
  get: () => props.inputText,
  set: (v) => emit('update:inputText', v),
})

const shortChatId = computed(() => {
  const id = props.chatId
  if (!id || id.length <= 16) return id
  return `${id.slice(0, 8)}…${id.slice(-4)}`
})

const lastStreamAssistant = computed(() => {
  if (props.streamMode !== 'accumulate') return null
  for (let i = props.messages.length - 1; i >= 0; i--) {
    if (props.messages[i].role === 'assistant') return props.messages[i]
  }
  return null
})

function showStreamCursor(msg) {
  return (
    props.loading &&
    props.streamMode === 'accumulate' &&
    msg.id === lastStreamAssistant.value?.id
  )
}

function scrollToBottom() {
  nextTick(() => {
    const el = messageListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function handleSend() {
  emit('send', scrollToBottom)
}

function stopGeneration() {
  emit('stop')
}

watch(
  () => props.messages.length,
  () => scrollToBottom()
)

defineExpose({ scrollToBottom })
</script>

<style scoped>
.chat-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--color-bg);
}

.chat-room {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  width: 100%;
  max-width: var(--chat-max-w);
  margin: 0 auto;
  background: var(--color-bg-elevated);
  border-left: 1px solid var(--color-border);
  border-right: 1px solid var(--color-border);
}

@media (min-width: 960px) {
  .chat-room {
    margin-top: 0;
    border-radius: 0;
  }
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px var(--safe-x);
  min-height: var(--header-h);
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--theme-primary) 25%, var(--color-bg-card)) 0%,
    var(--color-bg-card) 100%
  );
  flex-shrink: 0;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-text);
  font-size: 18px;
  border: 1px solid var(--color-border);
  transition: background 0.2s, border-color 0.2s;
  flex-shrink: 0;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--theme-primary);
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.header-info {
  min-width: 0;
}

.title {
  font-size: clamp(15px, 3.5vw, 17px);
  font-weight: 600;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-id {
  margin-top: 2px;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--color-text-dim);
  word-break: break-all;
}

.chat-id .label {
  color: var(--theme-primary);
  margin-right: 4px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  padding: 5px 10px;
  border-radius: 20px;
  background: rgba(0, 212, 255, 0.1);
  color: var(--color-accent);
  border: 1px solid rgba(0, 212, 255, 0.25);
  white-space: nowrap;
  flex-shrink: 0;
}

.pulse {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-accent);
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.4;
    transform: scale(0.9);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px var(--safe-x);
  background:
    radial-gradient(ellipse at 20% 0%, rgba(99, 102, 241, 0.06), transparent 50%),
    var(--color-bg);
  -webkit-overflow-scrolling: touch;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  min-height: min(320px, 50vh);
  color: var(--color-text-muted);
  text-align: center;
  font-size: 14px;
  line-height: 1.7;
  padding: 24px;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  align-items: flex-start;
}

.row-user {
  flex-direction: row-reverse;
}

.bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: min(85%, 640px);
  min-width: 0;
}

.row-user .bubble-wrap {
  align-items: flex-end;
}

.step-badge {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: var(--theme-primary-2);
  margin-bottom: 4px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(124, 58, 237, 0.15);
  border: 1px solid var(--color-border);
}

.bubble {
  width: 100%;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  text-align: left;
}

.bubble.user {
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--theme-primary) 80%, #1a1a2e) 0%,
    color-mix(in srgb, var(--theme-primary-2) 60%, #1a1a2e) 100%
  );
  color: var(--color-text);
  border-bottom-right-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.bubble.assistant {
  background: var(--color-bg-card);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-bottom-left-radius: 4px;
}

.bubble-text {
  margin: 0;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
  width: 100%;
}

.cursor {
  animation: blink 1s step-end infinite;
  color: var(--color-accent);
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.error-bar {
  padding: 10px var(--safe-x);
  background: rgba(248, 113, 113, 0.12);
  color: var(--color-danger);
  font-size: 13px;
  border-top: 1px solid rgba(248, 113, 113, 0.25);
  flex-shrink: 0;
}

.chat-footer {
  padding: 12px var(--safe-x) 14px;
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-card);
  flex-shrink: 0;
}

.input-area {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  resize: none;
  outline: none;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 0.2s, box-shadow 0.2s;
  text-align: left;
}

.input-area::placeholder {
  color: var(--color-text-dim);
}

.input-area:focus {
  border-color: var(--theme-primary);
  box-shadow: 0 0 0 3px var(--theme-glow);
}

.input-area:disabled {
  opacity: 0.6;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.btn {
  padding: 9px 22px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.15s, opacity 0.2s;
}

.btn:active:not(:disabled) {
  transform: scale(0.98);
}

.btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(
    135deg,
    var(--theme-primary) 0%,
    var(--theme-primary-2) 100%
  );
  color: #fff;
}

.btn-primary:not(:disabled):hover {
  filter: brightness(1.08);
}

.btn-secondary {
  background: var(--color-surface);
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  color: var(--color-text);
  border-color: var(--color-text-dim);
}

@media (max-width: 640px) {
  .bubble-wrap {
    max-width: calc(100% - 52px);
  }

  .status-tag {
    display: none;
  }

  .chat-header {
    gap: 8px;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .chat-room {
    max-width: 100%;
  }
}
</style>
