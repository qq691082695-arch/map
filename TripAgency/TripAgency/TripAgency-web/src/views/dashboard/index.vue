<template>
  <div class="page-container" v-loading="loading">
    <!-- 订单状态统计卡片 -->
    <el-row :gutter="16">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div>
              <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row-gap">
      <!-- 服务商分类统计 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="shadow-hover">
          <template #header>
            <div class="card-title"><el-icon color="#1677ff"><OfficeBuilding /></el-icon>服务商分类统计</div>
          </template>
          <el-table :data="typeStats" size="default">
            <el-table-column prop="typeLabel" label="分类" width="120">
              <template #default="{ row }">
                <el-tag :type="typeTag(row.type)" effect="light" round>{{ row.typeLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="服务商数量">
              <template #default="{ row }"><span class="count-num">{{ row.count }}</span> 家</template>
            </el-table-column>
            <el-table-column label="占比">
              <template #default="{ row }">
                <el-progress :percentage="percent(row.count)" :stroke-width="14" :show-text="false" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 最近订单 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="shadow-hover">
          <template #header>
            <div class="card-title"><el-icon color="#1677ff"><Tickets /></el-icon>最近订单</div>
          </template>
          <el-empty v-if="!recentOrders.length" description="暂无订单" :image-size="80" />
          <el-table v-else :data="recentOrders" size="default">
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
          <div class="more-btn-row">
            <el-button text type="primary" @click="$router.push('/order')">
              查看全部订单<el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
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
const loading = ref(false)

const statuses = [{ value: 'PENDING', label: '待确认' }, { value: 'CONFIRMED', label: '已确认' }, { value: 'CANCELLED', label: '已取消' }]
const statusLabel = value => statuses.find(item => item.value === value)?.label || value
const statusTag = value => ({ CONFIRMED: 'success', PENDING: 'warning', CANCELLED: 'info' }[value] || 'info')
const typeLabel = value => ({ TRAVEL: '出行', HOTEL: '住宿', FOOD: '餐饮' }[value] || value)
const typeTag = value => ({ TRAVEL: 'primary', HOTEL: 'success', FOOD: 'warning' }[value] || 'info')

const statCards = computed(() => [
  { label: '全部订单', value: total.value?.totalCount ?? '--', icon: 'Tickets', color: '#1677ff', bg: 'linear-gradient(135deg,#1677ff,#69b1ff)' },
  { label: '待确认', value: total.value?.pendingCount ?? '--', icon: 'Bell', color: '#fa8c16', bg: 'linear-gradient(135deg,#fa8c16,#ffc069)' },
  { label: '已确认', value: total.value?.confirmedCount ?? '--', icon: 'CircleCheck', color: '#52c41a', bg: 'linear-gradient(135deg,#52c41a,#95de64)' },
  { label: '已取消', value: total.value?.cancelledCount ?? '--', icon: 'CircleClose', color: '#f5222d', bg: 'linear-gradient(135deg,#f5222d,#ff9c9c)' }
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
  loading.value = true
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
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 16px;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--app-shadow-hover) !important;
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
  color: #fff;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.14);
  flex-shrink: 0;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 2px;
}
.row-gap {
  margin-top: 8px;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
}
.count-num {
  font-weight: 600;
  color: #303133;
  margin-right: 2px;
}
.more-btn-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
