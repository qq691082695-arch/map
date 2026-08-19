# 小程序 API 字段与联调契约 v1

状态：窗口 01A 已冻结；`map-overview`、`businesses/{id}` 已实现，其余接口按后续窗口完成。

依据：`AGENTS.md`、`地图商家预约系统_后端与数据库PRD_v1.6.docx`、`V001__baseline.sql`。如后续需求改变本文件中的冻结项，必须同步更新 PRD、Flyway、OpenAPI 和测试。

角色边界：系统使用者只有微信小程序用户和平台管理员。本文中的“服务商”是 `business` 业务数据，不是登录角色，不拥有账号、后台、认证上下文或数据权限。

## 1. 通用约定

- API 前缀：`/api/v1/app`。
- JSON 字段使用 lowerCamelCase；数据库字段使用 snake_case。
- 业务日期格式为 `YYYY-MM-DD`，服务端业务时区为 `Asia/Shanghai`。
- 枚举通过 API 传输时只使用后端大写值，中文与 emoji 仅由前端映射展示。
- 所有响应包含 `code`、`message`、`data`、`requestId`。
- 小程序订单接口的 `openid` 由前端传入，仅用于订单归属辨别，不代表后端强认证。
- 列表默认 `page=1&pageSize=20`，`pageSize` 最大 100，订单按 `createdAt DESC, id DESC` 稳定排序。
- 创建订单必须携带 `Idempotency-Key` 请求头；同一 `openid + Idempotency-Key` 只能对应同一请求内容。

## 2. 冻结枚举

| 语义 | API/Java/数据库值 | 前端建议显示 |
|---|---|---|
| 服务商/服务类型 | `TRAVEL` | 出行 |
| 服务商/服务类型 | `HOTEL` | 住宿 |
| 服务商/服务类型 | `FOOD` | 餐饮 |
| 订单状态 | `PENDING` | 待确认/预约中 |
| 订单状态 | `CONFIRMED` | 已确认 |
| 订单状态 | `CANCELLED` | 已取消 |
| 出行服务方式 | `DAY_CHARTER` | 按日包车 |
| 出行服务方式 | `ROUND_TRIP` | 往返接送 |
| 用餐时段 | `BREAKFAST` | 早餐 |
| 用餐时段 | `LUNCH` | 午餐 |
| 用餐时段 | `DINNER` | 晚餐 |
| 取消来源 | `USER` | 用户 |
| 取消来源 | `ADMIN` | 平台管理员 |

不得增加“已拒绝”等订单状态。平台管理员不接受预约时使用 `CANCELLED`，取消来源固定为 `ADMIN` 且原因必填。

## 3. 窗口 01 字段决策

| 现有前端字段 | 决策 | 原因与后续处理 |
|---|---|---|
| `room.price` | MVP 契约移除 | PRD 与 `business_hotel_room` 没有价格字段；未获确认前不得新增数据库列 |
| `avgPrice` | MVP 契约移除 | PRD 与 `business` 没有人均价字段 |
| `cuisineList` | 不作为后端字段 | 当前数据库表达的是推荐菜/菜品，不是菜系；前端改为使用 `recommendedDishes`/`dishes` |
| `typeLabel` | 前端派生 | 后端只返回 `businessType`，前端映射中文和 emoji |
| `desc` | 合并为 `intro` | 对应 `business.intro`，避免 `desc`/`intro` 双字段 |
| `roomList[].roomName` | 改为 `rooms[].name` | 保留真实 `id`，供下单提交 `roomId` |
| 静态 `shopAllList` | 联调后移除 | 数据改由地图聚合和商家详情接口提供 |
| 静态 `polygonsList` | 联调后移除 | 数据改由 `university.polygon_json` 提供 |

如果未来确认需要房价、人均价或菜系筛选，应先明确计价单位、币种、区间表达、历史订单快照和查询需求，然后新建后续 Flyway 迁移；不得修改已经发布的 `V001__baseline.sql`。

## 4. 地图聚合 DTO

接口：`GET /api/v1/app/map-overview?type=TRAVEL|HOTEL|FOOD`，`type` 可省略。

### UniversityMapItem

| API 字段 | 类型 | 数据库来源 | 说明 |
|---|---|---|---|
| `id` | long | `university.id` | 高校 ID |
| `name` | string | `university.name` | 高校名称 |
| `intro` | string/null | `university.intro` | 简介 |
| `polygonPoints` | array | `university.polygon_json` | 点对象包含 `latitude`、`longitude`，至少 3 个不同点 |
| `imageUrls` | string[] | 高校文件关联 | 只返回公开 URL，按关联 `sort_no` 稳定排序 |

### BusinessMapItem

| API 字段 | 类型 | 数据库来源 | 说明 |
|---|---|---|---|
| `id` | long | `business.id` | 商家 ID |
| `name` | string | `business.name` | 商家名称 |
| `businessType` | enum | `business.business_type` | `TRAVEL/HOTEL/FOOD` |
| `address` | string | `business.address` | 地址 |
| `longitude` | decimal | `business.longitude` | 经度 |
| `latitude` | decimal | `business.latitude` | 纬度 |
| `intro` | string/null | `business.intro` | 简介，可用于地图弹层摘要 |
| `coverImageUrl` | string/null | 商家首张有效图片 | 不返回本地存储路径 |

地图接口只返回启用且未逻辑删除的高校、商家和有效图片。

## 5. 服务商详情 DTO

接口：`GET /api/v1/app/businesses/{id}`。

返回结构固定为 `{ common, detail }`。`common.businessType` 决定 `detail` 的具体结构。

### BusinessCommon

