<template>
  <view class="page">
    <!-- 状态筛选 -->
    <scroll-view scroll-x class="filter-scroll">
      <view class="filter-row">
        <view
          class="filter-tab"
          v-for="f in filterTabs"
          :key="f.value"
          :class="{ on: activeFilter === f.value }"
          hover-class="filter-tab-hover"
          @click="switchFilter(f.value)"
        >
          <text>{{ f.label }}</text>
          <text class="filter-count" v-if="countOf(f.value)">{{ countOf(f.value) }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="orders.length" class="order-list">
      <view class="order-card" :class="'order-' + o.status" v-for="o in filteredOrders" :key="o.id">
        <view class="card-top">
          <view class="biz-line">
            <view class="biz-avatar" :class="'avatar-' + o.serviceType">{{ typeEmoji(o.serviceType) }}</view>
            <text class="biz-name">{{ o.businessNameSnapshot }}</text>
          </view>
          <view class="status" :class="o.status"><view class="status-dot"></view>{{ statusLabel(o.status) }}</view>
        </view>
        <view class="card-row"><text class="row-label">订单号</text><text class="row-value">{{ o.orderNo }}</text></view>
        <view class="card-row"><text class="row-label">服务类型</text><text class="row-value">{{ typeLabel(o.serviceType) }}</text></view>
        <view class="card-row"><text class="row-label">服务日期</text><text class="date-chip">{{ o.serviceDate }}</text></view>
        <view class="card-row"><text class="row-label">出行人数</text><text class="row-value">{{ o.peopleNum }} 人</text></view>
        <view class="card-row"><text class="row-label">选择项</text><text class="row-value">{{ selectionText(o) }}</text></view>
        <view v-if="o.status === 'CANCELLED'" class="card-row">
          <text class="row-label">取消来源</text>
          <text class="row-value">{{ o.cancelSource === 'ADMIN' ? '平台管理员' : '用户' }}</text>
        </view>
        <view v-if="o.status === 'CANCELLED' && o.cancelReason" class="card-row">
          <text class="row-label">取消原因</text>
          <text class="row-value">{{ o.cancelReason }}</text>
        </view>
        <view v-if="o.status === 'PENDING'" class="cancel-wrap">
          <button class="cancel-btn" hover-class="btn-hover" @click="onCancel(o)">取消预约</button>
        </view>
      </view>
      <view v-if="!filteredOrders.length" class="empty small">
        <text class="empty-text">该状态下暂无订单</text>
      </view>
    </view>

    <view v-else-if="loading" class="empty">
      <view class="loading-spinner"></view>
      <text class="empty-text">加载中…</text>
    </view>

    <view v-else class="empty">
      <view class="empty-emoji">🗓</view>
      <text class="empty-text">暂无预约记录</text>
      <view class="empty-btn" hover-class="btn-hover" @click="goHome">去首页逛逛</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { getOrderList, cancelOrder } from '../../api/app'
import { getOpenid } from '../../common/auth'
import { SERVICE_TYPE_MAP, ORDER_STATUS_MAP, SERVICE_MODE_MAP, MEAL_PERIOD_MAP } from '../../common/config'

const orders = ref([])
const loading = ref(false)
const openid = getOpenid()

const filterTabs = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING', label: '待确认' },
  { value: 'CONFIRMED', label: '已确认' },
  { value: 'CANCELLED', label: '已取消' }
]
const activeFilter = ref('ALL')

const filteredOrders = computed(() =>
  activeFilter.value === 'ALL'
    ? orders.value
    : orders.value.filter(o => o.status === activeFilter.value)
)

const countOf = (status) =>
  status === 'ALL'
    ? orders.value.length
    : orders.value.filter(o => o.status === status).length

const switchFilter = (value) => {
  activeFilter.value = value
}

const statusLabel = (s) => ORDER_STATUS_MAP[s] || s
const typeLabel = (t) => (SERVICE_TYPE_MAP[t] ? SERVICE_TYPE_MAP[t].label : t)
const typeEmoji = (t) => (SERVICE_TYPE_MAP[t] ? SERVICE_TYPE_MAP[t].emoji : '')

const selectionText = (o) => {
  if (o.serviceType === 'TRAVEL') {
    const mode = o.serviceMode ? SERVICE_MODE_MAP[o.serviceMode] || o.serviceMode : ''
    return [o.carSpecSnapshot, '×' + o.carQuantity, mode].filter(Boolean).join(' / ')
  }
  if (o.serviceType === 'HOTEL') {
    return [o.roomSpecSnapshot, '×' + o.roomQuantity].filter(Boolean).join(' / ')
  }
  if (o.serviceType === 'FOOD') {
    return o.mealPeriod ? MEAL_PERIOD_MAP[o.mealPeriod] || o.mealPeriod : ''
  }
  return ''
}

