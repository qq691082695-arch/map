<template>
  <view class="page">
    <view v-if="business" class="create-wrap">
      <!-- 商家信息 -->
      <view class="section biz-card">
        <view class="biz-head">
          <text class="biz-name">{{ business.name }}</text>
          <text class="biz-tag">{{ typeLabel }}</text>
        </view>
        <text class="biz-addr">{{ business.address }}</text>
      </view>

      <!-- 联系信息 -->
      <view class="section">
        <view class="sec-title">联系信息</view>
        <view class="field">
          <text class="field-label">联系人</text>
          <input v-model="form.contactName" class="field-input" placeholder="请输入联系人姓名" maxlength="64" />
        </view>
        <view class="field">
          <text class="field-label">联系电话</text>
          <input v-model="form.contactPhone" class="field-input" type="number" placeholder="请输入联系电话" maxlength="32" />
        </view>
        <view class="field">
          <text class="field-label">出行人数</text>
          <view class="stepper">
            <view class="step-btn" @click="changePeople(-1)">-</view>
            <input v-model.number="form.peopleNum" class="step-input" type="number" @blur="normalizePeople" />
            <view class="step-btn" @click="changePeople(1)">+</view>
          </view>
        </view>
        <view class="field">
          <text class="field-label">服务日期</text>
          <picker mode="date" :start="minDate" :value="form.serviceDate" @change="onDateChange">
            <view class="picker-value">{{ form.serviceDate }}</view>
          </picker>
        </view>
      </view>

      <!-- 出行：车辆选择 -->
      <view v-if="business.businessType === 'TRAVEL'" class="section">
        <view class="sec-title">车辆选择</view>
        <view
          class="option-card"
          v-for="car in business.detail.cars"
          :key="car.id"
          :class="{ active: form.carId === car.id }"
          @click="form.carId = car.id"
        >
          <image v-if="car.imageUrl" class="option-img" :src="car.imageUrl" mode="aspectFill" />
          <view class="option-main">
            <text class="option-name">{{ car.model }}</text>
            <text class="option-sub">{{ car.seatNum }}座{{ car.description ? ' · ' + car.description : '' }}</text>
          </view>
          <view class="radio-dot" :class="{ on: form.carId === car.id }"></view>
        </view>
        <view class="field">
          <text class="field-label">车辆数量</text>
          <view class="stepper">
            <view class="step-btn" @click="changeQty('carQuantity', -1)">-</view>
            <input v-model.number="form.carQuantity" class="step-input" type="number" />
            <view class="step-btn" @click="changeQty('carQuantity', 1)">+</view>
          </view>
        </view>
        <view class="field">
          <text class="field-label">服务方式</text>
          <view class="mode-row">
            <view class="mode-pill" :class="{ on: form.serviceMode === 'DAY_CHARTER' }" @click="form.serviceMode = 'DAY_CHARTER'">按日包车</view>
            <view class="mode-pill" :class="{ on: form.serviceMode === 'ROUND_TRIP' }" @click="form.serviceMode = 'ROUND_TRIP'">往返接送</view>
          </view>
        </view>
      </view>

      <!-- 住宿：房型选择 -->
      <view v-if="business.businessType === 'HOTEL'" class="section">
        <view class="sec-title">房型选择</view>
        <view
          class="option-card"
          v-for="room in business.detail.rooms"
          :key="room.id"
          :class="{ active: form.roomId === room.id }"
          @click="form.roomId = room.id"
        >
          <image v-if="room.imageUrl" class="option-img" :src="room.imageUrl" mode="aspectFill" />
          <view class="option-main">
            <text class="option-name">{{ room.name }}</text>
            <text class="option-sub">{{ room.bedSpec }}{{ room.description ? ' · ' + room.description : '' }}</text>
          </view>
          <view class="radio-dot" :class="{ on: form.roomId === room.id }"></view>
        </view>
        <view class="field">
          <text class="field-label">房间数量</text>
          <view class="stepper">
            <view class="step-btn" @click="changeQty('roomQuantity', -1)">-</view>
            <input v-model.number="form.roomQuantity" class="step-input" type="number" />
            <view class="step-btn" @click="changeQty('roomQuantity', 1)">+</view>
          </view>
        </view>
      </view>

      <!-- 餐饮：用餐时段 -->
      <view v-if="business.businessType === 'FOOD'" class="section">
        <view class="sec-title">用餐时段</view>
        <view class="mode-row">
          <view class="mode-pill" :class="{ on: form.mealPeriod === 'BREAKFAST' }" @click="form.mealPeriod = 'BREAKFAST'">早餐</view>
          <view class="mode-pill" :class="{ on: form.mealPeriod === 'LUNCH' }" @click="form.mealPeriod = 'LUNCH'">午餐</view>
          <view class="mode-pill" :class="{ on: form.mealPeriod === 'DINNER' }" @click="form.mealPeriod = 'DINNER'">晚餐</view>
        </view>
        <view v-if="business.detail.recommendedDishes" class="hint">推荐菜：{{ business.detail.recommendedDishes }}</view>
      </view>

      <button class="submit-btn" :disabled="submitting" @click="submit">
        {{ submitting ? '提交中...' : '提交预约' }}
      </button>
    </view>

    <!-- 商家缺失空态（避免在 onLoad 中自动路由触发开发态 routeDone 报错） -->
    <view v-else class="empty">
      <text class="empty-text">商家不存在或已下架</text>
      <view class="empty-btn" @click="goBack">返回</view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBusinessDetail, createOrder } from '../../api/app'
