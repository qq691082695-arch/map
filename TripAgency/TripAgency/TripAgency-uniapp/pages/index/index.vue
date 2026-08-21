<template>
  <view class="page-wrap">
    <!-- 地图组件：中心点/缩放随所选分类自适应，红框为服务区域 -->
    <map
      id="tripMap"
      class="map-container"
      :longitude="viewCenter.longitude"
      :latitude="viewCenter.latitude"
      :scale="viewScale"
      :markers="allMarkers"
      :polygons="polygonsList"
      :enable-poi="false"
      @markertap="onMarkerTap"
      @labeltap="onMarkerTap"
    ></map>

    <!-- 首次加载遮罩 -->
    <view v-if="loading" class="loading-mask">
      <view class="loading-spinner"></view>
      <text class="loading-text">正在加载地图数据…</text>
    </view>

    <!-- 地图左上角图例 -->
    <view class="map-legend">
      <view class="legend-chip">
        <view class="legend-dot"></view>
        <text>高校服务区域</text>
      </view>
    </view>

    <!-- 我的预约入口 -->
    <view class="my-orders-btn" hover-class="my-orders-btn-hover" @click="gotoOrders">📋 我的预约</view>

    <!-- 底部抽屉：分类列表与商家详情在同一区域切换，不再叠加弹窗 -->
    <view class="bottom-panel" :class="{ 'detail-mode': drawerDetail }">
      <view class="drawer-handle"></view>
      <!-- 分类大按钮 -->
      <view v-if="!drawerDetail" class="cat-btns">
        <view
          class="cat-btn"
          v-for="tab in catTabs"
          :key="tab.type"
          :class="{ active: activeType === tab.type }"
          @click="toggleType(tab.type)"
        >
          <view class="cat-emoji">{{ tab.emoji }}</view>
          <view class="cat-label">{{ tab.label }}</view>
          <view class="cat-count">{{ countOf(tab.type) }}家</view>
        </view>
      </view>

      <!-- 选中分类后展示该类商家列表 -->
      <view v-if="activeType && !drawerDetail" class="cat-list-block">
        <view class="list-head">
          <text class="list-title">{{ activeEmoji }} {{ activeLabel }}</text>
          <text class="list-sub">共 {{ filteredShopList.length }} 家</text>
          <text class="list-close" @click="toggleType(activeType)">收起 ▲</text>
        </view>
        <scroll-view
          scroll-x
          class="cat-list-scroll"
          :scroll-into-view="scrollIntoView"
          scroll-with-animation
        >
          <view class="cat-list-wrap">
            <view
              :id="'card-' + item.id"
              class="shop-card"
              v-for="item in filteredShopList"
              :key="item.id"
              :class="['type-' + item.type, { selected: item.id === selectedId }]"
              hover-class="shop-card-hover"
              @click="openShopDrawer(item)"
            >
              <view class="card-tag" :class="'tag-' + item.type">{{ item.typeLabel }}</view>
              <image v-if="item.coverImageUrl" class="card-img" :src="item.coverImageUrl" mode="aspectFit" />
              <view v-else class="card-img card-img-ph"><text class="card-img-emoji">{{ item.emoji }}</text></view>
              <view class="card-name">{{ item.name }}</view>
              <view class="card-addr">📍 {{ item.address }}</view>
              <view class="card-intro">{{ item.intro }}</view>
            </view>
          </view>
        </scroll-view>
        <!-- 空分类提示 -->
        <view v-if="!filteredShopList.length" class="list-empty">
          <text class="list-empty-text">该分类暂无可用商家</text>
        </view>
      </view>

      <!-- 商家详情直接替换抽屉内容 -->
      <view v-if="drawerDetail && currentShop" class="drawer-detail">
        <view class="drawer-detail-head">
          <view class="drawer-back" hover-class="drawer-action-hover" @click="closeShopDrawer">‹ 返回列表</view>
          <view class="drawer-title">商家详情</view>
          <view class="drawer-close" hover-class="drawer-action-hover" @click="collapseDrawer">收起</view>
        </view>

        <scroll-view scroll-y class="drawer-detail-scroll">
          <view v-if="currentShop.coverImageUrl" class="drawer-image-wrap" @click="previewShopImage">
            <image class="drawer-image" :src="currentShop.coverImageUrl" mode="aspectFit" />
            <view class="image-tip">点击查看原图</view>
          </view>
          <view v-else class="drawer-image drawer-image-ph">
            <text class="drawer-image-emoji">{{ currentShop.emoji }}</text>
          </view>

          <view class="detail-summary">
            <view class="detail-name-row">
              <text class="detail-name">{{ currentShop.name }}</text>
              <text class="detail-tag" :class="'tag-' + currentShop.type">{{ currentShop.typeLabel }}</text>
            </view>
            <view class="detail-address"><text class="detail-icon">📍</text>{{ currentShop.address || '暂无地址' }}</view>
            <view v-if="currentShop.intro" class="detail-intro">{{ currentShop.intro }}</view>
          </view>
        </scroll-view>

        <view class="drawer-actions">
          <button class="secondary-btn" hover-class="btn-hover" @click="gotoDetail">查看完整详情</button>
          <button class="primary-btn" hover-class="btn-hover" @click="gotoCreate">立即预约</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMapOverview } from '../../api/app'
