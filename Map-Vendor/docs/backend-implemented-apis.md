# 地图商家预约系统——当前已完成后端接口清单

> 整理日期：2026-08-19  
> 核对口径：以 `src/main/java` 中实际存在的 Spring MVC Controller 路由为准，并结合 OpenAPI 契约与集成测试交叉核对。  
> 当前合计：**40 个源码已实现业务路由**（管理员业务接口 37 个，App/公共业务路由 3 个）；此外配置了 1 个 Actuator 健康检查端点。

## 1. 联调通用约定

- API 业务前缀：`/api/v1`
- 管理端前缀：`/api/v1/admin`
- 小程序端前缀：`/api/v1/app`
- 分页参数：`page` 默认 `1`；`pageSize` 默认 `20`，最大 `100`
- 日期格式：`YYYY-MM-DD`
- 业务类型：`TRAVEL`、`HOTEL`、`FOOD`
- 订单状态：`PENDING`、`CONFIRMED`、`CANCELLED`
- 内容状态：`ENABLED`、`DISABLED`
- 除文件下载和 Excel 导出外，接口统一返回：

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "requestId": "请求追踪 ID"
}
```

- 分页接口的 `data` 结构：

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "pageSize": 20
}
```

### 重要安全边界

- Java 后端不提供管理员登录、Session、JWT 或令牌接口；小程序仅提供一次性 `code` 换取 `openid` 的静默身份接口。
- `/api/v1/admin/**` 当前**不做后端登录认证**，生产环境必须通过 Nginx 网络访问限制、HTTPS、限流和审计日志保护。
- 系统不存在商家账号、商家登录或 `/api/v1/merchant/**`。

## 2. 管理端——高校管理（6 个）

| 方法 | 路径 | 用途 | 主要参数/请求体 |
|---|---|---|---|
| GET | `/api/v1/admin/universities` | 分页查询高校 | `page`、`pageSize`、可选 `keyword`、`status` |
| GET | `/api/v1/admin/universities/{id}` | 查询高校详情 | 路径参数 `id` |
| POST | `/api/v1/admin/universities` | 新增高校 | `UniversitySaveRequest` |
| PUT | `/api/v1/admin/universities/{id}` | 编辑高校 | 路径参数 `id` + `UniversitySaveRequest` |
| PATCH | `/api/v1/admin/universities/{id}/status` | 启用或禁用高校 | `{ "status": "ENABLED 或 DISABLED" }` |
| DELETE | `/api/v1/admin/universities/{id}` | 逻辑删除高校 | 路径参数 `id` |

新增/编辑请求体主要字段：

```json
{
  "name": "高校名称，必填，最长 128 字符",
  "intro": "简介",
  "polygonPoints": [
    { "longitude": 116.397, "latitude": 39.908 }
  ],
  "imageResourceIds": [1, 2]
}
```

说明：多边形至少需要 3 个不同坐标点；图片资源最多关联 20 个。

## 3. 管理端——服务商管理（6 个）

| 方法 | 路径 | 用途 | 主要参数/请求体 |
|---|---|---|---|
| GET | `/api/v1/admin/businesses` | 分页查询服务商 | `page`、`pageSize`、可选 `keyword`、`type`、`status` |
| GET | `/api/v1/admin/businesses/{id}` | 查询服务商详情 | 路径参数 `id` |
| POST | `/api/v1/admin/businesses` | 新增服务商 | `BusinessSaveRequest` |
| PUT | `/api/v1/admin/businesses/{id}` | 编辑服务商 | 路径参数 `id` + `BusinessSaveRequest`；类型不可修改 |
| PATCH | `/api/v1/admin/businesses/{id}/status` | 启用或禁用服务商 | `{ "status": "ENABLED 或 DISABLED" }` |
| DELETE | `/api/v1/admin/businesses/{id}` | 逻辑删除服务商 | 路径参数 `id` |

新增/编辑请求体主要字段：

```json
{
  "name": "服务商名称",
  "address": "地址",
  "longitude": 116.397,
  "latitude": 39.908,
  "businessType": "TRAVEL",
  "intro": "简介",
  "foodContactName": null,
  "foodContactPhone": null,
  "foodRecommendedDishes": null,
  "imageResourceIds": [1, 2]
}
```

说明：`foodContactName`、`foodContactPhone`、`foodRecommendedDishes` 只适用于 `FOOD` 类型；图片资源最多关联 20 个。