import { getOpenid } from '../../common/auth'
import { today, resolveImg } from '../../common/util'
import { saveLocalOrder } from '../../common/local-orders'
import { mockBusinesses } from '../../common/mock'
import { SERVICE_TYPE_MAP } from '../../common/config'

const business = ref(null)
const submitting = ref(false)
const minDate = today()

const form = reactive({
  openid: getOpenid(),
  contactName: '',
  contactPhone: '',
  peopleNum: 1,
  serviceDate: today(),
  carId: null,
  carQuantity: 1,
  serviceMode: 'DAY_CHARTER',
  roomId: null,
  roomQuantity: 1,
  mealPeriod: 'LUNCH'
})

const typeLabel = computed(() => {
  const t = business.value ? business.value.businessType : ''
  return SERVICE_TYPE_MAP[t] ? SERVICE_TYPE_MAP[t].label : ''
})

const normalizeImages = (data) => {
  if (!data || !data.common) return null
  const common = data.common
  return Object.assign({}, common, {
    imageUrls: (common.imageUrls || []).map(resolveImg),
    detail: Object.assign({}, data.detail, {
      cars: ((data.detail && data.detail.cars) || []).map(c => Object.assign({}, c, { imageUrl: resolveImg(c.imageUrl) })),
      rooms: ((data.detail && data.detail.rooms) || []).map(r => Object.assign({}, r, { imageUrl: resolveImg(r.imageUrl) })),
      dishes: ((data.detail && data.detail.dishes) || []).map(d => Object.assign({}, d, { imageUrl: resolveImg(d.imageUrl) }))
    })
  })
}

const loadBusiness = (id) => {
  getBusinessDetail(id)
    .then((data) => {
      business.value = normalizeImages(data)
    })
    .catch((e) => {
      // 网络不可达或后端窗口 06B 接口未实现（未知路由 404）时使用演示数据
      if (e.statusCode === 0 || (e.statusCode === 404 && e.code === 'ERROR')) {
        const mock = mockBusinesses.find(b => b.id === id)
        business.value = mock ? normalizeImages(mock) : null
        return
      }
      uni.showToast({ title: e.message || '商家加载失败', icon: 'none' })
    })
}

onLoad((options) => {
  loadBusiness(Number(options.businessId))
})

const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.reLaunch({ url: '/pages/index/index' })
  }
}

const onDateChange = (e) => {
  form.serviceDate = e.detail.value
}

const changePeople = (delta) => {
  const next = (form.peopleNum || 1) + delta
  form.peopleNum = Math.max(1, next)
}

const normalizePeople = () => {
  const n = parseInt(form.peopleNum, 10)
  form.peopleNum = isNaN(n) || n < 1 ? 1 : n
}

const changeQty = (key, delta) => {
  const next = (form[key] || 1) + delta
  form[key] = Math.max(1, next)
}

const validate = () => {
  if (!business.value) return '商家不存在或已下架'
  if (!form.contactName || !form.contactName.trim()) return '请填写联系人姓名'
  if (!form.contactPhone || !form.contactPhone.trim()) return '请填写联系电话'
  const n = parseInt(form.peopleNum, 10)
  if (isNaN(n) || n < 1) return '出行人数必须为正整数'
  if (!form.serviceDate) return '请选择服务日期'
  const type = business.value.businessType
  if (type === 'TRAVEL') {
    if (!form.carId) return '请选择车辆'
    if (!form.carQuantity || form.carQuantity < 1) return '车辆数量必须大于0'
  } else if (type === 'HOTEL') {
    if (!form.roomId) return '请选择房型'
    if (!form.roomQuantity || form.roomQuantity < 1) return '房间数量必须大于0'
  }
  return ''
}

const buildPayload = () => {
  const type = business.value.businessType
  const payload = {
    openid: form.openid,
    contactName: form.contactName.trim(),
    contactPhone: form.contactPhone.trim(),
    peopleNum: parseInt(form.peopleNum, 10),
    serviceDate: form.serviceDate,
    businessId: business.value.id,
    serviceType: type
  }
  if (type === 'TRAVEL') {
    payload.carId = form.carId
    payload.carQuantity = form.carQuantity
    payload.serviceMode = form.serviceMode
  } else if (type === 'HOTEL') {
    payload.roomId = form.roomId
    payload.roomQuantity = form.roomQuantity
  } else {
    payload.mealPeriod = form.mealPeriod
  }
  return payload
}