import { SERVICE_TYPE_MAP } from '../../common/config'
import { resolveImg } from '../../common/util'

const currentShop = ref(null)
const drawerDetail = computed(() => Boolean(currentShop.value))
const activeType = ref('')          // '' 表示未选分类，显示全部商家
const selectedId = ref(null)        // 地图/列表中当前选中的商家
const scrollIntoView = ref('')      // 横向列表自动滚动定位

// 初始视野：聚焦武汉大学主校区（点选点位时中心切换到该点）
const viewCenter = ref({ longitude: 114.362, latitude: 30.5385 })
const viewScale = ref(13)

// ==========三个分类配置==========
const catTabs = [
  { type: 'travel', label: '出行', emoji: '🚗' },
  { type: 'hotel',  label: '住宿', emoji: '🏨' },
  { type: 'food',   label: '饮食', emoji: '🍜' }
]

// ==========真实地图数据（GET /api/v1/app/map-overview）==========
const uniList = ref([])          // 启用未删除的高校区域
const bizList = ref([])          // 启用未删除的商家点位
const loading = ref(true)        // 首次加载中

// 高校区域生成地图多边形（polygonPoints 结构同后端 GeoPoint）
const polygonsList = computed(() => {
  return uniList.value.map(u => ({
    points: u.polygonPoints,
    strokeWidth: 2,
    strokeColor: "#ff0000",
    fillColor: "#ff000033"
  }))
})

// ============高校区域名称标注：取多边形几何中心点，用 marker + label 展示名称============
const polygonCenter = (points) => {
  const n = (points || []).length
  if (!n) return null
  let area = 0
  let cx = 0
  let cy = 0
  for (let i = 0; i < n; i++) {
    const p = points[i]
    const q = points[(i + 1) % n]
    const f = p.longitude * q.latitude - q.longitude * p.latitude
    area += f
    cx += (p.longitude + q.longitude) * f
    cy += (p.latitude + q.latitude) * f
  }
  area *= 0.5
  if (Math.abs(area) < 1e-10) {
    const sumLon = points.reduce((s, p) => s + p.longitude, 0)
    const sumLat = points.reduce((s, p) => s + p.latitude, 0)
    return { longitude: sumLon / n, latitude: sumLat / n }
  }
  return { longitude: cx / (6 * area), latitude: cy / (6 * area) }
}

// 高校名称标注：透明占位图标的 marker，label 显示高校名（负数 id 避免与商家点位冲突）
const buildUniMarker = (u, center) => {
  let iconPath = 'uni-pin.png'
  // #ifdef H5
  iconPath = new URL(`../static/uni-pin.png`, import.meta.url).href
  // #endif
  // #ifdef MP-WEIXIN
  iconPath = '/static/uni-pin.png'
  // #endif
  const labelW = Math.round(u.name.length * 12) + 16
  return {
    id: -(Math.abs(u.id) + 100000),
    longitude: center.longitude,
    latitude: center.latitude,
    iconPath,
    width: 32,
    height: 32,
    anchor: { x: 0.5, y: 0.5 },
    zIndex: 2,
    label: {
      content: u.name,
      color: '#1f2937',
      fontSize: 12,
      bgColor: '#ffffff',
      borderColor: '#ff0000',
      borderWidth: 1,
      borderRadius: 6,
      padding: 3,
      anchorX: -Math.round(labelW / 2),
      anchorY: -10
    }
  }
}

const uniNameMarkers = computed(() => {
  return uniList.value.map(u => {
    const center = polygonCenter(u.polygonPoints)
    if (!center) return null
    return buildUniMarker(u, center)
  }).filter(Boolean)
})

