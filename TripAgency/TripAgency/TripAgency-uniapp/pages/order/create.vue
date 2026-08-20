<template>
  <view class="page">
    <!-- 加载态 -->
    <view v-if="loading" class="loading-wrap">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中…</text>
    </view>

    <view v-else-if="business" class="create-wrap">
      <!-- 商家信息 -->
      <view class="section biz-card">
        <view class="biz-head">
          <text class="biz-name">{{ business.name }}</text>
          <text class="biz-tag" :class="'tag-' + business.businessType">{{ typeLabel }}</text>
        </view>
        <text class="biz-addr">{{ business.address }}</text>
      </view>

      <!-- 联系信息 -->
      <view class="section">
        <view class="sec-title"><text class="sec-num">1</text>联系信息</view>
        <view class="field">
          <text class="field-label">👤 联系人</text>
          <input v-model="form.contactName" class="field-input" placeholder="请输入联系人姓名" maxlength="64" cursor-spacing="50" confirm-type="next" />
        </view>
        <view class="field">
          <text class="field-label">📞 联系电话</text>
          <input v-model="form.contactPhone" class="field-input" type="number" placeholder="请输入联系电话" maxlength="32" cursor-spacing="50" confirm-type="done" />
        </view>
        <view class="field">
          <text class="field-label">👥 出行人数</text>
          <view class="stepper">
            <view class="step-btn" hover-class="step-btn-hover" @click="changePeople(-1)">−</view>
            <input v-model.number="form.peopleNum" class="step-input" type="number" @blur="normalizePeople" cursor-spacing="50" />
            <view class="step-btn" hover-class="step-btn-hover" @click="changePeople(1)">+</view>
          </view>
        </view>
        <view class="field">
          <text class="field-label">📅 服务日期</text>
          <picker mode="date" :start="minDate" :value="form.serviceDate" @change="onDateChange">
            <view class="picker-value">{{ form.serviceDate }}</view>
          </picker>
        </view>
      </view>

      <!-- 出行：车辆选择 -->
      <view v-if="business.businessType === 'TRAVEL'" class="section">
        <view class="sec-title"><text class="sec-num">2</text>车辆选择</view>
        <view
          class="option-card"
          v-for="car in business.detail.cars"
          :key="car.id"
          :class="{ active: form.carId === car.id }"
          hover-class="option-card-hover"
          @click="form.carId = car.id"
        >
          <image v-if="car.imageUrl" class="option-img" :src="car.imageUrl" mode="aspectFill" />
          <view class="option-main">
            <text class="option-name">{{ car.model }}</text>
            <text class="option-sub">{{ car.seatNum }}座{{ car.description ? ' · ' + car.description : '' }}</text>
          </view>
          <view class="radio-dot" :class="{ on: form.carId === car.id }">
            <text v-if="form.carId === car.id" class="radio-check">✓</text>
          </view>
        </view>
        <view class="field">
          <text class="field-label">车辆数量</text>
          <view class="stepper">
            <view class="step-btn" hover-class="step-btn-hover" @click="changeQty('carQuantity', -1)">−</view>
            <input v-model.number="form.carQuantity" class="step-input" type="number" cursor-spacing="50" />
            <view class="step-btn" hover-class="step-btn-hover" @click="changeQty('carQuantity', 1)">+</view>
          </view>
        </view>
        <view class="field">
          <text class="field-label">服务方式</text>
          <view class="mode-row">
            <view class="mode-pill" :class="{ on: form.serviceMode === 'DAY_CHARTER' }" hover-class="mode-pill-hover" @click="form.serviceMode = 'DAY_CHARTER'">🚗 按日包车</view>
            <view class="mode-pill" :class="{ on: form.serviceMode === 'ROUND_TRIP' }" hover-class="mode-pill-hover" @click="form.serviceMode = 'ROUND_TRIP'">🔄 往返接送</view>
          </view>
        </view>
      </view>

      <!-- 住宿：房型选择 -->
      <view v-if="business.businessType === 'HOTEL'" class="section">
        <view class="sec-title"><text class="sec-num">2</text>房型选择</view>
        <view
          class="option-card"
          v-for="room in business.detail.rooms"
          :key="room.id"
          :class="{ active: form.roomId === room.id }"
          hover-class="option-card-hover"
          @click="form.roomId = room.id"
        >
          <image v-if="room.imageUrl" class="option-img" :src="room.imageUrl" mode="aspectFill" />
          <view class="option-main">
            <text class="option-name">{{ room.name }}</text>
            <text class="option-sub">{{ room.bedSpec }}{{ room.description ? ' · ' + room.description : '' }}</text>
          </view>
          <view class="radio-dot" :class="{ on: form.roomId === room.id }">
            <text v-if="form.roomId === room.id" class="radio-check">✓</text>
          </view>
        </view>
        <view class="field">
          <text class="field-label">房间数量</text>
          <view class="stepper">
            <view class="step-btn" hover-class="step-btn-hover" @click="changeQty('roomQuantity', -1)">−</view>
            <input v-model.number="form.roomQuantity" class="step-input" type="number" cursor-spacing="50" />
            <view class="step-btn" hover-class="step-btn-hover" @click="changeQty('roomQuantity', 1)">+</view>
          </view>
        </view>
      </view>

      <!-- 餐饮：用餐时段 -->
      <view v-if="business.businessType === 'FOOD'" class="section">
        <view class="sec-title"><text class="sec-num">2</text>用餐时段</view>
        <view class="mode-row">
          <view class="mode-pill" :class="{ on: form.mealPeriod === 'BREAKFAST' }" hover-class="mode-pill-hover" @click="form.mealPeriod = 'BREAKFAST'">🌅 早餐</view>
          <view class="mode-pill" :class="{ on: form.mealPeriod === 'LUNCH' }" hover-class="mode-pill-hover" @click="form.mealPeriod = 'LUNCH'">☀️ 午餐</view>
          <view class="mode-pill" :class="{ on: form.mealPeriod === 'DINNER' }" hover-class="mode-pill-hover" @click="form.mealPeriod = 'DINNER'">🌙 晚餐</view>
        </view>
        <view v-if="business.detail.recommendedDishes" class="hint">🍽 推荐菜：{{ business.detail.recommendedDishes }}</view>
      </view>

      <!-- 吸底提交栏 -->
      <view class="submit-bar">
        <button class="submit-btn" hover-class="btn-hover" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : '提交预约' }}
        </button>
      </view>
    </view>

    <!-- 商家缺失空态（避免在 onLoad 中自动路由触发开发态 routeDone 报错） -->
    <view v-else class="empty">
      <view class="empty-emoji">🏫</view>
      <text class="empty-text">商家不存在或已下架</text>
      <view class="empty-btn" hover-class="btn-hover" @click="goBack">返回</view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBusinessDetail, createOrder } from '../../api/app'
