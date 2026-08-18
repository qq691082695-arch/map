# 管理端附属资源字段契约 v1

路由统一位于 `/api/v1/admin/businesses/{businessId}`，车辆、房型、菜品分别使用 `cars`、`rooms`、`dishes`。列表参数为 `page`、`pageSize`（最大 100）和可选 `status=ENABLED|DISABLED`；稳定排序为车辆/房型 `createdAt DESC,id DESC`，菜品 `sortNo ASC,id DESC`。

- 车辆仅允许挂在 `TRAVEL` 服务商：`model`、`seatNum`（正整数）、`description?`、`imageResourceId?`。
- 房型仅允许挂在 `HOTEL` 服务商：`name`、`bedSpec`、`description?`、`imageResourceId?`。
- 菜品仅允许挂在 `FOOD` 服务商：`name`、`description?`、`imageResourceId?`、`sortNo`（非负整数）。
- 创建状态固定为 `ENABLED`；状态接口只接受 `ENABLED|DISABLED`；删除是逻辑删除并同步禁用，不提供物理删除。
- `{id}` 必须属于路径中的服务商；跨服务商访问按资源不存在处理。服务商类型不匹配返回 `BUSINESS_TYPE_MISMATCH`。
- `imageResourceId` 引用 `POST /api/v1/admin/files/images` 返回的启用文件资源；新增和编辑时校验资源存在、启用且未删除。

前端现有价格、单位、酒店面积、早餐等 mock 字段不在已确认后端口径内，本窗口未新增这些数据库字段。