// 地图总 markers：高校名称标注 + 商家点位
const allMarkers = computed(() => [...uniNameMarkers.value, ...markerList.value])

// ============商家归一化：后端 businessType 转为页面分类值============
const iconFor = (businessType) => {
  if (businessType === 'TRAVEL') return 'map-blue.png'
  if (businessType === 'HOTEL') return 'map-red.png'
  return 'map-green.png'
}

const normalizeShop = (raw) => {
  const businessType = raw.businessType || (raw.type ? String(raw.type).toUpperCase() : '')
  return {
    id: raw.id,
    name: raw.name,
    businessType,
    type: businessType.toLowerCase(),
    typeLabel: (SERVICE_TYPE_MAP[businessType] || {}).label || raw.typeLabel || businessType,
    emoji: (SERVICE_TYPE_MAP[businessType] || {}).emoji || '📍',
    longitude: Number(raw.longitude),
    latitude: Number(raw.latitude),
    address: raw.address,
    intro: raw.intro || raw.desc || '',
    coverImageUrl: resolveImg(raw.coverImageUrl || (raw.imageUrls && raw.imageUrls[0]) || ''),
    iconPath: iconFor(businessType)
  }
}

// ============地图聚合数据加载：高校区域 + 三类商家============
const loadData = () => {
  loading.value = true
  getMapOverview()
    .then((data) => {
      uniList.value = (data && data.universities) || []
      bizList.value = (data && data.businesses) || []
    })
    .catch((e) => {
      uniList.value = []
      bizList.value = []
      uni.showToast({ title: e.message || '地图数据加载失败', icon: 'none' })
    })
    .finally(() => {
      loading.value = false
    })
}

onLoad(() => loadData())

const shopAllList = computed(() => bizList.value.map(normalizeShop))

// ============按分类过滤后的商家============
const filteredShopList = computed(() => {
  if (!activeType.value) return shopAllList.value
  return shopAllList.value.filter(s => s.type === activeType.value)
})

const countOf = (type) => shopAllList.value.filter(s => s.type === type).length

const activeLabel = computed(() =>
  catTabs.find(t => t.type === activeType.value)?.label || ''
)

const activeEmoji = computed(() =>
  catTabs.find(t => t.type === activeType.value)?.emoji || ''
)

// ============地图：点击点位 -> 该点居中且放大2级，并向上偏移避免被底部抽屉遮挡============
const zoomToShop = (shop) => {
  const scale = Math.min(viewScale.value + 2, 18)
  viewScale.value = scale
  // 把地图中心移到该点南侧（纬度 - offset），点位即显示在屏幕上方约 27% 处，≈ 抽屉上方剩余地图区域的中间
  const sysInfo = uni.getSystemInfoSync()
  const screenH = (sysInfo && sysInfo.windowHeight) || 667
  const degPerPx = 360 / (256 * Math.pow(2, scale))
  const latOffset = degPerPx * screenH * 0.23
  viewCenter.value = { longitude: shop.longitude, latitude: shop.latitude - latOffset }
}

// 返回当前分类列表，地图保持在当前视野。
const closeShopDrawer = () => {
  currentShop.value = null
  selectedId.value = null
}

// 收起整个抽屉详情，回到默认分类入口。
const collapseDrawer = () => {
  currentShop.value = null
  selectedId.value = null
  activeType.value = ''
  scrollIntoView.value = ''
}

// ============marker 列表（随分类过滤，选中态放大）============
const markerList = computed(() =>
  filteredShopList.value.map(shop => {
    let realIcon = shop.iconPath
    // #ifdef H5
    realIcon = new URL(`../static/${shop.iconPath}`, import.meta.url).href
    // #endif
    // #ifdef MP-WEIXIN
    realIcon = `/static/${shop.iconPath}`
    // #endif

const selected = shop.id === selectedId.value
    const w = selected ? 38 : 30
    const h = selected ? 40 : 32
    const labelW = Math.round(shop.name.length * 11) + 14
    return {
      id: shop.id,
      longitude: shop.longitude,
      latitude: shop.latitude,
      iconPath: realIcon,
      width: w,
      height: h,
      zIndex: selected ? 10 : 1,
      label: {
        content: shop.name,
        color: '#333333',
        fontSize: 11,
        bgColor: '#ffffff',
        borderColor: selected ? '#1677ff' : '#d0d5dd',
        borderWidth: 1,
        borderRadius: 6,
        padding: 4,
        anchorX: -Math.round(labelW / 2),
        anchorY: -6
      }
    }
  })
)

