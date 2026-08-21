# 窗口 02A：全量登录认证遗留清理设计

状态：设计完成，待窗口 02B 实施。本窗口只盘点和冻结清理方案，不修改已执行的 `V001__baseline.sql`，不删表、不删除认证代码。

## 1. 结论与目标边界

1. Java 后端不提供管理员或商家登录、改密、密码校验、Session、JWT、Bearer Token、刷新/注销。小程序仅允许一次性 `code2Session` 静默交换并返回 `openid`，不建立服务端登录态。
2. `/api/v1/admin/**` 当前不做后端身份认证或授权；管理员后台的前端登录只属于前端本地门禁，不得调用 Java 登录接口，也不得把本地 token 放入后端 `Authorization` 契约。
3. `/api/v1/app/**` 接收 uni-app 缓存并传入的 `openid` 作为订单归属辨别字段。静默交换降低随意构造概率，但订单接口未绑定服务端会话，`openid` 仍不是完整认证凭据。
4. `business` 是服务商业务数据，不是登录主体；不存在商家账号、商家后台、商家认证上下文、商家数据权限或 `/api/v1/merchant/**`。
5. 窗口 02B 应以一个发布单元完成数据库迁移、后端代码删除、契约/测试/部署校验更新；不得新增替代登录实现。

## 2. 全量遗留盘点

### 2.1 数据库与 Flyway

| 位置 | 遗留 | 影响 | 02B 动作 |
|---|---|---|---|
| `V001__baseline.sql` | `sys_admin`：`username`、`password_hash`、`token_version`、`last_login_at`、状态与唯一索引 | 固化已取消的管理员后端账号体系 | 通过新迁移删除整表；不修改 V001 |
| `V001__baseline.sql` | `merchant_account`：账号、密码、`token_version`、登录时间及 `business` 外键 | 把服务商错误建模为登录主体 | 按 01B 数据归一方案最后删除整表 |
| `reserve_order` CHECK | `cancel_source` 仍允许 `MERCHANT` | 保留商家操作来源 | 先归一历史数据，再收紧为 `USER/ADMIN` |
| `order_status_log` CHECK | `operator_type` 仍允许 `MERCHANT` | 保留商家认证/操作语义 | 先归一历史数据，再收紧为 `USER/ADMIN/SYSTEM` |
| `deploy/mysql/verify_schema.sql` | 把两个账号表计入目标业务表 | 会把遗留结构误判为正确 | 改为校验账号表不存在及目标业务表完整 |
| `BaselineMigrationContractTest` | 断言 V001 创建两个账号表 | 只验证历史基线，不能验证最终结构 | 保留 V001 不变证据；新增 V001→V002 最终结构测试 |

未发现其他业务表引用 `sys_admin`；`merchant_account` 的唯一业务外键为其自身指向 `business`。`token_version` 只存在于两个遗留账号表及 `BackendPrincipal`，因此无需迁移到其他表。

### 2.2 Java、依赖与运行时安全规则

| 文件/位置 | 遗留 | 02B 动作 |
|---|---|---|
| `security/BackendPrincipal.java` | `ADMIN/MERCHANT`、账号 ID、businessId、tokenVersion | 删除 |
| `security/MerchantScope.java` | 商家主体与数据归属授权 | 删除 |
| `security/SecurityConfig.java` | `PasswordEncoder`/BCrypt、无状态 Session 策略、Admin/Merchant `authenticated()` 规则 | 删除整个认证配置；若仅为 CORS/CSRF 保留过滤链，必须明确 Admin/App 均 `permitAll` 且不含认证组件 |
| `RestAuthenticationEntryPoint` | 401“请先登录”处理 | 删除 |
| `RestAccessDeniedHandler` | Spring Security 授权失败处理 | 随认证链删除；业务 403 继续由统一业务异常处理 |
| `spring-boot-starter-security` | 引入完整认证自动配置 | 若无其他安全用途则从 `pom.xml` 删除 |
| `MapVendorApplication` | 排除 `UserDetailsServiceAutoConfiguration` | Security 依赖删除后同步删除该排除项 |
| `OpenApiConfig` | JWT Bearer scheme、商家 API 描述与分组 | 删除 security scheme、商家描述和 merchant 分组；保留 app/admin 业务分组 |
| `CorsConfig` | 允许 `Authorization` 请求头 | 删除该头，仅保留业务所需头 |
| `module/merchant/package-info.java` | 商家模块占位 | 删除 |
| `module/statistics/package-info.java` | 商家统计描述 | 改为平台管理员全局统计 |
| `CancelSource.MERCHANT` | 商家取消来源 | 删除；与数据库迁移同窗口发布 |

