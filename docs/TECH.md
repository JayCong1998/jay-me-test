# 杰迷结业考试 (jay-me-test) 技术设计文档

> 版本：v2.0 | 日期：2026-07-17 | 状态：迭代开发中

---

## 1. 技术栈

### 1.1 技术选型

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **运行时** | JDK | 21 | LTS 版本，虚拟线程支持 |
| **后端框架** | Spring Boot | 3.3.0 | 内嵌 Tomcat，自动配置 |
| **ORM** | MyBatis Plus | 3.5.7 | 零 SQL CRUD + 自定义注解查询 |
| **认证** | Sa-Token | 1.38.0 | 轻量级认证框架 + JWT 集成 |
| **密码加密** | Spring Security Crypto | — | BCrypt 哈希 |
| **数据库** | MySQL | 8.0+ | 关系型数据库 |
| **API 文档** | Knife4j | 4.5.0 | Swagger UI 增强版，在线调试 |
| **AI 平台** | Spring AI Alibaba DashScope | 1.1.0.0 | 通义千问 LLM，流式响应 |
| **HTTP 客户端** | WebFlux | — | 响应式 HTTP 客户端（AI 流式调用） |
| **前端框架** | Vue 3 | 3.5.x | Composition API + `<script setup>` |
| **UI 组件库** | Element Plus | 2.9.x | 企业级 Vue 3 组件库 |
| **状态管理** | Pinia | 2.2.x | Vue 3 官方推荐 |
| **路由** | Vue Router | 4.4.x | Hash 模式 |
| **HTTP 客户端** | Axios | 1.7.x | 请求/响应拦截 |
| **构建工具** | Vite | 6.x | 极速 HMR |
| **语言** | TypeScript | 5.6.x | 类型安全 |
| **CSS 预处理** | Sass | 1.80.x | SCSS 语法 |

### 1.2 选型理由

- **MyBatis Plus** 而非 JPA/Hibernate：直接写 SQL 更灵活可控，支持自定义注解查询
- **MySQL** 而非 SQLite：成熟稳定的关系型数据库，支持并发读写、事务处理
- **Sa-Token** 而非 Spring Security：更轻量，API 简洁，原生支持 JWT 和注解鉴权
- **Knife4j** 而非 SpringDoc 裸用：提供可视化的在线 API 调试界面
- **Element Plus** 而非 Vant/TailwindCSS：提供开箱即用的移动端适配组件（Dialog、Radio、Progress 等）
- **Hash 路由** 而非 History 模式：H5 部署到静态服务器无需 Nginx fallback 配置
- **Spring AI Alibaba** 而非直接调用 DashScope API：统一 ChatModel/ChatClient 抽象，流式响应内置支持

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Vue 3 SPA (H5)                          │   │
│  │  Element Plus → Pinia → Vue Router → Axios          │   │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌────────┐    │   │
│  │  │ 首页 │ │答题页│ │结果页│ │证书页│ │专辑列表│    │   │
│  │  └──────┘ └──────┘ └──────┘ └──────┘ └────────┘    │   │
│  │  ┌──────┐ ┌──────┐                                  │   │
│  │  │ 登录 │ │ 排行 │   localStorage (昵称+Token+历史) │   │
│  │  └──────┘ └──────┘                                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                           │ HTTP/REST (+ JWT Header)        │
└───────────────────────────┼─────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────┐
│                        服务层                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Spring Boot 3 (JDK 21)                     │   │
│  │                                                      │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌───────────┐  │   │
│  │  │Question │ │ Stats   │ │ Auth    │ │ Album     │  │   │
│  │  │Controllr│ │Controllr│ │Controllr│ │Controllr  │  │   │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └─────┬─────┘  │   │
│  │       │           │           │             │         │   │
│  │  ┌────┴───┐ ┌────┴───┐ ┌────┴───┐ ┌──────┴──────┐  │   │
│  │  │Question│ │ Stats  │ │ Auth   │ │AlbumProgress│  │   │
│  │  │Service │ │Service │ │Service │ │Service      │  │   │
│  │  │·Round  │ │·等级   │ │·注册   │ │·解锁判断   │  │   │
│  │  │ 缓存   │ │·百分位 │ │·登录   │ │·进度更新   │  │   │
│  │  │·随机   │ │·持久化 │ │·JWT    │ │·通关反馈   │  │   │
│  │  └────┬───┘ └────┬───┘ └────┬───┘ └──────┬──────┘  │   │
│  │       │           │           │             │         │   │
│  │  ┌────┴───┐ ┌────┴───┐ ┌────┴───┐ ┌──────┴──────┐  │   │
│  │  │Question│ │GameRcd │ │ User   │ │AlbumProg   │  │   │
│  │  │Mapper  │ │Mapper  │ │Mapper  │ │Mapper      │  │   │
│  │  └────┬───┘ └────┬───┘ └────┬───┘ └──────┬──────┘  │   │
│  └───────┼──────────┼──────────┼─────────────┼─────────┘   │
│          │          │          │             │              │
│  ┌───────▼──────────▼──────────▼─────────────▼─────────┐   │
│  │                MySQL 8.0 (jaymetest)                  │   │
│  │  ┌──────────┐ ┌────────────┐ ┌──────┐ ┌───────────┐ │   │
│  │  │question  │ │game_record │ │user  │ │album_prog │ │   │
│  │  │(70 rows)│ │ (∞ rows)  │ │      │ │ress       │ │   │
│  │  └──────────┘ └────────────┘ └──────┘ └───────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  外部服务                                            │   │
│  │  ┌──────────────────────────┐                       │   │
│  │  │ DashScope (通义千问)      │  ← AI 流式问答       │   │
│  │  └──────────────────────────┘                       │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 数据流

