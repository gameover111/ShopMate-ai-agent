<template>
  <div class="chat-page" :data-theme="theme">
    <!-- 侧边栏遮罩 -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>

    <!-- 侧边栏 -->
    <aside class="chat-sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-header">
        <h2 class="sidebar-title">会话历史</h2>
        <button class="sidebar-close" @click="sidebarOpen = false">✕</button>
      </div>

      <button class="new-chat-btn" @click="handleNewChat">
        <span>＋</span> 新会话
      </button>

      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: s.id === chatId }"
          @click="handleSwitchSession(s)"
        >
          <div class="session-info">
            <span class="session-title">{{ s.title || '新会话' }}</span>
            <span class="session-time">{{ formatTime(s.updatedAt) }}</span>
          </div>
          <div class="session-actions" @click.stop>
            <button class="session-btn" title="重命名" @click="startRename(s)">✎</button>
            <button class="session-btn session-btn-del" title="删除" @click="handleDelete(s)">✕</button>
          </div>
        </div>

        <!-- 重命名输入 -->
        <div v-if="renamingId" class="rename-bar">
          <input
            v-model="renameText"
            class="rename-input"
            @keyup.enter="doRename"
            @keyup.escape="renamingId = null"
            ref="renameInputRef"
          />
          <button class="rename-ok" @click="doRename">✓</button>
        </div>

        <p v-if="sessions.length === 0" class="session-empty">暂无会话</p>
      </div>
    </aside>

    <!-- 主聊天区 -->
    <div class="chat-room">
      <header class="chat-header">
        <button class="menu-btn" @click="sidebarOpen = !sidebarOpen" title="会话列表">
          ☰
        </button>
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
        <button class="new-chat-icon-btn" title="新会话" @click="handleNewChat">＋</button>
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
    validator: (v) => ['shop-mate', 'manus', 'orchestrator'].includes(v),
  },
  aiAvatarType: {
    type: String,
    default: 'shop-mate',
    validator: (v) => ['shop-mate', 'manus', 'orchestrator'].includes(v),
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
  emptyHint: { type: String, default: '' },
  placeholder: { type: String, default: '输入消息，Enter 发送…' },
  sessions: { type: Array, default: () => [] },
  isLoggedIn: { type: Boolean, default: false },
})

const emit = defineEmits(['update:inputText', 'send', 'stop', 'newChat', 'switchSession', 'deleteSession', 'renameSession'])

const sidebarOpen = ref(false)
const renamingId = ref(null)
const renameText = ref('')
const renameInputRef = ref(null)
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

function handleNewChat() {
  if (!props.isLoggedIn) return
  sidebarOpen.value = false
  emit('newChat')
}

function handleSwitchSession(session) {
  sidebarOpen.value = false
  emit('switchSession', session)
}

function handleDelete(session) {
  emit('deleteSession', session)
}

function startRename(session) {
  renamingId.value = session.id
  renameText.value = session.title || ''
  nextTick(() => renameInputRef.value?.focus())
}

function doRename() {
  if (renamingId.value && renameText.value.trim()) {
    emit('renameSession', { id: renamingId.value, title: renameText.value.trim() })
  }
  renamingId.value = null
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 86400000) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
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
  min-height: 0;
  background: var(--color-bg);
  position: relative;
}

/* 侧边栏 */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 20;
}

.chat-sidebar {
  position: fixed;
  top: 0;
  left: -300px;
  width: 280px;
  height: 100vh;
  height: 100dvh;
  background: var(--color-bg-elevated);
  border-right: 1px solid var(--color-border);
  z-index: 30;
  display: flex;
  flex-direction: column;
  transition: left 0.25s ease;
}

.chat-sidebar.open {
  left: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
}

.sidebar-close {
  background: none;
  border: none;
  color: var(--color-text-dim);
  font-size: 16px;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 12px 16px;
  padding: 9px;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-accent);
  font-size: 13px;
  font-weight: 600;
  transition: border-color 0.2s, background 0.2s;
}

.new-chat-btn:hover {
  border-color: var(--color-accent);
  background: rgba(0, 212, 255, 0.05);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px 12px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 2px;
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.session-item.active {
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  display: block;
  font-size: 13px;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  display: block;
  font-size: 10px;
  color: var(--color-text-dim);
  margin-top: 2px;
}

.session-actions {
  display: none;
  gap: 2px;
  flex-shrink: 0;
}

.session-item:hover .session-actions {
  display: flex;
}

.session-btn {
  padding: 3px 6px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: var(--color-text-dim);
  font-size: 12px;
}

.session-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
}

.session-btn-del:hover {
  color: var(--color-danger);
}

.rename-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
}

.rename-input {
  flex: 1;
  padding: 5px 8px;
  border: 1px solid var(--color-accent);
  border-radius: 4px;
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 12px;
}

.rename-ok {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  background: var(--color-accent);
  color: #fff;
  font-size: 12px;
}

.session-empty {
  text-align: center;
  color: var(--color-text-dim);
  font-size: 13px;
  padding: 24px;
}

/* 聊天区 */
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

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
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

.menu-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.04);
  color: var(--color-text-muted);
  font-size: 18px;
  border: 1px solid var(--color-border);
  flex-shrink: 0;
}

.menu-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text);
}

.new-chat-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.04);
  color: var(--color-text-muted);
  font-size: 16px;
  flex-shrink: 0;
}

.new-chat-icon-btn:hover {
  color: var(--color-accent);
  border-color: var(--color-accent);
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
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
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.header-info { min-width: 0; }

.title {
  font-size: clamp(14px, 3vw, 16px);
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
}

.chat-id .label { color: var(--theme-primary); margin-right: 4px; }

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
  50% { opacity: 0.5; }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px var(--safe-x);
  background: radial-gradient(ellipse at 20% 0%, rgba(99, 102, 241, 0.06), transparent 50%), var(--color-bg);
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

.row-user { flex-direction: row-reverse; }

.bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: min(85%, 640px);
  min-width: 0;
}

.row-user .bubble-wrap { align-items: flex-end; }

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
  background: linear-gradient(135deg, color-mix(in srgb, var(--theme-primary) 80%, #1a1a2e) 0%, color-mix(in srgb, var(--theme-primary-2) 60%, #1a1a2e) 100%);
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
  50% { opacity: 0; }
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

.input-area::placeholder { color: var(--color-text-dim); }

.input-area:focus {
  border-color: var(--theme-primary);
  box-shadow: 0 0 0 3px var(--theme-glow);
}

.input-area:disabled { opacity: 0.6; }

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

.btn:active:not(:disabled) { transform: scale(0.98); }
.btn:disabled { opacity: 0.45; cursor: not-allowed; }

.btn-primary {
  background: linear-gradient(135deg, var(--theme-primary) 0%, var(--theme-primary-2) 100%);
  color: #fff;
}

.btn-primary:not(:disabled):hover { filter: brightness(1.08); }

.btn-secondary {
  background: var(--color-surface);
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover { color: var(--color-text); border-color: var(--color-text-dim); }

@media (max-width: 640px) {
  .bubble-wrap { max-width: calc(100% - 52px); }
  .status-tag { display: none; }
  .chat-header { gap: 6px; }
}
</style>
