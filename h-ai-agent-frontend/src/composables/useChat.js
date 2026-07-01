import { ref, nextTick } from 'vue'
import { fetchSSE } from '@/utils/sse'
import { buildSseUrl } from '@/api/chat'

/**
 * @typedef {'accumulate' | 'step'} StreamMode
 * accumulate：流式追加到最后一条 AI 气泡（智能客服）
 * step：每个 SSE 事件单独一条 AI 气泡（超级智能体步骤）
 */

/**
 * @param {{
 *   ssePath: string,
 *   getParams: (message: string) => Record<string, string>,
 *   streamMode?: StreamMode,
 * }} options
 */
export function useChat({ ssePath, getParams, streamMode = 'accumulate' }) {
  const messages = ref([])
  const inputText = ref('')
  const loading = ref(false)
  const error = ref('')
  let abortController = null

  function appendMessage(role, content, extra = {}) {
    messages.value.push({
      id: `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
      role,
      content,
      ...extra,
    })
  }

  function updateLastAssistantContent(chunk) {
    const list = messages.value
    const last = list[list.length - 1]
    if (last?.role === 'assistant') {
      last.content += chunk
    }
  }

  function handleStreamChunk(chunk, scrollToBottom) {
    if (streamMode === 'step') {
      appendMessage('assistant', chunk, { isStep: true })
    } else {
      const last = messages.value[messages.value.length - 1]
      if (last?.role !== 'assistant') {
        appendMessage('assistant', '')
      }
      updateLastAssistantContent(chunk)
    }
    scrollToBottom?.()
  }

  async function sendMessage(scrollToBottom) {
    const text = inputText.value.trim()
    if (!text || loading.value) return

    error.value = ''
    appendMessage('user', text)
    inputText.value = ''

    if (streamMode === 'accumulate') {
      appendMessage('assistant', '')
    }

    loading.value = true
    await nextTick()
    scrollToBottom?.()

    abortController = new AbortController()
    const url = buildSseUrl(ssePath, getParams(text))

    await fetchSSE(url, {
      signal: abortController.signal,
      onMessage: (chunk) => handleStreamChunk(chunk, scrollToBottom),
      onDone: () => {
        loading.value = false
        abortController = null
        if (streamMode === 'accumulate') {
          const last = messages.value[messages.value.length - 1]
          if (last?.role === 'assistant' && !last.content) {
            last.content = '（无回复内容）'
          }
        } else if (
          streamMode === 'step' &&
          messages.value.filter((m) => m.role === 'assistant').length === 0
        ) {
          appendMessage('assistant', '（无回复内容）', { isStep: true })
        }
        scrollToBottom?.()
      },
      onError: (err) => {
        loading.value = false
        abortController = null
        error.value = err.message || '请求失败'
        if (streamMode === 'step') {
          appendMessage('assistant', `错误：${error.value}`, { isStep: true })
        } else {
          const last = messages.value[messages.value.length - 1]
          if (last?.role === 'assistant' && !last.content) {
            last.content = `错误：${error.value}`
          }
        }
        scrollToBottom?.()
      },
    })
  }

  function stopGeneration() {
    abortController?.abort()
    abortController = null
    loading.value = false
  }

  return {
    messages,
    inputText,
    loading,
    error,
    sendMessage,
    stopGeneration,
    streamMode,
  }
}
