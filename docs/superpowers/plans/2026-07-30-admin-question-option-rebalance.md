# 管理端题目答案选项均衡实现计划

> **面向 AI 代理的工作者：** 使用 `superpowers-zh:executing-plans` 在当前会话逐任务实施本计划；步骤使用复选框跟踪进度。

**目标：** 管理员可以一键随机重排题目选项，以降低正确答案 A-D 的分布偏差，同时保持每题正确答案内容不变。

**架构：** 在 `AdminQuestionService` 聚合读写题库、维护临时答案计数并执行选项互换；控制器只暴露管理员路由。管理端 API 模块封装调用，`QuestionPage` 管理确认、提交状态、结果提示和刷新。

**技术栈：** Spring Boot 3、MyBatis-Plus、JUnit 5 + Mockito、React 18、TypeScript、Vite。

---

## 文件变更

- 修改 `backend/src/main/java/com/jaymetest/service/admin/AdminQuestionService.java`：实现均衡算法与响应组装。
- 创建 `backend/src/main/java/com/jaymetest/model/admin/QuestionOptionRebalanceResponse.java`：定义调整数量与 A-D 分布响应。
- 修改 `backend/src/main/java/com/jaymetest/controller/admin/AdminQuestionController.java`：增加 POST 路由。
- 创建 `backend/src/test/java/com/jaymetest/service/admin/AdminQuestionServiceTest.java`：覆盖交换保真和分布收敛。
- 修改 `admin/src/api/questionApi.ts`：封装均衡接口。
- 修改 `admin/src/pages/QuestionPage.tsx`：增加确认按钮、提交状态、结果提示。

### 任务 1：服务层均衡逻辑

**文件：**
- 创建：`backend/src/test/java/com/jaymetest/service/admin/AdminQuestionServiceTest.java`
- 创建：`backend/src/main/java/com/jaymetest/model/admin/QuestionOptionRebalanceResponse.java`
- 修改：`backend/src/main/java/com/jaymetest/service/admin/AdminQuestionService.java`

- [ ] **步骤 1：编写失败的服务测试**

```java
@Test
void rebalanceMovesAnswersToUnderrepresentedOptionsWithoutChangingCorrectText() {
    Question first = question(1L, "A", "correct-1", "B", "C", "D");
    Question second = question(2L, "A", "correct-2", "B", "C", "D");
    when(questionMapper.selectList(any())).thenReturn(List.of(first, second));

    QuestionOptionRebalanceResponse result = service.rebalanceOptions();

    assertThat(result.getAdjustedCount()).isPositive();
    assertThat(Set.of(first.getOptionA(), first.getOptionB(), first.getOptionC(), first.getOptionD()))
        .contains("correct-1");
    assertThat(optionAt(first, first.getCorrectOption())).isEqualTo("correct-1");
    verify(questionMapper, atLeastOnce()).updateById(any(Question.class));
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn -f backend/pom.xml -Dtest=AdminQuestionServiceTest test`

预期：FAIL，原因是 `rebalanceOptions` 和响应类型尚不存在。

- [ ] **步骤 3：实现最小服务逻辑**

```java
public QuestionOptionRebalanceResponse rebalanceOptions() {
    List<Question> questions = questionMapper.selectList(null);
    Map<String, Long> counts = countAnswers(questions);
    Collections.shuffle(questions);
    int adjustedCount = 0;
    for (Question question : questions) {
        Optional<String> target = selectUnderrepresentedTarget(question.getCorrectOption(), counts);
        if (target.isEmpty()) break;
        swapOptions(question, question.getCorrectOption(), target.get());
        counts.compute(question.getCorrectOption(), (key, value) -> value + 1);
        questionMapper.updateById(question);
        adjustedCount++;
    }
    return new QuestionOptionRebalanceResponse(adjustedCount, counts);
}
```

实现时将旧答案位置保存在局部变量，在交换后先对旧位置减一、再对目标位置加一，避免用已更新的 `correctOption` 计算错误计数；`selectUnderrepresentedTarget` 仅在最大最小计数差大于 1 时从最小计数位置中随机选择。

- [ ] **步骤 4：运行测试确认通过**

运行：`mvn -f backend/pom.xml -Dtest=AdminQuestionServiceTest test`

预期：PASS。

### 任务 2：管理员接口

**文件：**
- 修改：`backend/src/main/java/com/jaymetest/controller/admin/AdminQuestionController.java`
- 测试：`backend/src/test/java/com/jaymetest/service/admin/AdminQuestionServiceTest.java`

- [ ] **步骤 1：添加控制器测试所需的服务调用断言**

在服务测试保留 `rebalanceOptions` 返回的分布断言；控制器不引入 WebMvc 测试基础设施，仅保持现有轻量服务测试风格。

- [ ] **步骤 2：添加路由**

```java
@PostMapping("/rebalance-options")
public R<QuestionOptionRebalanceResponse> rebalanceOptions() {
    return R.ok(adminQuestionService.rebalanceOptions());
}
```

- [ ] **步骤 3：编译后端**

运行：`mvn -f backend/pom.xml -DskipTests compile`

预期：BUILD SUCCESS。

### 任务 3：管理端 API 与按钮

**文件：**
- 修改：`admin/src/api/questionApi.ts`
- 修改：`admin/src/pages/QuestionPage.tsx`

- [ ] **步骤 1：定义响应类型并封装请求**

```ts
export interface QuestionOptionRebalanceResult {
  adjustedCount: number
  answerDistribution: Record<'A' | 'B' | 'C' | 'D', number>
}

export function rebalanceQuestionOptions() {
  return apiClient.post<never, QuestionOptionRebalanceResult>('/questions/rebalance-options')
}
```

- [ ] **步骤 2：增加按钮处理函数**

```tsx
async function handleRebalance() {
  if (!window.confirm('将随机重排所有题目的选项位置，是否继续？')) return
  setRebalancing(true)
  setError('')
  try {
    const result = await rebalanceQuestionOptions()
    setNotice(`已调整 ${result.adjustedCount} 题；A/B/C/D：${result.answerDistribution.A}/${result.answerDistribution.B}/${result.answerDistribution.C}/${result.answerDistribution.D}`)
    await load()
  } catch (err) {
    setError(err instanceof Error ? err.message : '均衡失败')
  } finally {
    setRebalancing(false)
  }
}
```

将“均衡答案分布”按钮放在“新增题目”按钮旁，使用 `variant="secondary"` 并在 `rebalancing` 时禁用；在现有错误提示附近渲染成功提示。

- [ ] **步骤 3：构建管理端**

运行：`npm run build`

工作目录：`admin`

预期：`tsc -b && vite build` 成功结束。

### 任务 4：全量验证

**文件：**
- 测试：`backend/src/test/java/com/jaymetest/service/admin/AdminQuestionServiceTest.java`

- [ ] **步骤 1：运行后端受影响测试**

运行：`mvn -f backend/pom.xml -Dtest=AdminQuestionServiceTest,QuestionServiceTest test`

预期：全部 PASS。

- [ ] **步骤 2：复查工作区变更**

运行：`git diff --check; git status --short`

预期：无空白错误；仅包含本计划列出的后端、管理端和文档文件（以及用户已有的独立修改）。