// ============交互============
// 点击分类大按钮：再次点击同分类收起，点击其它分类切换（地图位置不动）
const toggleType = (type) => {
  if (activeType.value === type) {
    activeType.value = ''
    currentShop.value = null
    selectedId.value = null
    scrollIntoView.value = ''
    return
  }
  activeType.value = type
  currentShop.value = null
  selectedId.value = null
  scrollIntoView.value = ''
  nextTick(() => {
    const first = filteredShopList.value[0]
    if (first) scrollIntoView.value = 'card-' + first.id
  })
}

const onMarkerTap = (e) => {
  const shopId = e.detail.markerId
  const shop = shopAllList.value.find(s => s.id === shopId)
  if (!shop) return
  if (activeType.value !== shop.type) {
    activeType.value = shop.type
  }
  selectedId.value = shop.id
  nextTick(() => { scrollIntoView.value = 'card-' + shop.id })
  openShopDrawer(shop)
}

// 在底部抽屉内显示商家摘要，不再创建遮罩或居中弹窗。
const showShopDrawer = (shopInfo) => {
  selectedId.value = shopInfo.id
  currentShop.value = shopInfo
}

const openShopDrawer = (shopInfo) => {
  zoomToShop(shopInfo)
  showShopDrawer(shopInfo)
}

const previewShopImage = () => {
  const url = currentShop.value && currentShop.value.coverImageUrl
  if (!url) return
  uni.previewImage({ current: url, urls: [url] })
}

const gotoDetail = () => {
  uni.navigateTo({ url: `/pages/shop/detail?shopId=${currentShop.value.id}` })
}

const gotoCreate = () => {
  uni.navigateTo({ url: `/pages/order/create?businessId=${currentShop.value.id}` })
}

const gotoOrders = () => {
  uni.navigateTo({ url: '/pages/order/list' })
}
</script>

<style scoped>
.page-wrap {
  width: 100%;
  height: 100vh;
  position: relative;
  background: #f8f8f8;
}
.map-container {
  width: 100%;
  height: 100%;
}

/* ==========首次加载遮罩========== */
.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(248, 249, 250, 0.65);
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

/* ==========地图左上角图例========== */
.map-legend {
  position: absolute;
  left: 20rpx;
  top: 20rpx;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}
.legend-chip {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 18rpx;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 24rpx;
  font-size: 22rpx;
  color: #555;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}
.legend-dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 4rpx;
  background: #ff4d4f;
  margin-right: 8rpx;
}
/* ==========我的预约入口========== */
.my-orders-btn {
  position: absolute;
  right: 20rpx;
  top: 20rpx;
  z-index: 10;
  padding: 14rpx 28rpx;
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
  font-size: 26rpx;
  font-weight: 500;
  border-radius: 32rpx;
  box-shadow: 0 6rpx 16rpx rgba(22, 119, 255, 0.35);
  transition: opacity 0.2s;
}
.my-orders-btn-hover {
  opacity: 0.85;
}

/* ==========底部面板========== */
.bottom-panel {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 32rpx 32rpx 0 0;
  padding: 12rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -12rpx 44rpx rgba(24, 39, 64, 0.16);
  transition: height 0.25s ease;
}
.bottom-panel.detail-mode {
  height: 72vh;
  padding-bottom: calc(18rpx + env(safe-area-inset-bottom));
}
.drawer-handle {
  width: 72rpx;
  height: 8rpx;
  margin: 0 auto 16rpx;
  border-radius: 8rpx;
  background: #d7dce4;
}

/* 分类大按钮 */
.cat-btns {
  display: flex;
  gap: 20rpx;
}
.cat-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 26rpx 0 22rpx;
  border-radius: 16rpx;
  background: #f4f6fa;
  border: 2rpx solid transparent;
}
.cat-btn.active {
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  border-color: #1677ff;
  box-shadow: 0 8rpx 20rpx rgba(22, 119, 255, 0.35);
}
.cat-emoji {
  font-size: 44rpx;
  line-height: 1;
}
.cat-label {
  margin-top: 10rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}
.cat-btn.active .cat-label {
  color: #fff;
}
.cat-count {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #999;
}
.cat-btn.active .cat-count {
  color: rgba(255, 255, 255, 0.85);
}

