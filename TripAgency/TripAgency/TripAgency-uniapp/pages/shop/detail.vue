<template>
  <view class="detail-wrap">
    <!-- 加载态 -->
    <view v-if="loading" class="loading-wrap">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中…</text>
    </view>

    <view v-else-if="shop" class="detail-card">
      <!-- 商家相册轮播 -->
      <swiper v-if="shop.imageUrls && shop.imageUrls.length" class="banner" circular indicator-dots indicator-active-color="#1677ff">
        <swiper-item v-for="(img, i) in shop.imageUrls" :key="i">
          <image class="banner-img" :src="img" mode="aspectFill" />
        </swiper-item>
      </swiper>
      <view v-else class="banner banner-ph">
        <text class="banner-ph-emoji">{{ typeEmoji }}</text>
        <text class="banner-ph-text">{{ typeLabel }} · {{ shop.name }}</text>
      </view>

      <view class="head">
        <text class="head-emoji">{{ typeEmoji }}</text>
        <view class="head-main">
          <view class="name">{{ shop.name }}</view>
          <view class="type-tag" :class="'tag-' + shop.businessType">{{ typeLabel }}</view>
        </view>
      </view>

      <view class="info-row" v-if="shop.address">
        <text class="info-icon">📍</text>
        <text class="info-text">{{ shop.address }}</text>
      </view>

      <!-- 商家简介（三类通用） -->
      <view v-if="shop.intro" class="block">
        <view class="label">商家介绍</view>
        <view class="text">{{ shop.intro }}</view>
      </view>

      <!-- 出行：车辆列表 -->
      <view v-if="shop.businessType === 'TRAVEL'" class="block">
        <view class="label sub-label">可选车辆（{{ shop.detail.cars.length }}）</view>
        <view class="item-card" v-for="car in shop.detail.cars" :key="car.id" hover-class="card-hover">
          <image v-if="car.imageUrl" class="item-img" :src="car.imageUrl" mode="aspectFill" />
          <view class="item-main">
            <text class="item-name">{{ car.model }}</text>
            <text class="item-sub">{{ car.seatNum }}座{{ car.description ? ' · ' + car.description : '' }}</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
      </view>

      <!-- 住宿：房型列表 -->
      <view v-if="shop.businessType === 'HOTEL'" class="block">
        <view class="label sub-label">可选房型（{{ shop.detail.rooms.length }}）</view>
        <view class="item-card" v-for="room in shop.detail.rooms" :key="room.id" hover-class="card-hover">
          <image v-if="room.imageUrl" class="item-img" :src="room.imageUrl" mode="aspectFill" />
          <view class="item-main">
            <text class="item-name">{{ room.name }}</text>
            <text class="item-sub">{{ room.bedSpec }}{{ room.description ? ' · ' + room.description : '' }}</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
      </view>

      <!-- 餐饮：联系方式 + 推荐菜 + 菜品 -->
      <view v-if="shop.businessType === 'FOOD'" class="block">
        <view v-if="shop.detail.contactName || shop.detail.contactPhone" class="contact-box">
          <view class="contact-info">
            <text class="contact-label">{{ shop.detail.contactName || '商家' }}</text>
            <text class="contact-phone">{{ shop.detail.contactPhone }}</text>
          </view>
          <button class="call-btn" hover-class="btn-hover" @click="callMerchant">拨打电话</button>
        </view>
        <view v-if="shop.detail.recommendedDishes" class="info-row">
          <text class="info-icon">🍽</text>
          <text class="info-text"><text class="rec-label">推荐菜：</text>{{ shop.detail.recommendedDishes }}</text>
        </view>
        <view v-if="shop.detail.dishes && shop.detail.dishes.length" class="block">
          <view class="label sub-label">精选菜品（{{ shop.detail.dishes.length }}）</view>
          <view class="item-card" v-for="dish in shop.detail.dishes" :key="dish.id" hover-class="card-hover">
            <image v-if="dish.imageUrl" class="item-img" :src="dish.imageUrl" mode="aspectFill" />
            <view class="item-main">
              <text class="item-name">{{ dish.name }}</text>
              <text v-if="dish.description" class="item-sub">{{ dish.description }}</text>
            </view>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 吸底操作栏：去这里 + 立即预约 -->
      <view class="bottom-bar">
        <button class="loc-btn" hover-class="btn-hover" @click="openLocation">📍 去这里</button>
        <button class="primary-btn" hover-class="btn-hover" @click="gotoCreate">立即预约下单</button>
      </view>
    </view>
    <view v-else class="empty">
      <view class="empty-emoji">🏫</view>
      <text class="empty-text">商家不存在或已下架</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBusinessDetail } from '../../api/app'
import { SERVICE_TYPE_MAP } from '../../common/config'
import { resolveImg } from '../../common/util'

const shop = ref(null)
const loading = ref(true)

const typeLabel = computed(() => {
  const t = shop.value ? shop.value.businessType : ''
  return SERVICE_TYPE_MAP[t] ? SERVICE_TYPE_MAP[t].label : ''
})

