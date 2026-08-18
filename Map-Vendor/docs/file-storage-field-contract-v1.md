# 窗口 03D 文件上传与资源关联契约

- 上传：`POST /api/v1/admin/files/images`，`multipart/form-data`，字段名 `file`。
- 允许：JPEG、PNG、WebP；单文件最大 10 MiB。扩展名、声明 MIME 与内容签名必须一致，原始文件名不得含路径片段。
- 响应：`resourceId`、公开 `url`、`mimeType`、`sizeBytes`。数据库仅保存相对 `storageKey`、URL、MIME、大小、SHA-256 和原始名称，不保存二进制正文或绝对物理路径。
- 高校和服务商通过 `imageResourceIds` 有序关联多图；车辆、房型、菜品通过 `imageResourceId` 关联单图。只接受启用且未删除的文件资源。
- 本地文件目录由 `MAP_VENDOR_STORAGE_ROOT` 指定，默认 `D:/Map-Vendor-Data/images`；URL 前缀由 `MAP_VENDOR_PUBLIC_BASE_URL` 指定，默认 `/files`。`MAP_VENDOR_STORAGE_MINIMUM_FREE_BYTES` 默认 5 GiB，低于阈值会写告警日志，无法容纳当前文件时拒绝上传。目录必须位于源码和 Jar 之外，并纳入每日增量/每周全量备份与定期孤儿文件清理。
- Java 应用提供 `/files/**` 受控读取用于本地联调；生产 Nginx 可用同一目录的只读 `alias` 提供文件，必须启用 HTTPS 和 `nosniff`。

前端流程：先上传获取 `resourceId`，再在高校/服务商/附属资源保存请求中提交该 ID。上传成功不自动建立业务关联。
