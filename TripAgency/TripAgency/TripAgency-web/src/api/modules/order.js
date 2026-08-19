import { createApi, cleanParams, pageParams } from '../request'

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

export { getOrders, getOrder, confirmOrder, cancelOrder }
