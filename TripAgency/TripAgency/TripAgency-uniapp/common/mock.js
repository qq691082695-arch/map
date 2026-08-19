// ============共享演示数据：结构对齐后端 BusinessDetail { common, detail } 契约============
// 后端联调后应替换为 GET /api/v1/app/businesses/{id} 返回的数据
export const mockBusinesses = [
  {
    id: 1,
    name: '商务接送车队',
    businessType: 'TRAVEL',
    address: '武汉市江汉区商务区',
    longitude: 114.322,
    latitude: 30.601,
    intro: '本公司专注武汉全域商务接送，提供7-55座车辆，支持出差接机、会务接送，专职司机。',
    imageUrls: [],
    detail: {
      kind: 'TRAVEL',
      cars: [
        { id: 101, model: '别克GL8商务车', seatNum: 7, description: '市区商务接送、接机送站' },
        { id: 102, model: '丰田考斯特中巴', seatNum: 19, description: '会议团建用车' },
        { id: 103, model: '金龙大巴', seatNum: 55, description: '大型团队出行' }
      ]
    }
  },
  {
    id: 2,
    name: '楚河汉街商务快捷酒店',
    businessType: 'HOTEL',
    address: '武昌楚河汉街附近',
    longitude: 114.305,
    latitude: 30.582,
    intro: '靠近地铁，适合出差商务入住。',
    imageUrls: [],
    detail: {
      kind: 'HOTEL',
      rooms: [
        { id: 201, name: '商务大床房', bedSpec: '1.8m大床', description: '含早，带独立办公区' },
        { id: 202, name: '商务双床房', bedSpec: '1.2m×2床', description: '适合双人出差' },
        { id: 203, name: '行政套房', bedSpec: '2m大床', description: '含会客区，可商务接待' }
      ]
    }
  },
  {
    id: 3,
    name: '洪山广场铂悦酒店',
    businessType: 'HOTEL',
    address: '洪山广场地铁站旁',
    longitude: 114.335,
    latitude: 30.596,
    intro: '高档商务酒店，配套会议室。',
    imageUrls: [],
    detail: {
      kind: 'HOTEL',
      rooms: [
        { id: 301, name: '豪华大床房', bedSpec: '1.8m大床', description: '高层视野，含双早' },
        { id: 302, name: '豪华双床房', bedSpec: '1.2m×2床', description: '商务出差首选' },
        { id: 303, name: '总裁套房', bedSpec: '2m大床', description: '含独立会客厅' }
      ]
    }
  },
  {
    id: 4,
    name: '洪山商务简餐酒楼',
    businessType: 'FOOD',
    address: '洪山广场周边',
    longitude: 114.331,
    latitude: 30.593,
    intro: '支持出差团体简餐、小型商务接待。',
    imageUrls: [],
    detail: {
      kind: 'FOOD',
      contactName: '李经理',
      contactPhone: '027-88888888',
      recommendedDishes: '酱烧武昌鱼、湖北藕汤、小炒黄牛肉',
      dishes: [
        { id: 401, name: '酱烧武昌鱼', description: '湖北特色，肉质鲜嫩' },
        { id: 402, name: '湖北藕汤', description: '地道粉藕煨汤' },
        { id: 403, name: '小炒黄牛肉', description: '下饭经典' }
      ]
    }
  },
  {
    id: 5,
    name: '楚宴融合菜馆',
    businessType: 'FOOD',
    address: '水果湖商圈',
    longitude: 114.298,
    latitude: 30.588,
    intro: '适合企业客户正式商务宴请。',
    imageUrls: [],
    detail: {
      kind: 'FOOD',
      contactName: '王店长',
      contactPhone: '027-66666666',
      recommendedDishes: '楚汉荷香鸡、洞庭鱼头王、秘制鸭舌',
      dishes: [
        { id: 501, name: '楚汉荷香鸡', description: '招牌菜，清香软嫩' },
        { id: 502, name: '洞庭鱼头王', description: '剁椒风味' },
        { id: 503, name: '秘制鸭舌', description: '卤香浓郁' }
      ]
    }
  }
]