<template>
  <view class="page-wrap">
    <!-- 地图组件：中心点/缩放随所选分类自适应，红框为服务区域 -->
    <map
      id="tripMap"
      class="map-container"
      :longitude="viewCenter.longitude"
      :latitude="viewCenter.latitude"
      :scale="viewScale"
      :markers="markerList"
      :polygons="polygonsList"
      :enable-poi="false"
      @markertap="onMarkerTap"
    ></map>

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
          <text class="cat-emoji">{{ tab.emoji }}</text>
          <text class="cat-label">{{ tab.label }}</text>
          <text class="cat-count">{{ countOf(tab.type) }}家</text>
        </view>
      </view>

      <!-- 选中分类后展示该类商家列表 -->
      <view v-if="activeType" class="cat-list-block">
        <view class="list-head">
          <text class="list-title">{{ activeLabel }}</text>
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
              :class="{ selected: item.id === selectedId }"
              @click="openShopPopup(item)"
            >
              <view class="card-tag">{{ item.typeLabel }}</view>
              <view class="card-name">{{ item.name }}</view>
              <view class="card-addr">{{ item.address }}</view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 商家详情弹窗 -->
    <view v-if="popupShow" class="mask" @click="closePopup">
      <view class="popup" @click.stop>
        <view class="popup-title">商家详情</view>
        <view class="popup-content" v-if="currentShop">
          <view class="row"><text class="label">商家名称：</text><text>{{ currentShop.name }}</text></view>
          <view class="row"><text class="label">服务类别：</text><text>{{ currentShop.typeLabel }}</text></view>
          <view class="row"><text class="label">地址：</text><text>{{ currentShop.address }}</text></view>

          <!-- 🚗出行：服务商介绍，无其他子项 -->
          <view v-if="currentShop.type === 'travel'" class="desc-block">
            <view class="label">服务商介绍：</view>
            <view class="desc-text">{{ currentShop.intro }}</view>
          </view>

          <!-- 🏨酒店：多房型展示 -->
          <view v-if="currentShop.type === 'hotel'" class="desc-block">
            <view class="label">房型列表：</view>
            <view class="item-card" v-for="(room,idx) in currentShop.roomList" :key="idx">
              <text>{{ room.roomName }}</text>
              <text class="price">{{ room.price }}</text>
            </view>
          </view>

          <!-- 🍜餐饮：菜系标签 + 人均 -->
          <view v-if="currentShop.type === 'food'" class="desc-block">
            <view class="row">
              <text class="label">人均消费：</text>
              <text class="price">{{ currentShop.avgPrice }}</text>
            </view>
            <view class="label">菜系分类：</view>
            <view class="tag-wrap">
              <text class="cuisine-tag" v-for="tag in currentShop.cuisineList" :key="tag">{{tag}}</text>
            </view>
          </view>

          <view class="row">
            <text class="label">备注简介：</text>
            <text>{{ currentShop.desc }}</text>
          </view>
        </view>

        <view class="popup-btn-wrap">
          <button class="primary-btn" @click="gotoDetail">前往预约下单</button>
          <button class="cancel-btn" @click="closePopup">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, getCurrentInstance } from 'vue'

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

// ==========武汉大学主校区（文理学部+工学部+信息学部）红色区域==========
const polygonsList = ref([
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
])

// ============商家模拟数据，iconPath只写文件名============
const shopAllList = ref([
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
    roomList:[
      {roomName:"商务大床房",price:"260元/晚"},
      {roomName:"商务双床房",price:"280元/晚"},
      {roomName:"行政套房",price:"420元/晚"}
    ],
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
    roomList:[
      {roomName:"豪华大床房",price:"320元/晚"},
      {roomName:"豪华双床房",price:"340元/晚"},
      {roomName:"总裁套房",price:"580元/晚"}
    ],
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
    avgPrice:"80‑120元/人",
    cuisineList:["湖北菜","家常菜","商务简餐","团建桌餐"],
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
    avgPrice:"130‑180元/人",
    cuisineList:["楚菜","湘菜","粤式茶点","商务宴请"],
    desc:"适合企业客户正式商务宴请",
    iconPath: "map-green.png"
  }
])

