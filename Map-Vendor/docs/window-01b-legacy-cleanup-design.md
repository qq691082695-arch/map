# 窗口 01B：数据库与代码遗留清理设计

状态：设计完成，待窗口 02B 实施。本文不修改已执行的 `V001__baseline.sql`，不执行删表或数据改写。

## 1. 范围与结论

本窗口只设计已取消的“商家作为系统角色”遗留清理。目标系统只有微信小程序用户和平台管理员；`business` 始终是服务商业务数据，不是登录主体。

结论如下：

1. 窗口 02A 复核后，统一新增单个后向迁移 `V002__remove_auth_and_merchant_role_legacy.sql`：先归一历史 `MERCHANT` 订单数据和日志，再收紧 CHECK，最后删除 `merchant_account` 与 `sys_admin`。
2. 不修改 V001。空库仍按 `V001 -> V002` 建库；已有 V001 数据库只执行 V002，最终结构一致。
3. 历史 `MERCHANT` 操作不能静默伪装成新的业务行为。迁移将其归一为 `ADMIN`，同时在原因中加入固定审计标记 `[LEGACY_MERCHANT]`，保留原始原因。
4. 同一发布窗口删除 Java 枚举、商家作用域、商家 OpenAPI 分组、商家安全规则和相关测试遗留；不得新增替代性的商家认证实现。
5. `sys_admin`、管理员登录、安全组件和 `token_version` 已由窗口 02A 完成盘点；实施时与本设计合并到同一 V002/02B 发布单元，详细顺序见 `window-02a-auth-legacy-cleanup-design.md`。

## 2. 已盘点遗留

### 2.1 数据库

| 位置 | 遗留 | 影响 |
|---|---|---|
| `V001__baseline.sql` | `merchant_account` 表及到 `business` 的外键 | 把服务商错误建模为账号主体 |
| `reserve_order.ck_reserve_order_cancel_fields` | `cancel_source` 允许 `MERCHANT` | 目标值域应只有 `USER`、`ADMIN` |
| `order_status_log.ck_order_status_log_operator` | `operator_type` 允许 `MERCHANT` | 目标值域应只有 `USER`、`ADMIN`、`SYSTEM` |
| `deploy/mysql/verify_schema.sql` | 把 `merchant_account`、`sys_admin` 计入目标表 | 清理后会产生错误验收结果 |
| `BaselineMigrationContractTest` | 断言 V001 必须创建 `merchant_account`、`sys_admin` | 只验证历史基线，不能验证迁移后的目标结构 |

V001 还包含 `sys_admin` 和两个账号表的 `token_version`。它们不属于 01B 的商家专项实施范围，但必须在 02A 盘点、02B 清理。

### 2.2 Java、路由和文档生成配置

| 文件/位置 | 遗留 | 后续动作 |
|---|---|---|
| `module/merchant/package-info.java` | 商家模块占位 | 删除目录/文件 |
| `BackendPrincipal` | `ADMIN/MERCHANT` 角色、businessId、tokenVersion | 02B 随全量登录遗留删除 |
| `MerchantScope` | 商家数据权限检查 | 删除 |
| `CancelSource` | Java 值 `MERCHANT` | 删除该枚举值 |
| `SecurityConfig` | `/api/v1/merchant/**`、管理员/商家认证规则及密码编码器 | 02B 按无后端登录边界整体改造；不得只放行 merchant 路由 |
| `OpenApiConfig` | merchant 分组、Bearer/JWT、商家端描述 | 删除 merchant 分组；02B 删除整个后端令牌契约 |
| `module/statistics/package-info.java` | “merchant statistics” 描述 | 改为仅平台全局统计 |
| `MerchantScopeTest` | 固化商家主体与跨商家权限 | 删除 |
| `ArchitectureRulesTest` | 只禁止 App 依赖 `BackendPrincipal` | 改成全源码禁止认证 Principal、merchant 包和 merchant 路由 |

