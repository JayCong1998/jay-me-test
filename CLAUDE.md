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

**杰迷结业考试 (jay-me-test)** — 周杰伦粉丝答题 H5 应用。核心玩法：10 道随机单选题 → 5 级杰迷等级鉴定 → 电子证书分享。支持免登游客模式和注册用户模式。

**V2 新增模块**：用户注册/登录（Sa-Token JWT）、专辑闯关模式（15 张专辑解锁进度）、排行榜（总榜/日榜/等级榜）、AI 问答（DashScope）。

详细设计文档见 [docs/TECH.md](docs/TECH.md)（技术设计）和 [docs/PRD.md](docs/PRD.md)（产品需求）。

## 项目结构

```
jay-me-test/
├── CLAUDE.md                          # 项目指引（本文件）
├── docs/
│   ├── PRD.md                         # 产品需求文档
│   ├── TECH.md                        # 技术设计文档
│   └── info.md                        # 开发信息（账号、题目分类）
│
├── backend/                           # Spring Boot 3 后端
│   ├── pom.xml
│   └── src/main/java/com/jaymetest/
│       ├── JayMeTestApplication.java
│       ├── config/
│       │   ├── WebConfig.java         # CORS 跨域
│       │   ├── MyBatisPlusConfig.java # 分页插件
│       │   ├── Knife4jConfig.java     # OpenAPI 文档
│       │   ├── SaTokenConfig.java     # Sa-Token 路由拦截
│       │   └── StpInterfaceImpl.java  # 权限角色加载
│       ├── controller/
│       │   ├── HealthController.java  # GET /api/health
│       │   ├── AuthController.java    # 注册/登录/当前用户
│       │   ├── QuestionController.java# 抽题/校验/复活
│       │   ├── StatsController.java   # 提交结果/统计/我的记录
│       │   ├── LeaderboardController.java # 排行榜
│       │   └── AlbumController.java   # 专辑列表/专辑抽题
│       ├── ai/
│       │   ├── controller/AiController.java # AI 问答接口
│       │   └── service/AiService.java       # AI 调用编排
│       ├── service/
│       │   ├── QuestionService.java   # 抽题、校验、复活、缓存清理
│       │   ├── StatsService.java      # 等级、百分位、持久化、统计
│       │   ├── AuthService.java       # 注册/登录/用户信息
│       │   ├── LeaderboardService.java# 排行榜查询
│       │   ├── AlbumProgressService.java # 解锁/进度/闯关权限
│       │   └── RoundCache.java        # Round 缓存数据结构
│       ├── mapper/
│       │   ├── QuestionMapper.java    # 按难度/专辑随机抽题
│       │   ├── GameRecordMapper.java  # 百分位/排行/用户记录
│       │   ├── UserMapper.java        # 按邮箱查用户
│       │   └── AlbumProgressMapper.java # 专辑进度 CRUD
│       ├── model/
│       │   ├── entity/                # Question, GameRecord, User, AlbumProgress
│       │   ├── dto/                   # 请求/响应 DTO（R, RoundDTO, GameResultDTO 等）
│       │   └── enums/                 # FanLevel, QuestionCategory, DifficultyLevel, AlbumKey
│       └── exception/
│           ├── GlobalExceptionHandler.java
│           └── BusinessException.java
│
├── frontend/                          # Vue 3 + Vite SPA
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.ts                    # createApp + router/pinia/element-plus
│       ├── App.vue
│       ├── router/index.ts            # Hash 路由（8 个页面）
│       ├── pages/
│       │   ├── HomePage.vue           # 首页：昵称、模式选择
│       │   ├── QuizPage.vue           # 答题页
│       │   ├── ResultPage.vue         # 结果页
│       │   ├── CertificatePage.vue    # 证书页
│       │   ├── AlbumListPage.vue      # 专辑闯关列表
│       │   ├── LeaderboardPage.vue    # 排行榜
│       │   ├── LoginPage.vue          # 登录
│       │   └── RegisterPage.vue       # 注册
│       ├── components/
│       │   ├── quiz/                  # QuestionCard, FeedbackBar
│       │   └── album/                 # AlbumCard
│       ├── composables/
│       │   ├── useQuiz.ts             # 答题流程编排
│       │   └── useTimer.ts            # 计时器
│       ├── stores/
│       │   ├── gameStore.ts           # 游戏状态（内存）
│       │   ├── userStore.ts           # 用户数据（localStorage）
│       │   ├── authStore.ts           # 登录态（token + 用户信息）
│       │   └── albumStore.ts          # 专辑进度状态
│       ├── api/
│       │   ├── client.ts              # Axios 实例（baseURL /api）
│       │   ├── questionApi.ts         # 题目接口
│       │   ├── statsApi.ts            # 统计接口
│       │   ├── authApi.ts             # 认证接口
│       │   ├── albumApi.ts            # 专辑接口
│       │   └── leaderboardApi.ts      # 排行榜接口
│       └── utils/
│           ├── constants.ts           # 等级配置、分享文案
│           ├── levels.ts              # 客户端等级计算（降级用）
│           ├── albums.ts              # 专辑常量（15 张 + 解锁阈值）
│           └── format.ts              # 日期格式化
│
└── database/                          # 数据库脚本
    ├── create_table.sql               # DDL（question + game_record + user + album_progress）
    ├── add_question.sql               # 70 道题目种子数据
    ├── add_user.sql                   # 测试用户
    ├── migration_album_mode.sql       # V2 迁移脚本（album 列 + album_progress 表 + game_record 扩展）
    └── backfill_album.sql             # 回填现有题目的 album 字段
```

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
mvn test                         # 运行测试
```

API 文档 (Knife4j)：`http://localhost:8080/doc.html`

