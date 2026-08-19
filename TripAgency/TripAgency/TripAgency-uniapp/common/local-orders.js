// ============本地演示订单存储：后端联调后可移除============
import { getOpenid } from './auth'

const KEY = 'trip_agency_demo_orders'

function read() {
  try {
    return uni.getStorageSync(KEY) || []
  } catch (e) {
    return []
  }
}

function write(list) {
  try {
    uni.setStorageSync(KEY, list)
  } catch (e) {}
}

export function getLocalOrders() {
  const openid = getOpenid()
  return read()
    .filter(o => o.openid === openid)
    .sort((a, b) => (b.id || 0) - (a.id || 0))
}

export function saveLocalOrder(order) {
  const list = read()
  list.unshift(order)
  write(list)
  return order
}

export function cancelLocalOrder(orderId) {
  const list = read()
  const target = list.find(o => o.id === orderId)
  if (target && target.status === 'PENDING') {
    target.status = 'CANCELLED'
    target.cancelSource = 'USER'
    target.cancelReason = '用户主动取消'
    target.cancelledAt = new Date().toISOString()
    write(list)
    return target
  }
  return null
}