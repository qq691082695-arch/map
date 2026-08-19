import * as authApi from './modules/auth'
import * as universityApi from './modules/university'
import * as businessApi from './modules/business'
import * as orderApi from './modules/order'
import * as statisticsApi from './modules/statistics'
import * as fileApi from './modules/file'

export const api = {
  ...authApi,
  ...universityApi,
  ...businessApi,
  ...orderApi,
  ...statisticsApi,
  ...fileApi
}

export default api
