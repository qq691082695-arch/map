<template>
  <view class="detail-wrap">
    <view v-if="shop" class="detail-card">
      <view class="head">
        <text class="name">{{ shop.name }}</text>
        <text class="tag">{{ shop.typeLabel }}</text>
      </view>
      <view class="row"><text class="label">服务类别：</text><text>{{ shop.typeLabel }}</text></view>
      <view class="row"><text class="label">地址：</text><text>{{ shop.address }}</text></view>

      <!-- 出行 -->
      <view v-if="shop.type === 'travel'" class="block">
        <view class="label">服务商介绍：</view>
        <view class="text">{{ shop.intro }}</view>
      </view>

      <!-- 酒店房型 -->
      <view v-if="shop.type === 'hotel'" class="block">
        <view class="label">房型列表：</view>
        <view class="item-card" v-for="(room,idx) in shop.roomList" :key="idx">
          <text>{{ room.roomName }}</text>
          <text class="price">{{ room.price }}</text>
        </view>
      </view>

      <!-- 餐饮 -->
      <view v-if="shop.type === 'food'" class="block">
        <view class="row">
          <text class="label">人均消费：</text>
          <text class="price">{{ shop.avgPrice }}</text>
        </view>
        <view class="label">菜系分类：</view>
        <view class="tag-wrap">
          <text class="cuisine-tag" v-for="tag in shop.cuisineList" :key="tag">{{tag}}</text>
        </view>
      </view>

      <view class="row">
        <text class="label">备注简介：</text>
        <text>{{ shop.desc }}</text>
      </view>

      <button class="primary-btn" @click="submitOrder">立即预约下单</button>
    </view>
    <view v-else class="empty">商家不存在或已下架</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const shopId = ref(null)
const shop = ref(null)

const shopAllList = [
  {
    id: 1, name: "商务接送车队", type: "travel", typeLabel: "🚗出行",
    address: "武汉市江汉区商务区",
    intro: "本公司专注武汉全域商务接送，提供7‑55座车辆，支持出差接机、会务接送，专职司机。",
    desc: "可提供企业长期合作包车服务"
  },
  {
    id: 2, name: "楚河汉街商务快捷酒店", type: "hotel", typeLabel: "🏨住宿",
    address: "武昌楚河汉街附近",
    roomList: [
      { roomName: "商务大床房", price: "260元/晚" },
      { roomName: "商务双床房", price: "280元/晚" },
      { roomName: "行政套房", price: "420元/晚" }
    ],
    desc: "靠近地铁，适合出差商务入住"
  },
  {
    id: 3, name: "洪山广场铂悦酒店", type: "hotel", typeLabel: "🏨住宿",
    address: "洪山广场地铁站旁",
    roomList: [
      { roomName: "豪华大床房", price: "320元/晚" },
      { roomName: "豪华双床房", price: "340元/晚" },
      { roomName: "总裁套房", price: "580元/晚" }
    ],
    desc: "高档商务酒店，配套会议室"
  },
  {
    id: 4, name: "洪山商务简餐酒楼", type: "food", typeLabel: "🍜饮食",
    address: "洪山广场周边",
    avgPrice: "80‑120元/人",
    cuisineList: ["湖北菜", "家常菜", "商务简餐", "团建桌餐"],
    desc: "支持出差团体简餐、小型商务接待"
  },
  {
    id: 5, name: "楚宴融合菜馆", type: "food", typeLabel: "🍜饮食",
    address: "水果湖商圈",
    avgPrice: "130‑180元/人",
    cuisineList: ["楚菜", "湘菜", "粤式茶点", "商务宴请"],
    desc: "适合企业客户正式商务宴请"
  }
]

onLoad((options) => {
  shopId.value = Number(options.shopId)
  shop.value = shopAllList.find(s => s.id === shopId.value) || null
})

const submitOrder = () => {
  uni.showToast({ title: '预约成功，商家将尽快与您联系', icon: 'none' })
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
}
.label {
  color: #666;
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
  justify-content: space-between;
  padding: 14rpx 16rpx;
  background: #f7f8fa;
  border-radius: 10rpx;
  margin-top: 10rpx;
  font-size: 26rpx;
}
.price {
  color: #f56c6c;
}
.tag-wrap {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-top: 10rpx;
}
.cuisine-tag {
  padding: 6rpx 14rpx;
  background: #e8f3ff;
  color: #1677ff;
  border-radius: 8rpx;
  font-size: 24rpx;
}
.primary-btn {
  margin-top: 40rpx;
  background: #1677ff;
  color: #fff;
}
.empty {
  text-align: center;
  color: #999;
  margin-top: 200rpx;
}
</style>
