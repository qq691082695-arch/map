// ============小程序 App API 封装（对应 /api/v1/app/** 契约）============
import { request } from '../common/request'

export function exchangeWechatCode(code) {
  return request({ url: '/app/wechat/session', method: 'POST', data: { code } })
}

export function createOrder(payload) {
  return request({ url: '/app/orders', method: 'POST', data: payload, idempotency: true })
}

export function getOrderList({ openid, page = 1, pageSize = 20 }) {
  return request({ url: '/app/orders', data: { openid, page, pageSize } })
}

export function getOrderDetail(id, openid) {
  return request({ url: `/app/orders/${id}`, data: { openid } })
}

export function cancelOrder(id, openid) {
  return request({ url: `/app/orders/${id}/cancel`, method: 'POST', data: { openid } })
}

export function getBusinessDetail(id) {
  return request({ url: `/app/businesses/${id}` })
}

export function getMapOverview(type) {
  return request({ url: '/app/map-overview', data: type ? { type } : {} })
}
