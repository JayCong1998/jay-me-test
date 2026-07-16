# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 沟通方式
- 默认中文回复；代码、命令、变量名、文件路径保持英文
- 结论先行，简洁直接，不先铺垫背景
- 给真实判断——方案有问题直接指出，发现更好做法主动说明

## Git
- 不自动 `git commit` 或 `git push`，除非我明确要求
- 提交前先展示将要提交的变更摘要
- commit message 使用简洁英文

## 红线操作
以下操作即使在 auto-accept 模式下也必须先问我：
- 删除文件、目录或 git 历史
- 修改 `.env`、密钥、token、证书、CI/CD 配置
- `git push`、`git rebase`、`git reset --hard`、强制推送
- 公开发布（`npm publish`、生产部署等）

## 项目概述

**杰迷结业考试 (jay-me-test)** — 免登 H5 答题应用，周杰伦粉丝答题 10 道随机单选题，获得 5 级杰迷等级鉴定和可分享的电子证书。移动端优先，以社交传播和裂变为目标。

详细设计文档见 [docs/TECH.md](docs/TECH.md)（技术设计）和 [docs/PRD.md](docs/PRD.md)（产品需求）。

## 开发命令

### 前端 (`frontend/`)

```bash
npm run dev        # Vite 开发服务器，端口 :5173，/api 代理到 localhost:8080
npm run build      # 类型检查 + 生产构建，输出到 dist/
npm run preview    # 预览生产构建
```

### 后端 (`backend/`)

```bash
# 需要 JDK 21 + MySQL 8.0 运行于 localhost:3306（数据库: jaymetest，用户: root/root）
mvn spring-boot:run              # 启动，端口 :8080
mvn test                         # 运行测试（目前仅有 Service 层测试）
```

API 文档 (Knife4j)：`http://localhost:8080/doc.html`

### 生产打包

1. `frontend/` 下执行 `npm run build` → 将 `dist/` 复制到 `backend/src/main/resources/static/`
2. `mvn package` → 生成含内嵌 Tomcat + Vue 静态文件的 Fat JAR

## 架构

### 数据流

```
Vue 组件 → API 模块 (questionApi/statsApi) → Axios (/api/*) → Spring Controller → Service → Mapper (MyBatis Plus) → MySQL
```

**核心安全模式**：`GET /api/questions/round` 返回的 `QuestionDTO` **不含** `correctOption`。答案校验在服务端通过 `POST /api/questions/check` 完成——后端持有一个以 UUID（roundId）为 key 的 `ConcurrentHashMap<String, RoundCache>`，前端拿不到正确答案，也无法预测 roundId。

### Round 生命周期

1. 客户端请求 `GET /api/questions/round?count=10` → 服务端生成 UUID `roundId`，随机抽取 6 道简单 + 4 道中等题目，将 `(questionId → correctOption)` 映射存入 `RoundCache`，返回不含答案的题目列表。
2. 客户端逐题通过 `POST /api/questions/check` 提交答案 → 服务端根据 `roundId` 查找缓存比对。
3. 客户端通过 `POST /api/stats/submit` 提交最终结果 → 服务端按 `roundId` 去重（每 round 只能提交一次），计算等级和百分位，持久化到 `game_record` 表。
4. `QuestionService.cleanExpiredCache()` 每 10 分钟执行一次，清除超过 30 分钟的 round。

### 后端关键文件

| 文件 | 职责 |
|------|------|
| [QuestionController.java](backend/src/main/java/com/jaymetest/controller/QuestionController.java) | 题目 API：抽题 `/round`、校验 `/check`、复活 `/revive` |
| [StatsController.java](backend/src/main/java/com/jaymetest/controller/StatsController.java) | 统计 API：提交结果 `/submit`、全局概览 `/overview` |
| [HealthController.java](backend/src/main/java/com/jaymetest/controller/HealthController.java) | 健康检查 `/api/health` |
| [QuestionService.java](backend/src/main/java/com/jaymetest/service/QuestionService.java) | 抽题、答案校验、复活、缓存清理 |
| [StatsService.java](backend/src/main/java/com/jaymetest/service/StatsService.java) | 等级匹配、百分位计算、结果持久化、全局统计 |
| [RoundCache.java](backend/src/main/java/com/jaymetest/service/RoundCache.java) | Round 缓存数据结构（answerMap + createdAt + 30min TTL） |
| [QuestionMapper.java](backend/src/main/java/com/jaymetest/mapper/QuestionMapper.java) | 题目查询：按难度随机抽取、总数统计 |
| [GameRecordMapper.java](backend/src/main/java/com/jaymetest/mapper/GameRecordMapper.java) | 游戏记录查询：百分位计数、平均分、等级分布 |
| [FanLevel.java](backend/src/main/java/com/jaymetest/model/enums/FanLevel.java) | 5 级枚举：PASSERBY(0-2) → JUNIOR(3-4) → INTERMEDIATE(5-6) → SENIOR(7-8) → ULTIMATE(9-10) |
| [R.java](backend/src/main/java/com/jaymetest/model/dto/R.java) | 统一响应包装 `{code, msg, data, timestamp}` — 所有 Controller 返回 `R<T>` |
| [GlobalExceptionHandler.java](backend/src/main/java/com/jaymetest/exception/GlobalExceptionHandler.java) | `@RestControllerAdvice` 统一捕获 BusinessException、参数校验异常、系统异常 |
| [WebConfig.java](backend/src/main/java/com/jaymetest/config/WebConfig.java) | CORS 跨域，允许所有来源访问 `/api/**` |
| [MyBatisPlusConfig.java](backend/src/main/java/com/jaymetest/config/MyBatisPlusConfig.java) | 分页插件（PaginationInnerInterceptor） |
| [Knife4jConfig.java](backend/src/main/java/com/jaymetest/config/Knife4jConfig.java) | OpenAPI 文档配置 |