当前不保留账号 Repository/Mapper/Service、JWT 解析或签发器、认证 Filter、`UserDetailsService`、Session 存取。小程序静默身份交换是唯一例外，不属于账号登录体系。

### 2.3 测试、CI 与生成契约

| 位置 | 当前问题 | 02B 动作 |
|---|---|---|
| `MerchantScopeTest` | 固化商家 Principal 与跨商家授权 | 删除 |
| `ArchitectureRulesTest` | 只禁止 App 依赖 `BackendPrincipal`，覆盖不足 | 增加全源码负向规则/文本扫描 |
| `.github/workflows/ci.yml` | 只扫描微信后端登录关键字 | 扩展为登录路由、JWT/Session、账号表目标结构、merchant 路由等禁用项 |
| 动态 OpenAPI | 仍发布 `backendBearer` 与 merchant 分组 | 测试生成文档不含 security scheme、登录路径、merchant 分组 |
| 静态 OpenAPI | 已无登录路径，但管理员无认证边界未写清 | 本窗口补充边界说明 |

负向契约不能机械禁止所有 `password`、`security` 或 `token` 字样：数据库连接密码、Flyway 凭据、文件 SHA-256、说明登录禁用边界的文档均是合法用途。扫描应限定生产 Java 包、API 路径、OpenAPI security scheme 和最终数据库结构。

### 2.4 管理员前端与小程序

管理员前端 `TripAgency-web` 当前存在 `/login` 页面、`stores/auth.js`、路由守卫和 `api/mock.js` 的本地 `admin/123456`、mock token。它们完全在前端 mock 中运行，当前没有 Java 登录接口，但默认弱口令和“后续接真实后端”的注释容易导致边界回退。

02B 后端清理不得以此前端 token 作为请求凭据。管理员前端实施窗口应改为项目明确批准的纯前端门禁方案，并至少做到：不硬编码默认密码、不把本地 token 当成后端授权、不发送 `Authorization`、不新增 `/api/v1/admin/login`。小程序由 uni-app 静默取得临时 code，再由 Java 后端交换 openid；后端不得返回 session_key 或签发令牌。

## 3. Flyway 兼容方案

### 3.1 迁移边界与文件

沿用 01B 的单一后向迁移设计，建议命名 `V002__remove_auth_and_merchant_role_legacy.sql`，一次完成：

1. 对 `MERCHANT` 订单取消和状态日志按 01B 规则归一为 `ADMIN`，保留 `[LEGACY_MERCHANT]` 审计标记。
2. 重建两个 CHECK，移除 `MERCHANT`。
3. 删除 `merchant_account`。
4. 删除 `sys_admin`。

不单独迁移 `token_version`：它只属于即将删除的账号表，删表即完成字段清理。不得修改已执行的 V001 或 checksum，也不得用 `IF EXISTS` 掩盖结构漂移。

### 3.2 发布前预检与归档

维护窗口停写后，备份全库并单独归档：

- `sys_admin`、`merchant_account` 的表结构、全部数据和行数；
- `reserve_order.cancel_source='MERCHANT'` 行；
- `order_status_log.operator_type='MERCHANT'` 行；
- `flyway_schema_history`、V001 checksum、相关 CHECK 定义；
- 查询是否存在仓库 V001 未定义的账号表外键、视图、触发器或存储程序依赖。

发现结构漂移或未知枚举值时停止迁移并人工核查，不用兜底 UPDATE 或 `repair` 跳过问题。

### 3.3 空库与升级路径

- 空库：依次执行不可变 V001 和 V002；账号表只短暂存在，最终结构不含 `sys_admin`、`merchant_account` 或 `token_version`。
- 已执行 V001：停写、备份、预检后执行 V002，再启动删除认证依赖的新应用。
- 应用顺序：不得先启动仍将 Admin/Merchant 要求认证的旧应用，也不得在数据库删表后回滚到依赖账号表的旧构建。

