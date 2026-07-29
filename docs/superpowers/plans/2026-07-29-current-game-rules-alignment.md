# 当前游戏规则同步实现计划

> **面向 AI 代理的工作者：** 使用 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）跟踪进度。

**目标：** 让前端降级结果、运行文档与当前 20 题/深渊续命规则一致，同时不修改排行榜逻辑。

**架构：** 前端通过本局题目总数计算离线百分比和等级；后端策略接口只保留真实被调用的复活 API；运行文档描述配置驱动的题量、专辑通关率与深渊续命边界。

**技术栈：** Vue 3、TypeScript、Vitest、Spring Boot、Java 21、Markdown。

---

### 任务 1：前端降级结果使用实际题数

**文件：**
- 修改：`frontend/src/utils/levels.ts`
- 修改：`frontend/src/composables/useQuiz.ts`
- 修改：`frontend/src/utils/levels.test.ts`

- [ ] **步骤 1：编写失败的测试**

在 `levels.test.ts` 添加：

```ts
it('calculates an 80 percent fallback score and level for a 20-question round', () => {
  expect(calcScore(16, 20)).toBe(80)
  expect(getLevelByScore(16, 20).key).toBe('SENIOR')
})
```

- [ ] **步骤 2：运行测试验证失败**

运行：`npm run test -- src/utils/levels.test.ts`

预期：失败，因为 `getLevelByScore` 当前只接收答对数，会把 16 题归入默认等级。

- [ ] **步骤 3：实现最少代码**

将等级函数改为基于百分比：

```ts
export function getLevelByScore(correctCount: number, totalQuestions: number): LevelConfig {
  const score = calcScore(correctCount, totalQuestions)
  return LEVELS.find(level => score >= level.minScore && score <= level.maxScore) || LEVELS[0]
}
```

并在 `finishAndSubmit` 中使用：

```ts
const level = getLevelByScore(gameStore.correctCount, gameStore.totalQuestions)
score: calcScore(gameStore.correctCount, gameStore.totalQuestions),
```

- [ ] **步骤 4：运行测试验证通过**

运行：`npm run test -- src/utils/levels.test.ts`

预期：PASS。

### 任务 2：清理未接入的复活能力契约

**文件：**
- 修改：`backend/src/main/java/com/jaymetest/service/game/GameStrategy.java`
- 修改：`backend/src/main/java/com/jaymetest/service/game/AbyssGameStrategy.java`

- [ ] **步骤 1：移除无调用方的能力声明**

删除接口的以下默认方法和深渊策略的对应 override，保留 `revive(...)`：

```java
default boolean supportsRevival() {
    return false;
}
```

```java
@Override
public boolean supportsRevival() {
    return gameRules.getRevivalCount() > 0;
}
```

- [ ] **步骤 2：编译并运行后端测试**

运行：`mvn test`

预期：BUILD SUCCESS。

### 任务 3：同步运行文档

**文件：**
- 修改：`docs/ARCHITECTURE.md`
- 修改：`docs/PRD.md`
- 修改：`docs/TECH.md`

- [ ] **步骤 1：同步题量与通关规则**

将面向当前运行版本的规则改为：经典和专辑各 20 题；经典难度按 `easy-weight: 0.6` 配置；专辑以 80% 正确率通关（当前配置对应 16/20）。

- [ ] **步骤 2：同步复活 API**

移除 `/api/questions/revive` 的接口说明；增加/更新 `POST /api/abyss/revive`，注明需登录、仅错误当前题可续命、首次错误且可续命时不返回正确答案与解析。

- [ ] **步骤 3：文本核对**

运行：

```powershell
rg -n '/api/questions/revive|count=10|6 简单 \+ 4 中等|8/10' docs/ARCHITECTURE.md docs/PRD.md docs/TECH.md
```

预期：无运行规则中的匹配；历史示例若保留，须明确标为历史资料。

### 任务 4：全量验证

**文件：** 无新增修改。

- [ ] **步骤 1：运行前端测试和构建**

运行：`npm run test; npm run build`

预期：测试及构建退出码均为 0。

- [ ] **步骤 2：运行管理端构建**

运行：`npm run build`

预期：退出码为 0。

- [ ] **步骤 3：确认工作区变更**

运行：`git diff --check` 和 `git diff --stat`。

预期：无空白错误；仅包含计划中的前端、后端策略和运行文档改动。
