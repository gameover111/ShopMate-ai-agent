<template>
  <ChatRoom
    title="AI 智能客服 · 店小二"
    theme="shop-mate"
    ai-avatar-type="shop-mate"
    stream-mode="accumulate"
    :chat-id="chatId"
    :messages="messages"
    v-model:input-text="inputText"
    :loading="loading"
    :error="error"
    empty-hint="你好，我是店小二。可以向我倾诉客服回复中的难题，我会帮你优化话术。"
    placeholder="描述客服场景或粘贴对话内容，Enter 发送…"
    @send="sendMessage"
    @stop="stopGeneration"
  />
</template>

<script setup>
import ChatRoom from '@/components/ChatRoom.vue'
import { useChat } from '@/composables/useChat'
import { SHOP_MATE_SSE_PATH } from '@/api/chat'
import { createChatId } from '@/utils/chatId'

const chatId = createChatId()

const { messages, inputText, loading, error, sendMessage, stopGeneration } =
  useChat({
    ssePath: SHOP_MATE_SSE_PATH,
    getParams: (message) => ({
      message,
      chatId,
    }),
    streamMode: 'accumulate',
  })
</script>
