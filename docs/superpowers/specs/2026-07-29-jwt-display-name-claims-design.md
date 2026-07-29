# JWT 展示名称声明设计

## 目标

在现有 Sa-Token `StpLogicJwtForSimple` 签发的 JWT 中，分别为普通用户和管理员加入可展示的昵称声明，供服务端在验签后读取。

## 范围

- 普通用户 token 写入 `nickname`。
- 管理员 token 写入 `nickname`。
- 不写入邮箱、密码、权限角色或其他敏感信息。
- 不改变当前 `login` 与 `admin` 两套登录类型、路由鉴权规则或 Simple JWT 的服务端登录态依赖。

## 签发与读取

- 普通用户注册、登录时，`AuthService` 通过 `SaLoginModel` 的 extra-data 将 `nickname` 传给 `StpUtil.login`。
- 管理员登录时，`AdminTokenService` 接收昵称，并通过 `SaLoginModel` 的 extra-data 调用独立的 `AdminStpUtil` 登录逻辑。
- 读取方使用 `SaJwtUtil.getPayloads(token, loginType, jwtSecret)` 验签后获取 `nickname`，不得用未验签的 Base64 解码结果作权限或业务判断。

## 数据一致性与安全边界

- JWT 是签发时快照。昵称更新后，已签发 token 内的昵称不变；用户或管理员下次登录获得新 token 后才更新。
- `nickname` 仅用于展示，不用于鉴权、授权或识别当前用户；当前身份仍以 Sa-Token 校验得到的登录 ID 为准。
- 本次不把 JWT 改为无状态模式，因此重启后是否保留登录态仍由 Sa-Token DAO 的持久化策略决定。

## 验证

- 为普通用户和管理员的签发逻辑添加回归测试。
- 测试对各自 token 使用对应 loginType 与密钥验签，并断言能取得预期 `nickname`。
- 测试不应把密码或邮箱作为 JWT 声明。
- 执行受影响后端测试及 `mvn test`。
