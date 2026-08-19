<template>
  <div class="page-container">
    <!-- 订单状态统计卡片 -->
    <el-row :gutter="16">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="26"><component :is="card.icon" /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row-gap">
      <!-- 服务商分类统计 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" header="服务商分类统计">
          <el-table :data="typeStats" size="default">
            <el-table-column prop="typeLabel" label="分类" width="120">
              <template #default="{ row }">
                <el-tag :type="typeTag(row.type)" effect="light">{{ row.typeLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="服务商数量">
              <template #default="{ row }">{{ row.count }} 家</template>
            </el-table-column>
            <el-table-column label="占比">
              <template #default="{ row }">
                <el-progress :percentage="percent(row.count)" :stroke-width="14" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 最近订单 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" header="最近订单">
          <el-table :data="recentOrders" size="default">
            <el-table-column prop="orderNo" label="订单号" width="160" show-overflow-tooltip />
            <el-table-column prop="businessNameSnapshot" label="服务商" min-width="140" show-overflow-tooltip />
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="typeTag(row.serviceType)">{{ typeLabel(row.serviceType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="contactName" label="联系人" min-width="100" show-overflow-tooltip />
            <el-table-column prop="serviceDate" label="服务日期" width="115" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-button link type="primary" class="more-btn" @click="$router.push('/order')">
            查看全部订单
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const total = ref(null)
const businesses = ref([])
const recentOrders = ref([])

const statuses = [{ value: 'PENDING', label: '待确认' }, { value: 'CONFIRMED', label: '已确认' }, { value: 'CANCELLED', label: '已取消' }]
const statusLabel = value => statuses.find(item => item.value === value)?.label || value
const statusTag = value => ({ CONFIRMED: 'success', PENDING: 'warning', CANCELLED: 'info' }[value] || 'info')
const typeLabel = value => ({ TRAVEL: '出行', HOTEL: '住宿', FOOD: '餐饮' }[value] || value)
const typeTag = value => ({ TRAVEL: 'primary', HOTEL: 'success', FOOD: 'warning' }[value] || 'info')

const statCards = computed(() => [
  { label: '全部订单', value: total.value?.totalCount ?? '--', icon: 'Tickets', bg: '#e6f4ff', color: '#1677ff' },
  { label: '待确认', value: total.value?.pendingCount ?? '--', icon: 'Bell', bg: '#fff7e6', color: '#fa8c16' },
  { label: '已确认', value: total.value?.confirmedCount ?? '--', icon: 'CircleCheck', bg: '#f6ffed', color: '#52c41a' },
  { label: '已取消', value: total.value?.cancelledCount ?? '--', icon: 'CircleClose', bg: '#fff1f0', color: '#f5222d' }
])

const typeStats = computed(() => {
  const counts = { TRAVEL: 0, HOTEL: 0, FOOD: 0 }
  businesses.value.forEach(item => {
    if (item.status === 'ENABLED' && counts[item.businessType] != null) counts[item.businessType] += 1
  })
  return [
    { type: 'TRAVEL', typeLabel: '出行', count: counts.TRAVEL },
    { type: 'HOTEL', typeLabel: '住宿', count: counts.HOTEL },
    { type: 'FOOD', typeLabel: '餐饮', count: counts.FOOD }
  ]
})

const percent = (count) => {
  const totalCount = typeStats.value.reduce((sum, item) => sum + item.count, 0) || 1
  return Math.round((count / totalCount) * 100)
}

onMounted(async () => {
  try {
    const [overview, businessRes, orderRes] = await Promise.all([
      api.getStatisticsOverview(),
      api.getBusinesses({ page: 1, size: 100 }),
      api.getOrders({ page: 1, size: 5 })
    ])
    total.value = overview.total
    businesses.value = businessRes.items
    recentOrders.value = orderRes.items
  } catch (error) {
    ElMessage.error(error.message)
  }
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 16px;
}
.stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
}
.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 2px;
}
.row-gap {
  margin-top: 8px;
}
.more-btn {
  margin-top: 12px;
}
</style>
