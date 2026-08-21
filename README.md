# Map Vendor 后端

地图商家预约系统后端基线，严格采用 JDK 8、Spring Boot 2.7.18、MyBatis-Plus、Flyway 和 MySQL 9.0.1 的模块化单体架构。

## 已建立的边界

- 系统使用者只包括微信小程序用户和平台管理员；商家（服务商）仅是平台管理员维护的业务数据，不是系统角色。
- `/api/v1/admin/**` 当前不做后端登录认证；管理员后台登录完全由前端完成。生产环境必须使用 Nginx 网络访问限制、HTTPS、限流和审计日志降低暴露风险。
- 小程序通过 `uni.login` 静默取得一次性 code，并调用 `/api/v1/app/wechat/session` 换取 `openid`；后端不返回 `session_key`，不建立 Session/JWT。订单接口仍以客户端传入的 `openid` 辨别归属，不是完整强认证。
- 后端不提供管理员或商家账号、密码校验、Session、JWT、令牌、商家后台或 `/api/v1/merchant/**`。
- 订单仅有 `PENDING`、`CONFIRMED`、`CANCELLED`；统计业务日期仅使用 `service_date`。
- 数据库结构只通过 `src/main/resources/db/migration` 下的 Flyway 脚本变更。

## 本地准备

1. 使用 `C:\Users\18942\.jdks\corretto-1.8.0_432`，或安装其他 JDK 8，并设置 `JAVA_HOME`。
2. 按 `deploy/mysql/README.md` 创建数据库和最小权限账号。
3. 复制 `.env.example` 中的变量到本机环境，并设置微信 AppID/AppSecret；禁止提交真实密码或 AppSecret。
4. 使用 `mvnw.cmd clean verify` 构建；首次执行会下载固定版本 Maven。
5. 使用 `deploy/windows/run-local.ps1` 启动本地环境，或先设置 `SPRING_PROFILES_ACTIVE=local` 再执行 `mvnw.cmd spring-boot:run`。

启动后可访问：

- 健康检查：`GET http://127.0.0.1:8080/actuator/health`
- 骨架连通检查：`GET http://127.0.0.1:8080/api/v1/app/system/ping`
- OpenAPI：`http://127.0.0.1:8080/swagger-ui.html`

应用端口不应直接暴露到公网；生产环境必须通过 Nginx 和 HTTPS。

## 工程结构

```text
src/main/java/com/mapvendor/
  common/                 统一响应、异常、requestId、MyBatis、OpenAPI
  security/               当前网络边界与遗留安全代码（后续清理窗口处理）
  module/                 admin、university、business、order、statistics
  integration/storage/    可替换的文件存储抽象
src/main/resources/
  db/migration/           Flyway 版本化迁移
deploy/                   数据库、Nginx 和运行环境材料
```

## Flyway 与 MySQL 9 注意事项

为保持 JDK 8，工程显式固定 Flyway 9.22.3，并同时引入该版本拆分出的 `flyway-mysql` 模块。MySQL 9.0.1 的正式环境兼容性必须在本机迁移验证后再放行；不可为了使用要求 Java 17 的新版 Flyway 而升级本项目 JDK。配置迁移账号后执行：

```powershell
.\deploy\mysql\verify_flyway.ps1 -FlywayPassword '<迁移账号密码>'
```

## 环境与日志

- `local`：便于开发阅读的文本日志。
- `prod`：JSON 结构化日志，并强制检查数据库密码、Flyway 密码、精确 CORS、HTTPS 文件地址及源码目录外的绝对存储路径。
- 默认日志目录为 `Map-Vendor/logs`，可通过 `MAP_VENDOR_LOG_PATH` 改为源码目录外的绝对路径。
- `application.log` 保存 INFO 及以上的完整应用日志；`error.log` 只保存 ERROR 及异常堆栈。日志按日期和大小滚动压缩，归档在 `logs/archive`。
- 使用 `deploy/windows/run-local.ps1` 时，Maven、JVM 和 Spring 启动全过程同时写入 `logs/startup-yyyyMMdd-HHmmss.log`；即使应用尚未完成 Logback 初始化，启动错误也不会丢失。
- 访问日志只记录 HTTP 方法、路径、状态码和耗时，不记录查询字符串、请求体、openid、完整手机号或令牌。
- CI 使用 Corretto JDK 8 执行 `clean verify`；微信能力仅允许静默 code2Session 交换，不得扩展为 Session/JWT 登录体系。