## 4. 管理端——车辆、房型、菜品（18 个）

三类资源均已完成分页、详情、新增、编辑、启停和逻辑删除。列表通用参数为 `page`、`pageSize` 和可选 `status`。

### 4.1 车辆（仅 TRAVEL 服务商）

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/v1/admin/businesses/{businessId}/cars` | 分页查询车辆 |
| GET | `/api/v1/admin/businesses/{businessId}/cars/{id}` | 查询车辆详情 |
| POST | `/api/v1/admin/businesses/{businessId}/cars` | 新增车辆 |
| PUT | `/api/v1/admin/businesses/{businessId}/cars/{id}` | 编辑车辆 |
| PATCH | `/api/v1/admin/businesses/{businessId}/cars/{id}/status` | 启用或禁用车辆 |
| DELETE | `/api/v1/admin/businesses/{businessId}/cars/{id}` | 逻辑删除车辆 |

车辆新增/编辑字段：`model`（必填）、`seatNum`（大于 0）、`description`、`imageResourceId`。

### 4.2 房型（仅 HOTEL 服务商）

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/v1/admin/businesses/{businessId}/rooms` | 分页查询房型 |
| GET | `/api/v1/admin/businesses/{businessId}/rooms/{id}` | 查询房型详情 |
| POST | `/api/v1/admin/businesses/{businessId}/rooms` | 新增房型 |
| PUT | `/api/v1/admin/businesses/{businessId}/rooms/{id}` | 编辑房型 |
| PATCH | `/api/v1/admin/businesses/{businessId}/rooms/{id}/status` | 启用或禁用房型 |
| DELETE | `/api/v1/admin/businesses/{businessId}/rooms/{id}` | 逻辑删除房型 |

房型新增/编辑字段：`name`（必填）、`bedSpec`（必填）、`description`、`imageResourceId`。