import { getOpenid } from '../../common/auth'
import { today, resolveImg } from '../../common/util'
import { SERVICE_TYPE_MAP } from '../../common/config'

const business = ref(null)
const submitting = ref(false)
const loading = ref(true)
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
      uni.showToast({ title: e.message || '商家加载失败', icon: 'none' })
    })
    .finally(() => {
      loading.value = false
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
  if (!form.openid) return '请先完成微信登录'
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

const afterSuccess = (order) => {
  submitting.value = false
  uni.showModal({
    title: '预约提交成功',
    content: '订单号：' + order.orderNo + '\n当前状态：待管理员确认',
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
      afterSuccess(order)
    })
    .catch((e) => {
      submitting.value = false
      uni.showToast({ title: e.message || '预约提交失败', icon: 'none' })
    })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 24rpx 24rpx 200rpx;
}
.create-wrap {
  padding-bottom: 60rpx;
}
.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 26rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
}
.sec-title {
  display: flex;
  align-items: center;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}
.sec-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
  margin-right: 14rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
  font-size: 24rpx;
}
.biz-head {
  display: flex;
  align-items: center;
}
.biz-name {
  font-size: 36rpx;
  font-weight: bold;
  flex: 1;
  min-width: 0;
  word-break: break-all;
}
.biz-tag {
  flex-shrink: 0;
  margin-left: 16rpx;
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.tag-TRAVEL {
  color: #1677ff;
  background: #e8f3ff;
}
.tag-HOTEL {
  color: #d46b08;
  background: #fff3e0;
}
.tag-FOOD {
  color: #16a34a;
  background: #e8f8ec;
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
  color: #1677ff;
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
  border-radius: 50%;
  font-size: 32rpx;
  color: #333;
  transition: all 0.15s;
}
.step-btn-hover {
  background: #e0e8f5;
  transform: scale(0.92);
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
  transition: all 0.15s;
}
.option-card-hover {
  border-color: #bcd6ff;
  background: #f7faff;
}
.option-card.active {
  border-color: #1677ff;
  background: #f0f6ff;
  box-shadow: 0 6rpx 16rpx rgba(22, 119, 255, 0.15);
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
  color: #1f2329;
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
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.radio-dot.on {
  border-color: #1677ff;
  background: #1677ff;
}
.radio-check {
  color: #fff;
  font-size: 24rpx;
  line-height: 1;
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
  transition: all 0.15s;
}
.mode-pill-hover {
  opacity: 0.85;
}
.mode-pill.on {
  border-color: #1677ff;
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
}
.hint {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #888;
  line-height: 1.6;
}
.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  padding: 16rpx 24rpx 20rpx;
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 -6rpx 24rpx rgba(0, 0, 0, 0.08);
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.submit-btn {
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
  font-weight: 500;
}
.submit-btn[disabled] {
  background: #a0c4ff;
  color: #fff;
}
.empty {
  text-align: center;
  padding-top: 200rpx;
}
.empty-emoji {
  font-size: 96rpx;
  margin-bottom: 20rpx;
}
.empty-text {
  color: #999;
  font-size: 28rpx;
}
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 240rpx;
}
.loading-spinner {
  width: 56rpx;
  height: 56rpx;
  border: 6rpx solid #e6ebf2;
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.loading-text {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #888;
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
