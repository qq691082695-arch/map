// ============统一请求封装：处理 { code, message, data, requestId } 响应契约============
import { API_BASE_URL } from './config'
import { buildIdempotencyKey } from './util'

const generateRequestId = () =>
  `${Date.now()}-${Math.random().toString(36).slice(2)}`

export function request({ url, method = 'GET', data, idempotency }) {
  const fullUrl = API_BASE_URL + url
  const header = {
    'Content-Type': 'application/json',
    'X-Request-Id': generateRequestId()
  }
  if (idempotency) {
    header['Idempotency-Key'] = buildIdempotencyKey(data)
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data,
      header,
      timeout: 15000,
      success: (res) => {
        const body = res.data || {}
        if (res.statusCode === 200 && body.code === 'OK') {
          resolve(body.data)
        } else {
          reject({
            statusCode: res.statusCode,
            code: body.code || 'ERROR',
            message: body.message || '请求失败',
            requestId: body.requestId
          })
        }
      },
      fail: (err) => {
        console.error(`[Request Fail] ${method} ${fullUrl}`, err)
        reject({ statusCode: 0, code: 'NETWORK_ERROR', message: '网络连接失败', detail: err })
      }
    })
  })
}