### 前端关键文件

| 文件 | 职责 |
|------|------|
| [useQuiz.ts](frontend/src/composables/useQuiz.ts) | 编排完整答题流程：开始 → 提交答案 → 复活 → 完成并提交 |
| [useTimer.ts](frontend/src/composables/useTimer.ts) | 答题计时器 composable |
| [gameStore.ts](frontend/src/stores/gameStore.ts) | 内存状态（Pinia）：roundId、题目列表、当前索引、答案/结果 Map、复活、游戏阶段 |
| [userStore.ts](frontend/src/stores/userStore.ts) | localStorage 持久化（Pinia）：昵称、历史记录（最多 20 条）、最高分 |
| [client.ts](frontend/src/api/client.ts) | Axios 实例，baseURL `/api`，10s 超时，响应拦截器解包 `R<T>` |
| [questionApi.ts](frontend/src/api/questionApi.ts) | `/api/questions/*` 接口封装：fetchRound、checkAnswer、revive |
| [statsApi.ts](frontend/src/api/statsApi.ts) | `/api/stats/*` 接口封装：submitResult、fetchOverview |
| [constants.ts](frontend/src/utils/constants.ts) | `R<T>` 类型、`LevelConfig[]` 等级配置、证书尺寸常量、分享文案模板 |
| [levels.ts](frontend/src/utils/levels.ts) | 客户端等级计算逻辑（API 提交失败时的降级方案） |
| [format.ts](frontend/src/utils/format.ts) | 日期/时间格式化工具 |
| [certificate.ts](frontend/src/utils/certificate.ts) | Canvas 证书图片生成 |
| [router/index.ts](frontend/src/router/index.ts) | Hash 路由：`/` → 首页，`/quiz` → 答题，`/result` → 结果，`/certificate` → 证书 |
| [HomePage.vue](frontend/src/pages/HomePage.vue) | 首页：昵称输入、开始答题 |
| [QuizPage.vue](frontend/src/pages/QuizPage.vue) | 答题页：进度条、题目卡片、选项、反馈 |
| [ResultPage.vue](frontend/src/pages/ResultPage.vue) | 结果页：等级展示、数据面板、分享 |
| [CertificatePage.vue](frontend/src/pages/CertificatePage.vue) | 证书页：Canvas 预览、保存/分享 |
| [QuestionCard.vue](frontend/src/components/quiz/QuestionCard.vue) | 题目卡片 + `el-radio-group` 选项 |
| [FeedbackBar.vue](frontend/src/components/quiz/FeedbackBar.vue) | 答题对错反馈条 |

### 答案安全

无需登录。答题安全基于服务端缓存：
- `RoundCache` 保存 `Map<Long (questionId), String (correctOption)>` 及 `Instant createdAt`
- 客户端拿到的题目数据中从不包含正确答案
- 复活接口返回 `{revived: true, remainingRevivals: 0}`，不返回答案本身
- `game_record` 表 `uk_round_id` 唯一约束防重复提交

### 数据库

`jaymetest` 库中两张表：`question`（70 题，通过 `database/add_question.sql` 手动导入）和 `game_record`（匿名答题结果，`round_id` UUID 去重）。Schema 通过 `database/create_table.sql` 手动初始化。

### 移动端 & 分享

- Hash 路由 (`createWebHashHistory`) — 静态部署无需 Nginx fallback
- Element Plus 中文语言包，触摸区域 ≥ 44×44px
- 分享三级降级：Web Share API → 剪贴板复制 → 手动引导蒙层
- Canvas 生成证书图片，规格 1080×1520px

## 开发注意事项

- 后端使用 Lombok (`@Data`、`@Slf4j`、`@RequiredArgsConstructor`)；IDE 需安装 Lombok 插件
- 服务层通过 `@RequiredArgsConstructor` 构造器注入 Mapper，不写 `@Autowired`
- Element Plus 组件通过 `unplugin-vue-components` 自动按需导入，无需手动注册
- `unplugin-auto-import` 自动导入 `vue`、`vue-router`、`pinia` API，无需手动 `import { ref } from 'vue'`
- TypeScript 路径别名 `@/` 映射到 `src/`
- 前端 `useQuiz.finishAndSubmit()` 在 API 调用失败时会降级为客户端本地计算等级（使用 `levels.ts`）
- MyBatis Plus `@Select` 中使用 MySQL `RAND()` 随机抽题；当前规模（< 200 题）可接受，更大题库需优化
- 数据库连接池使用 HikariCP（Spring Boot 默认），配置见 `application.yml`：最小空闲 5、最大 20
- 异常处理：业务异常统一抛 `BusinessException(code, msg)`，由 `GlobalExceptionHandler` 转为 `R.fail()` 响应
- 参数校验使用 `@Valid` + `jakarta.validation`，校验失败由 `GlobalExceptionHandler.handleValidationException()` 统一处理
