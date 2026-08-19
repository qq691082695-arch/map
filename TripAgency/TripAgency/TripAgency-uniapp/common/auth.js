// ============openid 获取：前端自行完成微信登录后写入本地，后端仅作订单归属辨别============
const OPENID_KEY = 'trip_agency_openid'
const DEMO_OPENID = 'demo_openid_001'

export function setOpenid(openid) {
  if (openid) uni.setStorageSync(OPENID_KEY, openid)
}

export function getOpenid() {
  let openid = ''
  try {
    openid = uni.getStorageSync(OPENID_KEY) || ''
  } catch (e) {
    openid = ''
  }
  if (!openid) {
    openid = DEMO_OPENID
    try {
      uni.setStorageSync(OPENID_KEY, openid)
    } catch (e) {}
  }
  return openid
}