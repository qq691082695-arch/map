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

    <!-- 地图左上角：图例 / 演示模式提示 -->
    <view class="map-legend">
      <view class="legend-chip">
        <view class="legend-dot"></view>
        <text>高校服务区域</text>
      </view>
      <view v-if="loadFailed" class="legend-chip legend-demo">演示数据</view>
    </view>

    <!-- 我的预约入口 -->
    <view class="my-orders-btn" hover-class="my-orders-btn-hover" @click="gotoOrders">📋 我的预约</view>

    <!-- 底部大分类按钮 + 点击某类后出现的列表 -->
    <view class="bottom-panel">
      <!-- 分类大按钮 -->
      <view class="cat-btns">
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
      <view v-if="activeType" class="cat-list-block">
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
              @click="openShopPopup(item)"
            >
              <view class="card-tag" :class="'tag-' + item.type">{{ item.typeLabel }}</view>
              <image v-if="item.coverImageUrl" class="card-img" :src="item.coverImageUrl" mode="aspectFill" />
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
    </view>

    <!-- 商家详情弹窗 -->
    <view v-if="popupShow" class="mask" @click="closePopup">
      <view class="popup" @click.stop>
        <image v-if="currentShop && currentShop.coverImageUrl" class="popup-banner" :src="currentShop.coverImageUrl" mode="aspectFill" />
        <view v-else-if="currentShop" class="popup-banner popup-banner-ph">
          <text class="popup-banner-emoji">{{ currentShop.emoji }}</text>
        </view>
        <view class="popup-head" v-if="currentShop">
          <view class="popup-name">{{ currentShop.name }}</view>
          <view class="popup-tag" :class="'tag-' + currentShop.type">{{ currentShop.typeLabel }}</view>
        </view>
        <view class="popup-body" v-if="currentShop">
          <view class="popup-row">
            <text class="popup-row-icon">📍</text>
            <text class="popup-addr">{{ currentShop.address }}</text>
          </view>
          <view class="popup-desc">{{ currentShop.intro }}</view>
        </view>

        <view class="popup-btn-wrap">
          <button class="primary-btn" hover-class="btn-hover" @click="gotoDetail">前往预约下单</button>
          <button class="cancel-btn" hover-class="btn-hover" @click="closePopup">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, getCurrentInstance } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMapOverview } from '../../api/app'
import { SERVICE_TYPE_MAP } from '../../common/config'
import { resolveImg } from '../../common/util'

const { proxy } = getCurrentInstance()

const popupShow = ref(false)
const currentShop = ref(null)
const activeType = ref('')          // '' 表示未选分类，显示全部商家
const selectedId = ref(null)        // 地图/列表中当前选中的商家
const scrollIntoView = ref('')      // 横向列表自动滚动定位

let popupTimer = null               // 弹窗延迟定时器

// 初始视野：聚焦武汉大学主校区（点选点位时中心切换到该点）
const viewCenter = ref({ longitude: 114.362, latitude: 30.5385 })
const viewScale = ref(13)

// ==========三个分类配置==========
const catTabs = [
  { type: 'travel', label: '出行', emoji: '🚗' },
  { type: 'hotel',  label: '住宿', emoji: '🏨' },
  { type: 'food',   label: '饮食', emoji: '🍜' }
]

// ==========武汉大学主校区（文理学部+工学部+信息学部）红色区域：接口不可达时的演示兜底==========
const DEMO_POLYGONS = [
  {
    points: [
      { latitude: 30.5262, longitude: 114.3560 }, // 信息学部南侧(珞喻路)
      { latitude: 30.5295, longitude: 114.3660 }, // 东湖南路内侧
      { latitude: 30.5335, longitude: 114.3725 }, // 东南近东湖
      { latitude: 30.5385, longitude: 114.3745 }, // 东侧文澜门/东湖滨
      { latitude: 30.5425, longitude: 114.3710 }, // 东北工学部/东湖水岸
      { latitude: 30.5430, longitude: 114.3630 }, // 北界八一路
      { latitude: 30.5390, longitude: 114.3555 }, // 正门/八一路
      { latitude: 30.5350, longitude: 114.3520 }, // 武大西门
      { latitude: 30.5300, longitude: 114.3508 }, // 西界珞狮路
      { latitude: 30.5262, longitude: 114.3560 }  // 闭合
    ],
    strokeWidth: 2,
    strokeColor: "#ff0000",
    fillColor: "#ff000033"
  }
]

// ==========真实地图数据（GET /api/v1/app/map-overview）==========
const uniList = ref([])          // 启用未删除的高校区域
const bizList = ref([])          // 启用未删除的商家点位
const loadFailed = ref(false)    // 接口不可达时使用演示数据兜底
const loading = ref(true)        // 首次加载中

// 高校区域生成地图多边形（polygonPoints 结构同后端 GeoPoint）
const polygonsList = computed(() => {
  if (loadFailed.value) return DEMO_POLYGONS
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
  if (loadFailed.value) {
    // 演示兜底：为演示区域标注名称
    return [buildUniMarker({ id: 0, name: '武汉大学（主校区）' }, { longitude: 114.362, latitude: 30.5385 })]
  }
  return uniList.value.map(u => {
    const center = polygonCenter(u.polygonPoints)
    if (!center) return null
    return buildUniMarker(u, center)
  }).filter(Boolean)
})

// 地图总 markers：高校名称标注 + 商家点位
const allMarkers = computed(() => [...uniNameMarkers.value, ...markerList.value])

