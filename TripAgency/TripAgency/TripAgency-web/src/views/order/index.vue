<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索订单号 / 商家 / openid"
        clearable
        style="width: 260px"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="handleSearch">
        <el-option v-for="s in statusList" :key="s" :label="s" :value="s" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="订单号" width="150" />
        <el-table-column label="类型" width="75">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shopName" label="商家" min-width="160" show-overflow-tooltip />
        <el-table-column label="下单人 openid" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="openid">{{ row.openid }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="订单内容" min-width="170" show-overflow-tooltip />
        <el-table-column label="金额" width="90">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="取消反馈" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.status === '已取消'" class="cancel-reason">{{ row.cancelReason }}</span>
            <span v-else class="no-reason">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="165" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '待确认'">
              <el-button link type="success" @click="confirmOrder(row)">确认</el-button>
              <el-button link type="danger" @click="openCancelDialog(row)">取消</el-button>
            </template>
            <template v-else>
              <span class="no-reason">无</span>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="total"
          :page-sizes="[5, 10, 20]"
          :page-size="query.size"
          v-model:current-page="query.page"
          @current-change="fetchList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 取消订单：必须填写原因 -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="480px" :close-on-click-modal="false">
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        class="cancel-alert"
      >
        <template #title>即将取消订单「{{ current.id }}」，请填写取消原因</template>
      </el-alert>
      <el-form label-position="top">
        <el-form-item label="取消原因（将反馈给客户）" required>
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="4"
            maxlength="100"
            show-word-limit
            placeholder="该原因将展示给下单客户，例如：商家房型已订满、无法满足服务需求等"
          />
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
import { Search } from '@element-plus/icons-vue'
import { api } from '@/api'

const statusList = ['待确认', '已确认', '已取消']

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })

const cancelVisible = ref(false)
const submitting = ref(false)
const current = ref({})
const cancelReason = ref('')

const typeTag = (t) => ({ 出行: 'primary', 住宿: 'success', 饮食: 'warning' }[t] || 'info')
const statusTag = (s) => ({ 已确认: 'success', 待确认: 'warning', 已取消: 'info' }[s] || 'info')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.getOrders(query)
    if (res.ok) {
      list.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.keyword = ''
  query.status = ''
  query.page = 1
  fetchList()
}

const handleSizeChange = () => {
  query.page = 1
  fetchList()
}

const confirmOrder = (row) => {
  ElMessageBox.confirm(`确认订单「${row.id}」？确认后状态将变为「已确认」。`, '确认订单', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await api.updateOrderStatus(row.id, '已确认')
      if (res.ok) {
        ElMessage.success(res.message)
        fetchList()
      } else {
        ElMessage.error(res.message)
      }
    })
    .catch(() => {})
}

const openCancelDialog = (row) => {
  current.value = row
  cancelReason.value = ''
  cancelVisible.value = true
}

const submitCancel = async () => {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请填写取消原因')
    return
  }
  submitting.value = true
  try {
    const res = await api.updateOrderStatus(current.value.id, '已取消', cancelReason.value.trim())
    if (res.ok) {
      ElMessage.success(res.message)
      cancelVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.amount {
  color: #f5222d;
  font-weight: 600;
}
.openid {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: #606266;
}
.cancel-reason {
  color: #909399;
  font-size: 13px;
}
.no-reason {
  color: #dcdfe6;
}
.cancel-alert {
  margin-bottom: 16px;
}
</style>