```
┌──────────────────────────────────────────────────────────────┐
│ 请求流程                                                      │
│                                                              │
│  Vue Component → API (axios) → Controller → Service → Mapper │
│       ↑                                                    ↓ │
│       └─────────── JSON Response ←─────────────────────────┘ │
│                                                              │
│ 认证流程：                                                     │
│   注册/登录 → AuthService → BCrypt校验 → Sa-Token JWT 签发   │
│   后续请求 → Axios拦截器注入 Authorization Header              │
│           → SaTokenConfig 路由拦截 → StpUtil 校验            │
│                                                              │
│ 示例：用户提交答案                                             │
│   QuizPage.vue                                               │
│     → questionApi.check({roundId, questionId, selectedOption})│
│       → POST /api/questions/check                            │
│         → QuestionController.check()                         │
│           → QuestionService.checkAnswer()                    │
│             → RoundCache.get(roundId)  // 服务端比对答案      │
│           ← AnswerResultDTO {correct, correctOption, explain} │
│       ← R<AnswerResultDTO>                                   │
│     ← 更新 gameStore + 显示反馈                               │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. 项目结构

### 3.1 后端项目结构

```
backend/
├── pom.xml
└── src/main/java/com/jaymetest/
    ├── JayMeTestApplication.java
    ├── ai/
    │   ├── controller/AiController.java       # /api/ai/query (流式)
    │   └── service/AiService.java             # AI 调用编排（占位）
    ├── config/
    │   ├── WebConfig.java                     # CORS 跨域
    │   ├── MyBatisPlusConfig.java             # 分页插件
    │   ├── Knife4jConfig.java                 # OpenAPI 文档
    │   ├── SaTokenConfig.java                 # 路由拦截规则
    │   └── StpInterfaceImpl.java              # 权限角色加载
    ├── controller/
    │   ├── HealthController.java              # GET /api/health
    │   ├── AuthController.java                # 注册/登录/当前用户
    │   ├── QuestionController.java            # 抽题/校验/复活
    │   ├── StatsController.java               # 提交结果/统计/我的记录
    │   ├── AlbumController.java               # 专辑列表/专辑抽题 (需登录)
    │   └── LeaderboardController.java         # 排行榜 (需登录)
    ├── service/
    │   ├── QuestionService.java               # 抽题 + 校验 + 复活 + 缓存清理
    │   ├── StatsService.java                  # 等级 + 百分位 + 持久化 + 统计
    │   ├── AuthService.java                   # 注册/登录/用户信息
    │   ├── AlbumProgressService.java          # 解锁/进度/通关反馈
    │   ├── LeaderboardService.java            # 多维度排行查询
    │   └── RoundCache.java                    # Round 缓存 (30min TTL)
    ├── mapper/
    │   ├── QuestionMapper.java                # extends BaseMapper<Question>
    │   ├── GameRecordMapper.java              # extends BaseMapper<GameRecord>
    │   ├── UserMapper.java                    # extends BaseMapper<User>
    │   └── AlbumProgressMapper.java           # extends BaseMapper<AlbumProgress>
    ├── model/
    │   ├── entity/
    │   │   ├── Question.java                  # @TableName("question")
    │   │   ├── GameRecord.java                # @TableName("game_record")
    │   │   ├── User.java                      # @TableName("user")
    │   │   └── AlbumProgress.java             # @TableName("album_progress")
    │   ├── dto/                               # 请求/响应 DTO (~20 个类)
    │   └── enums/
    │       ├── QuestionCategory.java          # LYRICS | WORKS | SCREEN | KNOWLEDGE
    │       ├── DifficultyLevel.java           # EASY | MEDIUM | HARD
    │       └── AlbumKey.java                  # 16 张专辑枚举
    └── exception/
        ├── GlobalExceptionHandler.java
        └── BusinessException.java
