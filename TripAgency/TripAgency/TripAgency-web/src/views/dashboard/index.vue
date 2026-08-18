<template>
  <div class="page-container">
    <!-- 统计卡片 -->
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
      <!-- 分类占比 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" header="商家分类统计">
          <el-table :data="data.typeStats" size="default">
            <el-table-column prop="typeLabel" label="分类" width="120">
              <template #default="{ row }">
                <el-tag :type="tagType(row.type)" effect="light">{{ row.typeLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="商家数量">
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
          <el-table :data="data.recentOrders" size="default">
            <el-table-column prop="id" label="订单号" width="150" />
            <el-table-column prop="shopName" label="商家" show-overflow-tooltip />
            <el-table-column prop="openid" label="openid" min-width="150" show-overflow-tooltip />
            <el-table-column prop="amount" label="金额" width="90">
              <template #default="{ row }">
                <span class="amount">¥{{ row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
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
import { api } from '@/api'

const data = ref({ typeStats: [], recentOrders: [] })

const statCards = computed(() => [
  { label: '商家总数', value: data.value.shopTotal ?? '--', icon: 'Shop', bg: '#e6f4ff', color: '#1677ff' },
  { label: '在线商家', value: data.value.shopOnline ?? '--', icon: 'CircleCheck', bg: '#f6ffed', color: '#52c41a' },
  { label: '全部订单', value: data.value.orderTotal ?? '--', icon: 'Tickets', bg: '#fff7e6', color: '#fa8c16' },
  { label: '待确认', value: data.value.orderPending ?? '--', icon: 'Bell', bg: '#fff1f0', color: '#f5222d' },
  { label: '累计成交额', value: '¥' + (data.value.orderAmount ?? 0), icon: 'Wallet', bg: '#e6f4ff', color: '#13c2c2' }
])

const percent = (count) => {
  const total = data.value.typeStats.reduce((s, t) => s + t.count, 0) || 1
  return Math.round((count / total) * 100)
}

const tagType = (t) => ({ travel: 'primary', hotel: 'success', food: 'warning' }[t] || 'info')
const statusTag = (s) =>
  ({ 已确认: 'success', 待确认: 'warning', 已取消: 'info' }[s] || 'info')

onMounted(async () => {
  const res = await api.getDashboard()
  if (res.ok) data.value = res.data
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
.amount {
  color: #f5222d;
  font-weight: 600;
}
.more-btn {
  margin-top: 12px;
}
</style>