# 架构说明

本文档记录项目的数据流、安全边界、核心业务生命周期和关键文件索引。`AGENTS.md` 只保留入口说明，详细架构维护在这里。

## 数据流

```text
Vue 组件 → API 模块 (questionApi/statsApi/...) → Axios (/api/*) → Spring Controller → Service → Mapper (MyBatis Plus) → MySQL
                                      ↑
                              Sa-Token JWT (登录用户)
```

## 核心安全模式

- **经典模式**：`GET /api/questions/round` 返回的 `QuestionDTO` **不含** `correctOption`。答案校验在服务端通过 `POST /api/questions/check` 完成；后端持有 `ConcurrentHashMap<String, RoundCache>`，前端无法获取正确答案、无法预测 roundId。
- **用户系统**：Sa-Token + JWT 双令牌模式。注册密码 BCrypt 加密。敏感接口（排行榜、专辑进度、我的记录）需登录，游客可正常答题。
- 复活接口返回 `{revived: true, remainingRevivals: 0}`，不返回答案本身。
- `game_record` 表 `uk_round_id` 唯一约束防重复提交。

## Round 生命周期

1. 客户端请求 `GET /api/questions/round?count=10&albumKey=xxx`，服务端生成 UUID `roundId`，随机抽取题目。经典模式为 6 简单 + 4 中等；专辑模式为指定专辑下按难度混合。
2. 服务端将 `(questionId → correctOption)` 映射存入 `RoundCache`，返回不含答案的题目列表。
3. 客户端逐题通过 `POST /api/questions/check` 提交答案，服务端根据 `roundId` 查找缓存比对。
4. 客户端通过 `POST /api/stats/submit` 提交最终结果，服务端按 `roundId` 去重，计算等级和百分位，持久化到 `game_record` 表。
5. 专辑模式下，得分 ≥ 8/10 自动解锁下一张专辑。
6. `QuestionService.cleanExpiredCache()` 每 10 分钟执行一次，清除超过 30 分钟的 round。

## 后端关键文件

| 文件 | 职责 |
|------|------|
| [QuestionController.java](../backend/src/main/java/com/jaymetest/controller/QuestionController.java) | 题目 API：抽题 `/round`、校验 `/check`、复活 `/revive` |
| [StatsController.java](../backend/src/main/java/com/jaymetest/controller/StatsController.java) | 统计 API：提交结果 `/submit`、全局概览 `/overview`、我的记录 `/my-records` |
| [AuthController.java](../backend/src/main/java/com/jaymetest/controller/AuthController.java) | 认证 API：注册 `/register`、登录 `/login`、当前用户 `/me` |
| [AlbumController.java](../backend/src/main/java/com/jaymetest/controller/AlbumController.java) | 专辑 API：专辑列表 `/list`、专辑抽题 `/round`（均需登录） |
| [LeaderboardController.java](../backend/src/main/java/com/jaymetest/controller/LeaderboardController.java) | 排行榜 API：总榜/日榜/等级榜（需登录） |
| [HealthController.java](../backend/src/main/java/com/jaymetest/controller/HealthController.java) | 健康检查 `/api/health` |
| [AiController.java](../backend/src/main/java/com/jaymetest/ai/controller/AiController.java) | AI 问答 `/api/ai/query`（DashScope 流式响应） |
| [QuestionService.java](../backend/src/main/java/com/jaymetest/service/QuestionService.java) | 抽题、答案校验、复活、缓存清理 |
| [StatsService.java](../backend/src/main/java/com/jaymetest/service/StatsService.java) | 等级匹配、百分位计算、结果持久化、全局统计 |
| [AuthService.java](../backend/src/main/java/com/jaymetest/service/AuthService.java) | 注册/登录逻辑、BCrypt 密码验证、JWT 令牌生成 |
| [AlbumProgressService.java](../backend/src/main/java/com/jaymetest/service/AlbumProgressService.java) | 专辑解锁判断、进度查询/更新、闯关权限校验 |
| [LeaderboardService.java](../backend/src/main/java/com/jaymetest/service/LeaderboardService.java) | 多维度排行查询（总榜/日榜/等级榜） |
| [RoundCache.java](../backend/src/main/java/com/jaymetest/service/RoundCache.java) | Round 缓存数据结构（answerMap + createdAt + 30min TTL） |
| [QuestionMapper.java](../backend/src/main/java/com/jaymetest/mapper/QuestionMapper.java) | 题目查询：按难度随机抽取、按专辑随机抽取、总数统计 |
| [GameRecordMapper.java](../backend/src/main/java/com/jaymetest/mapper/GameRecordMapper.java) | 游戏记录查询：百分位计数、排行数据、用户记录 |
| [UserMapper.java](../backend/src/main/java/com/jaymetest/mapper/UserMapper.java) | 用户查询：按邮箱查找 |
| [AlbumProgressMapper.java](../backend/src/main/java/com/jaymetest/mapper/AlbumProgressMapper.java) | 专辑进度 CRUD：按 userId+albumKey 查询/更新 |
| [FanLevel.java](../backend/src/main/java/com/jaymetest/model/enums/FanLevel.java) | 5 级枚举：PASSERBY(0-2) → JUNIOR(3-4) → INTERMEDIATE(5-6) → SENIOR(7-8) → ULTIMATE(9-10) |
| [AlbumKey.java](../backend/src/main/java/com/jaymetest/model/enums/AlbumKey.java) | 16 张录音室专辑枚举（JAY → SUN_CHILD） |
| [R.java](../backend/src/main/java/com/jaymetest/model/dto/R.java) | 统一响应包装 `{code, msg, data, timestamp}` |
| [GlobalExceptionHandler.java](../backend/src/main/java/com/jaymetest/exception/GlobalExceptionHandler.java) | `@RestControllerAdvice` 统一异常拦截 |
| [SaTokenConfig.java](../backend/src/main/java/com/jaymetest/config/SaTokenConfig.java) | Sa-Token 路由拦截规则（白名单 + 需登录名单） |
| [StpInterfaceImpl.java](../backend/src/main/java/com/jaymetest/config/StpInterfaceImpl.java) | 权限/角色加载实现 |

