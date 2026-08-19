# 管理员统计字段契约 v1

接口：`GET /api/v1/admin/statistics/overview`。

- 可选筛选：`serviceDateFrom`、`serviceDateTo`（`YYYY-MM-DD`，均含边界）、`type`、`businessId`。
- 统计日期固定使用订单 `service_date`，不使用 `created_at`；自然日口径为 `Asia/Shanghai`。
- `total` 返回 `pendingCount`、`confirmedCount`、`cancelledCount` 和 `totalCount`。
- `businesses` 按 `businessId + businessNameSnapshot + businessType` 分组，使用订单快照保留服务商改名或删除前的历史含义。
- 分组稳定排序：订单合计降序、服务商 ID 升序、名称快照升序。
- 无匹配订单时，四项合计均为 `0`，`businesses` 为空数组。

响应仍使用统一 `code/message/data/requestId` 包装。管理员接口不做后端登录认证，生产环境必须通过 Nginx 网络边界、HTTPS、限流和审计保护。