```

### 3.2 前端项目结构

```
frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
└── src/
    ├── main.ts                               # createApp + use router/pinia/element-plus
    ├── App.vue                               # <router-view>
    ├── styles/
    │   └── global.scss                       # Element Plus 主题覆盖
    ├── router/
    │   └── index.ts                          # createWebHashHistory (8 个路由)
    ├── stores/
    │   ├── gameStore.ts                      # 游戏状态（内存）
    │   ├── userStore.ts                      # 用户数据（localStorage）
    │   ├── authStore.ts                      # 登录态（token + 用户信息）
    │   └── albumStore.ts                     # 专辑进度
    ├── composables/
    │   ├── useQuiz.ts                        # 答题流程编排
    │   └── useTimer.ts                       # 计时器
    ├── api/
    │   ├── client.ts                         # axios 实例（baseURL + 拦截器）
    │   ├── questionApi.ts                    # /api/questions/* 接口
    │   ├── statsApi.ts                       # /api/stats/* 接口
    │   ├── authApi.ts                        # /api/auth/* 接口
    │   ├── albumApi.ts                       # /api/albums/* 接口
    │   └── leaderboardApi.ts                 # /api/leaderboard 接口
    ├── pages/
    │   ├── HomePage.vue                      # 首页（模式选择）
    │   ├── QuizPage.vue                      # 答题页
    │   ├── ResultPage.vue                    # 结果页
    │   ├── CertificatePage.vue               # 证书页
    │   ├── AlbumListPage.vue                 # 专辑列表
    │   ├── LeaderboardPage.vue               # 排行榜
    │   ├── LoginPage.vue                     # 登录
    │   └── RegisterPage.vue                  # 注册
    ├── components/
    │   ├── quiz/
    │   │   ├── QuestionCard.vue              # 题目 + el-radio-group
    │   │   └── FeedbackBar.vue               # 对错反馈条
    │   └── album/
    │       └── AlbumCard.vue                 # 专辑卡片
    └── utils/
        ├── constants.ts                      # 等级配置、分享文案
        ├── levels.ts                         # 客户端等级计算（降级用）
        ├── albums.ts                         # 15 张专辑常量 + 解锁阈值
        └── format.ts                         # 日期格式化
