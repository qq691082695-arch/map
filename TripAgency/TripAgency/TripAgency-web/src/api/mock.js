// ============================================================
// 假数据层：内存数据库 + 模拟接口（带延迟、带分页/搜索）
// 后续接入真实后端时，仅需按同签名替换 src/api/index.js 即可
// ============================================================

const delay = (ms = 300) => new Promise((r) => setTimeout(r, ms))

// 固定写死账号
const ACCOUNT = { username: 'admin', password: '123456', nickname: '系统管理员', role: '超级管理员' }

// ---------------- 商家数据（出行/住宿/饮食 三大类） ----------------
// travel 商家带 items(车型)，hotel 商家带 items(房型，含床型/面积/含早等字段)，所有商家可上传 images(展示图)
// 每个车型/房型也可上传 images 展示
const catTypeMap = {
  travel: '出行',
  hotel: '住宿',
  food: '饮食'
}

let shopDb = [
  {
    id: 1,
    name: '商务接送车队',
    type: 'travel',
    typeLabel: '出行',
    contact: '张经理',
    phone: '13800000001',
    address: '武汉市江汉区商务区',
    lng: 114.2696,
    lat: 30.5958,
    status: 1,
    intro: '本公司专注武汉全域商务接送，提供7-55座车辆，支持出差接机、会务接送，专职司机。',
    desc: '可提供企业长期合作包车服务',
    images: [],
    items: [
      { id: 1, name: '商务轿车', price: 500, unit: '元/天', remark: '5座，含专职司机', images: [] },
      { id: 2, name: '商务GL8', price: 800, unit: '元/天', remark: '7座，适合团队出行', images: [] },
      { id: 3, name: '中巴19座', price: 1200, unit: '元/天', remark: '含司机，适合会务接送', images: [] },
      { id: 4, name: '大巴55座', price: 1800, unit: '元/天', remark: '企业团建、大型会务', images: [] }
    ],
    createTime: '2025-06-01 10:20:00'
  },
  {
    id: 2,
    name: '楚河汉街商务快捷酒店',
    type: 'hotel',
    typeLabel: '住宿',
    contact: '李店长',
    phone: '13800000002',
    address: '武昌楚河汉街附近',
    lng: 114.318,
    lat: 30.5498,
    status: 1,
    intro: '靠近地铁，适合出差商务入住',
    desc: '商务大床房260元/晚，商务双床房280元/晚，行政套房420元/晚',
    images: [],
    items: [
      { id: 1, name: '商务大床房', price: 260, unit: '元/晚', bedSpec: '1.8m大床 x1', bedCount: 1, area: 28, breakfast: true, remark: '近地铁，出行方便', images: [] },
      { id: 2, name: '商务双床房', price: 280, unit: '元/晚', bedSpec: '1.2m单人床 x2', bedCount: 2, area: 30, breakfast: true, remark: '适合两人同住', images: [] },
      { id: 3, name: '行政套房', price: 420, unit: '元/晚', bedSpec: '1.8m大床 x1', bedCount: 1, area: 45, breakfast: true, remark: '含下午茶', images: [] }
    ],
    createTime: '2025-06-02 09:10:00'
  },
  {
    id: 3,
    name: '洪山广场铂悦酒店',
    type: 'hotel',
    typeLabel: '住宿',
    contact: '王店长',
    phone: '13800000003',
    address: '洪山广场地铁站旁',
    lng: 114.353,
    lat: 30.5299,
    status: 1,
    intro: '高档商务酒店，配套会议室',
    desc: '豪华大床房320元/晚，豪华双床房340元/晚，总裁套房580元/晚',
    images: [],
    items: [
      { id: 1, name: '豪华大床房', price: 320, unit: '元/晚', bedSpec: '2m大床 x1', bedCount: 1, area: 35, breakfast: true, remark: '高层景观房', images: [] },
      { id: 2, name: '豪华双床房', price: 340, unit: '元/晚', bedSpec: '1.2m单人床 x2', bedCount: 2, area: 36, breakfast: true, remark: '双人入住', images: [] },
      { id: 3, name: '总裁套房', price: 580, unit: '元/晚', bedSpec: '2m大床 x1', bedCount: 1, area: 68, breakfast: true, remark: '含会客区、浴缸', images: [] }
    ],
    createTime: '2025-06-03 14:30:00'
  },
  {
    id: 4,
    name: '洪山商务简餐酒楼',
    type: 'food',
    typeLabel: '饮食',
    contact: '赵老板',
    phone: '13800000004',
    address: '洪山广场周边',
    lng: 114.351,
    lat: 30.532,
    status: 1,
    intro: '支持出差团体简餐、小型商务接待',
    desc: '人均80-120元，菜系：湖北菜、家常菜、商务简餐、团建桌餐',
    images: [],
    items: [],
    createTime: '2025-06-05 11:45:00'
  },
  {
    id: 5,
    name: '楚宴融合菜馆',
    type: 'food',
    typeLabel: '饮食',
    contact: '孙老板',
    phone: '13800000005',
    address: '水果湖商圈',
    lng: 114.3625,
    lat: 30.5545,
    status: 1,
    intro: '适合企业客户正式商务宴请',
    desc: '人均130-180元，菜系：楚菜、湘菜、粤式茶点、商务宴请',
    images: [],
    items: [],
    createTime: '2025-06-06 16:20:00'
  },
  {
    id: 6,
    name: '光谷国际商贸酒店',
    type: 'hotel',
    typeLabel: '住宿',
    contact: '周店长',
    phone: '13800000006',
    address: '光谷广场东街',
    lng: 114.3986,
    lat: 30.507,
    status: 1,
    intro: '国际连锁酒店，服务标准高',
    desc: '商务房398元/晚，行政房558元/晚，套房888元/晚',
    images: [],
    items: [
      { id: 1, name: '商务房', price: 398, unit: '元/晚', bedSpec: '1.8m大床 x1', bedCount: 1, area: 32, breakfast: true, remark: '含自助早餐', images: [] },
      { id: 2, name: '行政房', price: 558, unit: '元/晚', bedSpec: '1.8m大床 x1', bedCount: 1, area: 42, breakfast: true, remark: '含行政酒廊', images: [] },
      { id: 3, name: '套房', price: 888, unit: '元/晚', bedSpec: '1.8m大床 x1', bedCount: 1, area: 75, breakfast: true, remark: '含会客区、浴缸', images: [] }
    ],
    createTime: '2025-06-08 08:40:00'
  },
  {
    id: 7,
    name: '武大旁茶餐厅',
    type: 'food',
    typeLabel: '饮食',
    contact: '吴老板',
    phone: '13800000007',
    address: '八一路街道',
    lng: 114.366,
    lat: 30.539,
    status: 0,
    intro: '快捷茶餐厅，适合学生及商务简餐',
    desc: '人均40-80元，主打粤式茶点',
    images: [],
    items: [],
    createTime: '2025-06-10 12:00:00'
  },
  {
    id: 8,
    name: '远航商务租车',
    type: 'travel',
    typeLabel: '出行',
    contact: '郑经理',
    phone: '13800000008',
    address: '天河机场T3航站楼服务点',
    lng: 114.216,
    lat: 30.774,
    status: 1,
    intro: '机场接送、会议车队一站式服务',
    desc: '7座/19座/45座可选，提供接送机优惠套餐',
    images: [],
    items: [
      { id: 1, name: '5座轿车', price: 300, unit: '元/次', remark: '机场接送', images: [] },
      { id: 2, name: '7座商务', price: 450, unit: '元/次', remark: '机场接送、商务出行', images: [] },
      { id: 3, name: '19座中巴', price: 1100, unit: '元/天', remark: '含司机', images: [] },
      { id: 4, name: '45座大巴', price: 1700, unit: '元/天', remark: '含司机', images: [] }
    ],
    createTime: '2025-06-12 15:10:00'
  }
]

