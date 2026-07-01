import { API_BASE_URL } from './config'

/** 智能客服 SSE */
export const SHOP_MATE_SSE_PATH = '/ai/shop_mate_app/chat/sse'

/** 超级智能体 SSE */
export const MANUS_SSE_PATH = '/ai/manus/chat'

/**
 * 构建带查询参数的 SSE 请求 URL
 * @param {string} path - 接口路径（以 / 开头）
 * @param {Record<string, string>} params
 */
export function buildSseUrl(path, params = {}) {
  const base = API_BASE_URL.replace(/\/$/, '')
  const fullPath = path.startsWith('/') ? path : `/${path}`
  // 使用 window.location.origin 作为 base，支持相对路径的 API_BASE_URL（如 /api）
  const url = new URL(`${base}${fullPath}`, window.location.origin)
  Object.entries(params).forEach(([key, value]) => {
    if (value != null && value !== '') {
      url.searchParams.set(key, value)
    }
  })
  return url.toString()
}