const buildDemoOrder = (payload) => {
  const type = payload.serviceType
  const biz = business.value
  const now = new Date().toISOString()
  const item = {
    id: Date.now(),
    orderNo: 'MO' + String(Date.now()),
    businessId: payload.businessId,
    businessNameSnapshot: biz.name,
    serviceType: type,
    contactName: payload.contactName,
    contactPhone: payload.contactPhone,
    peopleNum: payload.peopleNum,
    serviceDate: payload.serviceDate,
    status: 'PENDING',
    createdAt: now,
    updatedAt: now
  }
  if (type === 'TRAVEL') {
    const car = biz.detail.cars.find(c => c.id === payload.carId)
    item.carId = payload.carId
    item.carSpecSnapshot = car ? car.model : ''
    item.carQuantity = payload.carQuantity
    item.serviceMode = payload.serviceMode
  } else if (type === 'HOTEL') {
    const room = biz.detail.rooms.find(r => r.id === payload.roomId)
    item.roomId = payload.roomId
    item.roomSpecSnapshot = room ? room.name + ' ' + room.bedSpec : ''
    item.roomQuantity = payload.roomQuantity
  } else {
    item.mealPeriod = payload.mealPeriod
  }
  return item
}

const afterSuccess = (order, demo) => {
  submitting.value = false
  const content = demo
    ? '后端接口暂不可用，已生成本地演示预约（待确认）'
    : '订单号：' + order.orderNo + '\n当前状态：待管理员确认'
  uni.showModal({
    title: demo ? '演示模式' : '预约提交成功',
    content,
    confirmText: '查看我的预约',
    cancelText: '继续逛逛',
    success: (r) => {
      if (r.confirm) {
        uni.redirectTo({ url: '/pages/order/list' })
      } else {
        uni.navigateBack()
      }
    }
  })
}

const submit = () => {
  if (submitting.value) return
  const err = validate()
  if (err) {
    uni.showToast({ title: err, icon: 'none' })
    return
  }
  const payload = buildPayload()
  submitting.value = true
  createOrder(payload)
    .then((order) => {
      afterSuccess(order, false)
    })
    .catch((e) => {
      submitting.value = false
      if (e.statusCode === 0) {
        const order = buildDemoOrder(payload)
        saveLocalOrder(order)
        afterSuccess(order, true)
        return
      }
      uni.showToast({ title: e.message || '预约提交失败', icon: 'none' })
    })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 24rpx;
}
.create-wrap {
  padding-bottom: 60rpx;
}
.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 26rpx 30rpx;
  margin-bottom: 20rpx;
}
.sec-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}
.biz-head {
  display: flex;
  align-items: center;
}
.biz-name {
  font-size: 36rpx;
  font-weight: bold;
}
.biz-tag {
  margin-left: 16rpx;
  font-size: 24rpx;
  color: #1677ff;
  background: #e8f3ff;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.biz-addr {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #888;
}
.field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.field:last-child {
  border-bottom: none;
}
.field-label {
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
  margin-right: 20rpx;
}
.field-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
}
.picker-value {
  font-size: 28rpx;
  color: #333;
}
.stepper {
  display: flex;
  align-items: center;
}
.step-btn {
  width: 56rpx;
  height: 56rpx;
  line-height: 56rpx;
  text-align: center;
  background: #f4f6fa;
  border-radius: 10rpx;
  font-size: 32rpx;
  color: #333;
}
.step-input {
  width: 90rpx;
  text-align: center;
  font-size: 28rpx;
}
.option-card {
  display: flex;
  align-items: center;
  padding: 22rpx 20rpx;
  border: 2rpx solid #e8ecf2;
  border-radius: 14rpx;
  margin-bottom: 16rpx;
}
.option-card.active {
  border-color: #1677ff;
  background: #f0f6ff;
}
.option-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 10rpx;
  background: #eceef2;
  flex-shrink: 0;
  margin-right: 20rpx;
}
.option-main {
  flex: 1;
}
.option-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}
.option-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #888;
}
.radio-dot {
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  border: 3rpx solid #c0c4cc;
}
.radio-dot.on {
  border-color: #1677ff;
  background: #1677ff;
}
.mode-row {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}
.mode-pill {
  padding: 14rpx 34rpx;
  border-radius: 30rpx;
  border: 2rpx solid #e0e4ea;
  background: #f7f8fa;
  font-size: 28rpx;
  color: #333;
}
.mode-pill.on {
  border-color: #1677ff;
  background: #1677ff;
  color: #fff;
}
.hint {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #888;
  line-height: 1.6;
}
.submit-btn {
  margin-top: 30rpx;
  background: #1677ff;
  color: #fff;
}
.submit-btn[disabled] {
  background: #a0c4ff;
  color: #fff;
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