// ---------------- 订单数据（openid 绑定下单人，无需小程序端登录） ----------------
// 状态流转：待确认(用户下单) -> 已确认(管理员确认) / 已取消(管理员取消，需填写原因，原因将反馈给客户)
let orderDb = [
  { id: 'OR20250615001', shopId: 2, shopName: '楚河汉街商务快捷酒店', openid: 'oXK9a5mFpQ2vYz8TbNw4', type: '住宿', content: '商务大床房 x1 / 1晚', amount: 260, status: '待确认', cancelReason: '', createTime: '2025-06-15 09:30:00' },
  { id: 'OR20250615002', shopId: 1, shopName: '商务接送车队', openid: 'oXK9a5jLnR7uHd3SqKc1', type: '出行', content: '高铁站接站商务车', amount: 180, status: '已确认', cancelReason: '', createTime: '2025-06-15 10:12:00' },
  { id: 'OR20250615003', shopId: 4, shopName: '洪山商务简餐酒楼', openid: 'oXK9a5zTqY6wBf0VxEm2', type: '饮食', content: '8人商务简餐', amount: 880, status: '已确认', cancelReason: '', createTime: '2025-06-15 11:00:00' },
  { id: 'OR20250615004', shopId: 5, shopName: '楚宴融合菜馆', openid: 'oXK9a5mFpQ2vYz8TbNw4', type: '饮食', content: '10人商务宴请桌', amount: 1600, status: '待确认', cancelReason: '', createTime: '2025-06-15 14:45:00' },
  { id: 'OR20250615005', shopId: 3, shopName: '洪山广场铂悦酒店', openid: 'oXK9a5hGrP8sDn4QwLb3', type: '住宿', content: '豪华双床房 x2 / 2晚', amount: 1360, status: '已确认', cancelReason: '', createTime: '2025-06-15 16:20:00' },
  { id: 'OR20250616001', shopId: 8, shopName: '远航商务租车', openid: 'oXK9a5cVwQ2xRt6KmNj5', type: '出行', content: '机场送机 7座别克', amount: 220, status: '已确认', cancelReason: '', createTime: '2025-06-16 08:05:00' },
  { id: 'OR20250616002', shopId: 6, shopName: '光谷国际商贸酒店', openid: 'oXK9a5kMgH3jTs7PbCd6', type: '住宿', content: '商务房 x3 / 1晚', amount: 1194, status: '待确认', cancelReason: '', createTime: '2025-06-16 10:40:00' },
  { id: 'OR20250616003', shopId: 2, shopName: '楚河汉街商务快捷酒店', openid: 'oXK9a5zTqY6wBf0VxEm2', type: '住宿', content: '行政套房 x1 / 3晚', amount: 1260, status: '已取消', cancelReason: '房型已订满，与用户协商后取消', createTime: '2025-06-16 13:15:00' },
  { id: 'OR20250616004', shopId: 1, shopName: '商务接送车队', openid: 'oXK9a5cVwQ2xRt6KmNj5', type: '出行', content: '会务用车 19座中巴 半天', amount: 900, status: '已确认', cancelReason: '', createTime: '2025-06-16 15:50:00' },
  { id: 'OR20250617001', shopId: 4, shopName: '洪山商务简餐酒楼', openid: 'oXK9a5jLnR7uHd3SqKc1', type: '饮食', content: '12人团建桌餐', amount: 1560, status: '已确认', cancelReason: '', createTime: '2025-06-17 09:25:00' },
  { id: 'OR20250617002', shopId: 5, shopName: '楚宴融合菜馆', openid: 'oXK9a5kMgH3jTs7PbCd6', type: '饮食', content: '6人商务接待', amount: 780, status: '已取消', cancelReason: '用户临时变更行程，主动取消', createTime: '2025-06-17 11:35:00' }
]

