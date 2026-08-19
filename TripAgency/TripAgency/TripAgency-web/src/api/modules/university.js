import { createApi, cleanParams, pageParams } from '../request'

const prepareQuery = (params = {}) => {
  const { show, ...rest } = params
  const status = show === true ? 'ENABLED' : show === false ? 'DISABLED' : ''
  return cleanParams(pageParams({ ...rest, status }))
}

const {
  getUniversities,
  getUniversity,
  addUniversity,
  updateUniversity,
  updateUniversityStatus,
  deleteUniversity
} = createApi({
  getUniversities: { method: 'get', url: '/api/v1/admin/universities', prepare: prepareQuery },
  getUniversity: { method: 'get', url: '/api/v1/admin/universities/:id' },
  addUniversity: { method: 'post', url: '/api/v1/admin/universities', body: true },
  updateUniversity: { method: 'put', url: '/api/v1/admin/universities/:id', body: true },
  updateUniversityStatus: { method: 'patch', url: '/api/v1/admin/universities/:id/status', field: 'status' },
  deleteUniversity: { method: 'delete', url: '/api/v1/admin/universities/:id' }
})

export { getUniversities, getUniversity, addUniversity, updateUniversity, updateUniversityStatus, deleteUniversity }