`id`、`name`、`businessType`、`address`、`longitude`、`latitude`、`intro`、`imageUrls`。其中 `imageUrls` 只包含公开 URL。

### TravelDetail

字段 `cars`，每项包含：

- `id`：车辆 ID，对应 `business_travel_car.id`。
- `model`：车型/车辆规格。
- `seatNum`：座位数，正整数。
- `description`：说明，可空。
- `imageUrl`：公开图片 URL，可空。

### HotelDetail

字段 `rooms`，每项包含：

- `id`：房型 ID，对应 `business_hotel_room.id`。
- `name`：房型名称。
- `bedSpec`：床型规格。
- `description`：说明，可空。
- `imageUrl`：公开图片 URL，可空。

MVP 不返回价格。

### FoodDetail

- `contactName`：餐饮联系人，可空。
- `contactPhone`：餐饮联系电话，可空；是否展示须遵守最小可见原则。
- `recommendedDishes`：商家推荐菜说明，可空，对应 `business.food_recommended_dishes`。
- `dishes`：菜品数组，每项包含 `id`、`name`、`description`、`imageUrl`。

MVP 不返回人均价或菜系标签。

## 6. 创建预约 DTO

接口：`POST /api/v1/app/orders`。

### 公共必填字段

| API 字段 | 数据库字段 | 约束 |
|---|---|---|
| `openid` | `reserve_order.openid` | 非空；前端提供，不是强认证 |
| `contactName` | `contact_name` | 非空，最长 64 |
| `contactPhone` | `contact_phone` | 非空，最长 32；日志必须脱敏 |
| `peopleNum` | `people_num` | 正整数 |
| `serviceDate` | `service_date` | `YYYY-MM-DD` |
| `businessId` | `business_id` | 服务商必须启用且未删除 |
| `serviceType` | `service_type` | 必须与服务商 `business_type` 相同 |

`businessNameSnapshot`、`businessType`、车辆/房型规格快照、`orderNo`、`status`、时间戳和 `version` 均由后端生成，禁止相信前端提交的快照或状态。

### 分类必填字段

- `TRAVEL`：`carId`、`carQuantity > 0`、`serviceMode`；车辆必须启用、未删除且属于 `businessId`。
- `HOTEL`：`roomId`、`roomQuantity > 0`；房型必须启用、未删除且属于 `businessId`。
- `FOOD`：`mealPeriod`，只允许 `BREAKFAST/LUNCH/DINNER`。

不同类型的专属字段互斥，不能同时提交。创建成功状态固定为 `PENDING`。

## 7. 订单查询 DTO

列表接口：`GET /api/v1/app/orders?openid=&page=&pageSize=`。

详情接口：`GET /api/v1/app/orders/{id}?openid=`。

`openid` 必须与订单记录完全匹配；手机号不能作为订单归属凭据。订单返回字段包括：

- `id`、`orderNo`、`businessId`、`businessNameSnapshot`、`serviceType`。
- `contactName`、脱敏后的 `contactPhone`、`peopleNum`、`serviceDate`。
- `status`、分类选择项及其快照。
- `confirmedAt`、`cancelledAt`、`cancelSource`、`cancelReason`。
- `createdAt`、`updatedAt`。

列表分页数据结构固定为 `items`、`page`、`pageSize`、`total`。详情和列表不得因为商家、车辆或房型后来改名/删除而丢失快照展示。

## 8. 前端映射规则

| 后端字段/值 | 当前前端字段或用途 |
|---|---|
| `businessType` | 替代 `type`；`TRAVEL/HOTEL/FOOD` 映射到 `travel/hotel/food` CSS 或组件分支 |
| `businessType` 中文映射 | 替代静态 `typeLabel` |
| `universities[].polygonPoints` | 生成地图 `polygons` |
| `businesses[]` | 生成 `markerList` 和分类商家卡片 |
| `coverImageUrl` | 商家封面；地图分类图标仍可使用前端静态资源 |
| `common.intro` | 替代 `intro` 与 `desc` 的重复表达 |
| `detail.cars` | 出行车辆选择列表，提交真实 `carId` |
| `detail.rooms` | 酒店房型选择列表，提交真实 `roomId` |
| `detail.dishes` | 餐饮菜品展示，不作为订单主要选择项 |

前端不得根据名称反查车辆或房型 ID，不得提交中文枚举，不得在接口失败时显示预约成功。

## 9. 错误语义约定

- 参数格式或校验失败：HTTP 400。
- 服务商、车辆或房型不存在、已删除或对小程序不可见：HTTP 404。
- 服务商禁用导致不可预约：HTTP 409 或统一业务冲突码。
- 幂等键复用但请求内容不同：HTTP 409。
- 同一订单状态并发冲突或终态重复变更：HTTP 409。
- 服务端异常：HTTP 500，响应不得包含 SQL、堆栈、完整手机号、openid 或令牌。

具体业务 `code` 在实现窗口中集中定义，HTTP 语义不得偏离本节。

## 10. App DTO 核对结论

- 地图：`MapOverview`、`UniversityMapItem`、`BusinessMapItem` 与当前已实现 DTO 字段一致；仅表达高校区域和服务商业务数据，不含角色或认证字段。
- 详情：冻结为 `BusinessDetail { common, detail }`，三类附属资源均保留真实 ID；后续实现不得加入服务商账号、登录或权限字段。
- 创建预约：以 `openid` 辨别归属，服务商和车辆/房型由真实 ID 关联，后端生成订单号、状态和展示快照。
- 订单：列表、详情和取消均要求传入 `openid`；取消来源只允许 `USER`、`ADMIN`，用户只能取消自己的 `PENDING` 订单。