MySQL DDL 隐式提交，V002 不具备整体事务回滚。必须在 V001 快照副本上演练；失败时保持停机，优先从备份恢复到新实例并切回。

## 4. 代码与契约实施顺序（窗口 02B）

1. 新增 V002、预检 SQL 和空库/升级路径 MySQL 9.0.1 集成测试。
2. 删除 `BackendPrincipal`、`MerchantScope`、认证异常处理器、商家模块占位、`CancelSource.MERCHANT` 及对应测试。
3. 删除 Spring Security 登录认证骨架、BCrypt Bean、账号认证规则和无用依赖；保证 `/api/v1/admin/**` 与 `/api/v1/app/**` 不依赖 Principal/Session/Token。
4. 删除动态 OpenAPI Bearer scheme、merchant 分组和 `Authorization` CORS 头，更新说明与生成契约测试。
5. 更新数据库最终结构校验、部署说明、统计描述和 CI 负向契约。
6. 先执行迁移，再部署新应用；运行全量回归并验证 Nginx 边界。

## 5. `/admin/**` 无后端认证风险与部署控制

这是已接受但高风险的产品边界：任何能访问 Admin API 的客户端都可尝试执行管理操作。前端登录不能构成服务端安全边界，也不能替代网络隔离。

上线前必须同时落实：

- 应用只监听受控接口或 `127.0.0.1:8080`，禁止 8080 直接暴露公网；
- Nginx 对 `/api/v1/admin/` 使用独立 `location`，只允许内网、VPN 或明确 IP 白名单；公开 App API 使用另一 `location`；
- 全站 HTTPS，HTTP 仅重定向；正确传递并校验代理协议头；
- Admin 写接口、下单、查询和导出分别限流；超限返回统一错误并记录 requestId；
- 关键写操作审计至少包含 requestId、动作、目标、结果、来源地址和时间，不记录完整手机号/openid；
- CORS 精确到管理员前端域名，但明确 CORS 不是认证或网络访问控制；
- 管理接口访问日志、限流告警和异常写操作告警纳入上线验收。

当前 Nginx 模板把全部 `/api/` 放在同一公开 location，尚未满足 Admin 独立网络来源限制与限流要求；应在 02B 的部署校验或最迟 09C 收口，生产前不得按现模板直接上线。

## 6. 02B 必测与验收清单

- [ ] V001 文件与 checksum 未变化；空库 V001→V002 和已有 V001→V002 均在 MySQL 9.0.1 成功。
- [ ] 最终结构不存在 `sys_admin`、`merchant_account`、`token_version`，业务表、订单数据与索引完整。
- [ ] 历史 `MERCHANT` 行可对账且带审计标记；新 CHECK 拒绝 `MERCHANT`。
- [ ] 生产源码无登录 Controller、密码校验、Session/JWT/Bearer、认证 Filter、Principal、商家作用域或 `/merchant/**`。
- [ ] `/api/v1/admin/**` 与 `/api/v1/app/**` 业务请求不要求 Authorization、Cookie、Session 或后端登录。
- [ ] 静态和生成 OpenAPI 无 login/logout/token/password 路径、无 security scheme、无 merchant 分组，并明确 openid 不是强认证。
- [ ] CI 负向契约覆盖管理员、商家和微信后端登录遗留；不会误报数据库连接凭据等基础设施密码。
- [ ] Nginx 验证 Admin 独立访问边界、HTTPS、限流、审计头；8080 不对公网监听。
- [ ] 全量 Maven 测试及现有地图集成测试通过。

## 7. 回滚与恢复

本清理包含删表和非事务 DDL，不提供自动 down migration。V002 前必须备份并导出账号遗留及 `MERCHANT` 审计数据。迁移中途失败时保持应用停机，依据已执行语句和 Flyway 记录从备份恢复；不得盲目 `repair` 后重跑。迁移成功但应用回归失败时，只能回滚到不依赖账号表、Principal 或 `MERCHANT` 的兼容构建；否则数据库与应用必须一起从备份恢复。
