# 管理员订单查询字段契约 v1

## 接口

- `GET /api/v1/admin/orders`：默认 `page=1&pageSize=20`，最大 100；按 `created_at DESC, id DESC` 稳定排序。
- 筛选参数：`serviceDateFrom`、`serviceDateTo`（均含边界）、`status`、`type`、`businessId`。
- `GET /api/v1/admin/orders/{id}`：返回订单详情；不存在返回 `ORDER_NOT_FOUND`。
- `POST /api/v1/admin/orders/{id}/confirm`：仅允许 `PENDING -> CONFIRMED`。
- `POST /api/v1/admin/orders/{id}/cancel`：仅允许 `PENDING -> CANCELLED`，请求体为 `{ "reason": "取消原因" }`，去除首尾空白后必填且最长 500 字符。

## 状态机与并发

- 确认和取消均使用 `WHERE id = ? AND status = 'PENDING'` 条件更新并递增 `version`；并发竞争最多一个请求成功。
- 非待确认订单或竞争失败返回 HTTP 409 / `ORDER_STATUS_CONFLICT`；订单不存在返回 HTTP 404 / `ORDER_NOT_FOUND`。
- 状态更新与 `order_status_log` 在同一事务内完成。管理员操作的 `operator_type` 固定为 `ADMIN`，取消时 `cancel_source` 固定为 `ADMIN`，日志保存 `X-Request-Id`，不记录完整手机号或 openid。

## 历史与隐私口径

- `businessNameSnapshot`、`carSpecSnapshot`、`roomSpecSnapshot` 和 `optionSnapshotJson` 直接读取订单快照，不使用当前服务商或资源主数据覆盖。
- 列表只返回 `contactPhoneMasked`；详情可返回完整 `contactPhone` 供管理员处理预约。
- 列表和详情均只返回 `openidMasked`，不返回完整 openid。完整手机号和 openid 不得写入日志。
- `/api/v1/admin/**` 当前无后端认证，生产必须置于 Nginx 网络访问限制、HTTPS、限流和审计边界之后。

## 前端现有字段映射

- mock `customer` -> `contactName`
- mock `phone` -> 列表 `contactPhoneMasked` / 详情 `contactPhone`
- mock `shopName` -> `businessNameSnapshot`
- mock 中文状态 -> `PENDING` / `CONFIRMED` / `CANCELLED`
- mock `amount` 当前后端无对应字段，04A 不新增金额口径。

## 管理员前端阶段 04 联调同步

- 订单页已改用真实接口，不再调用 `mock.js` 的订单方法。
- 列表请求参数固定映射为：前端 `page/size` -> 后端 `page/pageSize`；日期范围 -> `serviceDateFrom/serviceDateTo`；状态、类型、服务商分别传 `status/type/businessId`。
- 页面枚举直接传后端英文值：`PENDING/CONFIRMED/CANCELLED`、`TRAVEL/HOTEL/FOOD`，只在展示层转换中文。
- 列表展示 `orderNo`、`serviceType`、`businessNameSnapshot`、`contactName`、`contactPhoneMasked`、`openidMasked`、`peopleNum`、`serviceDate`、`status`、`createdAt`。
- 详情通过 `GET /api/v1/admin/orders/{id}` 单独加载，完整手机号只展示于详情；分类内容按车辆、房型或用餐时段字段展示。
- 确认调用 `POST /{id}/confirm`，无请求体；取消调用 `POST /{id}/cancel`，请求体 `{ "reason": "..." }`，前后端均限制 500 字符。
- 旧前端的 `keyword`、`content`、`amount` 和中文状态传参没有后端口径，已从订单页移除，未新增数据库字段。
