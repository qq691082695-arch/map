// ============通用工具============
import { API_BASE_URL } from './config'

// 基于请求内容的确定性幂等键：同一内容复用同一键，内容变化自动换新键
export function buildIdempotencyKey(payload) {
  const raw = JSON.stringify(payload || {})
  let hash = 5381
  for (let i = 0; i < raw.length; i++) {
    hash = ((hash << 5) + hash + raw.charCodeAt(i)) >>> 0
  }
  return 'ord-' + hash.toString(36) + '-' + raw.length.toString(36)
}

export function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function today() {
  return formatDate(new Date())
}

export function maskPhone(phone) {
  if (!phone) return ''
  const s = String(phone)
  if (s.length < 7) return s
  return s.slice(0, 3) + '****' + s.slice(-4)
}

// 图片地址归一化：后端可能返回相对路径（如 /files/xx.png），此处按 API 来源补全为绝对地址
// 注意：小程序运行时没有 URL 全局对象，必须用纯字符串提取 origin
export function resolveImg(url) {
  if (!url) return ''
  const s = String(url)
  if (/^https?:\/\//i.test(s) || s.startsWith('data:') || s.startsWith('wxfile://')) return s
  if (s.startsWith('/')) {
    const m = API_BASE_URL.match(/^https?:\/\/[^/]+/)
    return m ? m[0] + s : s
  }
  return s
}