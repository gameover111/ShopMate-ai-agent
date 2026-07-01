/**
 * 生成聊天室 ID，用于区分会话
 */
export function createChatId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID().replace(/-/g, '')
  }
  return `chat_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
}