const load = (silent) => {
  if (!openid) {
    orders.value = []
    loading.value = false
    uni.showToast({ title: '请先完成微信登录', icon: 'none' })
    return
  }
  if (!silent) loading.value = true
  getOrderList({ openid, page: 1, pageSize: 50 })
    .then((data) => {
      orders.value = (data && data.items) || []
      loading.value = false
    })
    .catch((e) => {
      loading.value = false
      uni.showToast({ title: e.message || '加载失败', icon: 'none' })
    })
}

onLoad(() => load(false))
onShow(() => {
  if (orders.value.length) load(true)
})
onPullDownRefresh(() => {
  load(true)
  setTimeout(() => uni.stopPullDownRefresh(), 300)
})

const onCancel = (o) => {
  uni.showModal({
    title: '取消预约',
    content: '确认取消该预约吗？',
    success: (r) => {
      if (!r.confirm) return
      cancelOrder(o.id, openid)
        .then((updated) => {
          uni.showToast({ title: '已取消', icon: 'none' })
          load(true)
        })
        .catch((e) => {
          uni.showToast({ title: e.message || '取消失败', icon: 'none' })
        })
    }
  })
}

const goHome = () => {
  uni.reLaunch({ url: '/pages/index/index' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 20rpx 24rpx;
}
.filter-scroll {
  width: 100%;
  white-space: nowrap;
  margin-bottom: 20rpx;
}
.filter-row {
  display: inline-flex;
  gap: 16rpx;
}
.filter-tab {
  display: inline-flex;
  align-items: center;
  padding: 12rpx 28rpx;
  border-radius: 30rpx;
  background: #fff;
  border: 2rpx solid #e4e8ef;
  font-size: 26rpx;
  color: #555;
  transition: all 0.15s;
}
.filter-tab-hover {
  opacity: 0.8;
}
.filter-tab.on {
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  border-color: #1677ff;
  color: #fff;
  font-weight: 500;
}
.filter-count {
  margin-left: 8rpx;
  font-size: 22rpx;
  color: #999;
}
.filter-tab.on .filter-count {
  color: rgba(255, 255, 255, 0.85);
}
.order-card {
  position: relative;
  overflow: hidden;
  background: #fff;
  border-radius: 20rpx;
  padding: 26rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
}
.order-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 26rpx;
  bottom: 26rpx;
  width: 6rpx;
  border-radius: 0 6rpx 6rpx 0;
  background: #c0c4cc;
}
.order-card.order-PENDING::before {
  background: #e6a23c;
}
.order-card.order-CONFIRMED::before {
  background: #1677ff;
}
.order-card.order-CANCELLED::before {
  background: #c0c4cc;
}
.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}
.biz-line {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  margin-right: 12rpx;
}
.biz-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  flex-shrink: 0;
  margin-right: 16rpx;
}
.avatar-TRAVEL {
  background: #e8f3ff;
}
.avatar-HOTEL {
  background: #fff3e0;
}
.avatar-FOOD {
  background: #e8f8ec;
}
.biz-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.status {
  display: inline-flex;
  align-items: center;
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}
.status-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  margin-right: 8rpx;
}
.status.PENDING {
  color: #e6a23c;
  background: #fdf3e3;
}
.status.PENDING .status-dot {
  background: #e6a23c;
}
.status.CONFIRMED {
  color: #1677ff;
  background: #e8f3ff;
}
.status.CONFIRMED .status-dot {
  background: #1677ff;
}
.status.CANCELLED {
  color: #909399;
  background: #f2f3f5;
}
.status.CANCELLED .status-dot {
  background: #909399;
}
.card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;
  font-size: 26rpx;
}
.row-label {
  color: #888;
  flex-shrink: 0;
  margin-right: 20rpx;
}
.row-value {
  color: #333;
  text-align: right;
  flex: 1;
  word-break: break-all;
}
.date-chip {
  flex: 1;
  text-align: right;
  font-size: 26rpx;
  font-weight: 500;
  color: #1677ff;
  background: #e8f3ff;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  display: inline-flex;
  justify-content: flex-end;
}
.cancel-wrap {
  margin-top: 16rpx;
  display: flex;
  justify-content: flex-end;
}
.cancel-btn {
  width: 200rpx;
  background: #fff;
  color: #f56c6c;
  border: 2rpx solid #f56c6c;
  font-size: 26rpx;
}
.empty {
  text-align: center;
  padding-top: 180rpx;
}
.empty.small {
  padding-top: 60rpx;
}
.empty-emoji {
  font-size: 96rpx;
  margin-bottom: 20rpx;
}
.loading-spinner {
  width: 56rpx;
  height: 56rpx;
  margin: 0 auto 20rpx;
  border: 6rpx solid #e6ebf2;
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.empty-text {
  color: #999;
  font-size: 28rpx;
}
.empty-btn {
  margin: 40rpx auto 0;
  width: 260rpx;
  padding: 20rpx 0;
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>