### 生产打包

1. `frontend/` 下执行 `npm run build` → 将 `dist/` 复制到 `backend/src/main/resources/static/`
2. `mvn package` → 生成含内嵌 Tomcat + Vue 静态文件的 Fat JAR

## 架构

### 数据流

```
Vue 组件 → API 模块 (questionApi/statsApi/...) → Axios (/api/*) → Spring Controller → Service → Mapper (MyBatis Plus) → MySQL
                                      ↑
                              Sa-Token JWT (登录用户)
```

### 核心安全模式

- **经典模式**：`GET /api/questions/round` 返回的 `QuestionDTO` **不含** `correctOption`。答案校验在服务端通过 `POST /api/questions/check` 完成——后端持有 `ConcurrentHashMap<String, RoundCache>`，前端无法获取正确答案、无法预测 roundId。
- **用户系统**：Sa-Token + JWT 双令牌模式。注册密码 BCrypt 加密。敏感接口（排行榜、专辑进度、我的记录）需登录，游客可正常答题。
- 复活接口返回 `{revived: true, remainingRevivals: 0}`，不返回答案本身。
- `game_record` 表 `uk_round_id` 唯一约束防重复提交。

### Round 生命周期

1. 客户端请求 `GET /api/questions/round?count=10&albumKey=xxx` → 服务端生成 UUID `roundId`，随机抽取题目（经典模式：6 简单 + 4 中等；专辑模式：指定专辑下按难度混合），将 `(questionId → correctOption)` 映射存入 `RoundCache`，返回不含答案的题目列表。
2. 客户端逐题通过 `POST /api/questions/check` 提交答案 → 服务端根据 `roundId` 查找缓存比对。
3. 客户端通过 `POST /api/stats/submit` 提交最终结果 → 服务端按 `roundId` 去重，计算等级和百分位，持久化到 `game_record` 表（含 `mode`、`albumKey`、`userId`、`nickname` 字段）。
4. 专辑模式下，得分 ≥ 8/10 自动解锁下一张专辑。
5. `QuestionService.cleanExpiredCache()` 每 10 分钟执行一次，清除超过 30 分钟的 round。

### 后端关键文件