// ---------------- 高校数据（坐标区域为一组围成校园区域的经纬度点） ----------------
let universityDb = [
  {
    id: 1,
    name: '武汉大学',
    address: '武汉市武昌区八一路299号',
    contact: '刘老师',
    phone: '027-68750000',
    intro: '教育部直属重点综合性大学，国家“双一流”建设高校，坐拥珞珈山、东湖风光。',
    area: [
      { lng: 114.3505, lat: 30.5425 },
      { lng: 114.352, lat: 30.548 },
      { lng: 114.366, lat: 30.551 },
      { lng: 114.376, lat: 30.544 },
      { lng: 114.373, lat: 30.535 },
      { lng: 114.359, lat: 30.529 }
    ],
    show: true,
    createTime: '2025-06-10 09:00:00'
  },
  {
    id: 2,
    name: '华中科技大学',
    address: '武汉市洪山区珞喻路1037号',
    contact: '陈老师',
    phone: '027-87540000',
    intro: '教育部直属重点大学，国家“双一流”建设高校，被誉为“新中国高等教育发展的缩影”。',
    area: [
      { lng: 114.403, lat: 30.509 },
      { lng: 114.413, lat: 30.512 },
      { lng: 114.426, lat: 30.519 },
      { lng: 114.427, lat: 30.511 },
      { lng: 114.418, lat: 30.503 },
      { lng: 114.406, lat: 30.505 }
    ],
    show: true,
    createTime: '2025-06-10 09:30:00'
  },
  {
    id: 3,
    name: '华中师范大学',
    address: '武汉市洪山区珞喻路152号',
    contact: '王老师',
    phone: '027-67860000',
    intro: '教育部直属重点师范大学，国家“双一流”建设高校，桂子山上育英才。',
    area: [
      { lng: 114.342, lat: 30.52 },
      { lng: 114.35, lat: 30.528 },
      { lng: 114.358, lat: 30.526 },
      { lng: 114.36, lat: 30.517 },
      { lng: 114.349, lat: 30.515 }
    ],
    show: true,
    createTime: '2025-06-10 10:00:00'
  },
  {
    id: 4,
    name: '武汉理工大学',
    address: '武汉市洪山区珞狮路122号',
    contact: '李老师',
    phone: '027-87650000',
    intro: '教育部直属重点大学，国家“双一流”建设高校，建材建工、交通、汽车三大行业办学特色。',
    area: [
      { lng: 114.325, lat: 30.502 },
      { lng: 114.334, lat: 30.506 },
      { lng: 114.342, lat: 30.5 },
      { lng: 114.336, lat: 30.493 },
      { lng: 114.327, lat: 30.496 }
    ],
    show: true,
    createTime: '2025-06-10 10:30:00'
  },
  {
    id: 5,
    name: '中南财经政法大学',
    address: '武汉市东湖新技术开发区南湖大道182号',
    contact: '赵老师',
    phone: '027-88380000',
    intro: '教育部直属重点大学，财经政法类高校，学科实力突出。',
    area: [
      { lng: 114.365, lat: 30.47 },
      { lng: 114.375, lat: 30.478 },
      { lng: 114.383, lat: 30.473 },
      { lng: 114.379, lat: 30.465 },
      { lng: 114.369, lat: 30.467 }
    ],
    show: false,
    createTime: '2025-06-10 11:00:00'
  }
]

