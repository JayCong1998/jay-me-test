# 游戏记录与深渊难度重构实现计划

> **面向 AI 代理的工作者：** 在当前会话中逐任务执行本计划；每项行为变更严格遵循红—绿—重构。仓库规则禁止自动提交，因此计划不包含 commit 步骤。

**目标：** 正确展示经典、专辑、深渊三种历史记录，并让深渊抽题难度真正受可验证配置控制。

**架构：** 后端以 `GameMode` 和游戏策略统一生成记录语义，通过 assembler 返回严格 DTO；前端以服务端记录为唯一来源，并通过纯展示转换器生成模式标签和成绩文案。深渊难度由配置属性、启动校验、概率策略和随机源四部分组成，游戏策略仅消费选择结果。

**技术栈：** Spring Boot 3.3、Java 21、MyBatis Plus、JUnit 5、Mockito、Vue 3、Pinia、TypeScript、Vitest、MySQL 8。

---

## 文件结构

### 后端记录语义

- 修改 `backend/src/main/java/com/jaymetest/model/dto/GameSubmitRequest.java`：将 `mode` 改为必填 `GameMode`，增加模式与 `albumKey` 的组合校验。
- 修改 `backend/src/main/java/com/jaymetest/model/dto/GameResultDTO.java`：补齐记录公共字段。
- 修改 `backend/src/main/java/com/jaymetest/model/dto/GameRecordDTO.java`：增加 `mode`、`albumKey`、`score`。
- 修改 `backend/src/main/java/com/jaymetest/model/entity/GameRecord.java`：使用 `LocalDateTime` 表达创建时间。
- 创建 `backend/src/main/java/com/jaymetest/service/game/GameRecordDTOAssembler.java`：严格解析数据库记录并调用对应策略生成成绩和等级。
- 修改 `backend/src/main/java/com/jaymetest/service/StatsService.java`：使用强类型模式、校验请求、调用 assembler，并保持游客落库。
- 修改 `backend/src/test/java/com/jaymetest/service/StatsServiceTest.java`：覆盖提交公共字段、游客落库与非法模式组合。
- 创建 `backend/src/test/java/com/jaymetest/service/game/GameRecordDTOAssemblerTest.java`：覆盖三种模式和数据完整性错误。

### 深渊难度

- 替换 `backend/src/main/java/com/jaymetest/service/game/AbyssDifficultyConfig.java`：改为列表式配置属性及启动校验。
- 创建 `backend/src/main/java/com/jaymetest/service/game/DifficultySelection.java`：定义 `EASY`、`MEDIUM`、`HARD`、`ANY`。
- 创建 `backend/src/main/java/com/jaymetest/service/game/RandomSource.java`：抽象 `[0, 1)` 随机数。
- 创建 `backend/src/main/java/com/jaymetest/service/game/ThreadLocalRandomSource.java`：生产随机源。
- 创建 `backend/src/main/java/com/jaymetest/service/game/AbyssDifficultyPolicy.java`：按 streak 和权重选择难度。
- 修改 `backend/src/main/java/com/jaymetest/service/game/AbyssGameStrategy.java`：注入 policy，并把 `ANY` 映射为无难度过滤。
- 修改 `backend/src/main/resources/application.yml`：配置七个连续难度档位。
- 创建 `backend/src/test/java/com/jaymetest/service/game/AbyssDifficultyPolicyTest.java`：覆盖所有区间和概率边界。
- 创建 `backend/src/test/java/com/jaymetest/service/game/AbyssDifficultyConfigTest.java`：覆盖配置校验失败。
- 创建或修改 `backend/src/test/java/com/jaymetest/service/game/AbyssGameStrategyTest.java`：证明策略消费 policy，并验证 `ANY` 查询。

### 前端记录展示