| 文件 | 职责 |
|------|------|
| [QuestionController.java](backend/src/main/java/com/jaymetest/controller/QuestionController.java) | 题目 API：抽题 `/round`、校验 `/check`、复活 `/revive` |
| [StatsController.java](backend/src/main/java/com/jaymetest/controller/StatsController.java) | 统计 API：提交结果 `/submit`、全局概览 `/overview`、我的记录 `/my-records` |
| [AuthController.java](backend/src/main/java/com/jaymetest/controller/AuthController.java) | 认证 API：注册 `/register`、登录 `/login`、当前用户 `/me` |
| [AlbumController.java](backend/src/main/java/com/jaymetest/controller/AlbumController.java) | 专辑 API：专辑列表 `/list`、专辑抽题 `/round`（均需登录） |
| [LeaderboardController.java](backend/src/main/java/com/jaymetest/controller/LeaderboardController.java) | 排行榜 API：总榜/日榜/等级榜（需登录） |
| [HealthController.java](backend/src/main/java/com/jaymetest/controller/HealthController.java) | 健康检查 `/api/health` |
| [AiController.java](backend/src/main/java/com/jaymetest/ai/controller/AiController.java) | AI 问答 `/api/ai/query`（DashScope 流式响��） |
| [QuestionService.java](backend/src/main/java/com/jaymetest/service/QuestionService.java) | 抽题、答案校验、复活、缓存清理 |
| [StatsService.java](backend/src/main/java/com/jaymetest/service/StatsService.java) | 等级匹配、百分位计算、结果持久化、全局统计 |
| [AuthService.java](backend/src/main/java/com/jaymetest/service/AuthService.java) | 注册/登录逻辑、BCrypt 密码验证、JWT 令牌生成 |
| [AlbumProgressService.java](backend/src/main/java/com/jaymetest/service/AlbumProgressService.java) | 专辑解锁判断、进度查询/更新、闯关权限校验 |
| [LeaderboardService.java](backend/src/main/java/com/jaymetest/service/LeaderboardService.java) | 多维度排行查询（总榜/日榜/等级榜） |
| [RoundCache.java](backend/src/main/java/com/jaymetest/service/RoundCache.java) | Round 缓存数据结构（answerMap + createdAt + 30min TTL） |
| [QuestionMapper.java](backend/src/main/java/com/jaymetest/mapper/QuestionMapper.java) | 题目查询：按难度随机抽取、按专辑随机抽取、总数统计 |
| [GameRecordMapper.java](backend/src/main/java/com/jaymetest/mapper/GameRecordMapper.java) | 游戏记录查询：百分位计数、排行数据、用户记录 |
| [UserMapper.java](backend/src/main/java/com/jaymetest/mapper/UserMapper.java) | 用户查询：按邮箱查找 |
| [AlbumProgressMapper.java](backend/src/main/java/com/jaymetest/mapper/AlbumProgressMapper.java) | 专辑进度 CRUD：按 userId+albumKey 查询/更新 |
| [FanLevel.java](backend/src/main/java/com/jaymetest/model/enums/FanLevel.java) | 5 级枚举：PASSERBY(0-2) → JUNIOR(3-4) → INTERMEDIATE(5-6) → SENIOR(7-8) → ULTIMATE(9-10) |
| [AlbumKey.java](backend/src/main/java/com/jaymetest/model/enums/AlbumKey.java) | 15 张录音室专辑枚举（JAY → GREATEST_WORKS） |
| [R.java](backend/src/main/java/com/jaymetest/model/dto/R.java) | 统一响应包装 `{code, msg, data, timestamp}` |
| [GlobalExceptionHandler.java](backend/src/main/java/com/jaymetest/exception/GlobalExceptionHandler.java) | `@RestControllerAdvice` 统一异常拦截 |
| [SaTokenConfig.java](backend/src/main/java/com/jaymetest/config/SaTokenConfig.java) | Sa-Token 路由拦截规则（白名单 + 需登录名单） |
| [StpInterfaceImpl.java](backend/src/main/java/com/jaymetest/config/StpInterfaceImpl.java) | 权限/角色加载实现 |

### 前端关键文件

