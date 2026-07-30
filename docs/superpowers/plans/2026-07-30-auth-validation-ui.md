# 认证校验与 UI 优化实施计划

> **面向 AI 代理的工作者：** 使用测试先行完成每项行为变更；不要提交 Git，除非用户另行要求。

**目标：** 在登录与注册时提供实时前端校验、准确显示后端错误，并在服务端和数据库层确保昵称唯一。

**架构：** 新增一个前端纯校验模块，由两个认证页面共享。后端通过 DTO 约束和 `AuthService` 业务检查保护注册，再以数据库唯一索引处理并发竞争。Axios 拦截器将所有 HTTP 错误转换成页面可展示的业务消息。

**技术栈：** Vue 3、TypeScript、Vitest、Axios、Spring Boot 3、Jakarta Validation、MyBatis Plus、MySQL 8。

---

### 任务 1：前端认证校验模型

**文件：**

- 新建：`frontend/src/utils/authValidation.ts`
- 新建：`frontend/src/utils/authValidation.test.ts`
- 修改：`frontend/src/pages/LoginPage.vue`
- 修改：`frontend/src/pages/RegisterPage.vue`

- [ ] 编写失败测试，要求 `validateNickname('周')` 返回“昵称长度为 2–10 个字符”，`validatePassword('12345')` 返回“密码长度为 6–10 位”，以及合法输入返回空消息。
- [ ] 运行 `npm run test -- src/utils/authValidation.test.ts`，确认测试因校验模块不存在而失败。
- [ ] 实现 `validateEmail`、`validatePassword`、`validateNickname` 与表单状态计算，分别返回空字符串或用户可见中文错误消息。
- [ ] 重新运行该测试，确认通过。
- [ ] 在两个页面绑定输入和失焦校验，渲染字段级提示，并仅在所有规则通过时启用提交按钮。

### 任务 2：前端错误消息与认证 UI

**文件：**

- 修改：`frontend/src/api/client.ts`
- 新建：`frontend/src/api/client.test.ts`
- 修改：`frontend/src/pages/LoginPage.vue`
- 修改：`frontend/src/pages/RegisterPage.vue`
- 修改：`frontend/src/pages/auth-common.scss`

- [ ] 编写失败测试，模拟 HTTP 400 响应 `{ msg: '邮箱格式不正确' }`，断言客户端拒绝的错误消息为“邮箱格式不正确”。
- [ ] 运行 `npm run test -- src/api/client.test.ts`，确认当前拦截器未提取该消息而失败。
- [ ] 将 HTTP 错误分支改为读取响应体 `msg`，保留超时、断网与 401 的现有处理。
- [ ] 重新运行客户端测试，确认通过。
- [ ] 更新认证模板与样式，加入认证标识、字段说明、密码显示开关和有效/错误状态；保持窄屏可用。

### 任务 3：后端注册规则与昵称唯一性

**文件：**

- 修改：`backend/src/main/java/com/jaymetest/model/dto/UserRegisterRequest.java`
- 修改：`backend/src/main/java/com/jaymetest/service/AuthService.java`
- 新建：`backend/src/test/java/com/jaymetest/service/AuthServiceTest.java`
- 新建：`database/releases/v4/002_add_user_nickname_unique.sql`
- 新建：`database/releases/v4/README.md`
- 修改：`database/baseline/schema.sql`
- 修改：`database/snapshots/2026-07-28_schema.sql`

- [ ] 编写失败服务测试：当 Mapper 返回同昵称用户时，注册抛出 `BusinessException` 且消息为“该昵称已被使用”。
- [ ] 运行 `mvn test -Dtest=AuthServiceTest`，确认测试因当前代码未检查昵称而失败。
- [ ] 将 DTO 约束改为昵称 2–10、密码 6–10；在插入前按规范化昵称查询，并抛出精确业务错误。
- [ ] 编写幂等 MySQL 脚本，先检查重复昵称并在无冲突时创建唯一索引；同步基线和快照的唯一键定义。
- [ ] 重新运行 `mvn test -Dtest=AuthServiceTest,GlobalExceptionHandlerTest`，确认通过。

### 任务 4：完整验证

**文件：**

- 验证前述变更，无新增文件。

- [ ] 运行 `npm run test` 与 `npm run build`（工作目录 `frontend`）。
- [ ] 运行 `mvn test`（工作目录 `backend`）。
- [ ] 检查 `git diff --check` 和 `git status --short`，只报告本任务文件，保留现有无关改动。
