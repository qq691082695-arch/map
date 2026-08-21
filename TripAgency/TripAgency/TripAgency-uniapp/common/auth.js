// ============openid 获取：小程序静默取 code，后端仅交换并返回 openid============
import { exchangeWechatCode } from '../api/app'

const OPENID_KEY = 'trip_agency_openid_v2'
const LEGACY_OPENID_KEY = 'trip_agency_openid'
let pendingOpenid = null

// 旧版本曾在 LEGACY_OPENID_KEY 中写入 demo_openid_001。
// 新版本不继承任何旧身份缓存，必须重新通过微信 code2Session 获取真实 openid。
try {
  uni.removeStorageSync(LEGACY_OPENID_KEY)
} catch (e) {
  // 存储不可用时由后续静默登录返回明确错误。
}

const isDemoOpenid = (openid) => /^(demo|mock|test)[_-]/i.test(String(openid || '').trim())

export function setOpenid(openid) {
  if (openid && !isDemoOpenid(openid)) uni.setStorageSync(OPENID_KEY, openid)
}

export function getOpenid() {
  try {
    const openid = uni.getStorageSync(OPENID_KEY) || ''
    if (isDemoOpenid(openid)) {
      uni.removeStorageSync(OPENID_KEY)
      return ''
    }
    return openid
  } catch (e) {
    return ''
  }
}

const loginForCode = () => new Promise((resolve, reject) => {
  // #ifdef MP-WEIXIN
  uni.login({
    provider: 'weixin',
    success: (result) => {
      if (result && result.code) {
        resolve(result.code)
      } else {
        reject(new Error('微信未返回临时登录凭证'))
      }
    },
    fail: () => reject(new Error('微信静默登录失败'))
  })
  // #endif

  // #ifndef MP-WEIXIN
  reject(new Error('当前运行环境不支持微信静默登录'))
  // #endif
})

export function ensureOpenid({ force = false } = {}) {
  const cached = getOpenid()
  if (cached && !force) return Promise.resolve(cached)
  if (pendingOpenid) return pendingOpenid

  const exchange = loginForCode()
    .then(code => exchangeWechatCode(code))
    .then((identity) => {
      const openid = identity && identity.openid
      if (!openid) throw new Error('身份服务未返回 openid')
      if (isDemoOpenid(openid)) throw new Error('身份服务返回了无效的演示 openid')
      setOpenid(openid)
      return openid
    })
  pendingOpenid = exchange.then((openid) => {
    pendingOpenid = null
    return openid
  }, (error) => {
    pendingOpenid = null
    throw error
  })
  return pendingOpenid
}
