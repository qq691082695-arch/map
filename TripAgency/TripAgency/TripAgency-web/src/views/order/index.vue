<template>
  <div class="page-container">
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="handleSearch">
        <el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 130px" @change="handleSearch">
        <el-option v-for="item in types" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.businessId" placeholder="全部服务商" clearable filterable style="width: 200px" @change="handleSearch">
        <el-option v-for="item in businesses" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-date-picker v-model="query.serviceDateRange" type="daterange" value-format="YYYY-MM-DD"
        range-separator="至" start-placeholder="服务日期起" end-placeholder="服务日期止"
        :shortcuts="dateShortcuts" @change="handleSearch" />
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <template #empty><el-empty description="暂无符合条件的订单" :image-size="90" /></template>
        <el-table-column prop="orderNo" label="订单编号" min-width="175" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }"><el-tag :type="typeTag(row.serviceType)" size="small">{{ typeLabel(row.serviceType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="businessNameSnapshot" label="服务商（下单快照）" min-width="170" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="100" show-overflow-tooltip />
        <el-table-column prop="contactPhoneMasked" label="联系电话" width="125" />
        <el-table-column prop="openidMasked" label="openid（脱敏）" min-width="145" show-overflow-tooltip>
          <template #default="{ row }"><span class="openid">{{ row.openidMasked }}</span></template>
        </el-table-column>
        <el-table-column prop="peopleNum" label="人数" width="70" />
        <el-table-column prop="serviceDate" label="服务日期" width="115" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button link type="success" :icon="CircleCheck" @click="confirmOrder(row)">确认</el-button>
              <el-button link type="danger" :icon="CircleClose" @click="openCancelDialog(row)">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" background
          layout="total, prev, pager, next, sizes" :total="total" :page-sizes="[10, 20, 50, 100]"
          @current-change="fetchList" @size-change="handleSizeChange" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" :title="detail.orderNo ? `订单详情 · ${detail.orderNo}` : '订单详情'" width="720px" :close-on-click-modal="false">
      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <el-descriptions v-else-if="detail.id" :column="2" border>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="服务商">{{ detail.businessNameSnapshot }}</el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ typeLabel(detail.serviceType) }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="服务日期">{{ detail.serviceDate }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ detail.peopleNum }}</el-descriptions-item>
        <el-descriptions-item label="服务内容" :span="2">{{ optionText(detail) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.cancelSource" label="取消来源">{{ cancelSourceLabel(detail.cancelSource) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.cancelReason" label="取消原因" :span="detail.cancelSource ? 1 : 2">{{ detail.cancelReason }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="取消订单" width="480px" :close-on-click-modal="false">
      <el-alert type="warning" :closable="false" show-icon class="cancel-alert">
        <template #title>即将取消订单「{{ current.orderNo }}」，请填写取消原因</template>
      </el-alert>
      <el-form label-position="top">
        <el-form-item label="取消原因（将反馈给客户）" required>
          <el-input v-model="cancelReason" type="textarea" :rows="4" maxlength="500" show-word-limit
            placeholder="例如：服务资源已满，无法满足本次预约" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">再想想</el-button>
        <el-button type="danger" :loading="submitting" @click="submitCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { api } from '@/api'

const statuses = [{ value: 'PENDING', label: '待确认' }, { value: 'CONFIRMED', label: '已确认' }, { value: 'CANCELLED', label: '已取消' }]
const types = [{ value: 'TRAVEL', label: '出行' }, { value: 'HOTEL', label: '住宿' }, { value: 'FOOD', label: '餐饮' }]
const statusLabel = value => statuses.find(item => item.value === value)?.label || value
const typeLabel = value => types.find(item => item.value === value)?.label || value
const typeTag = value => ({ TRAVEL: 'primary', HOTEL: 'success', FOOD: 'warning' }[value] || 'info')
const statusTag = value => ({ CONFIRMED: 'success', PENDING: 'warning', CANCELLED: 'info' }[value] || 'info')
const cancelSourceLabel = value => ({ USER: '用户', ADMIN: '平台管理员' }[value] || value)
const serviceModeLabel = value => ({ DAY_CHARTER: '包车一日', ROUND_TRIP: '往返' }[value] || value)
const mealPeriodLabel = value => ({ BREAKFAST: '早餐', LUNCH: '午餐', DINNER: '晚餐' }[value] || value)

const loading = ref(false)
const list = ref([])
const total = ref(0)
const businesses = ref([])
const query = reactive({ status: '', type: '', businessId: '', serviceDateRange: [], page: 1, size: 20 })
const dateShortcuts = [
  { text: '今天', value: () => { const d = new Date(); return [d, d] } },
  { text: '最近 7 天', value: () => { const end = new Date(); const start = new Date(); start.setDate(start.getDate() - 6); return [start, end] } },
  { text: '最近 30 天', value: () => { const end = new Date(); const start = new Date(); start.setDate(start.getDate() - 29); return [start, end] } }
]
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({})
const cancelVisible = ref(false)
const submitting = ref(false)
const current = ref({})
const cancelReason = ref('')

const optionText = order => {
  if (order.serviceType === 'TRAVEL') return `${order.carSpecSnapshot || '车辆'} × ${order.carQuantity || 0}，${serviceModeLabel(order.serviceMode)}`
  if (order.serviceType === 'HOTEL') return `${order.roomSpecSnapshot || '房型'} × ${order.roomQuantity || 0}`
  if (order.serviceType === 'FOOD') return mealPeriodLabel(order.mealPeriod)
  return '—'
}
const showError = error => ElMessage.error(error?.message || '请求失败')
const fetchList = async () => {
  loading.value = true
  try {
    const data = await api.getOrders(query)
    list.value = data.items
    total.value = data.total
  } catch (error) { showError(error) } finally { loading.value = false }
}
const loadBusinesses = async () => {
  try {
    const data = await api.getBusinesses({ page: 1, size: 100 })
    businesses.value = data.items
  } catch (error) { showError(error) }
}
const handleSearch = () => { query.page = 1; fetchList() }
const handleReset = () => { Object.assign(query, { status: '', type: '', businessId: '', serviceDateRange: [], page: 1 }); fetchList() }
const handleSizeChange = () => { query.page = 1; fetchList() }
const showDetail = async row => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = {}
  try { detail.value = await api.getOrder(row.id) } catch (error) { showError(error); detailVisible.value = false } finally { detailLoading.value = false }
}
const confirmOrder = row => ElMessageBox.confirm(`确认订单「${row.orderNo}」？确认后用户不可取消。`, '确认订单', {
  confirmButtonText: '确认', cancelButtonText: '返回', type: 'warning'
}).then(async () => {
  try { await api.confirmOrder(row.id); ElMessage.success('订单已确认'); fetchList() } catch (error) { showError(error); fetchList() }
}).catch(() => {})
const openCancelDialog = row => { current.value = row; cancelReason.value = ''; cancelVisible.value = true }
const submitCancel = async () => {
  const reason = cancelReason.value.trim()
  if (!reason) return ElMessage.warning('请填写取消原因')
  submitting.value = true
  try { await api.cancelOrder(current.value.id, reason); ElMessage.success('订单已取消'); cancelVisible.value = false; fetchList() }
  catch (error) { showError(error); fetchList() } finally { submitting.value = false }
}
onMounted(() => { fetchList(); loadBusinesses() })
</script>

<style scoped>
.toolbar { flex-wrap: wrap; }
.openid { font-family: Consolas, Monaco, monospace; font-size: 12px; color: #606266; }
.cancel-alert { margin-bottom: 16px; }
</style>
