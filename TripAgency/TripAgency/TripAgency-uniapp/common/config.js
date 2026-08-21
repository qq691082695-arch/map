// ============全局配置：接口地址与业务枚举映射（与后端契约保持一致）============
let envBase = ''
let devtoolsBase = ''
try {
  envBase = import.meta.env.VITE_API_BASE_URL || ''
  devtoolsBase = import.meta.env.VITE_API_BASE_URL_DEVTOOLS || ''
} catch (e) {
  envBase = ''
  devtoolsBase = ''
}
let API_BASE_URL = envBase || 'http://localhost:8080/api/v1'

// 微信开发者工具运行在本机，不应通过局域网 IP 或系统代理绕回本机。
// 真机仍使用 VITE_API_BASE_URL 中的局域网/正式 HTTPS 地址。
// #ifdef MP-WEIXIN
try {
  const systemInfo = uni.getSystemInfoSync()
  if (systemInfo && systemInfo.platform === 'devtools') {
    API_BASE_URL = devtoolsBase || 'http://127.0.0.1:8080/api/v1'
  }
} catch (e) {
  // 保留环境配置地址；请求层会返回明确的网络错误。
}
// #endif

export { API_BASE_URL }

export const SERVICE_TYPE_MAP = {
  TRAVEL: { label: '出行', emoji: '🚗' },
  HOTEL: { label: '住宿', emoji: '🏨' },
  FOOD: { label: '餐饮', emoji: '🍜' }
}

export const ORDER_STATUS_MAP = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  CANCELLED: '已取消'
}

export const SERVICE_MODE_MAP = {
  DAY_CHARTER: '按日包车',
  ROUND_TRIP: '往返接送'
}

export const MEAL_PERIOD_MAP = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐'
}
