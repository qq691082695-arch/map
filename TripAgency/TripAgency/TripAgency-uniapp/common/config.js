// ============全局配置：接口地址与业务枚举映射（与后端契约保持一致）============
let envBase = ''
try {
  envBase = import.meta.env.VITE_API_BASE_URL || ''
} catch (e) {
  envBase = ''
}
export const API_BASE_URL = envBase || 'http://localhost:8080/api/v1'

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