当前未发现可调用的 Merchant Controller，但配置已预留 `/api/v1/merchant/**` 和 OpenAPI 分组，因此仍构成必须清理的入口契约。

## 3. V002 迁移设计

### 3.1 发布前只读预检

在生产备份完成后执行并留存结果：

```sql
SELECT cancel_source, COUNT(*)
FROM reserve_order
GROUP BY cancel_source;

SELECT operator_type, COUNT(*)
FROM order_status_log
GROUP BY operator_type;

SELECT COUNT(*) AS merchant_account_count FROM merchant_account;

SELECT ro.id, ro.order_no, ro.cancel_reason
FROM reserve_order ro
WHERE ro.cancel_source = 'MERCHANT';
```

若发现 V001 定义以外的值，停止迁移并人工核查；不得用兜底 UPDATE 吞掉未知值。迁移前必须做数据库备份，并单独导出 `merchant_account` 及所有 `MERCHANT` 行作为审计归档。

### 3.2 确定的数据归一规则

| 原字段 | 原值 | 目标值 | 审计保留 |
|---|---|---|---|
| `reserve_order.cancel_source` | `MERCHANT` | `ADMIN` | `cancel_reason` 前置 `[LEGACY_MERCHANT] `；原因为 NULL 时写入 `[LEGACY_MERCHANT] 历史商家角色取消` |
| `order_status_log.operator_type` | `MERCHANT` | `ADMIN` | `reason` 使用同一标记；原因为 NULL 时写入 `[LEGACY_MERCHANT] 历史商家角色操作` |

选择 `ADMIN` 是因为旧商家取消属于人工业务方取消，且目标 `cancel_source` 没有 `SYSTEM`。固定标记明确说明它不是新模型下的平台管理员操作，避免静默篡改审计语义。更新时使用 `LEFT(reason, 500)` 控制字段长度，并在迁移前归档完整原值。

### 3.3 SQL 执行顺序

V002 按以下固定顺序编写：

1. 删除 `reserve_order.ck_reserve_order_cancel_fields`。
2. 删除 `order_status_log.ck_order_status_log_operator`。
3. 更新 `reserve_order.cancel_source='MERCHANT'` 的行并写入审计标记。
4. 更新 `order_status_log.operator_type='MERCHANT'` 的行并写入审计标记。
5. 断言两表已不存在 `MERCHANT`；Flyway SQL 可用存储过程或单独 Java migration 抛错，不能依赖人工目测。
6. 重新创建 `ck_reserve_order_cancel_fields`，取消来源只允许 `USER/ADMIN`，并继续保证管理员取消原因非空。
7. 重新创建 `ck_order_status_log_operator`，只允许 `USER/ADMIN/SYSTEM`。
8. 删除 `merchant_account`。其唯一外键是该表指向 `business`，所以无需改变业务表或历史订单。

约束必须“先删除旧约束、再改数据、再建新约束”；提前建新约束会被历史 `MERCHANT` 行阻断。删除 `merchant_account` 必须最后进行，确保前面失败时账号审计数据仍在。

MySQL DDL 会隐式提交，V002 不能被描述为全事务回滚。发布前必须在 V001 快照副本上演练；迁移失败时停止应用发布，依据 Flyway 失败记录和备份恢复，不执行 `repair` 后盲目重跑。

## 4. 空库与已迁移数据库兼容

### 空库

Flyway 顺序执行不可变的 V001，再执行 V002。虽然空库会短暂创建遗留表/旧 CHECK，但迁移结束后的目标结构不包含 `merchant_account` 和 `MERCHANT`。验收针对 `flyway_schema_history` 完成后的最终结构，不以 V001 文本作为目标结构。

### 已执行 V001 的数据库

维护窗口内先停写，备份和预检后执行 V002，再部署删除遗留代码的应用。当前仓库尚无商家业务 Controller，可采用停机迁移，不需要双写或兼容视图。不得先部署仍可能写入 `MERCHANT` 的旧代码后再收紧约束。

