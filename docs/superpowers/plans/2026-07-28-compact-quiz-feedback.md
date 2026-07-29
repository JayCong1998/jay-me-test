# 紧凑答题反馈实现计划

> **面向 AI 代理的工作者：** 使用测试驱动开发逐项实现并验证。

**目标：** 让答题正确和错误反馈在小屏幕内保持紧凑、避免重复动画与叠加阴影。

**架构：** `FeedbackBar` 负责紧凑结果信息和按需展开解析；`QuizPage` 只负责挂载反馈，不再额外施加入场变换；`QuestionCard` 在锁定答案后保留清晰选中态但移除光晕。

**技术栈：** Vue 3、SCSS、Vitest。

---

### 任务 1：锁定反馈的紧凑结构与动画约束

**文件：**
- 创建：`frontend/src/components/quiz/feedbackBarCompactStyle.test.ts`
- 修改：`frontend/src/components/quiz/FeedbackBar.vue`
- 修改：`frontend/src/pages/QuizPage.vue`

- [x] 编写静态回归测试，要求错误反馈的解析默认折叠，反馈组件只使用单一淡入动画，外层不再使用 `feedback-enter` Transition。
- [x] 运行 `npm run test -- src/components/quiz/feedbackBarCompactStyle.test.ts`，确认现状失败。
- [x] 实现紧凑反馈头部、解析展开按钮和单一 180ms 淡入上移动画。
- [x] 再次运行该测试，确认通过。

### 任务 2：移除锁定选项的视觉噪声

**文件：**
- 修改：`frontend/src/components/quiz/questionOptionHoverStyle.test.ts`
- 修改：`frontend/src/components/quiz/QuestionCard.vue`

- [x] 增加断言，锁定选项不使用 `box-shadow`，禁用选项不整体降低透明度。
- [x] 运行 `npm run test -- src/components/quiz/questionOptionHoverStyle.test.ts`，确认现状失败。
- [x] 调整锁定状态的样式。
- [x] 再次运行该测试，确认通过。

### 任务 3：构建验证

**文件：**
- 验证：`frontend/src/components/quiz/FeedbackBar.vue`
- 验证：`frontend/src/components/quiz/QuestionCard.vue`
- 验证：`frontend/src/pages/QuizPage.vue`

- [x] 运行相关 Vitest 用例。
- [x] 运行 `npm run build`，确认类型检查和 Vite 生产构建成功。
