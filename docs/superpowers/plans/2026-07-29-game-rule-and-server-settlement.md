# 玩法规则配置化与服务端结算实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 用服务端规则与 Round 作答状态生成可信的三种玩法结算结果。

**架构：** `GameRuleProperties` 是新局规则的唯一来源；`GameRoundCache` 保留每局规则快照与作答状态；`StatsService` 从缓存生成成绩并在成功持久化后清除 Round。客户端只请求开局、逐题校验与按 roundId 结算。

**技术栈：** Spring Boot 3、Java 21、JUnit 5/Mockito、Vue 3、TypeScript、Vitest。

---

## 文件结构

- 创建：`backend/src/main/java/com/jaymetest/config/GameRuleProperties.java` — 绑定游戏规则配置与校验。
- 修改：`backend/src/main/resources/application.yml` — 提供三种玩法的默认规则。
- 修改：`backend/src/main/java/com/jaymetest/service/game/GameRoundCache.java` — 保存规则快照和作答事实。
- 修改：`backend/src/main/java/com/jaymetest/service/game/*GameStrategy.java` — 使用规则创建 Round 并写入作答事实。
- 修改：`backend/src/main/java/com/jaymetest/service/StatsService.java` — 以 Round 结算。
- 修改：控制器、DTO、前端 API 与 `useQuiz` — 删除客户端可控结算参数。
- 创建/修改：后端 JUnit 与前端 Vitest 测试 — 锁定规则与防伪造行为。

### 任务 1：规则配置与策略

- [ ] 编写失败测试：构造经典规则 `questionCount=5`、`EASY=0.4`，断言策略向 mapper 请求 2 EASY 与 3 MEDIUM。
- [ ] 运行该测试，预期因尚未存在 `GameRuleProperties` 构造依赖而失败。
- [ ] 创建配置类，加入 `@ConfigurationProperties(prefix = "game")`；策略构造注入该类并移除固定题数、权重与批大小。
- [ ] 在 `application.yml` 加入现有默认规则并运行目标测试，预期通过。

### 任务 2：Round 作答状态

- [ ] 编写失败测试：Round 记录一次正确与一次错误答案，结算快照返回正确数、已答数和复活数；重复答题抛业务异常。
- [ ] 运行测试，预期因缓存没有记录 API 而失败。
- [ ] 扩展 `GameRoundCache` 与策略校验路径，记录模式、专辑、题目总数、答案结果、复活次数和深渊 streak。
- [ ] 运行目标测试，预期通过。

### 任务 3：服务端结算与接口收口

- [ ] 编写失败测试：提交请求携带伪造的 `correctCount=10`，Round 仅有 6 个正确答案时，持久化与返回结果均为 6。
- [ ] 运行测试，预期当前实现错误地使用请求参数而失败。
- [ ] 让 `StatsService` 读取 Round 快照结算，校验模式和专辑一致性，成功后移除 Round；DTO 删除客户端成绩字段。
- [ ] 控制器移除 `count` 参数；前端开局、提交仅发送允许字段，所有 `/10` 展示改为服务端总题数。
- [ ] 运行后端目标测试与前端受影响测试，预期通过。

### 任务 4：全量验证

- [ ] 运行 `mvn test`、`npm run test` 与 `npm run build`，检查退出码为 0。
- [ ] 检查 `git diff --check`，确认无空白错误。