- 修改 `frontend/package.json` 和 `frontend/package-lock.json`：增加 Vitest 测试命令与依赖。
- 修改 `frontend/src/api/statsApi.ts`：使用严格的 `GameMode`，补齐结果和历史 DTO 字段。
- 创建 `frontend/src/stores/recordStore.ts`：管理登录用户的服务端历史状态。
- 创建 `frontend/src/utils/recordPresentation.ts`：纯函数生成模式标签、成绩文案和颜色。
- 创建 `frontend/src/utils/recordPresentation.test.ts`：覆盖三种模式和专辑名。
- 修改 `frontend/src/stores/userStore.ts`：只保留昵称持久化，移除游戏历史职责。
- 修改 `frontend/src/composables/useQuiz.ts`：提交成功后不再写本地历史。
- 修改 `frontend/src/pages/HomePage.vue`：使用 `recordStore` 混合展示三种模式，退出时清空记录。

### 数据库

- 修改 `database/create_table.sql`：移除 `mode` 默认值，增加模式和专辑字段的 CHECK 约束。

## 任务 1：建立干净基线

- [ ] 运行后端测试：`cd backend && mvn test`。
- [ ] 预期：现有 JUnit 测试全部通过；如失败，先按系统化调试流程报告并定位。
- [ ] 运行前端构建：`cd frontend && npm run build`。
- [ ] 预期：类型检查和 Vite 构建退出码为 0。

## 任务 2：统一后端游戏记录语义

- [ ] 先在 `StatsServiceTest` 增加失败测试：提交 `ABYSS` 后结果包含 `roundId`、`mode`、`score`、`usedRevival=false`，并捕获插入实体验证游客 `userId=null`。
- [ ] 运行：`cd backend && mvn -Dtest=StatsServiceTest test`。
- [ ] 预期：因 DTO 缺少公共字段或请求模式类型不符而失败。
- [ ] 在 `GameRecordDTOAssemblerTest` 增加失败测试：经典记录生成百分制成绩；深渊记录保留 streak 成绩和深渊等级；非法数据库模式抛 `IllegalStateException`；专辑字段与模式不匹配时抛异常。
- [ ] 运行：`cd backend && mvn -Dtest=GameRecordDTOAssemblerTest test`。
- [ ] 预期：因 assembler 尚不存在而失败。
- [ ] 最小实现 `GameSubmitRequest`、两个 DTO、`GameRecordDTOAssembler` 和 `StatsService` 调整：

```java
@NotNull(message = "mode 不能为空")
private GameMode mode;

@AssertTrue(message = "albumKey 与 mode 不匹配")
public boolean isAlbumKeyValid() {
    return mode == null
            || (mode == GameMode.ALBUM) == (albumKey != null && !albumKey.isBlank());
}
```

```java
GameMode mode = request.getMode();
GameStrategy strategy = strategyFactory.get(mode);
record.setMode(mode.name());
```

- [ ] assembler 必须通过 `GameMode.valueOf(record.getMode())` 严格解析，并使用 `strategy.calculateScore()`、`strategy.evaluateLevel()`；禁止经典模式降级。
- [ ] 运行两个目标测试，预期全部通过。
- [ ] 运行完整后端测试，确认没有回归。

## 任务 3：实现可配置深渊难度策略

- [ ] 先创建 `AbyssDifficultyConfigTest`，用真实配置对象验证以下非法输入：首档不从 0 开始、区间断裂、区间重叠、非末档无上界、权重和不为 1、未知难度、非正权重。
- [ ] 运行：`cd backend && mvn -Dtest=AbyssDifficultyConfigTest test`。
- [ ] 预期：新列表模型和校验方法不存在而失败。
- [ ] 最小实现列表配置和 `@PostConstruct validate()`；错误信息包含档位索引和原因。
- [ ] 运行配置测试，预期通过。
- [ ] 创建 `AbyssDifficultyPolicyTest`，用固定 `RandomSource` 覆盖 streak 边界 `0/2/3/5/6/9/10/14/15/19/20/29/30`，并验证累计权重分界。
- [ ] 运行：`cd backend && mvn -Dtest=AbyssDifficultyPolicyTest test`。
- [ ] 预期：policy 尚不存在而失败。
- [ ] 最小实现 `DifficultySelection`、`RandomSource`、`ThreadLocalRandomSource` 和 `AbyssDifficultyPolicy`。
- [ ] 运行 policy 测试，预期通过。
- [ ] 创建 `AbyssGameStrategyTest` 失败测试：固定 policy 返回 `HARD` 时查询包含难度，返回 `ANY` 时查询不包含难度；目标难度无题时回退 `ANY`。
- [ ] 运行：`cd backend && mvn -Dtest=AbyssGameStrategyTest test`。
- [ ] 预期：`AbyssGameStrategy` 尚未注入 policy 而失败。
- [ ] 修改 `AbyssGameStrategy`：删除 `determineDifficulty()` 和 `Math.random()`，改为 `difficultyPolicy.select(streak)`；仅数据访问边界把 `ANY` 转换为无过滤条件。
- [ ] 将 `application.yml` 改为七档 `tiers` 配置。
- [ ] 运行三组深渊测试及完整后端测试，预期全部通过。

