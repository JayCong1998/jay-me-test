# JWT 展示名称声明实现计划

> **面向 AI 代理的工作者：** 必须使用子技能：使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 让普通用户和管理员登录时签发的 JWT 均包含经签名的 `nickname` 扩展声明。

**架构：** 普通用户在 `AuthService` 签发 token 时用 `SaLoginModel` 传入 extra-data；管理员通过扩展 `AdminStpUtil` 与 `AdminTokenService` 的登录参数传递相同数据。测试直接验证 Sa-Token Simple JWT 对扩展声明的签发与验签读取，服务测试验证昵称从实体流向签发层。

**技术栈：** Java 21、Spring Boot 3、Sa-Token 1.38.0、JUnit 5、Mockito。

---

## 文件职责

- 修改 `backend/src/main/java/com/jaymetest/service/AuthService.java`：普通用户注册、登录时将昵称加入 Sa-Token extra-data。
- 修改 `backend/src/main/java/com/jaymetest/admin/AdminStpUtil.java`：提供接收 extra-data 的管理员登录入口。
- 修改 `backend/src/main/java/com/jaymetest/service/admin/AdminTokenService.java`：将管理员昵称传给独立管理员登录逻辑。
- 修改 `backend/src/main/java/com/jaymetest/service/admin/AdminAuthService.java`：登录成功后将实体昵称传给 token 服务。
- 创建 `backend/src/test/java/com/jaymetest/auth/JwtDisplayNameClaimsTest.java`：验证普通与管理员登录类型的 JWT 都能验签读取昵称，且类型不能混用。
- 修改 `backend/src/test/java/com/jaymetest/service/admin/AdminAuthServiceTest.java`：验证管理员昵称被传递给 token 服务。

### 任务 1：为 JWT 扩展声明建立失败测试

**文件：**
- 创建：`backend/src/test/java/com/jaymetest/auth/JwtDisplayNameClaimsTest.java`

- [ ] **步骤 1：编写失败的 JWT 声明测试**

```java
@Test
void signedUserTokenExposesNicknameOnlyForUserLoginType() {
    StpLogic logic = new StpLogicJwtForSimple();
    String token = logic.createTokenValue(42L, "default-device", 3600,
            Map.of("nickname", "Jay"));

    assertEquals("Jay", SaJwtUtil
            .getPayloads(token, "login", JWT_SECRET)
            .getStr("nickname"));
    assertThrows(SaJwtException.class,
            () -> SaJwtUtil.getPayloads(token, "admin", JWT_SECRET));
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn -Dtest=JwtDisplayNameClaimsTest test`

预期：失败，因为测试类尚不存在。

- [ ] **步骤 3：实现普通与管理员 JWT 声明测试**

```java
private static final String JWT_SECRET = "jaymetest2024secretkeyformusic";

private StpLogicJwtForSimple jwtLogic(String loginType) {
    return new StpLogicJwtForSimple(loginType) {
        @Override
        public String jwtSecretKey() {
            return JWT_SECRET;
        }
    };
}
```

补充管理员断言：以 `jwtLogic("admin")` 生成 token、读取 `nickname` 为 `Operator`，并断言用 `login` 类型验签失败。

- [ ] **步骤 4：运行测试确认通过**

运行：`mvn -Dtest=JwtDisplayNameClaimsTest test`

预期：PASS，两个登录类型均可读取自己的昵称且不能交叉验签。

### 任务 2：将管理员昵称传入 token 签发层

**文件：**
- 修改：`backend/src/main/java/com/jaymetest/admin/AdminStpUtil.java`
- 修改：`backend/src/main/java/com/jaymetest/service/admin/AdminTokenService.java`
- 修改：`backend/src/main/java/com/jaymetest/service/admin/AdminAuthService.java`
- 修改：`backend/src/test/java/com/jaymetest/service/admin/AdminAuthServiceTest.java`

- [ ] **步骤 1：扩展现有管理员服务测试，使其失败**

在 `loginReturnsAdminProfileAndTokenWhenPasswordMatches` 中把 stub 和断言改为：

```java
when(adminTokenService.login(7L, "Operator")).thenReturn("admin-token");

verify(adminTokenService).login(7L, "Operator");
```

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn -Dtest=AdminAuthServiceTest test`

预期：编译失败，因为 `AdminTokenService.login(long, String)` 尚不存在。

- [ ] **步骤 3：实现最小管理员签发参数传递**

在 `AdminStpUtil` 新增：

```java
public static void login(Object id, Map<String, Object> extraData) {
    STP_LOGIC.login(id, new SaLoginModel().setExtraData(extraData));
}
```

将 `AdminTokenService.login` 改为：

```java
public String login(long adminId, String nickname) {
    AdminStpUtil.login(adminId, Map.of("nickname", nickname));
    return AdminStpUtil.getTokenValue();
}
```

并在 `AdminAuthService.login` 中调用 `adminTokenService.login(admin.getId(), admin.getNickname())`。

- [ ] **步骤 4：运行测试确认通过**

运行：`mvn -Dtest=AdminAuthServiceTest test`

预期：PASS，管理员登录使用实体中的昵称签发 token。

### 任务 3：将普通用户昵称写入注册与登录 token

**文件：**
- 修改：`backend/src/main/java/com/jaymetest/service/AuthService.java`

- [ ] **步骤 1：定义普通用户签发调用的预期**

在 `AuthService` 的注册与登录路径中，将现有调用替换为下列目标形式：

```java
StpUtil.login(user.getId(), new SaLoginModel()
        .setExtra("nickname", user.getNickname()));
String token = StpUtil.getTokenValue();
```

- [ ] **步骤 2：运行后端编译确认当前调用不可用**

运行：`mvn -DskipTests compile`

预期：在导入或 API 调用尚未补齐时编译失败。

- [ ] **步骤 3：实现最小普通用户 extra-data 签发代码**

导入 `cn.dev33.satoken.stp.SaLoginModel`，在 `register` 与 `login` 的两处 `StpUtil.login(user.getId())` 改为：

```java
StpUtil.login(user.getId(), new SaLoginModel()
        .setExtra("nickname", user.getNickname()));
```

- [ ] **步骤 4：运行后端编译确认通过**

运行：`mvn -DskipTests compile`

预期：BUILD SUCCESS。

### 任务 4：执行完整验证

**文件：**
- 不修改文件。

- [ ] **步骤 1：运行 JWT 声明与管理员服务测试**

运行：`mvn -Dtest=JwtDisplayNameClaimsTest,AdminAuthServiceTest test`

预期：全部 PASS。

- [ ] **步骤 2：运行全量后端测试**

运行：`mvn test`

预期：BUILD SUCCESS，测试无失败。

- [ ] **步骤 3：检查最终变更范围**

运行：`git diff --check; git status --short`

预期：无空白错误；仅报告本计划涉及的代码、测试和文档改动，以及执行前已存在的用户改动。