const typeEmoji = computed(() => {
  const t = shop.value ? shop.value.businessType : ''
  return SERVICE_TYPE_MAP[t] ? SERVICE_TYPE_MAP[t].emoji : ''
})

const normalizeImages = (data) => {
  if (!data) return null
  const common = data.common
  if (!common) return null
  return Object.assign({}, common, {
    imageUrls: (common.imageUrls || []).map(resolveImg),
    detail: Object.assign({}, data.detail, {
      cars: ((data.detail && data.detail.cars) || []).map(c => Object.assign({}, c, { imageUrl: resolveImg(c.imageUrl) })),
      rooms: ((data.detail && data.detail.rooms) || []).map(r => Object.assign({}, r, { imageUrl: resolveImg(r.imageUrl) })),
      dishes: ((data.detail && data.detail.dishes) || []).map(d => Object.assign({}, d, { imageUrl: resolveImg(d.imageUrl) }))
    })
  })
}

onLoad((options) => {
  const id = Number(options.shopId)
  getBusinessDetail(id)
    .then((data) => {
      shop.value = normalizeImages(data)
    })
    .catch((e) => {
      uni.showToast({ title: e.message || '商家加载失败', icon: 'none' })
    })
    .finally(() => {
      loading.value = false
    })
})

const callMerchant = () => {
  const phone = shop.value && shop.value.detail && shop.value.detail.contactPhone
  if (!phone) {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: String(phone) })
}

const openLocation = () => {
  const s = shop.value
  if (!s || !s.longitude || !s.latitude) {
    uni.showToast({ title: '暂无可定位信息', icon: 'none' })
    return
  }
  uni.openLocation({
    longitude: Number(s.longitude),
    latitude: Number(s.latitude),
    name: s.name,
    address: s.address || ''
  })
}

const gotoCreate = () => {
  uni.navigateTo({ url: `/pages/order/create?businessId=${shop.value.id}` })
}
</script>

<style scoped>
.detail-wrap {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 24rpx 24rpx 200rpx;
}
.detail-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
}
.banner {
  width: 100%;
  height: 360rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
}
.banner-img {
  width: 100%;
  height: 100%;
}
.banner-ph {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4fb 0%, #e4ebf5 100%);
}
.banner-ph-emoji {
  font-size: 110rpx;
  opacity: 0.8;
}
.banner-ph-text {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #8892a3;
}
.head {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.head-emoji {
  font-size: 56rpx;
  margin-right: 20rpx;
}
.head-main {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f2329;
  word-break: break-all;
}
.type-tag {
  display: inline-flex;
  margin-top: 10rpx;
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
.info-row {
  display: flex;
  align-items: flex-start;
  font-size: 26rpx;
  color: #555;
  line-height: 1.6;
  margin-bottom: 8rpx;
}
.info-icon {
  flex-shrink: 0;
  margin-right: 8rpx;
}
.info-text {
  flex: 1;
  word-break: break-all;
}
.rec-label {
  color: #666;
  font-weight: 600;
}
.block {
  margin: 24rpx 0;
}
.label {
  color: #666;
  font-weight: 600;
}
.sub-label {
  display: block;
  margin-bottom: 12rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #1677ff;
  line-height: 1.4;
}
.text {
  margin-top: 8rpx;
  color: #333;
  line-height: 1.7;
}
.item-card {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: #f7f8fa;
  border-radius: 12rpx;
  margin-top: 12rpx;
  font-size: 26rpx;
  border: 2rpx solid transparent;
  transition: all 0.15s;
}
.card-hover {
  background: #eef3fb;
  border-color: #bcd6ff;
}
.item-img {
  width: 140rpx;
  height: 140rpx;
  border-radius: 10rpx;
  background: #eceef2;
  flex-shrink: 0;
  margin-right: 20rpx;
}
.item-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.item-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2329;
}
.item-sub {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #888;
}
.item-arrow {
  flex-shrink: 0;
  margin-left: 12rpx;
  font-size: 40rpx;
  color: #c0c4cc;
}
.contact-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #fff7e6;
  border: 2rpx solid #ffd591;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}
.contact-info {
  display: flex;
  flex-direction: column;
}
.contact-label {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}
.contact-phone {
  margin-top: 6rpx;
  font-size: 26rpx;
  color: #666;
}
.call-btn {
  flex-shrink: 0;
  width: 200rpx;
  background: linear-gradient(135deg, #ffb84d 0%, #ff9a2e 100%);
  color: #fff;
  font-size: 26rpx;
  margin: 0;
}
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  gap: 20rpx;
  padding: 16rpx 24rpx 20rpx;
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 -6rpx 24rpx rgba(0, 0, 0, 0.08);
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.loc-btn {
  flex: 1;
  background: #fff;
  color: #1677ff;
  border: 2rpx solid #1677ff;
  font-size: 28rpx;
}
.primary-btn {
  flex: 2;
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
  font-weight: 500;
}
.empty {
  text-align: center;
  margin-top: 160rpx;
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
</style>