// ---------------- 通用工具 ----------------
const genId = (arr) => (arr.length ? Math.max(...arr.map((i) => Number(i.id))) + 1 : 1)
const nowStr = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// ================= API =================
export const api = {
  login({ username, password }) {
    return delay(400).then(() => {
      if (username === ACCOUNT.username && password === ACCOUNT.password) {
        const token = 'mock-token-' + Date.now()
        return { ok: true, data: { token, user: { username: ACCOUNT.username, nickname: ACCOUNT.nickname, role: ACCOUNT.role } } }
      }
      return { ok: false, message: '账号或密码错误，请使用 admin / 123456 登录' }
    })
  },

  getDashboard() {
    return delay(350).then(() => ({
      ok: true,
      data: {
        shopTotal: shopDb.length,
        shopOnline: shopDb.filter((s) => s.status === 1).length,
        orderTotal: orderDb.length,
        orderPending: orderDb.filter((o) => o.status === '待确认').length,
        orderAmount: orderDb.filter((o) => o.status !== '已取消').reduce((sum, o) => sum + o.amount, 0),
        typeStats: ['travel', 'hotel', 'food'].map((t) => ({
          type: t,
          typeLabel: catTypeMap[t],
          count: shopDb.filter((s) => s.type === t).length
        })),
        recentOrders: orderDb.slice(0, 5)
      }
    }))
  },

  // ---------- 商家 ----------
  getShops({ keyword = '', type = '', status = null, page = 1, size = 10 } = {}) {
    return delay().then(() => {
      let list = [...shopDb]
      if (keyword) list = list.filter((s) => s.name.includes(keyword) || s.address.includes(keyword) || s.contact.includes(keyword))
      if (type) list = list.filter((s) => s.type === type)
      if (status !== null && status !== '') list = list.filter((s) => s.status === Number(status))
      const total = list.length
      const start = (page - 1) * size
      return { ok: true, data: { list: list.slice(start, start + size), total } }
    })
  },

  addShop(payload) {
    return delay().then(() => {
      const shop = {
        ...payload,
        id: genId(shopDb),
        typeLabel: catTypeMap[payload.type] || payload.type,
        images: [],
        items: payload.type === 'food' ? [] : [],
        createTime: nowStr()
      }
      shopDb.unshift(shop)
      return { ok: true, data: shop, message: '新增成功' }
    })
  },

  updateShop(payload) {
    return delay().then(() => {
      const idx = shopDb.findIndex((s) => s.id === Number(payload.id))
      if (idx === -1) return { ok: false, message: '商家不存在' }
      const next = { ...shopDb[idx], ...payload, id: Number(payload.id), typeLabel: catTypeMap[payload.type] || payload.type }
      next.items = Array.isArray(next.items) ? next.items : []
      next.images = Array.isArray(next.images) ? next.images : []
      if (next.type === 'food') next.items = []
      shopDb[idx] = next
      return { ok: true, message: '保存成功' }
    })
  },

  deleteShop(id) {
    return delay().then(() => {
      shopDb = shopDb.filter((s) => s.id !== Number(id))
      return { ok: true, message: '删除成功' }
    })
  },

  // ---------- 商家子项（出行=车型 / 酒店=房型） ----------
  addShopItem(shopId, payload) {
    return delay().then(() => {
      const shop = shopDb.find((s) => s.id === Number(shopId))
      if (!shop) return { ok: false, message: '商家不存在' }
      const item = { ...payload, id: genId(shop.items), images: payload.images || [] }
      shop.items.unshift(item)
      return { ok: true, data: item, message: '新增成功' }
    })
  },

  updateShopItem(shopId, payload) {
    return delay().then(() => {
      const shop = shopDb.find((s) => s.id === Number(shopId))
      if (!shop) return { ok: false, message: '商家不存在' }
      const idx = shop.items.findIndex((i) => i.id === Number(payload.id))
      if (idx === -1) return { ok: false, message: '子项不存在' }
      shop.items[idx] = { ...shop.items[idx], ...payload, id: Number(payload.id), images: payload.images || [] }
      return { ok: true, message: '保存成功' }
    })
  },

  deleteShopItem(shopId, itemId) {
    return delay().then(() => {
      const shop = shopDb.find((s) => s.id === Number(shopId))
      if (!shop) return { ok: false, message: '商家不存在' }
      shop.items = shop.items.filter((i) => i.id !== Number(itemId))
      return { ok: true, message: '删除成功' }
    })
  },

  // ---------- 商家展示图 ----------
  updateShopImages(shopId, images) {
    return delay().then(() => {
      const shop = shopDb.find((s) => s.id === Number(shopId))
      if (!shop) return { ok: false, message: '商家不存在' }
      shop.images = images
      return { ok: true, message: '图片已保存' }
    })
  },

  // ---------- 订单 ----------
  getOrders({ keyword = '', status = '', page = 1, size = 10 } = {}) {
    return delay().then(() => {
      let list = [...orderDb]
      if (keyword) list = list.filter((o) => o.shopName.includes(keyword) || o.openid.includes(keyword) || o.id.includes(keyword))
      if (status) list = list.filter((o) => o.status === status)
      const total = list.length
      const start = (page - 1) * size
      return { ok: true, data: { list: list.slice(start, start + size), total } }
    })
  },

  updateOrderStatus(id, status, cancelReason = '') {
    return delay().then(() => {
      const o = orderDb.find((i) => i.id === id)
      if (!o) return { ok: false, message: '订单不存在' }
      if (status === '已取消' && o.status === '已确认') return { ok: false, message: '已确认的订单不可取消' }
      if (status === '已取消' && !cancelReason) return { ok: false, message: '取消订单必须填写原因' }
      o.status = status
      o.cancelReason = status === '已取消' ? cancelReason : ''
      return { ok: true, message: status === '已取消' ? '订单已取消' : '订单已确认' }
    })
  },

  deleteOrder(id) {
    return delay().then(() => {
      orderDb = orderDb.filter((o) => o.id !== id)
      return { ok: true, message: '删除成功' }
    })
  },

  // ---------- 高校 ----------
  getUniversities({ keyword = '', show = null, page = 1, size = 10 } = {}) {
    return delay().then(() => {
      let list = [...universityDb]
      if (keyword) list = list.filter((u) => u.name.includes(keyword) || u.address.includes(keyword))
      if (show !== null && show !== '') list = list.filter((u) => u.show === show)
      const total = list.length
      const start = (page - 1) * size
      return { ok: true, data: { list: list.slice(start, start + size), total } }
    })
  },

  addUniversity(payload) {
    return delay().then(() => {
      const university = {
        ...payload,
        id: genId(universityDb),
        area: Array.isArray(payload.area) ? payload.area : [],
        show: !!payload.show,
        createTime: nowStr()
      }
      universityDb.unshift(university)
      return { ok: true, data: university, message: '新增成功' }
    })
  },

  updateUniversity(payload) {
    return delay().then(() => {
      const idx = universityDb.findIndex((u) => u.id === Number(payload.id))
      if (idx === -1) return { ok: false, message: '高校不存在' }
      universityDb[idx] = {
        ...universityDb[idx],
        ...payload,
        id: Number(payload.id),
        area: Array.isArray(payload.area) ? payload.area : universityDb[idx].area,
        show: !!payload.show
      }
      return { ok: true, message: '保存成功' }
    })
  },

  deleteUniversity(id) {
    return delay().then(() => {
      universityDb = universityDb.filter((u) => u.id !== Number(id))
      return { ok: true, message: '删除成功' }
    })
  }
}