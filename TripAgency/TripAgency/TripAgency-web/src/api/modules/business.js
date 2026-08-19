import { createApi, http, cleanParams, pageParams } from '../request'

const resourcePath = (type) => ({ travel: 'cars', hotel: 'rooms', food: 'dishes' }[type])

const prepareQuery = (params = {}) => {
  const { type, status, ...rest } = params
  const next = { ...rest }
  if (type) next.type = String(type).toUpperCase()
  if (status === 1 || status === '1') next.status = 'ENABLED'
  else if (status === 0 || status === '0') next.status = 'DISABLED'
  return cleanParams(pageParams(next))
}

const {
  getBusinesses,
  getBusiness,
  addBusiness,
  updateBusiness,
  updateBusinessStatus,
  deleteBusiness
} = createApi({
  getBusinesses: { method: 'get', url: '/api/v1/admin/businesses', prepare: prepareQuery },
  getBusiness: { method: 'get', url: '/api/v1/admin/businesses/:id' },
  addBusiness: { method: 'post', url: '/api/v1/admin/businesses', body: true },
  updateBusiness: { method: 'put', url: '/api/v1/admin/businesses/:id', body: true },
  updateBusinessStatus: { method: 'patch', url: '/api/v1/admin/businesses/:id/status', field: 'status' },
  deleteBusiness: { method: 'delete', url: '/api/v1/admin/businesses/:id' }
})

export const getBusinessResources = (businessId, type, params = {}) =>
  http('get', `/api/v1/admin/businesses/${businessId}/${resourcePath(type)}`, {
    params: cleanParams(pageParams(params))
  })

export const addBusinessResource = (businessId, type, data) =>
  http('post', `/api/v1/admin/businesses/${businessId}/${resourcePath(type)}`, { data })

export const updateBusinessResource = (businessId, type, { id, ...data }) =>
  http('put', `/api/v1/admin/businesses/${businessId}/${resourcePath(type)}/${id}`, { data })

export const updateBusinessResourceStatus = (businessId, type, id, status) =>
  http('patch', `/api/v1/admin/businesses/${businessId}/${resourcePath(type)}/${id}/status`, {
    data: { status }
  })

export const deleteBusinessResource = (businessId, type, id) =>
  http('delete', `/api/v1/admin/businesses/${businessId}/${resourcePath(type)}/${id}`)

export { getBusinesses, getBusiness, addBusiness, updateBusiness, updateBusinessStatus, deleteBusiness }
