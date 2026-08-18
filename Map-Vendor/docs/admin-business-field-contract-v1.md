# 管理端服务商字段与前端映射（03B）

- 服务商是平台管理员维护的业务数据，不是系统角色；不存在账号、登录、密码、令牌或权限字段。
- 后端枚举固定为 `TRAVEL`、`HOTEL`、`FOOD`；现有前端 mock 的 `travel`、`hotel`、`food` 需由前端适配层转换。
- `name/address/longitude/latitude/businessType` 为创建和编辑必填；类型创建后不可修改。
- `foodContactName/foodContactPhone/foodRecommendedDishes` 只适用于 `FOOD`，另外两类传入会返回 `VALIDATION_ERROR`。
- `status` 使用 `ENABLED/DISABLED`，创建固定为 `ENABLED`，通过独立状态接口修改。
- 列表按 `createdAt DESC, id DESC` 稳定排序，支持 `keyword`（名称或地址）、`type`、`status` 筛选。
- 删除为逻辑删除，删除后管理端普通查询返回不存在，历史订单快照及引用不改变。
- 前端 mock 中 `price/unit` 以及酒店 `area/breakfast` 不在当前数据库与 03B 契约内，后端未擅自增加这些字段。
- 03D 已补充图片关联：新增/编辑请求可传 `imageResourceIds`（最多 20 个、不重复），按数组顺序整体替换服务商图片；响应 `images` 返回 `resourceId`、`url`、`sortNo`。
