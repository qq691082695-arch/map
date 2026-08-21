# 安全与身份边界 v1

状态：窗口 02B 已清理后端登录认证与商家角色遗留。

## 系统角色

系统使用者仅有微信小程序用户和平台管理员。`business` 表示由平台管理员维护的服务商业务数据，不是登录主体；服务商没有账号、后台、登录、令牌、认证上下文或独立数据权限，也不存在 `/api/v1/merchant/**`。

## 小程序接口

- 微信小程序在订单功能需要身份时调用 `wx.login`，将一次性临时 `code` 提交到 `POST /api/v1/app/wechat/session`；Java 后端调用微信 `code2Session` 并只返回 `openid`，不返回 `session_key`，不建立 Session 或小程序 JWT。
- 微信 AppSecret 只能通过服务端环境变量 `MAP_VENDOR_WECHAT_APP_SECRET` 注入，禁止写入源码、前端、OpenAPI、日志或通用配置；生产环境同时显式配置 `MAP_VENDOR_WECHAT_APP_ID`。
- `/api/v1/app/orders` 相关接口仍接收前端传入的 `openid` 作为订单归属辨别字段。静默交换降低了普通客户端随意构造 openid 的概率，但订单接口本身没有校验 openid 与当前微信会话的绑定，不能描述为完整强认证。
- 生产环境必须启用 HTTPS、接口限流和审计日志；日志不得记录完整 `openid` 或手机号。
- 手机号不能替代 `openid` 作为订单归属字段。

## 管理员接口

- 管理员后台登录完全由前端完成，Java 后端不提供管理员账号、登录、改密、密码校验、Session、JWT 或管理端令牌。
- `/api/v1/admin/**` 当前不做后端登录认证。这意味着只要网络可达，请求方即可尝试调用管理接口，是已知的高风险边界。
- 生产部署必须让管理员接口只经 Nginx 暴露，并落实网络来源限制（如内网、VPN 或 IP 白名单）、HTTPS、限流和关键写操作审计；不得将应用 8080 端口直接暴露公网。
- 关键写操作应记录 `requestId`、操作类型、目标资源、结果和时间；敏感字段必须脱敏。

## 订单操作边界

- 新订单固定为 `PENDING`。
- 仅平台管理员可执行 `PENDING -> CONFIRMED`。
- 对应 `openid` 用户或平台管理员可执行 `PENDING -> CANCELLED`；管理员取消原因必填，来源为 `ADMIN`。
- `cancelSource` 仅允许 `USER`、`ADMIN`，不存在 `MERCHANT`。
- `CONFIRMED`、`CANCELLED` 是终态；状态变更使用条件更新或乐观锁，并与状态日志同事务。

## 遗留处理说明

窗口 02B 已新增 V002：归一并标记历史 `MERCHANT` 审计数据，收紧订单约束，删除 `sys_admin`、`merchant_account` 及随表存在的 `token_version`。代码已删除 Spring Security、密码编码器、Principal、商家作用域、认证异常处理和 Bearer/JWT OpenAPI 契约。不可变 V001 保持原 checksum，仅用于历史升级链。
