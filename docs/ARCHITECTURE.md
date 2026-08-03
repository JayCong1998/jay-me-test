# 杰迷试炼架构说明

> 本文档只记录系统运行时结构、模块协作、核心生命周期和安全边界。产品规则见 [PRD.md](PRD.md)；技术栈、接口清单和开发命令见 [TECH.md](TECH.md)。

## 1. 系统边界

```text
用户浏览器
  ├─ frontend: Vue 3 H5，面向普通用户
  └─ admin: React 管理端，面向运营管理员

Spring Boot Backend
  ├─ 公共用户 API: /api/**
  ├─ 管理端 API: /api/admin/**
  ├─ 本地 Round 缓存: Guava Cache
  └─ MySQL: 持久化题库、用户、记录、专辑进度、管理员
```

前端和管理端都是独立 SPA。开发期通过 Vite 代理访问后端；生产期可将用户端构建产物放入 Spring Boot 静态资源，也可以由独立静态服务托管。

## 2. 请求数据流

```text
Vue / React 页面
  -> api 模块
  -> Axios
  -> Spring Controller
  -> Service
  -> Strategy / Hook / Cache
  -> Mapper
  -> MySQL
```

响应统一由 `R<T>` 包装。业务错误由 `BusinessException` 抛出，再由 `GlobalExceptionHandler` 转为统一失败响应。

## 3. 认证边界

### 3.1 用户端认证

- 用户注册和登录使用 `/api/auth/register`、`/api/auth/login`。
- 密码以 BCrypt 哈希存储。
- 登录后 token 放入 `Authorization` 请求头。
- 用户端本地存储键为 `jaymetest_auth`。
- 公开接口包括健康检查、注册登录、经典开局、通用答题校验、结果提交和统计概览。
- 专辑、深渊、排行榜、个人记录和 `/api/auth/me` 需要登录。

### 3.2 管理端认证

- 管理端路径统一为 `/api/admin/**`。
- 管理端使用 `AdminStpUtil` 和独立登录类型。
- `/api/admin/auth/login` 放行，其余管理端接口必须管理员登录。
- 管理端本地存储键为 `jaymetest_admin_auth`。

### 3.3 关键安全原则

- `QuestionDTO` 不返回 `correctOption`。
- 答案校验必须经过后端 Round 缓存。
- 结算成绩以服务端 Round 缓存统计为准，不信任前端提交的正确数。
- `game_record.round_id` 有唯一约束，防止同一 Round 重复提交。
- 专辑解锁由服务端校验，前端展示状态不能作为权限依据。
- 深渊续命前不返回正确答案和解析，避免重答前泄题。

## 4. 游戏策略结构

```text
QuestionService
  ├─ ClassicGameStrategy
  ├─ AlbumGameStrategy
  └─ AbyssGameStrategy

GameResultService
  -> GameStrategyFactory
  -> Strategy.calculateScore()
  -> Strategy.evaluateLevel()
  -> Strategy.getPostSubmitHooks()
```

`QuestionService` 是题目相关入口。开局时直接调用对应策略；通用校验时先从 `RoundCacheManager` 取出 Round，再通过 `GameStrategyFactory` 按 `GameMode` 分派给对应策略。

固定题量玩法共享 `AbstractFixedRoundGameStrategy`：

- 生成 `roundId`。
- 抽题并组装 `QuestionDTO`。
- 保存 `questionId -> correctOption`。
- 单题校验时记录作答结果。

深渊玩法实现批次式策略：

- 开局创建可变 Round 缓存。
- 按当前 streak 选择难度阶梯。
- 题目逐批追加到同一个 Round。
- 强制按服务端题目顺序作答。

## 5. Round 生命周期

### 5.1 固定题量模式

```text
GET /api/classic/round
或 GET /api/albums/round
  -> 策略抽取 20 道题
  -> RoundCacheManager 保存 Round
  -> 返回不含答案的题目列表

POST /api/questions/check
  -> 按 roundId 读取缓存
  -> 校验 questionId 和 selectedOption
  -> 记录该题是否正确
  -> 返回正确答案和解析

POST /api/game-results
  -> 校验 Round 存在且未提交
  -> 固定题量模式要求已答完整局
  -> 从缓存统计 correctCount
  -> 计算 score、accuracy、level、beatPercentage
  -> 插入 game_record
  -> 执行玩法后置钩子
  -> 删除 Round 缓存
```

### 5.2 深渊模式

```text
POST /api/abyss/start
  -> 创建深渊 Round
  -> 按 streak=0 生成首批 5 题
  -> 返回题目、streak、剩余续命次数

POST /api/abyss/check
  -> 校验当前题必须是服务端顺序中的下一题
  -> 答对：记录正确，streak + 1，推进题目指针
  -> 答错：记录错误，Round 进入 failed 状态
  -> 若可续命：不返回正确答案和解析

POST /api/abyss/revive
  -> 只允许当前答错题续命
  -> 清除该题错误记录
  -> 消耗 1 次续命
  -> Round 退出 failed 状态

POST /api/abyss/batch
  -> 仅当当前批次全部答完且未失败时允许
  -> 按最新 streak 生成下一批题

POST /api/game-results
  -> 仅当 Round 已失败时允许
  -> correctCount 即 streak
  -> score 等于 streak
```

### 5.3 缓存有效期

`RoundCacheManager` 使用 Guava Cache，Round 写入后 30 分钟过期。过期后继续校验或结算会返回业务错误，前端应提示重新开局。

## 6. 结算与后置钩子

`GameResultService` 是唯一结算入口：

