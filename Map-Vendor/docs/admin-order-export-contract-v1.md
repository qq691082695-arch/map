# 管理员订单 Excel 导出契约 v1

## 接口与筛选

- `GET /api/v1/admin/orders/export`，返回 `.xlsx` 二进制文件。
- `serviceDateFrom`、`serviceDateTo` 必填，格式 `YYYY-MM-DD`，按 `service_date` 左右边界均包含；`status`、`type`、`businessId` 可选。
- 筛选 SQL 与 `GET /api/v1/admin/orders` 共用同一 MyBatis `filter`，排序同为 `created_at DESC, id DESC`。
- 默认日期范围最多 366 天、结果最多 10,000 行；超过时分别返回 `EXPORT_DATE_RANGE_EXCEEDED`、`EXPORT_ROW_LIMIT_EXCEEDED`。可通过 `MAP_VENDOR_EXPORT_MAX_DATE_RANGE_DAYS`、`MAP_VENDOR_EXPORT_MAX_ROWS` 调整。

## 文件字段与隐私

列顺序：订单号、服务日期、状态、服务类型、服务商 ID、服务商名称、联系人、联系电话、人数、车辆规格、车辆数量、服务方式、房型规格、房间数量、用餐时段、取消来源、取消原因、创建时间、确认时间、取消时间。

- 导出面向受网络边界保护的平台管理员，包含完整联系电话，不包含 openid。
- `business_name_snapshot`、车辆/房型规格等均取订单快照，主数据变化不影响历史导出。
- 审计日志记录 requestId、筛选范围、结果、行数，不记录手机号、联系人或 openid。
- `/api/v1/admin/**` 当前无后端认证；生产必须经 Nginx 网络访问限制、HTTPS、限流和审计保护。

## 流式与联调

- 服务端使用 Apache POI `SXSSFWorkbook`，内存窗口 100 行；数据库按 `MAP_VENDOR_EXPORT_FETCH_SIZE`（默认 500）分批读取。
- 前端应以 Blob 下载，文件名从 `Content-Disposition` 获取；错误响应仍为统一 JSON `{code,message,data,requestId}`。
