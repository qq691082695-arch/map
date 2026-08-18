import { api as mockApi } from './mock'

const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const queryString = (params = {}) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) search.set(key, value)
  })
  return search.toString() ? `?${search}` : ''
}
const request = async (path, options = {}) => {
  const headers = new Headers(options.headers || {})
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  if (options.method && options.method !== 'GET') headers.set('X-Request-Id', crypto.randomUUID?.() || `${Date.now()}-${Math.random()}`)
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers })
  const payload = await response.json().catch(() => null)
  if (!response.ok || !payload || payload.code !== 'OK') throw new Error(payload?.message || `请求失败（HTTP ${response.status}）`)
  return payload.data
}
const pageParams = ({ page = 1, size = 10, ...rest } = {}) => ({ page, pageSize: size, ...rest })
const pageResult = (data) => ({ ok: true, data: { list: data.items || [], total: data.total || 0 } })
const resourcePath = (type) => ({ travel: 'cars', hotel: 'rooms', food: 'dishes' }[type])

export const api = {
  ...mockApi,
  async getUniversities(params = {}) {
    const status = params.show === true ? 'ENABLED' : params.show === false ? 'DISABLED' : ''
    return pageResult(await request(`/api/v1/admin/universities${queryString(pageParams({ ...params, status, show: undefined }))}`))
  },
  async addUniversity(payload) { return { ok: true, data: await request('/api/v1/admin/universities', { method: 'POST', body: JSON.stringify(payload) }), message: '新增成功' } },
  async updateUniversity(payload) { return { ok: true, data: await request(`/api/v1/admin/universities/${payload.id}`, { method: 'PUT', body: JSON.stringify(payload) }), message: '保存成功' } },
  async updateUniversityStatus(id, status) { return { ok: true, data: await request(`/api/v1/admin/universities/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }), message: status === 'ENABLED' ? '已展示' : '已隐藏' } },
  async deleteUniversity(id) { await request(`/api/v1/admin/universities/${id}`, { method: 'DELETE' }); return { ok: true, message: '删除成功' } },
  async getShops(params = {}) {
    const type = params.type ? params.type.toUpperCase() : ''
    const status = params.status === 1 || params.status === '1' ? 'ENABLED' : params.status === 0 || params.status === '0' ? 'DISABLED' : ''
    return pageResult(await request(`/api/v1/admin/businesses${queryString(pageParams({ ...params, type, status }))}`))
  },
  async addShop(payload) { return { ok: true, data: await request('/api/v1/admin/businesses', { method: 'POST', body: JSON.stringify(payload) }), message: '新增成功' } },
  async updateShop(payload) { return { ok: true, data: await request(`/api/v1/admin/businesses/${payload.id}`, { method: 'PUT', body: JSON.stringify(payload) }), message: '保存成功' } },
  async updateShopStatus(id, status) { return { ok: true, data: await request(`/api/v1/admin/businesses/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }), message: status === 'ENABLED' ? '已启用' : '已禁用' } },
  async deleteShop(id) { await request(`/api/v1/admin/businesses/${id}`, { method: 'DELETE' }); return { ok: true, message: '删除成功' } },
  async getShopItems(businessId, type, params = {}) { return pageResult(await request(`/api/v1/admin/businesses/${businessId}/${resourcePath(type)}${queryString(pageParams(params))}`)) },
  async addShopItem(businessId, type, payload) { return { ok: true, data: await request(`/api/v1/admin/businesses/${businessId}/${resourcePath(type)}`, { method: 'POST', body: JSON.stringify(payload) }), message: '新增成功' } },
  async updateShopItem(businessId, type, payload) { return { ok: true, data: await request(`/api/v1/admin/businesses/${businessId}/${resourcePath(type)}/${payload.id}`, { method: 'PUT', body: JSON.stringify(payload) }), message: '保存成功' } },
  async updateShopItemStatus(businessId, type, id, status) { return { ok: true, data: await request(`/api/v1/admin/businesses/${businessId}/${resourcePath(type)}/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }), message: status === 'ENABLED' ? '已启用' : '已禁用' } },
  async deleteShopItem(businessId, type, id) { await request(`/api/v1/admin/businesses/${businessId}/${resourcePath(type)}/${id}`, { method: 'DELETE' }); return { ok: true, message: '删除成功' } },
  async uploadImage(file) { const body = new FormData(); body.append('file', file); return request('/api/v1/admin/files/images', { method: 'POST', body }) }
}
