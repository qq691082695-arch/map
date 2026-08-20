// ============openid 获取：前端自行完成微信登录后写入本地，后端仅作订单归属辨别============
const OPENID_KEY = 'trip_agency_openid'

export function setOpenid(openid) {
  if (openid) uni.setStorageSync(OPENID_KEY, openid)
}

export function getOpenid() {
  try {
    return uni.getStorageSync(OPENID_KEY) || ''
  } catch (e) {
    return ''
  }
}
