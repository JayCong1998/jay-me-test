# 深渊 Round 状态机实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 让深渊模式只能按服务端下发顺序作答，错误后只能续命或结算。

**架构：** `GameRoundCache` 为深渊 Round 保存题目顺序、当前题序号与失败状态；`AbyssGameStrategy` 通过缓存原子方法校验作答、续命和批次生成。其他模式继续使用已有的答案记录逻辑。

**技术栈：** Java 21、Spring Boot、JUnit 5、Mockito。

---

### 任务 1：为深渊状态约束添加失败测试

**文件：**
- 修改：`backend/src/test/java/com/jaymetest/service/game/AbyssGameStrategyTest.java`
- 测试：`backend/src/test/java/com/jaymetest/service/game/AbyssGameStrategyTest.java`

- [ ] **步骤 1：编写失败的测试**

```java
assertThrows(BusinessException.class,
        () -> strategy.checkAnswer("round-1", 2L, "A", cacheManager));

strategy.checkAnswer("round-1", 1L, "B", cacheManager);
assertThrows(BusinessException.class,
        () -> strategy.checkAnswer("round-1", 2L, "A", cacheManager));
```

- [ ] **步骤 2：运行测试验证失败**

运行：`mvn -Dtest=AbyssGameStrategyTest test`

预期：FAIL，因为当前实现仅验证题目是否属于 Round。

### 任务 2：实现深渊 Round 状态机

**文件：**
- 修改：`backend/src/main/java/com/jaymetest/service/game/GameRoundCache.java`
- 修改：`backend/src/main/java/com/jaymetest/service/game/AbyssGameStrategy.java`

- [ ] **步骤 1：保存题目顺序及状态**

```java
private final List<Long> abyssQuestionOrder = new ArrayList<>();
private int abyssCurrentQuestionIndex;
private boolean abyssFailed;
```

- [ ] **步骤 2：以原子操作校验并记录当前题答案**

```java
public synchronized void recordAbyssAnswer(Long questionId, boolean correct) {
    requireCurrentAbyssQuestion(questionId);
    recordAnswer(questionId, correct);
    if (correct) abyssCurrentQuestionIndex++;
    else abyssFailed = true;
}
```

- [ ] **步骤 3：续命只能恢复当前错误题，批次仅可在已答完当前批次后生成**

```java
public synchronized void reviveCurrentAbyssQuestion(Long questionId) { /* 清除错误并恢复 ACTIVE */ }
public synchronized void ensureAbyssCanGenerateBatch() { /* reject FAILED or unanswered */ }
```

- [ ] **步骤 4：运行目标测试验证通过**

运行：`mvn -Dtest=AbyssGameStrategyTest test`

预期：PASS。

### 任务 3：运行受影响模块回归验证

**文件：**
- 测试：`backend/src/test/java/com/jaymetest/service/game/AbyssGameStrategyTest.java`

- [ ] **步骤 1：运行全部后端测试**

运行：`mvn test`

预期：BUILD SUCCESS，0 failures，0 errors。