/* 选中分类后的列表 */
.cat-list-block {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}
.list-head {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}
.list-title {
  font-size: 28rpx;
  font-weight: 600;
}
.list-sub {
  font-size: 24rpx;
  color: #999;
  margin-left: 12rpx;
}
.list-close {
  margin-left: auto;
  font-size: 24rpx;
  color: #1677ff;
  padding: 6rpx 10rpx;
}
.cat-list-scroll {
  width: 100%;
  padding-bottom: 4rpx;
}
.list-empty {
  padding: 40rpx 0 20rpx;
  text-align: center;
}
.list-empty-text {
  font-size: 24rpx;
  color: #999;
}
.cat-list-wrap {
  display: flex;
  gap: 20rpx;
}
.shop-card {
  flex-shrink: 0;
  width: 300rpx;
  background: #fff;
  border: 2rpx solid #e8ecf2;
  border-top: 6rpx solid #1677ff;
  border-radius: 16rpx;
  padding: 20rpx;
  transition: all 0.2s;
}
.shop-card.type-hotel {
  border-top-color: #ff9a2e;
}
.shop-card.type-food {
  border-top-color: #22c55e;
}
.shop-card-hover {
  opacity: 0.88;
  border-color: #bcd6ff;
}
.shop-card.selected {
  border-color: #1677ff;
  border-top-color: #1677ff;
  background: #f0f6ff;
  box-shadow: 0 6rpx 16rpx rgba(22, 119, 255, 0.2);
}
.card-tag {
  font-size: 22rpx;
  margin-bottom: 10rpx;
  display: inline-flex;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.tag-travel {
  color: #1677ff;
  background: #e8f3ff;
}
.tag-hotel {
  color: #d46b08;
  background: #fff3e0;
}
.tag-food {
  color: #16a34a;
  background: #e8f8ec;
}
.shop-card.selected .card-tag {
  color: #1677ff;
}
.card-img {
  width: 100%;
  height: 160rpx;
  border-radius: 10rpx;
  background: #f3f5f8;
  margin-bottom: 10rpx;
}
.card-img-ph {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4fb 0%, #e4ebf5 100%);
}
.card-img-emoji {
  font-size: 64rpx;
  opacity: 0.7;
}
.card-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2329;
  white-space: normal;
  line-height: 1.4;
}
.card-addr {
  font-size: 22rpx;
  color: #888;
  margin-top: 8rpx;
  white-space: normal;
}
.card-intro {
  font-size: 22rpx;
  color: #666;
  margin-top: 10rpx;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

/* ==========商家详情抽屉========== */
.drawer-detail {
  height: calc(100% - 24rpx);
  display: flex;
  flex-direction: column;
}
.drawer-detail-head {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  min-height: 64rpx;
  margin-bottom: 12rpx;
}
.drawer-back,
.drawer-close {
  font-size: 25rpx;
  color: #1677ff;
  padding: 12rpx 4rpx;
}
.drawer-close {
  text-align: right;
}
.drawer-action-hover {
  opacity: 0.6;
}
.drawer-title {
  font-size: 29rpx;
  font-weight: 600;
  color: #1f2937;
}
.drawer-detail-scroll {
  flex: 1;
  min-height: 0;
}
.drawer-image-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 20rpx;
  background: #f4f6f9;
}
.drawer-image {
  width: 100%;
  height: 360rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f6f9;
}
.drawer-image-ph {
  border-radius: 20rpx;
  background: linear-gradient(135deg, #f0f4fb 0%, #e4ebf5 100%);
}
.drawer-image-emoji {
  font-size: 120rpx;
  opacity: 0.75;
}
.image-tip {
  position: absolute;
  right: 16rpx;
  bottom: 14rpx;
  padding: 7rpx 14rpx;
  border-radius: 20rpx;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  font-size: 21rpx;
}
.detail-summary {
  padding: 24rpx 4rpx 18rpx;
}
.detail-name-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}
.detail-name {
  flex: 1;
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2329;
  line-height: 1.35;
}
.detail-tag {
  flex-shrink: 0;
  margin-top: 4rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}
.detail-address {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #555;
  line-height: 1.6;
}
.detail-icon {
  margin-right: 10rpx;
}
.detail-intro {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #596273;
  line-height: 1.7;
  background: #f7f8fa;
  border-radius: 16rpx;
  padding: 20rpx;
}
.drawer-actions {
  display: flex;
  gap: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #edf0f4;
}
.primary-btn {
  flex: 1.35;
  margin: 0;
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
  font-weight: 500;
  border-radius: 14rpx;
}
.secondary-btn {
  flex: 1;
  margin: 0;
  background: #eef5ff;
  color: #1677ff;
  border-radius: 14rpx;
}
</style>
