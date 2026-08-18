# Map Vendor 数据库初始化

数据库固定基线为 MySQL Community Server 9.0.1、`utf8mb4` 和严格 SQL mode。表结构由 Flyway 迁移维护，业务账号不应拥有 DDL 权限。

## 初始化步骤

1. 使用管理员账号打开 `create_database.sql`，先替换两个 `CHANGE_ME_*` 密码，再执行脚本。
2. 后端配置 Flyway 使用 `map_vendor_flyway`，业务数据源使用 `map_vendor_app`。
3. 新库让 Flyway 依次执行 V001、V002；已有 V001 数据库先停写、备份并执行 `precheck_v002.sql`，归档结果后再迁移。
4. 使用迁移账号连接 `map_vendor`，执行 `verify_schema.sql`；业务表数必须为 `12`，所有 `legacy_*_should_be_0` 必须为 `0`。

也可以使用项目内的验证脚本执行真实 Flyway 迁移和校验（密码只作为当前进程参数，不写入项目文件）：

```powershell
.\deploy\mysql\verify_flyway.ps1 -FlywayPassword '<迁移账号密码>'
```

示例（PowerShell；密码由客户端提示输入，不写入命令历史）：

```powershell
mysql -h 127.0.0.1 -P 3306 -u root -p < deploy/mysql/create_database.sql
mysql -h 127.0.0.1 -P 3306 -u map_vendor_flyway -p map_vendor < deploy/mysql/verify_schema.sql
```

## 设计说明

- 订单仅允许 `PENDING`、`CONFIRMED`、`CANCELLED` 三种状态。
- 统计索引以 `service_date` 为首要业务日期。
- `openid` 直接保存在订单中；`wx_user` 只是内部映射，不构成后端微信登录。
- 车辆、房型、商家、高校和文件使用逻辑删除字段；订单没有删除设计。
- 图片通过 `file_resource` 和关联表维护，不使用逗号拼接路径。
- 订单类型字段由数据库 `CHECK` 约束兜底；“车辆/房型必须属于订单商家”仍需应用服务在同一事务内校验，因为 MySQL 外键无法表达跨表同商家约束。
- 订单状态更新应由应用执行 `UPDATE ... WHERE id = ? AND status = 'PENDING' AND version = ?`，并在同一事务写入 `order_status_log`。
- Java 后端不维护管理员账号或密码，也不创建默认管理员。不可变 V001 中的账号遗留由 V002 删除；最终结构不含 `sys_admin`、`merchant_account` 或 `token_version`。

## V002 发布与恢复

- V002 前停止写入并备份全库，额外归档两张账号表、所有 `MERCHANT` 行、约束定义和 `flyway_schema_history`。
- V002 将历史 `MERCHANT` 归一为 `ADMIN`，并在原因中保留 `[LEGACY_MERCHANT]` 标记；随后收紧 CHECK 并删除账号表。
- MySQL DDL 隐式提交，不提供自动 down migration。中途失败时保持停机，从备份恢复到新实例后切回；不得盲目执行 Flyway `repair`。
- 迁移成功后只能部署不依赖账号表、Principal 或 `MERCHANT` 的构建。旧构建若存在这些依赖，应用和数据库必须一起恢复。

本机当前检测到的服务为 MySQL Community Server 9.0.1（Windows 服务 `MySQL90`，端口 3306），与项目基线一致。

## Flyway 兼容性注意事项

MySQL 9 已被新版 Flyway 支持，但当前新版 Flyway 要求 Java 17+；项目后端仍固定为 JDK 1.8 与 Spring Boot 2.7.x。落地后端构建时必须单独验证所选旧版 Flyway 对 MySQL 9.0.1 的迁移、校验与修复行为，不能仅依赖 Spring Boot 默认依赖版本。数据库初始化阶段可先由 MySQL CLI 执行同一份版本化脚本，后续接入 Flyway 时需正确建立或基线化 schema history，避免重复执行。
