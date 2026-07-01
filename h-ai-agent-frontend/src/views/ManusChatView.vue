<template>
  <ChatRoom
    title="AI 超级智能体 · HManus"
    theme="manus"
    ai-avatar-type="manus"
    stream-mode="step"
    :chat-id="chatId"
    :messages="messages"
    v-model:input-text="inputText"
    :loading="loading"
    :error="error"
    empty-hint="我是 HManus，可以自主规划并逐步执行任务。每个执行步骤将单独展示。"
    placeholder="描述你的复杂任务，Enter 发送…"
    @send="sendMessage"
    @stop="stopGeneration"
  />
</template>

<script setup>
import ChatRoom from '@/components/ChatRoom.vue'
import { useChat } from '@/composables/useChat'
import { MANUS_SSE_PATH } from '@/api/chat'
import { createChatId } from '@/utils/chatId'

const chatId = createChatId()

const { messages, inputText, loading, error, sendMessage, stopGeneration } =
  useChat({
    ssePath: MANUS_SSE_PATH,
    getParams: (message) => ({ message }),
    streamMode: 'step',
  })
</script>
