<template>
  <view class="page">
    <view v-if="orders.length" class="order-list">
      <view class="order-card" v-for="o in orders" :key="o.id">
        <view class="card-top">
          <view class="biz-line">
            <text class="biz-type">{{ typeEmoji(o.serviceType) }}</text>
            <text class="biz-name">{{ o.businessNameSnapshot }}</text>
          </view>
          <text class="status" :class="o.status">{{ statusLabel(o.status) }}</text>
        </view>
        <view class="card-row"><text class="row-label">订单号</text><text class="row-value">{{ o.orderNo }}</text></view>
        <view class="card-row"><text class="row-label">服务类型</text><text class="row-value">{{ typeLabel(o.serviceType) }}</text></view>
        <view class="card-row"><text class="row-label">服务日期</text><text class="row-value">{{ o.serviceDate }}</text></view>
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
          <button class="cancel-btn" @click="onCancel(o)">取消预约</button>
        </view>
      </view>
    </view>

    <view v-else-if="!loading" class="empty">
      <text class="empty-text">暂无预约记录</text>
      <view class="empty-btn" @click="goHome">去首页逛逛</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { getOrderList, cancelOrder } from '../../api/app'
import { getOpenid } from '../../common/auth'
import { getLocalOrders, cancelLocalOrder } from '../../common/local-orders'
import { SERVICE_TYPE_MAP, ORDER_STATUS_MAP, SERVICE_MODE_MAP, MEAL_PERIOD_MAP } from '../../common/config'

const orders = ref([])
const loading = ref(false)
const openid = getOpenid()

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
  if (!silent) loading.value = true
  getOrderList({ openid, page: 1, pageSize: 50 })
    .then((data) => {
      orders.value = (data && data.items) || []
      loading.value = false
    })
    .catch((e) => {
      loading.value = false
      if (e.statusCode === 0) {
        orders.value = getLocalOrders()
      } else {
        uni.showToast({ title: e.message || '加载失败', icon: 'none' })
      }
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
          if (e.statusCode === 0) {
            cancelLocalOrder(o.id)
            uni.showToast({ title: '演示模式：已本地取消', icon: 'none' })
            load(true)
            return
          }
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
  padding: 24rpx;
}
.order-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 26rpx 30rpx;
  margin-bottom: 20rpx;
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
.biz-type {
  font-size: 32rpx;
  margin-right: 10rpx;
}
.biz-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.status {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}
.status.PENDING {
  color: #e6a23c;
  background: #fdf3e3;
}
.status.CONFIRMED {
  color: #1677ff;
  background: #e8f3ff;
}
.status.CANCELLED {
  color: #909399;
  background: #f2f3f5;
}
.card-row {
  display: flex;
  justify-content: space-between;
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
  padding-top: 200rpx;
}
.empty-text {
  color: #999;
  font-size: 28rpx;
}
.empty-btn {
  margin: 40rpx auto 0;
  width: 260rpx;
  padding: 20rpx 0;
  background: #1677ff;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>