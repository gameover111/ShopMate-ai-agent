/**
 * 通过 fetch 读取 SSE 流（GET）
 * 兼容 Spring WebFlux Flux<String> 与 SseEmitter 两种后端输出
 *
 * @param {string} url
 * @param {{
 *   onMessage: (chunk: string) => void,
 *   onDone?: () => void,
 *   onError?: (err: Error) => void,
 *   signal?: AbortSignal
 * }} callbacks
 */
export async function fetchSSE(url, { onMessage, onDone, onError, signal }) {
  
  let response
  try {
    response = await fetch(url, {
      method: 'GET',
      headers: { Accept: 'text/event-stream' },
      signal,
    })
  } catch (err) {
    if (err.name === 'AbortError') return
    onError?.(err instanceof Error ? err : new Error(String(err)))
    return
  }

  if (!response.ok) {
    onError?.(new Error(`请求失败: HTTP ${response.status}`))
    return
  }

  const reader = response.body?.getReader()
  if (!reader) {
    onError?.(new Error('浏览器不支持流式读取'))
    return
  }

  const decoder = new TextDecoder()
  let buffer = ''
  let eventDataLines = []

  const flushEvent = () => {
    if (eventDataLines.length === 0) return
    const payload = eventDataLines.join('\n')
    eventDataLines = []
    if (payload) onMessage(payload)
  }

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() ?? ''

      for (let rawLine of lines) {
        if (rawLine.endsWith('\r')) rawLine = rawLine.slice(0, -1)

        if (rawLine === '') {
          flushEvent()
          continue
        }

        if (rawLine.startsWith(':')) continue

        if (rawLine.startsWith('data:')) {
          eventDataLines.push(rawLine.slice(5).replace(/^\s/, ''))
        } else if (!rawLine.startsWith('event:') && !rawLine.startsWith('id:')) {
          // 非标准 SSE 行，按纯文本块处理
          flushEvent()
          onMessage(rawLine)
        }
      }
    }

    if (buffer.trim()) {
      if (buffer.startsWith('data:')) {
        eventDataLines.push(buffer.slice(5).replace(/^\s/, ''))
      } else {
        flushEvent()
        onMessage(buffer)
      }
    }
    flushEvent()
    onDone?.()
  } catch (err) {
    if (err.name === 'AbortError') return
    onError?.(err instanceof Error ? err : new Error(String(err)))
  } finally {
    reader.releaseLock()
  }
}
