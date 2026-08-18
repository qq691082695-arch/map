# 窗口 03A：高校管理前后端字段契约

管理员接口前缀：`/api/v1/admin/universities`。后端不提供管理员登录或令牌校验；部署时必须由 Nginx 网络边界、HTTPS、限流和审计保护。

## 页面字段映射

| 管理端页面含义 | API 字段 | 类型/规则 |
| --- | --- | --- |
| 高校 ID | `id` | int64，只读 |
| 高校名称 | `name` | string，必填，最长 128 |
| 高校简介 | `intro` | string，可空 |
| 多边形点 | `polygonPoints` | array，至少 3 个不同点 |
| 点纬度 | `polygonPoints[].latitude` | decimal，`-90..90` |
| 点经度 | `polygonPoints[].longitude` | decimal，`-180..180` |
| 状态 | `status` | `ENABLED` / `DISABLED` |
| 编辑时图片选择 | `imageResourceIds` | int64 array，最多 20 个；数组顺序即展示顺序 |
| 返回图片 | `images[]` | `{resourceId,url,sortNo}`；仅返回有效资源 |
| 创建/更新时间 | `createdAt` / `updatedAt` | ISO-8601 时间戳，只读 |

现有页面/mock 的明确映射如下：

| 当前前端 | 后端契约 |
| --- | --- |
| `query.size` | `pageSize` |
| `query.show=true/false` | `status=ENABLED/DISABLED` |
| `data.list` | 统一响应中的 `data.items` |
| `area[].lng` / `area[].lat` | `polygonPoints[].longitude` / `polygonPoints[].latitude` |
| `show=true/false` | `status=ENABLED/DISABLED`；切换时调用独立 status 接口 |
| `createTime` | `createdAt` |
| `res.ok` | 判断 HTTP 状态及统一响应 `code === "OK"` |

当前页面还提交 `address`、`contact`、`phone`，但 v1.7 PRD 与现有 `university` 表没有这些字段。窗口 03A 不擅自扩表，真实联调前需由产品确认是删除前端字段还是同步修改 PRD/Flyway/OpenAPI/测试。当前 `keyword` 仅按高校名称搜索，不能实现页面文案中的“名称 / 地址”。页面提示“最后一个与第一个闭合”，后端采用至少 3 个不同点的口径，不要求重复首点；前端可按绘图库需要自行闭合显示。

## 接口与行为

- `GET /api/v1/admin/universities?page=1&pageSize=20&keyword=&status=`：默认分页，最大 100；按 `createdAt DESC, id DESC` 稳定排序；逻辑删除记录不可见。
- `GET /api/v1/admin/universities/{id}`：详情；不存在或已删除返回 `404 / UNIVERSITY_NOT_FOUND`。
- `POST /api/v1/admin/universities`：新增，初始状态固定为 `ENABLED`。
- `PUT /api/v1/admin/universities/{id}`：编辑基础信息并整体替换图片关联。
- `PATCH /api/v1/admin/universities/{id}/status`：请求体 `{ "status": "ENABLED|DISABLED" }`。
- `DELETE /api/v1/admin/universities/{id}`：逻辑删除，不物理删除记录。

统一响应为 `{code,message,data,requestId}`。写操作读取或生成 `X-Request-Id`，响应头回传，并写入不含请求正文的审计日志。

## 图片关联约束

本窗口管理已有 `file_resource` 的关联，不包含上传；上传接口属于窗口 03D。写入的资源必须为 `ACTIVE` 且未逻辑删除，ID 不得重复。编辑时省略或传空数组表示清空高校图片关联。

## 数据库与回滚

V001 已包含高校表、文件资源表及 `university_file_relation`，窗口 03A 未改变表结构，因此没有新增 Flyway 迁移。代码回滚不会破坏已有高校数据；若回滚业务代码，新增数据仍保持兼容。