### 4.3 菜品（仅 FOOD 服务商）

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/v1/admin/businesses/{businessId}/dishes` | 分页查询菜品 |
| GET | `/api/v1/admin/businesses/{businessId}/dishes/{id}` | 查询菜品详情 |
| POST | `/api/v1/admin/businesses/{businessId}/dishes` | 新增菜品 |
| PUT | `/api/v1/admin/businesses/{businessId}/dishes/{id}` | 编辑菜品 |
| PATCH | `/api/v1/admin/businesses/{businessId}/dishes/{id}/status` | 启用或禁用菜品 |
| DELETE | `/api/v1/admin/businesses/{businessId}/dishes/{id}` | 逻辑删除菜品 |

菜品新增/编辑字段：`name`（必填）、`description`、`imageResourceId`、`sortNo`（必填且不小于 0）。

资源状态请求体统一为：

```json
{ "status": "ENABLED" }
```

## 5. 管理端——文件上传（1 个）

| 方法 | 路径 | 用途 | 请求/响应 |
|---|---|---|---|
| POST | `/api/v1/admin/files/images` | 上传图片资源 | `multipart/form-data`，字段名 `file`；返回 `resourceId`、`url`、`mimeType`、`sizeBytes` |

当前只允许 JPEG、PNG、WebP，并校验真实 MIME、文件大小、扩展名和路径安全。

## 6. 管理端——订单管理（5 个）

| 方法 | 路径 | 用途 | 主要参数/请求体 |
|---|---|---|---|
| GET | `/api/v1/admin/orders` | 分页筛选全部订单 | `page`、`pageSize`；可选 `serviceDateFrom`、`serviceDateTo`、`status`、`type`、`businessId` |
| GET | `/api/v1/admin/orders/{id}` | 查询订单详情 | 使用下单时保存的历史快照展示 |
| POST | `/api/v1/admin/orders/{id}/confirm` | 管理员确认待确认订单 | 仅允许 `PENDING -> CONFIRMED` |
| POST | `/api/v1/admin/orders/{id}/cancel` | 管理员取消待确认订单 | 请求体 `{ "reason": "取消原因" }`，原因必填且最长 500 字符 |
| GET | `/api/v1/admin/orders/export` | 按筛选条件导出 Excel | 必填 `serviceDateFrom`、`serviceDateTo`；可选 `status`、`type`、`businessId` |

订单确认/取消采用条件更新，状态变更与 `order_status_log` 同事务；终态订单不能再次处理。Excel 导出上限为 10,000 行，成功响应为 `.xlsx` 文件流，不使用统一 JSON 包装。

## 7. 管理端——订单统计（1 个）

| 方法 | 路径 | 用途 | 主要参数 |
|---|---|---|---|
| GET | `/api/v1/admin/statistics/overview` | 按服务日期统计订单状态及服务商快照 | 可选 `serviceDateFrom`、`serviceDateTo`、`type`、`businessId` |

统计口径为 `service_date`，分别返回 `PENDING`、`CONFIRMED`、`CANCELLED` 和合计，不按订单创建时间统计。

## 8. App/公共接口（8 个）及健康检查

| 方法 | 路径 | 用途 | 主要参数/响应 |
|---|---|---|---|
| GET | `/api/v1/app/map-overview` | 小程序地图聚合查询 | 可选 `type=TRAVEL/HOTEL/FOOD`；返回启用高校区域和启用服务商点位 |
| POST | `/api/v1/app/wechat/session` | 微信静默身份交换 | 请求体含一次性 code；仅返回 openid，不返回 session_key，不创建 Session/JWT |
| GET | `/api/v1/app/businesses/{id}` | 小程序服务商详情 | 返回 `common + detail` 和启用附属资源 |
| POST | `/api/v1/app/orders` | 创建预约 | 必填 `Idempotency-Key`；保存快照并创建 `PENDING` 初始日志 |
| GET | `/api/v1/app/orders` | 按 openid 分页查询订单 | `openid` 必填；手机号脱敏；稳定排序 |
| GET | `/api/v1/app/orders/{id}` | 按 openid 查询订单详情 | `openid` 不匹配统一返回订单不存在 |
| POST | `/api/v1/app/orders/{id}/cancel` | 用户取消自己的待确认订单 | 请求体含 `openid`；仅 `PENDING -> CANCELLED`，记录 `USER` 日志 |
| GET | `/api/v1/app/system/ping` | App API 连通性检查 | 返回简单存活信息 |
| GET | `/files/{storageKey}` | 读取公开文件 | 返回文件二进制；设置实际 MIME、长度、`nosniff` 与禁用缓存头 |
| GET | `/actuator/health` | Spring Boot Actuator 健康检查（不计入上述 40 个业务路由） | Actuator 标准健康状态（是否对外暴露受环境配置控制） |

`map-overview` 的主要响应字段：

- `universities[]`：`id`、`name`、`intro`、`polygonPoints`、`imageUrls`
- `businesses[]`：`id`、`name`、`businessType`、`address`、`longitude`、`latitude`、`intro`、`coverImageUrl`

## 9. App 契约实现状态

下列接口已出现在 App OpenAPI 契约中，但当前源码中没有对应 Controller，**不能按已完成接口联调**：

| 计划接口 | 当前状态 | 对应开发窗口 |
|---|---|---|
| `GET /api/v1/app/businesses/{id}` | 已实现 | 06B 服务商详情查询 |
| `POST /api/v1/app/orders` | 已实现 | 07A：幂等创建、类型/资源归属校验、快照、PENDING 初始日志 |
| `GET /api/v1/app/orders` | 已实现 | 08A 用户订单查询与取消 |
| `GET /api/v1/app/orders/{id}` | 已实现 | 08A 用户订单查询与取消 |
| `POST /api/v1/app/orders/{id}/cancel` | 已实现 | 08A 用户订单查询与取消 |

## 10. 联调资料位置

- 管理端 OpenAPI：`docs/openapi/admin-api-contract-v1.yaml`
- App OpenAPI：`docs/openapi/app-api-contract-v1.yaml`
- 管理端内容联调说明：`docs/admin-content-frontend-integration-v1.md`
- 高校字段契约：`docs/admin-university-field-contract-v1.md`
- 服务商字段契约：`docs/admin-business-field-contract-v1.md`
- 附属资源字段契约：`docs/admin-business-resource-field-contract-v1.md`
- 订单字段契约：`docs/admin-order-field-contract-v1.md`
- 统计字段契约：`docs/admin-statistics-field-contract-v1.md`
- 导出契约：`docs/admin-order-export-contract-v1.md`
- 文件存储契约：`docs/file-storage-field-contract-v1.md`
- 安全边界：`docs/security-boundary-v1.md`

## 11. 当前完成范围小结

当前后端已完成管理员侧的高校、服务商、车辆/房型/菜品、图片上传、订单查询与状态处理、统计和 Excel 导出，以及小程序地图聚合、服务商详情、预约创建、用户订单查询与用户取消。
