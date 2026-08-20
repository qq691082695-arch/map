import axios from 'axios'

const BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

const generateRequestId = () =>
  typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(36).slice(2)}`

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 15000
})

request.interceptors.request.use((config) => {
  config.headers['X-Request-Id'] = generateRequestId()
  return config
})

request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') return response
    const payload = response.data
    if (payload && payload.code === 'OK') return payload.data
    return Promise.reject(new Error(payload?.message || '请求失败'))
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常，请稍后重试'
    return Promise.reject(new Error(message))
  }
)

export const pageParams = ({ page = 1, size = 10, ...rest } = {}) => ({ page, pageSize: size, ...rest })

export const cleanParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== '' && value !== null && value !== undefined))

export const buildUrl = (template, values = {}) =>
  template.replace(/:([a-zA-Z_]\w*)/g, (_, key) => encodeURIComponent(values[key] ?? ''))

export const http = (method, url, { data, params, responseType } = {}) => request({ method, url, data, params, responseType })

export const createApi = (definitions) => {
  const api = {}
  for (const [name, def] of Object.entries(definitions)) {
    const { method, url, body = false, field, prepare = (value) => value } = def
    api[name] = (...args) => {
      const [first, second] = args
      const isPlainObject = first && typeof first === 'object' && !Array.isArray(first) && !(first instanceof FormData)
      const id = isPlainObject ? first.id : first
      const config = {}
      if (field) {
        const value = second !== undefined ? second : isPlainObject ? first[field] : undefined
        if (value !== undefined) config.data = { [field]: value }
      } else if (isPlainObject) {
        const { id: _id, ...rest } = first
        const value = prepare(rest)
        if (Object.keys(value).length > 0) config[String(method).toLowerCase() === 'get' ? 'params' : 'data'] = value
      } else if (body && first !== undefined) {
        config.data = first
      }
      return http(method, buildUrl(url, { id }), config)
    }
  }
  return api
}

export default request
