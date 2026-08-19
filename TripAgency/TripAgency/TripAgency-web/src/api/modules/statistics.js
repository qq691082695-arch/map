import { createApi, cleanParams } from '../request'

const { getStatisticsOverview } = createApi({
  getStatisticsOverview: { method: 'get', url: '/api/v1/admin/statistics/overview', prepare: cleanParams }
})

export { getStatisticsOverview }