## 任务 4：前端纯展示转换器与记录状态

- [ ] 安装 Vitest 开发依赖并添加脚本：`npm install -D vitest`，`"test": "vitest run"`。
- [ ] 先创建 `recordPresentation.test.ts`，断言：

```ts
expect(getRecordPresentation(classic).scoreText).toBe('8/10')
expect(getRecordPresentation(album).modeLabel).toBe('专辑闯关 · 叶惠美')
expect(getRecordPresentation(abyss).scoreText).toBe('连续答对 12 题')
```

- [ ] 运行：`cd frontend && npm test -- src/utils/recordPresentation.test.ts`。
- [ ] 预期：转换器不存在而失败。
- [ ] 最小实现 `statsApi.ts` 的严格 DTO 和 `recordPresentation.ts`；等级称号使用服务端 `levelTitle`，只在前端查找颜色和专辑展示名。
- [ ] 再次运行转换器测试，预期通过。
- [ ] 创建 `recordStore.ts`，状态包含 `records/loading/error`，操作包含 `fetchMyRecords()` 和 `clear()`；不使用 `localStorage`。
- [ ] 简化 `userStore.ts`，仅保留昵称、`setNickname()`、`reset()` 和昵称持久化。
- [ ] 修改 `useQuiz.ts`，删除 `GameRecord`、`formatDate()` 和 `addGameRecord()` 调用；后端提交行为不变。
- [ ] 运行：`cd frontend && npm test`，预期全部通过。
- [ ] 运行：`cd frontend && npm run build`，预期类型检查和构建通过。

## 任务 5：首页混合历史时间线

- [ ] 修改 `HomePage.vue`：登录后调用 `recordStore.fetchMyRecords()`，游客不调用；退出登录时同时 `recordStore.clear()`。
- [ ] 移除普通模式专属的最佳分徽章，避免跨模式比较无意义的成绩。
- [ ] 每条记录以 `roundId` 为 key，并通过 `getRecordPresentation()` 渲染：

```vue
<span class="history-mode" :class="`history-mode--${item.modeTone}`">
  {{ item.modeLabel }}
</span>
<span class="history-score">{{ item.scoreText }}</span>
<span class="history-level" :style="{ color: item.levelColor }">
  {{ item.levelTitle }}
</span>
```

- [ ] 为经典、专辑、深渊模式标签增加金、紫、红三种样式，并保证移动端成绩文案不溢出。
- [ ] 运行前端测试与构建，预期全部通过。

## 任务 6：数据库约束与全量验收

- [ ] 修改 `database/create_table.sql`：`mode` 必填且无默认值；增加 `chk_game_record_mode` 和 `chk_game_record_album`。
- [ ] 搜索并清理旧历史职责与硬编码：

```powershell
rg -n "gameHistory|addGameRecord|syncFromServer|correctCount \* 10|Math\.random|determineDifficulty|early-tier" frontend backend
```

- [ ] 预期：业务代码中不再出现上述旧历史逻辑和深渊硬编码；测试说明文字可出现相关名称。
- [ ] 运行最终后端验证：`cd backend && mvn test`。
- [ ] 运行最终前端验证：`cd frontend && npm test && npm run build`。
- [ ] 检查 `git diff --check`，预期无空白错误。
- [ ] 对照设计文档逐项核验三种模式标签、专辑名、深渊成绩、游客落库、配置生效和非法配置失败。
- [ ] 展示变更摘要、验证结果和未提交状态，不执行 commit 或 push。
