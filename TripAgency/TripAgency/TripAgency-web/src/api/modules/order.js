import { createApi, http, cleanParams, pageParams } from '../request'

const prepareQuery = (params = {}) => {
  const { serviceDateRange, ...rest } = params
  const [serviceDateFrom, serviceDateTo] = serviceDateRange || []
  return cleanParams(pageParams({ ...rest, serviceDateFrom, serviceDateTo }))
}

const { getOrders, getOrder, confirmOrder, cancelOrder } = createApi({
  getOrders: { method: 'get', url: '/api/v1/admin/orders', prepare: prepareQuery },
  getOrder: { method: 'get', url: '/api/v1/admin/orders/:id' },
  confirmOrder: { method: 'post', url: '/api/v1/admin/orders/:id/confirm' },
  cancelOrder: { method: 'post', url: '/api/v1/admin/orders/:id/cancel', field: 'reason' }
})

export const exportOrders = async (params = {}) => {
  const query = prepareQuery(params)
  delete query.page
  delete query.pageSize
  const response = await http('get', '/api/v1/admin/orders/export', {
    params: query,
    responseType: 'blob'
  })
  const disposition = response.headers['content-disposition'] || ''
  const encodedName = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  return {
    blob: response.data,
    filename: encodedName ? decodeURIComponent(encodedName) : 'orders.xlsx'
  }
}

export { getOrders, getOrder, confirmOrder, cancelOrder }