// ============按分类过滤后的商家============
const filteredShopList = computed(() => {
  if (!activeType.value) return shopAllList.value
  return shopAllList.value.filter(s => s.type === activeType.value)
})

const countOf = (type) => shopAllList.value.filter(s => s.type === type).length

const activeLabel = computed(() =>
  catTabs.find(t => t.type === activeType.value)?.label || ''
)

// ============地图：点击点位 -> 该点居中且放大2级（不依赖 getScale 回调，保证在各端生效）============
const zoomToShop = (shop) => {
  viewCenter.value = { longitude: shop.longitude, latitude: shop.latitude } // 该点居中
  viewScale.value = Math.min(viewScale.value + 2, 18)                        // 放大一定比例
}

// ============关闭弹窗：恢复初始视野，放大和居中效果消失============
const restoreMapView = () => {
  if (viewScale.value === 13) return
  viewScale.value = 13
  viewCenter.value = { longitude: 114.362, latitude: 30.5385 }
}

const closePopup = () => {
  popupShow.value = false
  selectedId.value = null      // 取消点位高亮
  restoreMapView()
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
  popupTimer = setTimeout(() => openShopPopup(shop), 450)   // 让用户先看到地图变化
}

const openShopPopup = (shopInfo) => {
  selectedId.value = shopInfo.id
  currentShop.value = shopInfo
  popupShow.value = true
}

const gotoDetail = () => {
  popupShow.value = false
  uni.navigateTo({ url: `/pages/shop/detail?shopId=${currentShop.value.id}` })
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

/* ==========底部面板========== */
.bottom-panel {
  position: absolute;
  left: 20rpx;
  right: 20rpx;
  bottom: 40rpx;
  z-index: 9;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
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
  transition: all 0.2s;
}
.cat-btn.active {
  background: #1677ff;
  border-color: #1677ff;
  box-shadow: 0 6rpx 16rpx rgba(22, 119, 255, 0.35);
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
}
.cat-list-scroll {
  width: 100%;
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
  border-radius: 16rpx;
  padding: 20rpx;
}
.shop-card.selected {
  border-color: #1677ff;
  background: #f0f6ff;
}
.card-tag {
  font-size: 22rpx;
  color: #1677ff;
  margin-bottom: 8rpx;
}
.shop-card.selected .card-tag {
  color: #1677ff;
}
.card-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  white-space: normal;
  line-height: 1.4;
}
.card-addr {
  font-size: 22rpx;
  color: #888;
  margin-top: 8rpx;
  white-space: normal;
}

/* ==========弹窗========== */
.mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background: rgba(0,0,0,0.5);
  z-index:99;
  display:flex;
  align-items:center;
  justify-content:center;
}
.popup{
  width: 620rpx;
  max-height: 80vh;
  overflow-y: auto;
  background:#fff;
  border-radius:20rpx;
  padding:30rpx;
}
.popup-title{
  text-align:center;
  font-size:32rpx;
  font-weight:bold;
  margin-bottom:20rpx;
}
.row {
  margin-bottom: 16rpx;
  font-size: 28rpx;
}
.label {
  color: #666;
}
.desc-block{
  margin:20rpx 0;
}
.desc-text{
  margin-top:8rpx;
  color:#333;
  line-height:1.7;
}
/*酒店房型*/
.item-card{
  display:flex;
  justify-content:space-between;
  padding:14rpx 16rpx;
  background:#f7f8fa;
  border-radius:10rpx;
  margin-top:10rpx;
  font-size:26rpx;
}
.price{
  color:#f56c6c;
}
/*餐饮菜系标签*/
.tag-wrap{
  display:flex;
  gap:12rpx;
  flex-wrap:wrap;
  margin-top:10rpx;
}
.cuisine-tag{
  padding:6rpx 14rpx;
  background:#e8f3ff;
  color:#1677ff;
  border-radius:8rpx;
  font-size:24rpx;
}

.popup-btn-wrap{
  margin-top:30rpx;
  display:flex;
  gap:20rpx;
}
.primary-btn{
  flex:1;
  background:#1677ff;
  color:#fff;
}
.cancel-btn{
  flex:1;
  background:#eee;
}
</style>
