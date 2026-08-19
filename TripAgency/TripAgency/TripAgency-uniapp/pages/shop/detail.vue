<template>
  <view class="detail-wrap">
    <view v-if="shop" class="detail-card">
      <!-- 商家相册轮播 -->
      <swiper v-if="shop.imageUrls && shop.imageUrls.length" class="banner" circular indicator-dots indicator-active-color="#1677ff">
        <swiper-item v-for="(img, i) in shop.imageUrls" :key="i">
          <image class="banner-img" :src="img" mode="aspectFill" />
        </swiper-item>
      </swiper>

      <view class="head">
        <text class="name">{{ shop.name }}</text>
        <text class="tag">{{ typeLabel }}</text>
      </view>

      <view class="row"><text class="label">地址：</text><text class="addr">{{ shop.address }}</text></view>

      <!-- 商家简介（三类通用） -->
      <view v-if="shop.intro" class="block">
        <view class="label">商家介绍</view>
        <view class="text">{{ shop.intro }}</view>
      </view>

      <!-- 出行：车辆列表 -->
      <view v-if="shop.businessType === 'TRAVEL'" class="block">
        <view class="label sub-label">可选车辆（{{ shop.detail.cars.length }}）</view>
        <view class="item-card" v-for="car in shop.detail.cars" :key="car.id">
          <image v-if="car.imageUrl" class="item-img" :src="car.imageUrl" mode="aspectFill" />
          <view class="item-main">
            <text class="item-name">{{ car.model }}</text>
            <text class="item-sub">{{ car.seatNum }}座{{ car.description ? ' · ' + car.description : '' }}</text>
          </view>
        </view>
      </view>

      <!-- 住宿：房型列表 -->
      <view v-if="shop.businessType === 'HOTEL'" class="block">
        <view class="label sub-label">可选房型（{{ shop.detail.rooms.length }}）</view>
        <view class="item-card" v-for="room in shop.detail.rooms" :key="room.id">
          <image v-if="room.imageUrl" class="item-img" :src="room.imageUrl" mode="aspectFill" />
          <view class="item-main">
            <text class="item-name">{{ room.name }}</text>
            <text class="item-sub">{{ room.bedSpec }}{{ room.description ? ' · ' + room.description : '' }}</text>
          </view>
        </view>
      </view>

      <!-- 餐饮：联系方式 + 推荐菜 + 菜品 -->
      <view v-if="shop.businessType === 'FOOD'" class="block">
        <view v-if="shop.detail.contactName || shop.detail.contactPhone" class="contact-box">
          <view class="contact-info">
            <text class="contact-label">{{ shop.detail.contactName || '商家' }}</text>
            <text class="contact-phone">{{ shop.detail.contactPhone }}</text>
          </view>
          <button class="call-btn" @click="callMerchant">拨打电话</button>
        </view>
        <view v-if="shop.detail.recommendedDishes" class="row">
          <text class="label">推荐菜：</text>
          <text>{{ shop.detail.recommendedDishes }}</text>
        </view>
        <view v-if="shop.detail.dishes && shop.detail.dishes.length" class="block">
          <view class="label sub-label">精选菜品（{{ shop.detail.dishes.length }}）</view>
          <view class="item-card" v-for="dish in shop.detail.dishes" :key="dish.id">
            <image v-if="dish.imageUrl" class="item-img" :src="dish.imageUrl" mode="aspectFill" />
            <view class="item-main">
              <text class="item-name">{{ dish.name }}</text>
              <text v-if="dish.description" class="item-sub">{{ dish.description }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 去这里：调起地图导航 -->
      <view class="loc-row">
        <button class="loc-btn" @click="openLocation">📍 去这里</button>
      </view>

      <button class="primary-btn" @click="gotoCreate">立即预约下单</button>
    </view>
    <view v-else class="empty">商家不存在或已下架</view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBusinessDetail } from '../../api/app'
import { mockBusinesses } from '../../common/mock'
import { SERVICE_TYPE_MAP } from '../../common/config'
import { resolveImg } from '../../common/util'

const shop = ref(null)

const typeLabel = computed(() => {
  const t = shop.value ? shop.value.businessType : ''
  return SERVICE_TYPE_MAP[t] ? SERVICE_TYPE_MAP[t].label : ''
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

const fallbackToMock = (id) => {
  const mock = mockBusinesses.find(b => b.id === id)
  shop.value = mock ? normalizeImages(mock) : null
}

onLoad((options) => {
  const id = Number(options.shopId)
  getBusinessDetail(id)
    .then((data) => {
      shop.value = normalizeImages(data)
    })
    .catch((e) => {
      // 网络不可达或后端窗口 06B 接口未实现（未知路由 404）时使用演示数据
      if (e.statusCode === 0 || (e.statusCode === 404 && e.code === 'ERROR')) {
        fallbackToMock(id)
        return
      }
      uni.showToast({ title: e.message || '商家加载失败', icon: 'none' })
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
  padding: 24rpx;
}
.detail-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}
.banner {
  width: 100%;
  height: 360rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
}
.banner-img {
  width: 100%;
  height: 100%;
}
.head {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}
.name {
  font-size: 36rpx;
  font-weight: bold;
}
.tag {
  margin-left: 16rpx;
  font-size: 24rpx;
  color: #1677ff;
  background: #e8f3ff;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.row {
  margin-bottom: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
}
.addr {
  word-break: break-all;
}
.label {
  color: #666;
}
.sub-label {
  display: block;
  margin-bottom: 12rpx;
}
.block {
  margin: 24rpx 0;
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
  color: #333;
}
.item-sub {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #888;
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
  background: #ff9a2e;
  color: #fff;
  font-size: 26rpx;
  margin: 0;
}
.loc-row {
  margin-top: 24rpx;
}
.loc-btn {
  width: 100%;
  background: #fff;
  color: #1677ff;
  border: 2rpx solid #1677ff;
  font-size: 28rpx;
}
.primary-btn {
  margin-top: 24rpx;
  background: #1677ff;
  color: #fff;
}
.empty {
  text-align: center;
  color: #999;
  margin-top: 200rpx;
}
</style>
