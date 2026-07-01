const SITE_NAME = 'Shopmate 店小二'
const DEFAULT_DESC =
  'Shopmate 店小二 AI 应用中心，提供电商智能客服与超级智能体，基于 Spring AI 流式对话。'

function upsertMeta(attr, key, content) {
  if (!content) return
  let el = document.querySelector(`meta[${attr}="${key}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function upsertLink(rel, href) {
  if (!href) return
  let el = document.querySelector(`link[rel="${rel}"]`)
  if (!el) {
    el = document.createElement('link')
    el.setAttribute('rel', rel)
    document.head.appendChild(el)
  }
  el.setAttribute('href', href)
}

/**
 * 应用页面 SEO（title、description、keywords、Open Graph）
 * @param {import('vue-router').RouteMeta} meta
 */
export function applyPageSEO(meta = {}) {
  const title = meta.title ? `${meta.title} · ${SITE_NAME}` : `${SITE_NAME} · AI 应用中心`
  const description = meta.description || DEFAULT_DESC
  const keywords = meta.keywords || 'Shopmate,店小二,AI客服,HManus,智能体,Spring AI'
  const url = meta.canonical || window.location.href

  document.title = title
  upsertMeta('name', 'description', description)
  upsertMeta('name', 'keywords', keywords)
  upsertMeta('name', 'author', '韩淑成')
  upsertMeta('name', 'robots', 'index, follow')

  upsertMeta('property', 'og:type', 'website')
  upsertMeta('property', 'og:site_name', SITE_NAME)
  upsertMeta('property', 'og:title', title)
  upsertMeta('property', 'og:description', description)
  upsertMeta('property', 'og:url', url)
  upsertMeta('property', 'og:locale', 'zh_CN')

  upsertMeta('name', 'twitter:card', 'summary')
  upsertMeta('name', 'twitter:title', title)
  upsertMeta('name', 'twitter:description', description)

  upsertLink('canonical', meta.canonical || url)
}