| 文件 | 职责 |
|------|------|
| [useQuiz.ts](frontend/src/composables/useQuiz.ts) | 编排完整答题流程：开始 → 提交答案 → 复活 → 完成并提交 |
| [useTimer.ts](frontend/src/composables/useTimer.ts) | 答题计时器 composable |
| [gameStore.ts](frontend/src/stores/gameStore.ts) | 内存状态（Pinia）：roundId、题目列表、当前索引、答案/结果 Map、复活、游戏阶段 |
| [userStore.ts](frontend/src/stores/userStore.ts) | localStorage 持久化：昵称、历史记录（最多 20 条）、最高分 |
| [authStore.ts](frontend/src/stores/authStore.ts) | 登录态管理：token 持久化、用户信息、登录/注册/登出操作 |
| [albumStore.ts](frontend/src/stores/albumStore.ts) | 专辑进度状态：专辑列表、解锁状态、最高分 |
| [client.ts](frontend/src/api/client.ts) | Axios 实例，baseURL `/api`，10s 超时，响应拦截器解包 `R<T>`，请求拦截器注入 Sa-Token |
| [questionApi.ts](frontend/src/api/questionApi.ts) | `/api/questions/*` 接口：fetchRound、checkAnswer、revive |
| [statsApi.ts](frontend/src/api/statsApi.ts) | `/api/stats/*` 接口：submitResult、fetchOverview、fetchMyRecords |
| [authApi.ts](frontend/src/api/authApi.ts) | `/api/auth/*` 接口：register、login、fetchMe |
| [albumApi.ts](frontend/src/api/albumApi.ts) | `/api/albums/*` 接口：fetchAlbumList、fetchAlbumRound |
| [leaderboardApi.ts](frontend/src/api/leaderboardApi.ts) | `/api/leaderboard` 接口：fetchLeaderboard |
| [constants.ts](frontend/src/utils/constants.ts) | `R<T>` 类型、`LevelConfig[]` 等级配置、证书尺寸常量、分享文案模板 |
| [levels.ts](frontend/src/utils/levels.ts) | 客户端等级计算逻辑（API 提交失败时的降级方案） |
| [albums.ts](frontend/src/utils/albums.ts) | 15 张专辑常量（key、displayName、year、gradient）+ 解锁阈值 |
| [format.ts](frontend/src/utils/format.ts) | 日期/时间格式化工具 |
| [router/index.ts](frontend/src/router/index.ts) | Hash 路由：`/`→首页，`/quiz`→答题，`/result`→结果，`/certificate`→证书，`/albums`→专辑，`/leaderboard`→排行，`/login`→登录，`/register`→注册 |
| [HomePage.vue](frontend/src/pages/HomePage.vue) | 首页：昵称输入、模式选择（经典/专辑）、开始答题 |
| [QuizPage.vue](frontend/src/pages/QuizPage.vue) | 答题页：进度条、题目卡片、选项、反馈 |
| [ResultPage.vue](frontend/src/pages/ResultPage.vue) | 结果页：等级展示、数据面板、分享 |
| [CertificatePage.vue](frontend/src/pages/CertificatePage.vue) | 证书页：Canvas 预览、保存/分享 |
| [AlbumListPage.vue](frontend/src/pages/AlbumListPage.vue) | 专辑闯关列表：15 张专辑卡片、解锁状态、最高分 |
| [LeaderboardPage.vue](frontend/src/pages/LeaderboardPage.vue) | 排行榜：总榜/日榜/等级榜切换 |
| [QuestionCard.vue](frontend/src/components/quiz/QuestionCard.vue) | 题目卡片 + `el-radio-group` 选项 |
| [FeedbackBar.vue](frontend/src/components/quiz/FeedbackBar.vue) | 答题对错反馈条 |
| [AlbumCard.vue](frontend/src/components/album/AlbumCard.vue) | 专辑卡片：封面渐变、年份、锁定/解锁状态、最高分 |

### 数据库

`jaymetest` 库中 4 张表：

| 表 | 说明 |
|----|------|
| `question` | 题库（70 题），含 `category`、`difficulty`、`album` 列 |
| `game_record` | 匿名/登录游戏记录，`round_id` UUID 去重，含 `mode`、`album_key`、`user_id`、`nickname` |
| `user` | 注册用户，邮箱+BCrypt 哈希密码+昵称 |
| `album_progress` | 专辑闯关进度，`(user_id, album_key)` 唯一约束，含 `unlocked`、`best_score`、`total_attempts` |

脚本位于 `database/`：
- `create_table.sql` — 建库建表
- `add_question.sql` — 70 道题目种子数据
- `add_user.sql` — 测试用户
- `migration_album_mode.sql` — V2 专辑模式迁移（question 表新增 album 列 + 新建 album_progress 表 + game_record 扩展）
- `backfill_album.sql` — 回填现有题目的 album 字段

### 专辑闯关模式

- 15 张录音室专辑（Jay 2000 → 最伟大的作品 2022），按发行时间排序
- 首张专辑 `JAY` 默认解锁，后续专辑需前一专辑得分 ≥ 8/10 解锁
- 每道题关联 `album` 字段，跨专辑/非录音室曲目 `album = NULL`（不纳入专辑模式抽题）
- 专辑进度持久化到 `album_progress` 表，记录最高分和挑战次数

### 用户系统

- Sa-Token + JWT 双令牌模式，`/api/auth/register` 和 `/api/auth/login`
- `SaTokenConfig` 定义路由拦截规则：公开接口（`/api/health/**`、`/api/auth/**`、`/api/questions/**`、`/api/stats/overview`、`/api/stats/submit`）放行，其余需登录
- 密码 BCrypt 加密存储
- 游客仍可完整答题，登录后可查看历史记录、排行榜、专辑进度

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
- 参数校验使用 `@Valid` + `jakarta.validation`，校验失败由 `GlobalExceptionHandler` 统一处理
- Sa-Token 配置：`token-style: random-64`，超时 7 天（604800s），JWT 密钥在 `application.yml` 中
- Spring AI Alibaba DashScope：AI 问答接口返回 `Flux<String>` 流式响应
- Application 启动类所在包是 `com.jaymetest`，MyBatis Plus 自动扫描该包下的所有 mapper