```

---

## 4. 数据库设计

### 4.1 表结构

```sql
-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    category       VARCHAR(20) NOT NULL COMMENT '分类: LYRICS | WORKS | SCREEN | KNOWLEDGE',
    album          VARCHAR(50) NULL     COMMENT '所属专辑 (NULL=跨专辑通用)',
    difficulty     VARCHAR(20) NOT NULL COMMENT '难度: EASY | MEDIUM | HARD',
    question_text  TEXT        NOT NULL COMMENT '题目正文',
    option_a       TEXT        NOT NULL,
    option_b       TEXT        NOT NULL,
    option_c       TEXT        NOT NULL,
    option_d       TEXT        NOT NULL,
    correct_option CHAR(1)     NOT NULL COMMENT 'A|B|C|D',
    explanation    TEXT        NOT NULL COMMENT '答案解析',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_category     CHECK (category IN ('LYRICS', 'WORKS', 'SCREEN', 'KNOWLEDGE')),
    CONSTRAINT chk_difficulty   CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT chk_correct_opt  CHECK (correct_option IN ('A', 'B', 'C', 'D'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_q_category ON question(category);
CREATE INDEX idx_q_difficulty ON question(difficulty);
CREATE INDEX idx_q_cat_diff ON question(category, difficulty);
CREATE INDEX idx_q_album ON question(album);

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    email       VARCHAR(100) NOT NULL COMMENT '邮箱',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt 哈希密码',
    nickname    VARCHAR(20)  NOT NULL COMMENT '昵称',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 游戏记录表
CREATE TABLE IF NOT EXISTS game_record (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    round_id         VARCHAR(36) NOT NULL COMMENT 'UUID 去重',
    mode             VARCHAR(20) NOT NULL DEFAULT 'CLASSIC' COMMENT 'CLASSIC | ALBUM',
    album_key        VARCHAR(50) NULL     COMMENT '专辑模式下的专辑标识',
    user_id          BIGINT      NULL     COMMENT '用户ID（游客为NULL）',
    nickname         VARCHAR(20) NULL     COMMENT '昵称快照',
    total_questions  INT         NOT NULL DEFAULT 10,
    correct_count    INT         NOT NULL COMMENT '0-10',
    time_spent_secs  INT         NOT NULL COMMENT '秒',
    used_revival     TINYINT     NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_round_id UNIQUE (round_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_gr_score ON game_record(correct_count);
CREATE INDEX idx_gr_created ON game_record(created_at);
CREATE INDEX idx_gr_mode ON game_record(mode);
CREATE INDEX idx_gr_user ON game_record(user_id);

-- 专辑闯关进度表
CREATE TABLE IF NOT EXISTS album_progress (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    album_key         VARCHAR(50)  NOT NULL,
    unlocked          TINYINT      NOT NULL DEFAULT 0,
    best_score        INT          NOT NULL DEFAULT 0,
    total_attempts    INT          NOT NULL DEFAULT 0,
    first_passed_at   DATETIME     NULL COMMENT '首次通关时间(≥8/10)',
    last_attempted_at DATETIME     NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_album UNIQUE (user_id, album_key),
    INDEX idx_ap_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4.2 关键 Mapper 方法

```java
// QuestionMapper — 按难度随机抽题
@Select("SELECT * FROM question WHERE difficulty = #{difficulty} ORDER BY RAND() LIMIT #{limit}")
List<Question> selectRandomByDifficulty(String difficulty, int limit);

// QuestionMapper — 按专辑随机抽题
@Select("SELECT * FROM question WHERE album = #{album} ORDER BY RAND() LIMIT #{limit}")
List<Question> selectRandomByAlbum(String album, int limit);

// GameRecordMapper — 百分位统计
@Select("SELECT COUNT(*) FROM game_record WHERE correct_count < #{score}")
long countByCorrectCountLessThan(int score);

// UserMapper — 按邮箱查询
@Select("SELECT * FROM user WHERE email = #{email}")
User selectByEmail(String email);

// AlbumProgressMapper — 按用户+专辑查询
@Select("SELECT * FROM album_progress WHERE user_id = #{userId} AND album_key = #{albumKey}")
AlbumProgress selectByUserAndAlbum(long userId, String albumKey);

// AlbumProgressMapper — 按用户查全部
@Select("SELECT * FROM album_progress WHERE user_id = #{userId}")
List<AlbumProgress> selectByUserId(long userId);
```

---

## 5. REST API 规格

### 5.1 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1721000000000
}
```

### 5.2 接口清单

| # | Method | Path | Auth | 说明 |
|---|--------|------|------|------|
| 1 | `GET` | `/api/health` | — | 健康检查 |
| 2 | `GET` | `/api/questions/round?count=10` | — | 经典模式随机抽题 |
| 3 | `POST` | `/api/questions/check` | — | 校验单题答案 |
| 4 | `POST` | `/api/questions/revive` | — | 使用复活机会 |
| 5 | `POST` | `/api/stats/submit` | — | 提交游戏结果 |
| 6 | `GET` | `/api/stats/overview` | — | 全局统计概览 |
| 7 | `GET` | `/api/stats/my-records` | 需登录 | 当前用户的考试记录 |
| 8 | `POST` | `/api/auth/register` | — | 注册 |
| 9 | `POST` | `/api/auth/login` | — | 登录 |
| 10 | `GET` | `/api/auth/me` | 需登录 | 当前用户信息 |
| 11 | `GET` | `/api/albums/list` | 需登录 | 专辑列表及解锁状态 |
| 12 | `GET` | `/api/albums/round?albumKey=&count=10` | 需登录 | 专辑关卡抽题 |
| 13 | `GET` | `/api/leaderboard?type=&limit=&level=` | 需登录 | 排行榜 |
| 14 | `GET` | `/api/ai/query?message=` | — | AI 问答（流式，实验性） |

### 5.3 接口详细定义

#### 5.3.1 随机抽题（经典模式）

```
GET /api/questions/round?count=10

Response 200:
{
  "code": 200,
  "data": {
    "roundId": "uuid-xxxx",
    "questions": [
      {
        "id": 1,
        "category": "LYRICS",
        "difficulty": "EASY",
        "album": "SEVEN_SCENT",
        "questionText": "《七里香》中...",
        "options": ["A. ...", "B. ...", "C. ...", "D. ..."]
      }
    ]
  }
}
```
> **注意**：`QuestionDTO.options` 由 Entity 的 `getOptionsAsList()` 拼接 "A. " 前缀生成，**不含 correctOption 字段**。

#### 5.3.2 答案校验

```
POST /api/questions/check
Request:  { "roundId": "uuid-xxxx", "questionId": 1, "selectedOption": "A" }
Response: { "code": 200, "data": { "correct": true, "correctOption": "A", "explanation": "..." } }
```

#### 5.3.3 复活

```
POST /api/questions/revive
Request:  { "roundId": "uuid-xxxx", "questionId": 1 }
Response: { "code": 200, "data": { "revived": true, "remainingRevivals": 0 } }
```

#### 5.3.4 提交结果

```
POST /api/stats/submit
Request:
{
  "roundId": "uuid-xxxx",
  "correctCount": 7,
  "timeSpentSecs": 145,
  "usedRevival": 1,
  "mode": "CLASSIC",
  "albumKey": null
}

Response 200:
{
  "code": 200,
  "data": {
    "score": 70,
    "correctCount": 7,
    "totalQuestions": 10,
    "accuracy": 0.7,
    "timeSpentSecs": 145,
    "level": "SENIOR_FAN",
    "levelTitle": "高级杰迷",
    "levelDescription": "铁粉认证，演唱会前排选手就是你",
    "beatPercentage": 68.5,
    "totalPlayers": 12480,
    "albumResult": null   // null for CLASSIC mode
  }
}
```

#### 5.3.5 专辑模式提交结果（albumResult 示例）

```json
{
  "albumResult": {
    "albumKey": "JAY",
    "albumDisplayName": "Jay",
    "passed": true,
    "albumBestScore": 8,
    "isNewRecord": true,
    "unlockedNext": true,
    "nextAlbumKey": "FANTASY",
    "nextAlbumDisplayName": "范特西"
  }
}
```

#### 5.3.6 注册 / 登录

```
POST /api/auth/register
Request:  { "email": "test@example.com", "password": "123456", "nickname": "杰迷小明" }
Response: { "code": 200, "data": { "token": "xxx...", "user": { "id": 1, "email": "...", "nickname": "杰迷小明" } } }

POST /api/auth/login
Request:  { "email": "test@example.com", "password": "123456" }
Response: { "code": 200, "data": { "token": "xxx...", "user": { ... } } }
```

#### 5.3.7 专辑列表

```
GET /api/albums/list
Response:
{
  "code": 200,
  "data": [
    {
      "albumKey": "JAY",
      "displayName": "Jay",
      "year": 2000,
      "unlocked": true,
      "bestScore": 0,
      "totalAttempts": 0,
      "isFirst": true,
      "isLast": false
    },
    {
      "albumKey": "FANTASY",
      "displayName": "范特西",
      "year": 2001,
      "unlocked": false,
      "bestScore": 0,
      "totalAttempts": 0,
      "isFirst": false,
      "isLast": false
    }
    // ... 13 more
  ]
}
```

#### 5.3.8 专辑抽题

```
GET /api/albums/round?albumKey=JAY&count=10
// 返回格式与经典模式相同（RoundDTO），题目来自指定专辑
// 访问未解锁专辑返回 403
```

#### 5.3.9 排行榜

```
GET /api/leaderboard?type=total&limit=50
GET /api/leaderboard?type=daily&limit=50
GET /api/leaderboard?type=level&level=SENIOR_FAN&limit=50

Response:
{
  "code": 200,
  "data": {
    "entries": [
      { "rank": 1, "nickname": "杰迷达人", "score": 950, "level": "ULTIMATE", "gamesPlayed": 50 }
    ],
    "currentUserRank": 42
  }
}
```

#### 5.3.10 AI 问答

```
GET /api/ai/query?message=周杰伦的第一张专辑叫什么

Response: text/event-stream (SSE 流式)
// 逐字返回 AI 回复
```

---

## 6. 核心业务逻辑

### 6.1 随机抽题算法

```
经典模式（album = null）：
  1. 计算各难度数量：easyCount = 6, mediumCount = 4
  2. selectRandomByDifficulty('EASY', 6) + selectRandomByDifficulty('MEDIUM', 4)
  3. 合并 → Collections.shuffle() 打乱
  4. 生成 roundId → 存入 ConcurrentHashMap

专辑模式（album = "JAY"）：
  1. selectRandomByAlbum("JAY", 10)
  2. 专辑下有难度的自然混合，不强制比例
  3. 剩余逻辑同经典模式
```

### 6.2 5 级等级对照

```
答对 0-2 → PASSERBY     (路人粉)
答对 3-4 → JUNIOR       (初级杰迷)
答对 5-6 → INTERMEDIATE (中级杰迷)
答对 7-8 → SENIOR       (高级杰迷)
答对 9-10→ ULTIMATE     (终极杰迷)
```

### 6.3 百分位计算

```
percentile = (correct_count < userScore 的记录数 / 总记录数) × 100
保留 2 位小数，上限 99.99
```

### 6.4 Round 缓存策略

```
存储结构：ConcurrentHashMap<String, RoundCache>
  RoundCache { Map<Long, String> answerMap; Instant createdAt; }

TTL：30 分钟
清理：@Scheduled(fixedRate = 600_000) 每 10 分钟移除过期条目
```

### 6.5 专辑闯关核心算法

```
getAlbumList(userId):
  1. 从 AlbumKey 枚举遍历全部 16 张专辑
  2. 查询 album_progress WHERE user_id = #{userId} AND album_key = #{key}
  3. 首张专辑 (JAY) 无记录则自动创建 (unlocked=1)
  4. 组装 AlbumDTO 返回

processAlbumCompletion(userId, albumKey, correctCount):
  1. 查询或新建 album_progress 记录
  2. UPDATE total_attempts + 1, last_attempted_at = NOW()
  3. IF correctCount > best_score → UPDATE best_score, isNewRecord = true
  4. IF correctCount >= 8 (UNLOCK_THRESHOLD):
       IF 首次通关 → UPDATE first_passed_at, passed = true
       IF 不是最后一张 → UPDATE next album_progress SET unlocked = 1
  5. 返回 AlbumResultDTO

canAccessAlbum(userId, albumKey):
  1. 查询 album_progress WHERE user_id = #{userId} AND album_key = #{albumKey}
  2. IF 不存在 OR unlocked = 0 → throw BusinessException(403, "请先通关{上一张专辑名}(≥8/10)")
```

### 6.6 用户认证流程

```
注册：
  1. 校验邮箱格式、是否已注册
  2. BCrypt 加密密码
  3. 插入 user 表
  4. StpUtil.login(userId) → 签发 JWT → 返回 token + user 信息

登录：
  1. 按邮箱查询 user
  2. BCrypt 验证密码
  3. StpUtil.login(userId) → 签发 JWT → 返回 token + user 信息

请求鉴权：
  1. 前端 Axios 拦截器：请求头注入 Authorization: Bearer {token}
  2. SaTokenConfig 路由拦截：
     - 公开接口放行：/api/health/**, /api/auth/**, /api/questions/**, /api/stats/overview, /api/stats/submit
     - 其余 /api/** 需登录 → 未登录返回 401
```

---

## 7. 前端关键实现

### 7.1 Element Plus 组件使用

| 页面 | 使用的 Element Plus 组件 |
|------|-------------------------|
| HomePage | `el-input`, `el-dialog`, `el-button`, `el-card` |
| QuizPage | `el-progress`, `el-radio-group`, `el-radio`, `el-button`, `el-alert`, `el-message-box` |
| ResultPage | `el-card`, `el-tag`, `el-button`, `el-result` |
| CertificatePage | `el-button`, `el-message` |
| AlbumListPage | `el-card`, `el-button`, `el-empty`, `el-skeleton` |
| LoginPage | `el-input`, `el-button`, `el-form` |
| RegisterPage | `el-input`, `el-button`, `el-form` |
| LeaderboardPage | `el-tabs`, `el-table`, `el-tag` |

### 7.2 按需导入配置

```typescript
// vite.config.ts
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  server: { proxy: { '/api': 'http://localhost:8080' } }
})
```

### 7.3 Pinia Store 设计

**gameStore**（仅内存，不持久化）：
```typescript
state: {
  roundId: string | null,
  questions: Question[],
  currentIndex: number,
  answers: Map<number, string>,
  results: Map<number, boolean>,
  revivalRemaining: number,
  startTime: number,
  endTime: number | null,
  phase: 'idle' | 'playing' | 'finished',
  mode: 'classic' | 'album',        // 游戏模式
  albumKey: string | null,           // 专辑标识
}
```

**authStore**（localStorage 持久化）：
```typescript
state: {
  token: string | null,
  user: { id: number, email: string, nickname: string } | null,
  isLoggedIn: boolean,               // getter
}
```

**userStore**（localStorage 持久化）：
```typescript
state: {
  nickname: string,
  gameHistory: GameRecord[],
  totalGamesPlayed: number,
  bestScore: number,
  bestLevel: string,
}
```

**albumStore**（仅内存）：
```typescript
state: {
  albums: AlbumDTO[],
  loading: boolean,
  error: string | null,
  // getters: unlockedAlbums, lockedAlbums, progressSummary
}
```

### 7.4 证书生成 Canvas 方案

```
渲染层次（1080×1520px）：
  第1层：背景底图 certificate-bg.png
  第2层：用户昵称（金色粗体，y=480）
  第3层：等级称号（大字，y=620）
  第4层：得分 + 正确率（y=760）
  第5层：考试日期（y=830）

导出：canvas.toBlob('image/png') → download / share
```

### 7.5 分享三级降级

```typescript
// Tier 1: Web Share API
if (navigator.canShare?.(data)) → navigator.share(data)

// Tier 2: Clipboard
navigator.clipboard.writeText(shareText) → ElMessage.success('已复制')

// Tier 3: 手动引导
ElDrawer(底部) 展示 → "点击右上角...分享给朋友"
```

---

## 8. 安全设计

| 安全点 | 措施 |
|--------|------|
| 答案泄露 | GET `/round` 返回的 DTO 不含 `correctOption`，仅通过 POST `/check` 服务端比对 |
| Round 伪造 | `roundId` 为服务端生成 UUID，30 分钟过期，无法预测 |
| 数据篡改 | 答案校验在服务端完成，前端仅展示结果 |
| 密码安全 | BCrypt 哈希存储，`spring-security-crypto` |
| 身份认证 | Sa-Token JWT（random-64 token style），7 天过期 |
| 路由鉴权 | SaTokenConfig 路由拦截 + `@SaCheckLogin` 注解 |
| 专辑权限 | 服务端 `canAccessAlbum()` 校验解锁状态，前端展示仅是 UI |
| 重复提交 | `game_record.uk_round_id` UNIQUE 约束 |
| SQL 注入 | MyBatis Plus 使用 `#{}` 参数化查询 |
| XSS | Vue 默认对 `{{ }}` 插值做 HTML 转义 |

---

## 9. 部署架构

### 9.1 开发环境

```
┌─────────────┐     proxy /api     ┌──────────────────┐
│ Vite :5173  │ ─────────────────> │ SpringBoot :8080 │
│ (HMR 热更新) │                    │ (Knife4j :8080/doc.html) │
└─────────────┘                    └──────┬───────────┘
                                          │
                                   ┌──────▼───────────┐
                                   │ MySQL 8.0        │
                                   │ jaymetest        │
                                   └──────────────────┘
```

### 9.2 生产部署方案（Fat JAR）

```
┌──────────┐     ┌──────────┐     ┌─────────────────────────┐
│ 用户浏览器 │ ──> │  Nginx   │ ──> │ SpringBoot Fat JAR :8080 │
│  (H5)    │     │  :80     │     │ ├── /api/* (REST)        │
└──────────┘     └──────────┘     │ ├── /doc.html (Knife4j)  │
                                  │ └── /* (Vue dist static) │
                                  └─────────────┬───────────┘
                                                │
                                        ┌───────▼──────────┐
                                        │ MySQL 8.0        │
                                        │ jaymetest        │
                                        └──────────────────┘
```

### 9.3 关键配置

```yaml
# application.yml
server.port: 8080
spring:
  ai:
    dashscope:
      api-key: @dashscope.api.key@    # Maven 资源过滤注入
  datasource:
    url: jdbc:mysql://localhost:3306/jaymetest?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&createDatabaseIfNotExist=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config.id-type: auto

sa-token:
  token-name: Authorization
  timeout: 604800                       # 7 天
  token-style: random-64
  jwt-secret-key: jaymetest2024secretkeyformusic
```

---

## 10. 开发规范

### 10.1 后端规范

- 统一响应：所有 Controller 返回 `R<T>` 包装
- 异常处理：业务异常抛 `BusinessException`，由 `GlobalExceptionHandler` 统一捕获
- 日志：使用 `@Slf4j`（Lombok），关键操作打 INFO 日志
- Knife4j：Controller 类加 `@Tag(name = "xxx")`，方法加 `@Operation(summary = "xxx")`
- 鉴权：公开接口在 `SaTokenConfig` 放行；需登录接口加 `@SaCheckLogin` 或配置路由拦截
- 构造器注入：`@RequiredArgsConstructor` + `private final`，不写 `@Autowired`

### 10.2 前端规范

- Vue 组件使用 `<script setup lang="ts">` 语法
- 文件名：组件 PascalCase（`QuizPage.vue`），工具/Composable/Store camelCase（`useQuiz.ts`）
- Props/Emits 使用 TypeScript 类型声明
- API 调用统一通过 `api/` 模块，不在组件中直接写 `axios`
- Element Plus 组件按需自动导入，不手动注册
- 路由需登录的页面在 meta 中标记 `requiresAuth: true`

---

## 11. 依赖清单

### 11.1 后端 pom.xml

| GroupId | ArtifactId | Version | Scope |
|---------|-----------|---------|-------|
| org.springframework.boot | spring-boot-starter-web | 3.3.0 | compile |
| org.springframework.boot | spring-boot-starter-validation | 3.3.0 | compile |
| org.springframework.boot | spring-boot-starter-webflux | — | compile |
| com.baomidou | mybatis-plus-spring-boot3-starter | 3.5.7 | compile |
| com.mysql | mysql-connector-j | 8.3.0 | runtime |
| com.github.xiaoymin | knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 | compile |
| cn.dev33 | sa-token-spring-boot3-starter | 1.38.0 | compile |
| cn.dev33 | sa-token-jwt | 1.38.0 | compile |
| org.springframework.security | spring-security-crypto | — | compile |
| com.alibaba.cloud.ai | spring-ai-alibaba-starter-dashscope | 1.1.0.0 | compile |
| org.projectlombok | lombok | latest | optional |
| org.springframework.boot | spring-boot-starter-test | 3.3.0 | test |

### 11.2 前端 package.json

| 包名 | 版本 | 类型 |
|------|------|------|
| vue | ^3.5 | dependency |
| vue-router | ^4.4 | dependency |
| pinia | ^2.2 | dependency |
| axios | ^1.7 | dependency |
| element-plus | ^2.9 | dependency |
| @element-plus/icons-vue | ^2.3 | dependency |
| @vitejs/plugin-vue | ^5.1 | devDependency |
| vite | ^6.0 | devDependency |
| typescript | ^5.6 | devDependency |
| sass | ^1.80 | devDependency |
| unplugin-vue-components | ^0.27 | devDependency |
| unplugin-auto-import | ^0.18 | devDependency |