### 重跑与漂移

V002 是版本迁移，不写 `IF EXISTS` 掩盖结构漂移。测试分别从干净库和真实 V001 快照升级；若约束名或表结构与 V001 不同，迁移应失败并要求排查。不要修改 V001 checksum，也不要用 Flyway `repair` 把未经核查的漂移标记为成功。

## 5. 代码清理实施顺序（窗口 02B）

1. 增加 V002 及 MySQL 9.0.1 集成测试。
2. 删除 `CancelSource.MERCHANT`、`MerchantScope`、`module/merchant` 与 `MerchantScopeTest`。
3. 删除 merchant OpenAPI 分组、描述和 `/api/v1/merchant/**` 安全匹配；结合 02A 结论同步删除全部登录/令牌组件。
4. 更新统计包描述、数据库目标结构校验脚本和契约测试。
5. 增加负向源码/路由契约：生产源码和生成 OpenAPI 中不得出现 merchant 角色入口、`MERCHANT` 操作来源、Bearer/JWT 登录契约。
6. 先完成迁移，再启动新应用；验证 App 与 Admin 业务接口不依赖账号表或 Principal。

## 6. 必须新增或调整的测试

- 空库路径：V001 + V002 成功，`merchant_account` 不存在，新 CHECK 不接受 `MERCHANT`。
- 升级路径：准备含 `merchant_account`、`reserve_order.cancel_source=MERCHANT`、`order_status_log.operator_type=MERCHANT` 的 V001 数据，升级后行数不变、状态不变、原因带审计标记、目标枚举合法。
- NULL 与 500 字符原因：迁移后原因非空且不超过列长，原完整值可在迁移前归档中追溯。
- 约束测试：`USER`、`ADMIN` 和日志 `SYSTEM` 合法；两处 `MERCHANT` 均被数据库拒绝。
- 结构测试：目标表清单不含 `merchant_account`；业务表、外键和订单索引仍存在。
- 源码契约：无 `module.merchant`、`MerchantScope`、`BackendPrincipal.Role.MERCHANT`、merchant OpenAPI 分组或 `/api/v1/merchant/**`。
- 回归：现有地图集成测试和全量 Maven 测试通过；使用 MySQL 9.0.1 验证，H2 不能替代最终迁移验收。

## 7. 回滚与恢复策略

该清理包含删表和 MySQL 非事务 DDL，不提供自动 down migration。

- V002 执行前：全库备份；额外导出 `merchant_account`、两类 `MERCHANT` 行、表结构和 Flyway 历史。
- V002 尚未开始：直接取消发布。
- V002 中途失败：保持应用停机，记录已完成语句；优先从备份恢复到新实例并切回，而不是在生产库手工逆向拼接约束。
- V002 成功但应用回归失败：可回滚应用到不写 `MERCHANT`、不依赖账号表的兼容版本；若旧应用依赖商家账号/Principal，则必须同时恢复数据库备份，不能单独回滚应用。
- 审计恢复：`[LEGACY_MERCHANT]` 标记与迁移前导出共同保存原语义；不得在迁移后删除这些标记。

## 8. 02B 验收清单

- [ ] V001 文件及 checksum 未变化。
- [ ] 空库和 V001 升级路径均在 MySQL 9.0.1 成功。
- [ ] `merchant_account` 不存在，`business` 及历史订单无数据丢失。
- [ ] `reserve_order.cancel_source` 只接受 `USER/ADMIN`。
- [ ] `order_status_log.operator_type` 只接受 `USER/ADMIN/SYSTEM`。
- [ ] 历史 `MERCHANT` 行数量可对账，原因包含审计标记。
- [ ] 源码、测试、生成 OpenAPI 和路由中没有可调用的商家角色入口。
- [ ] `deploy/mysql/verify_schema.sql` 校验最终目标结构，不再把遗留账号表计为业务表。
- [ ] 全量测试通过，并记录备份、迁移、验证和恢复演练结果。