// ============商家模拟数据，iconPath只写文件名（接口不可达时的演示兜底）============
const DEMO_SHOPS = [
  {
    id: 1,
    name: "商务接送车队",
    type: "travel",
    typeLabel: "🚗出行",
    longitude: 114.322,
    latitude: 30.601,
    address: "武汉市江汉区商务区",
    intro:"本公司专注武汉全域商务接送，提供7‑55座车辆，支持出差接机、会务接送，专职司机。",
    desc:"可提供企业长期合作包车服务",
    iconPath: "map-blue.png"
  },
  {
    id: 2,
    name: "楚河汉街商务快捷酒店",
    type: "hotel",
    typeLabel: "🏨住宿",
    longitude: 114.305,
    latitude: 30.582,
    address: "武昌楚河汉街附近",
    intro:"靠近地铁，适合出差商务入住",
    desc:"靠近地铁，适合出差商务入住",
    iconPath: "map-red.png"
  },
  {
    id: 3,
    name: "洪山广场铂悦酒店",
    type: "hotel",
    typeLabel: "🏨住宿",
    longitude: 114.335,
    latitude: 30.596,
    address: "洪山广场地铁站旁",
    intro:"高档商务酒店，配套会议室",
    desc:"高档商务酒店，配套会议室",
    iconPath: "map-red.png"
  },
  {
    id: 4,
    name: "洪山商务简餐酒楼",
    type: "food",
    typeLabel: "🍜饮食",
    longitude: 114.331,
    latitude: 30.593,
    address: "洪山广场周边",
    intro:"支持出差团体简餐、小型商务接待",
    desc:"支持出差团体简餐、小型商务接待",
    iconPath: "map-green.png"
  },
  {
    id: 5,
    name: "楚宴融合菜馆",
    type: "food",
    typeLabel: "🍜饮食",
    longitude: 114.298,
    latitude: 30.588,
    address: "水果湖商圈",
    intro:"适合企业客户正式商务宴请",
    desc:"适合企业客户正式商务宴请",
    iconPath: "map-green.png"
  }
]

// ============商家归一化：真实数据（businessType 大写）与演示数据统一结构============
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
      loadFailed.value = false
      uniList.value = (data && data.universities) || []
      bizList.value = (data && data.businesses) || []
    })
    .catch((e) => {
      if (e.statusCode === 0) {
        loadFailed.value = true
      } else {
        uni.showToast({ title: e.message || '地图数据加载失败', icon: 'none' })
      }
    })
    .finally(() => {
      loading.value = false
    })
}

onLoad(() => loadData())

const shopAllList = computed(() =>
  (loadFailed.value ? DEMO_SHOPS : bizList.value).map(normalizeShop)
)

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

// ============关闭弹窗：取消点位高亮，地图保持在当前视野不跳回============
const closePopup = () => {
  popupShow.value = false
  selectedId.value = null      // 取消点位高亮
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
    selectedId.value = null
    scrollIntoView.value = ''
    return
  }
  activeType.value = type
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
  zoomToShop(shop)                                   // 先放大并让该点居中
  nextTick(() => { scrollIntoView.value = 'card-' + shop.id })
  clearTimeout(popupTimer)
  popupTimer = setTimeout(() => showShopPopup(shop), 450)   // 让用户先看到地图变化
}

// 仅打开弹窗（地图点位点击复用，避免重复缩放）
const showShopPopup = (shopInfo) => {
  selectedId.value = shopInfo.id
  currentShop.value = shopInfo
  popupShow.value = true
}

// 列表卡片点击：与点击地图点位行为一致，先居中该商家再打开弹窗
const openShopPopup = (shopInfo) => {
  zoomToShop(shopInfo)
  showShopPopup(shopInfo)
}

const gotoDetail = () => {
  popupShow.value = false
  uni.navigateTo({ url: `/pages/shop/detail?shopId=${currentShop.value.id}` })
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
.legend-demo {
  color: #d46b08;
  background: rgba(255, 247, 230, 0.95);
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
  left: 20rpx;
  right: 20rpx;
  bottom: 40rpx;
  z-index: 9;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.14);
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
  background: #eceef2;
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

/* ==========弹窗========== */
.mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background: rgba(0, 0, 0, 0.55);
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: center;
}
.popup {
  width: 620rpx;
  max-height: 80vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 24rpx;
  padding-bottom: 30rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.25);
}
.popup-banner {
  width: 100%;
  height: 300rpx;
  border-radius: 24rpx 24rpx 0 0;
  background: #f0f2f5;
}
.popup-banner-ph {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4fb 0%, #e4ebf5 100%);
}
.popup-banner-emoji {
  font-size: 120rpx;
  opacity: 0.75;
}
.popup-head {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx 0;
}
.popup-name {
  flex: 1;
  min-width: 0;
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2329;
}
.popup-tag {
  flex-shrink: 0;
  margin-left: 16rpx;
  font-size: 24rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.popup-body {
  padding: 16rpx 30rpx 0;
}
.popup-row {
  display: flex;
  align-items: flex-start;
  font-size: 26rpx;
  color: #555;
  line-height: 1.6;
}
.popup-row-icon {
  flex-shrink: 0;
  margin-right: 8rpx;
}
.popup-addr {
  flex: 1;
  word-break: break-all;
}
.popup-desc {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.7;
  background: #f7f8fa;
  border-radius: 12rpx;
  padding: 16rpx 18rpx;
}
.popup-btn-wrap {
  margin: 30rpx 30rpx 0;
  display: flex;
  gap: 20rpx;
}
.primary-btn {
  flex: 1;
  background: linear-gradient(135deg, #4a9dff 0%, #1677ff 100%);
  color: #fff;
  font-weight: 500;
}
.cancel-btn {
  flex: 1;
  background: #f0f2f5;
  color: #555;
}
</style>