## 前端关键文件

| 文件 | 职责 |
|------|------|
| [useQuiz.ts](../frontend/src/composables/useQuiz.ts) | 编排完整答题流程：开始 → 提交答案 → 复活 → 完成并提交 |
| [useTimer.ts](../frontend/src/composables/useTimer.ts) | 答题计时器 composable |
| [gameStore.ts](../frontend/src/stores/gameStore.ts) | 内存状态（Pinia）：roundId、题目列表、当前索引、答案/结果 Map、复活、游戏阶段 |
| [userStore.ts](../frontend/src/stores/userStore.ts) | localStorage 持久化：昵称、历史记录（最多 20 条）、最高分 |
| [authStore.ts](../frontend/src/stores/authStore.ts) | 登录态管理：token 持久化、用户信息、登录/注册/登出操作 |
| [albumStore.ts](../frontend/src/stores/albumStore.ts) | 专辑进度状态：专辑列表、解锁状态、最高分 |
| [client.ts](../frontend/src/api/client.ts) | Axios 实例，baseURL `/api`，10s 超时，响应拦截器解包 `R<T>`，请求拦截器注入 Sa-Token |
| [questionApi.ts](../frontend/src/api/questionApi.ts) | `/api/questions/*` 接口：fetchRound、checkAnswer、revive |
| [statsApi.ts](../frontend/src/api/statsApi.ts) | `/api/stats/*` 接口：submitResult、fetchOverview、fetchMyRecords |
| [authApi.ts](../frontend/src/api/authApi.ts) | `/api/auth/*` 接口：register、login、fetchMe |
| [albumApi.ts](../frontend/src/api/albumApi.ts) | `/api/albums/*` 接口：fetchAlbumList、fetchAlbumRound |
| [leaderboardApi.ts](../frontend/src/api/leaderboardApi.ts) | `/api/leaderboard` 接口：fetchLeaderboard |
| [constants.ts](../frontend/src/utils/constants.ts) | `R<T>` 类型、`LevelConfig[]` 等级配置、证书尺寸常量、分享文案模板 |
| [levels.ts](../frontend/src/utils/levels.ts) | 客户端等级计算逻辑（API 提交失败时的降级方案） |
| [albums.ts](../frontend/src/utils/albums.ts) | 15 张专辑常量（key、displayName、year、gradient）+ 解锁阈值 |
| [format.ts](../frontend/src/utils/format.ts) | 日期/时间格式化工具 |
| [router/index.ts](../frontend/src/router/index.ts) | Hash 路由：`/`→首页，`/quiz`→答题，`/result`→结果，`/certificate`→证书，`/albums`→专辑，`/leaderboard`→排行，`/login`→登录，`/register`→注册 |
| [HomePage.vue](../frontend/src/pages/HomePage.vue) | 首页：昵称输入、模式选择（经典/专辑）、开始答题 |
| [QuizPage.vue](../frontend/src/pages/QuizPage.vue) | 答题页：进度条、题目卡片、选项、反馈 |
| [ResultPage.vue](../frontend/src/pages/ResultPage.vue) | 结果页：等级展示、数据面板、分享 |
| [CertificatePage.vue](../frontend/src/pages/CertificatePage.vue) | 证书页：Canvas 预览、保存/分享 |
| [AlbumListPage.vue](../frontend/src/pages/AlbumListPage.vue) | 专辑闯关列表：15 张专辑卡片、解锁状态、最高分 |
| [LeaderboardPage.vue](../frontend/src/pages/LeaderboardPage.vue) | 排行榜：总榜/日榜/等级榜切换 |
| [QuestionCard.vue](../frontend/src/components/quiz/QuestionCard.vue) | 题目卡片 + `el-radio-group` 选项 |
| [FeedbackBar.vue](../frontend/src/components/quiz/FeedbackBar.vue) | 答题对错反馈条 |
| [AlbumCard.vue](../frontend/src/components/album/AlbumCard.vue) | 专辑卡片：封面渐变、年份、锁定/解锁状态、最高分 |