1. 按 `roundId` 查询 `game_record`，拒绝重复提交。
2. 从 `RoundCacheManager` 读取 Round。
3. 按 Round 内部状态判断是否允许结算。
4. 通过策略计算分数和等级。
5. 尝试读取当前登录用户；游客记录允许 `user_id = null`。
6. 插入 `game_record`。
7. 计算同模式击败率。
8. 执行策略后置钩子。
9. 删除 Round 缓存。

当前后置钩子：

| Hook | 触发玩法 | 职责 |
| --- | --- | --- |
| `AlbumUnlockHook` | `ALBUM` | 更新专辑最佳分、挑战次数、通关时间，并在达到阈值时解锁下一张专辑。 |

## 7. 专辑进度模型

专辑枚举 `AlbumKey` 按发行顺序排列。当前 API 入参和数据库 `album_key` 使用专辑显示名，例如 `Jay`、`范特西`、`太阳之子`。

```text
用户首次请求专辑列表
  -> 查询 album_progress
  -> 若第一张专辑无记录，自动创建 unlocked=1
  -> 返回所有专辑及进度

用户请求专辑题目
  -> AlbumProgressService.canAccessAlbum()
  -> 已解锁才允许抽题

专辑结算
  -> 更新当前专辑 best_score / total_attempts / first_passed_at
  -> 若正确率 >= game.album.pass-accuracy
  -> 创建或更新下一专辑 unlocked=1
```

## 8. 排行榜聚合

排行榜只统计登录用户。

| 榜单 | 聚合方式 |
| --- | --- |
| 经典榜 | 用户维度取最佳经典记录。 |
| 专辑榜 | 用户每张专辑取最佳记录，再聚合通关专辑数、总成绩和总用时。 |
| 深渊榜 | 用户维度取最高 streak。 |

排序都包含稳定 tie-breaker，避免分页时排名抖动。

## 9. 前端运行时状态

### 9.1 用户端

- `gameStore` 只保存当前答题 Round 的内存状态，刷新后不会恢复。
- `authStore` 持久化 token 和用户信息。
- `albumStore` 保存专辑列表、加载态和错误态。
- `recordStore` 支持个人记录分页、下拉刷新和触底加载。
- `useQuiz` 统一编排开局、答题、深渊续命、深渊预加载和结算。

### 9.2 管理端

- `AuthContext` 保存管理员 token。
- `ProtectedRoute` 负责登录守卫。
- 管理端 API 客户端遇到 401/403 会清除本地登录态并跳转登录页。

## 10. 关键文件索引

### 10.1 后端

| 文件 | 职责 |
| --- | --- |
| `backend/src/main/java/com/jaymetest/controller/ClassicController.java` | 经典模式开局入口。 |
| `backend/src/main/java/com/jaymetest/controller/QuestionController.java` | 通用单题校验入口。 |
| `backend/src/main/java/com/jaymetest/controller/AbyssController.java` | 深渊开局、批次、校验和续命。 |
| `backend/src/main/java/com/jaymetest/controller/AlbumController.java` | 专辑列表和专辑开局。 |
| `backend/src/main/java/com/jaymetest/controller/GameResultController.java` | 游戏结果提交。 |
| `backend/src/main/java/com/jaymetest/controller/LeaderboardController.java` | 三类排行榜查询。 |
| `backend/src/main/java/com/jaymetest/service/GameResultService.java` | 结算主流程。 |
| `backend/src/main/java/com/jaymetest/service/game/cache/GameRoundCache.java` | Round 内部状态和深渊状态机。 |
| `backend/src/main/java/com/jaymetest/service/game/cache/RoundCacheManager.java` | Round TTL 缓存。 |
| `backend/src/main/java/com/jaymetest/service/game/strategy/impl/ClassicGameStrategy.java` | 经典模式策略。 |
| `backend/src/main/java/com/jaymetest/service/game/strategy/impl/AlbumGameStrategy.java` | 专辑模式策略。 |
| `backend/src/main/java/com/jaymetest/service/game/strategy/impl/AbyssGameStrategy.java` | 深渊模式策略。 |
| `backend/src/main/java/com/jaymetest/service/AlbumProgressService.java` | 专辑解锁和进度。 |
| `backend/src/main/java/com/jaymetest/mapper/GameRecordMapper.java` | 记录统计和排行榜 SQL。 |
| `backend/src/main/java/com/jaymetest/config/SaTokenConfig.java` | 用户端和管理端认证拦截。 |

### 10.2 用户端

| 文件 | 职责 |
| --- | --- |
| `frontend/src/composables/useQuiz.ts` | 答题流程编排。 |
| `frontend/src/stores/gameStore.ts` | 当前游戏状态。 |
| `frontend/src/api/client.ts` | Axios 客户端。 |
| `frontend/src/pages/HomePage.vue` | 首页和玩法入口。 |
| `frontend/src/pages/QuizPage.vue` | 答题交互。 |
| `frontend/src/pages/ResultPage.vue` | 结果展示和后续动作。 |
| `frontend/src/pages/AlbumListPage.vue` | 专辑列表和挑战入口。 |
| `frontend/src/pages/LeaderboardPage.vue` | 排行榜。 |
| `frontend/src/pages/ProfilePage.vue` | 用户身份和个人记录。 |

### 10.3 管理端

| 文件 | 职责 |
| --- | --- |
| `admin/src/main.tsx` | 路由、主题和登录守卫。 |
| `admin/src/context/AuthContext.tsx` | 管理员登录态。 |
| `admin/src/api/client.ts` | 管理端 Axios 客户端。 |
| `backend/src/main/java/com/jaymetest/controller/admin` | 管理端后端入口。 |
