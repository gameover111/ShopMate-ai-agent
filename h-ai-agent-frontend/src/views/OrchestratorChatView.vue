<template>
  <ChatRoom
    title="多 Agent 编排器 · Orchestrator"
    theme="manus"
    ai-avatar-type="orchestrator"
    stream-mode="step"
    :chat-id="chatId"
    :messages="messages"
    v-model:input-text="inputText"
    :loading="loading"
    :error="error"
    :sessions="sessions"
    :is-logged-in="isLoggedIn"
    empty-hint="我是多 Agent 编排器，会自动判断使用 HManus 还是店小二来帮你。"
    placeholder="描述你的需求，我会自动分派给最合适的 Agent…"
    @send="sendMessage"
    @stop="stopGeneration"
    @new-chat="handleNewChat"
    @switch-session="handleSwitchSession"
    @delete-session="handleDeleteSession"
    @rename-session="handleRenameSession"
  />
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import ChatRoom from '@/components/ChatRoom.vue'
import { useChat } from '@/composables/useChat'
import { useAuth } from '@/stores/auth'
import { createChatId } from '@/utils/chatId'
import http from '@/api/config'
import { fetchSessions, createSession, deleteSession, renameSession, fetchSessionMessages } from '@/api/session'

const ORCHESTRATOR_SSE_PATH = '/ai/orchestrator/chat'

const SESSION_TYPE = 'orchestrator'

const { isLoggedIn } = useAuth()
const sessions = ref([])
const anonChatId = createChatId()

const {
  messages, inputText, loading, error,
  chatId, setChatId, loadMessages, clearMessages,
  sendMessage, stopGeneration,
} = useChat({
  ssePath: ORCHESTRATOR_SSE_PATH,
  getParams: (message, id) => ({ message, chatId: id || anonChatId }),
  streamMode: 'step',
})

async function loadSessions() {
  if (!isLoggedIn.value) return
  try { sessions.value = await fetchSessions(SESSION_TYPE) } catch { /* ignore */ }
}

async function handleNewChat() {
  if (!isLoggedIn.value) return
  try {
    const session = await createSession(SESSION_TYPE)
    sessions.value.unshift(session)
    setChatId(session.id)
    clearMessages()
  } catch (e) { console.error('创建会话失败', e) }
}

async function handleSwitchSession(session) {
  setChatId(session.id)
  try {
    const msgs = await http.get(`/ai/orchestrator/sessions/${session.id}/messages`)
    loadMessages(msgs)
  } catch {
    try {
      const msgs = await fetchSessionMessages(session.id)
      loadMessages(msgs)
    } catch { clearMessages() }
  }
}

async function handleDeleteSession(session) {
  try {
    await deleteSession(session.id)
    sessions.value = sessions.value.filter((s) => s.id !== session.id)
    if (chatId.value === session.id) { clearMessages(); setChatId('') }
  } catch (e) { console.error('删除会话失败', e) }
}

async function handleRenameSession({ id, title }) {
  try {
    const updated = await renameSession(id, title)
    const idx = sessions.value.findIndex((s) => s.id === id)
    if (idx !== -1) sessions.value[idx] = updated
  } catch (e) { console.error('重命名失败', e) }
}

onMounted(async () => {
  await loadSessions()
  if (isLoggedIn.value && sessions.value.length === 0) {
    await handleNewChat()
  } else if (sessions.value.length > 0) {
    await handleSwitchSession(sessions.value[0])
  }
})

watch(isLoggedIn, (loggedIn) => { if (loggedIn) loadSessions() })
</script>