## 数据库

`jaymetest` 库中 5 张表：

| 表 | 说明 |
|----|------|
| `question` | 题库（基础 70 题，可通过扩充脚本增加到 200 题），含 `category`、`difficulty`、`album` 列 |
| `game_record` | 匿名/登录游戏记录，`round_id` UUID 去重，含 `mode`、`album_key`、`user_id`、`nickname` |
| `user` | 注册用户，邮箱+BCrypt 哈希密码+昵称 |
| `album_progress` | 专辑闯关进度，`(user_id, album_key)` 唯一约束，含 `unlocked`、`best_score`、`total_attempts` |
| `admin_user` | 后台管理用户，含 `username`、`password`、`role`、`enabled` |

脚本位于 `database/`，不使用 Flyway、Liquibase 等迁移框架：

- `baseline/schema.sql`：从零初始化基础表结构
- `baseline/seed_001_initial_questions.sql`：初始 70 道题目
- `baseline/seed_002_question_expansion.sql`：题库扩充到 200 题
- `baseline/seed_003_test_users.sql`：本地测试用户
- `releases/v3/001_add_admin_console.sql`：第三版后台管理表和默认本地管理员账号
- `snapshots/2026-07-28_schema.sql`：当前表结构快照，只用于查看全貌和人工排查

## 数据库脚本管理规范

- 所有数据库变更按产品版本放入 `database/releases/vX/`，文件名使用 `{三位递增编号}_{action}_{object}.sql`，例如 `002_add_abyss_record_indexes.sql`。
- `baseline/` 只放从零初始化所需脚本；版本升级脚本只放 `releases/vX/`。
- 已提交或已执行到共享环境的历史 SQL 不直接修改；发现问题时新增下一个编号脚本修正。
- 一个脚本只处理一个清晰的业务变更，脚本顶部写明用途、前置条件和影响表。
- 涉及种子数据或回填数据时，优先写成幂等脚本；不能幂等时必须在版本 `README.md` 中注明。
- 本地从零初始化按 `baseline/schema.sql`、`baseline/seed_*.sql` 顺序执行。
- 版本升级按 `releases/vX/` 下编号顺序执行，并在对应版本 `README.md` 记录执行说明。
- `snapshots/` 需要随表结构变化同步更新，但不能替代 `releases/vX/` 下的增量脚本。
- 提交数据库相关改动时，变更摘要必须列出新增或调整的 SQL 文件。

## 专辑闯关模式

- 15 张录音室专辑（Jay 2000 → 最伟大的作品 2022），按发行时间排序。
- 首张专辑 `JAY` 默认解锁，后续专辑需前一专辑得分 ≥ 8/10 解锁。
- 每道题关联 `album` 字段，跨专辑/非录音室曲目 `album = NULL`，不纳入专辑模式抽题。
- 专辑进度持久化到 `album_progress` 表，记录最高分和挑战次数。

## 用户系统

- Sa-Token + JWT 双令牌模式，`/api/auth/register` 和 `/api/auth/login`。
- `SaTokenConfig` 定义路由拦截规则：公开接口（`/api/health/**`、`/api/auth/**`、`/api/questions/**`、`/api/stats/overview`、`/api/stats/submit`）放行，其余需登录。
- 密码 BCrypt 加密存储。
- 游客仍可完整答题，登录后可查看历史记录、排行榜、专辑进度。

## 移动端与分享

- Hash 路由 (`createWebHashHistory`)：静态部署无需 Nginx fallback。
- Element Plus 中文语言包，触摸区域 ≥ 44×44px。
- 分享三级降级：Web Share API → 剪贴板复制 → 手动引导蒙层。
- Canvas 生成证书图片，规格 1080×1520px。
