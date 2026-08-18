# 阶段 03 管理员内容管理前端联调说明

管理员前端 `TripAgency-web` 已从阶段 03 的内存 mock 切换到真实后端接口。登录、Dashboard 和订单仍保留原 mock 行为，等待对应阶段接口完成；内容管理不携带后端登录令牌。

## 通用约定

- 开发环境由 Vite 将 `/api`、`/files` 代理到 `http://localhost:8080`；也可通过 `VITE_API_BASE_URL` 指定后端根地址。
- 后端响应 `{code,message,data,requestId}`，前端以 HTTP 成功且 `code === "OK"` 判定成功。
- 写请求发送 `X-Request-Id`；分页页面仍使用内部字段 `size`，API 层转换为 `pageSize`。
- 状态统一使用 `ENABLED`、`DISABLED`；页面仅在筛选控件中转换为数字/布尔展示值。

## 高校

- 列表：`GET /api/v1/admin/universities?page&pageSize&keyword&status`，读取 `data.items` 和 `data.total`。
- 保存请求仅提交 `name`、`intro`、`polygonPoints[{longitude,latitude}]`、`imageResourceIds`。
- 页面已移除后端不存在的高校地址、联系人、电话；多边形无需重复首点。
- 状态使用 `PATCH /{id}/status`，删除使用逻辑删除接口。

## 服务商

- 前端筛选值 `travel/hotel/food` 在 API 层转换为 `TRAVEL/HOTEL/FOOD`。
- 保存字段与 `BusinessSaveRequest` 一致；`foodContactName`、`foodContactPhone`、`foodRecommendedDishes` 只在餐饮类型显示和提交。
- 已移除未确认的价格、单位、酒店面积、早餐等字段；类型在编辑时不可修改。
- 图片先调用 `POST /api/v1/admin/files/images`，再把返回的 `resourceId` 放入 `imageResourceIds`。

## 车辆、房型、菜品

- 路由分别为 `/cars`、`/rooms`、`/dishes`，三类都支持分页、详情、创建、编辑、独立状态更新和逻辑删除。
- 车辆提交 `model`、`seatNum`、`description`、`imageResourceId`。
- 房型提交 `name`、`bedSpec`、`description`、`imageResourceId`。
- 菜品提交 `name`、`description`、`imageResourceId`、`sortNo`。
- 附属资源仅关联单张图片；页面上传后保存 `imageResourceId`。

## 运行

先启动 Java 后端的 8080 端口，再在 `TripAgency/TripAgency/TripAgency-web` 运行 `npm run dev`。生产环境必须通过 Nginx/HTTPS 提供 `/api/v1/admin/**` 和 `/files/**`，不得直接公开 8080。
