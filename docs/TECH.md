# 杰迷结业考试 (jay-me-test) 技术设计文档

> 版本：v1.0 | 日期：2026-07-15 | 状态：设计阶段

---

## 1. 技术栈

### 1.1 技术选型

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **运行时** | JDK | 21 | LTS 版本，虚拟线程支持 |
| **后端框架** | Spring Boot | 3.3.x | 内嵌 Tomcat，自动配置 |
| **ORM** | MyBatis Plus | 3.5.7 | 零 SQL CRUD + 自定义注解查询 |
| **数据库** | MySQL | 8.0+ | 关系型数据库，稳定可靠 |
| **API 文档** | Knife4j | 4.5.0 | Swagger UI 增强版，在线调试 |
| **前端框架** | Vue 3 | 3.5.x | Composition API + `<script setup>` |
| **UI 组件库** | Vant | 4.10.0 | 企业级 Vue 3 组件库 |
| **状态管理** | Pinia | 2.2.x | Vue 3 官方推荐 |
| **路由** | Vue Router | 4.4.x | Hash 模式 |
| **HTTP 客户端** | Axios | 1.7.x | 请求/响应拦截 |
| **构建工具** | Vite | 6.x | 极速 HMR |
| **语言** | TypeScript | 5.6.x | 类型安全 |

### 1.2 选型理由

- **MyBatis Plus** 而非 JPA/Hibernate：直接写 SQL 更灵活可控，支持自定义注解查询
- **MySQL** 而非 SQLite：MySQL 是成熟稳定的关系型数据库，支持并发读写、事务处理，适合生产环境部署
- **Knife4j** 而非 SpringDoc 裸用：提供可视化的在线 API 调试界面，开发联调效率更高
- **Element Plus** 而非 TailwindCSS：提供开箱即用的移动端适配组件（Dialog、Radio、Progress 等），减少手工样式工作量
- **Hash 路由** 而非 History 模式：H5 部署到静态服务器无需 Nginx fallback 配置

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Vue 3 SPA (H5)                          │   │
│  │  Element Plus → Pinia → Vue Router → Axios          │   │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐     │   │
│  │  │ 首页   │ │ 答题页  │ │ 结果页  │ │ 证书页    │     │   │
│  │  └────────┘ └────────┘ └────────┘ └──────────┘     │   │
│  │        localStorage (昵称 + 历史)                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                           │ HTTP/REST                       │
└───────────────────────────┼─────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────┐
│                        服务层                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Spring Boot 3 (JDK 21)                  │   │
│  │                                                      │   │
│  │  ┌──────────────┐  ┌──────────────┐                 │   │
│  │  │ Question     │  │ Stats        │  Controller     │   │
│  │  │ Controller   │  │ Controller   │                 │   │
│  │  └──────┬───────┘  └──────┬───────┘                 │   │
│  │         │                 │                          │   │
│  │  ┌──────▼───────┐  ┌──────▼───────┐                 │   │
│  │  │ Question     │  │ Stats        │  Service        │   │
│  │  │ Service      │  │ Service      │                 │   │
│  │  │ · Round缓存  │  │ · 等级计算   │                 │   │
│  │  │ · 随机抽题   │  │ · 百分位     │                 │   │
│  │  └──────┬───────┘  └──────┬───────┘                 │   │
│  │         │                 │                          │   │
│  │  ┌──────▼───────┐  ┌──────▼───────┐                 │   │
│  │  │ Question     │  │ GameRecord   │  Mapper         │   │
│  │  │ Mapper       │  │ Mapper       │                 │   │
│  │  └──────┬───────┘  └──────┬───────┘                 │   │
│  └─────────┼─────────────────┼─────────────────────────┘   │
│            │                 │                              │
│  ┌─────────▼─────────────────▼─────────────────────────┐   │
│  │              MySQL 8.0 (jaymetest)                    │   │
│  │  ┌──────────────┐  ┌──────────────┐                 │   │
│  │  │ question     │  │ game_record  │                 │   │
│  │  │ (70 rows)   │  │ (∞ rows)    │                 │   │
│  │  └──────────────┘  └──────────────┘                 │   │
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
└── src/
    ├── main/
    │   ├── java/com/jaymetest/
    │   │   ├── JayMeTestApplication.java          # @SpringBootApplication
    │   │   │
    │   │   ├── config/
    │   │   │   ├── WebConfig.java                 # CORS 跨域
    │   │   │   ├── MyBatisPlusConfig.java         # 分页插件 + 自动填充
    │   │   │   └── Knife4jConfig.java             # OpenAPI 文档配置
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── QuestionController.java        # 题目 API
    │   │   │   └── StatsController.java           # 统计 API
    │   │   │
    │   │   ├── service/
    │   │   │   ├── QuestionService.java           # 抽题 + 校验 + 复活
    │   │   │   └── StatsService.java              # 等级 + 百分位
    │   │   │
    │   │   ├── mapper/
    │   │   │   ├── QuestionMapper.java            # extends BaseMapper<Question>
    │   │   │   └── GameRecordMapper.java          # extends BaseMapper<GameRecord>
    │   │   │
    │   │   ├── model/
    │   │   │   ├── entity/
    │   │   │   │   ├── Question.java              # @TableName("question")
    │   │   │   │   └── GameRecord.java            # @TableName("game_record")
    │   │   │   ├── dto/
    │   │   │   │   ├── QuestionDTO.java
    │   │   │   │   ├── AnswerRequest.java
    │   │   │   │   ├── AnswerResultDTO.java
    │   │   │   │   ├── GameSubmitRequest.java
    │   │   │   │   ├── GameResultDTO.java
    │   │   │   │   └── StatsOverviewDTO.java
    │   │   │   └── enums/
    │   │   │       ├── QuestionCategory.java      # LYRICS, ALBUM
    │   │   │       ├── DifficultyLevel.java       # EASY, MEDIUM
    │   │   │       └── FanLevel.java              # 5 级等级
    │   │   │
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java    # @RestControllerAdvice
    │   │       └── BusinessException.java
    │   │
    │   └── resources/
    │       └── application.yml
    │
    └── test/java/com/jaymetest/
        ├── service/
        │   ├── QuestionServiceTest.java
        │   └── StatsServiceTest.java
        └── controller/
            ├── QuestionControllerTest.java
            └── StatsControllerTest.java
```

### 3.2 前端项目结构

```
frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
└── src/
    ├── main.ts                                   # createApp + use router/pinia/element-plus
    ├── App.vue                                   # <router-view>
    ├── styles/
    │   └── global.scss                           # Element Plus 主题覆盖
    │
    ├── router/
    │   └── index.ts                              # createWebHashHistory
    │
    ├── stores/
    │   ├── gameStore.ts                          # 游戏进行中状态（内存）
    │   └── userStore.ts                          # 用户数据（localStorage 持久化）
    │
    ├── composables/
    │   ├── useQuiz.ts                            # 答题流程编排
    │   ├── useTimer.ts                           # 计时器
    │   ├── useCertificate.ts                     # Canvas 证书生成
    │   ├── useShare.ts                           # 三级降级分享
    │   └── useStorage.ts                         # localStorage 类型安全封装
    │
    ├── api/
    │   ├── client.ts                             # axios 实例（baseURL + 拦截器）
    │   ├── questionApi.ts                        # /api/questions/* 接口
    │   └── statsApi.ts                           # /api/stats/* 接口
    │
    ├── pages/
    │   ├── HomePage.vue                          # 首页
    │   ├── QuizPage.vue                          # 答题页
    │   ├── ResultPage.vue                        # 结果页
    │   └── CertificatePage.vue                   # 证书页
    │
    ├── components/
    │   ├── home/
    │   │   ├── NicknameDialog.vue                # 昵称弹窗
    │   │   └── HistoryPanel.vue                  # 历史记录
    │   ├── quiz/
    │   │   ├── QuizHeader.vue                    # 进度 + 计时 + 复活标记
    │   │   ├── QuestionCard.vue                  # 题目 + el-radio-group 选项
    │   │   └── FeedbackBar.vue                   # 对错反馈条
    │   ├── result/
    │   │   ├── LevelDisplay.vue                  # 等级徽章 + 称号
    │   │   └── StatsGrid.vue                     # 数据面板
    │   └── certificate/
    │       ├── CertPreview.vue                   # 证书视觉预览
    │       └── ShareGuide.vue                    # 分享引导
    │
    ├── utils/
    │   ├── levels.ts                             # 等级计算（同后端逻辑）
    │   ├── constants.ts
    │   └── format.ts
    │
    └── assets/
        └── certificate-bg.png                    # 证书背景底图
```

---

## 4. 数据库设计

### 4.1 表结构

```sql
-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    category       VARCHAR(20) NOT NULL COMMENT '分类: LYRICS | ALBUM',
    difficulty     VARCHAR(20) NOT NULL COMMENT '难度: EASY | MEDIUM',
    question_text  TEXT        NOT NULL COMMENT '题目正文',
    option_a       TEXT        NOT NULL COMMENT '选项 A 内容',
    option_b       TEXT        NOT NULL COMMENT '选项 B 内容',
    option_c       TEXT        NOT NULL COMMENT '选项 C 内容',
    option_d       TEXT        NOT NULL COMMENT '选项 D 内容',
    correct_option CHAR(1)     NOT NULL COMMENT '正确答案: A|B|C|D',
    explanation    TEXT        NOT NULL COMMENT '答案解析',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_category     CHECK (category IN ('LYRICS', 'ALBUM')),
    CONSTRAINT chk_difficulty   CHECK (difficulty IN ('EASY', 'MEDIUM')),
    CONSTRAINT chk_correct_opt  CHECK (correct_option IN ('A', 'B', 'C', 'D'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

CREATE INDEX idx_q_category ON question(category);
CREATE INDEX idx_q_difficulty ON question(difficulty);
CREATE INDEX idx_q_cat_diff ON question(category, difficulty);

-- 匿名游戏记录表
CREATE TABLE IF NOT EXISTS game_record (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    round_id         VARCHAR(36) NOT NULL COMMENT 'UUID 去重',
    total_questions  INT         NOT NULL DEFAULT 10,
    correct_count    INT         NOT NULL COMMENT '答对数量 0-10',
    time_spent_secs  INT         NOT NULL COMMENT '答题总用时（秒）',
    used_revival     TINYINT     NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_round_id UNIQUE (round_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏记录表';

CREATE INDEX idx_gr_score ON game_record(correct_count);
CREATE INDEX idx_gr_created ON game_record(created_at);
```

### 4.2 实体与 Mapper 映射

```java
// Entity
@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String category;        // LYRICS | ALBUM
    private String difficulty;      // EASY | MEDIUM
    private String questionText;
    private String optionA, optionB, optionC, optionD;
    private String correctOption;   // A | B | C | D
    private String explanation;
    private String createdAt;
    private String updatedAt;
}

// Mapper
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    @Select("SELECT * FROM question WHERE difficulty = #{difficulty} ORDER BY RAND() LIMIT #{limit}")
    List<Question> selectRandomByDifficulty(@Param("difficulty") String difficulty,
                                             @Param("limit") int limit);
}

@Mapper
public interface GameRecordMapper extends BaseMapper<GameRecord> {
    @Select("SELECT COUNT(*) FROM game_record WHERE correct_count < #{score}")
    long countByCorrectCountLessThan(@Param("score") int score);
}
```

### 4.3 种子数据加载

题库数据通过 SQL 脚本手动初始化，脚本位于项目根目录 `database/` 文件夹：
- `create_table.sql` — DDL，建库建表
- `add_question.sql` — 插入 70 道题目种子数据

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

| # | Method | Path | 说明 |
|---|--------|------|------|
| 1 | `GET` | `/api/health` | 健康检查 |
| 2 | `GET` | `/api/questions/round?count=10` | 随机抽取一局题目 |
| 3 | `POST` | `/api/questions/check` | 校验单题答案 |
| 4 | `POST` | `/api/questions/revive` | 使用复活机会 |
| 5 | `POST` | `/api/stats/submit` | 提交游戏结果 |
| 6 | `GET` | `/api/stats/overview` | 全局统计概览 |

### 5.3 接口详细定义

#### 5.3.1 健康检查

```
GET /api/health
Response: { "code": 200, "msg": "success", "data": { "status": "ok" }, "timestamp": ... }
```

#### 5.3.2 随机抽题

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
        "questionText": "《七里香》中...",
        "options": ["A. ...", "B. ...", "C. ...", "D. ..."]
      }
    ]
  }
}
```
> **注意**：`QuestionDTO.options` 由 Entity 的 `getOptionsAsList()` 拼接 "A. " 前缀生成，**不含 correctOption 字段**。

#### 5.3.3 答案校验

```
POST /api/questions/check
Request:
{
  "roundId": "uuid-xxxx",
  "questionId": 1,
  "selectedOption": "A"
}

Response 200:
{
  "code": 200,
  "data": {
    "correct": true,
    "correctOption": "A",
    "explanation": "出自《七里香》副歌第一句..."
  }
}
```

#### 5.3.4 复活

```
POST /api/questions/revive
Request:  { "roundId": "uuid-xxxx", "questionId": 1 }
Response: { "code": 200, "data": { "revived": true, "remainingRevivals": 0 } }
```

#### 5.3.5 提交结果

```
POST /api/stats/submit
Request:
{
  "roundId": "uuid-xxxx",
  "correctCount": 7,
  "timeSpentSecs": 145,
  "usedRevival": 1
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
    "totalPlayers": 12480
  }
}
```

#### 5.3.6 全局统计

```
GET /api/stats/overview

Response 200:
{
  "code": 200,
  "data": {
    "totalPlayers": 12480,
    "totalGames": 35120,
    "averageScore": 62.3,
    "levelDistribution": {
      "PASSERBY": 15.2,
      "JUNIOR": 28.7,
      "INTERMEDIATE": 30.1,
      "SENIOR": 18.5,
      "ULTIMATE": 7.5
    }
  }
}
```

---

## 6. 核心业务逻辑

### 6.1 随机抽题算法

```
输入：count = 10
步骤：
  1. 计算各难度数量：easyCount = 6, mediumCount = 4
  2. 各难度随机取：
     SELECT * FROM question WHERE difficulty = 'EASY' ORDER BY RAND() LIMIT 6
     SELECT * FROM question WHERE difficulty = 'MEDIUM' ORDER BY RAND() LIMIT 4
  3. 合并两个结果集 → Collections.shuffle() 打乱顺序
  4. 生成 roundId (UUID) → 构建 (questionId → correctOption) Map 存入 ConcurrentHashMap
  5. 转换为 QuestionDTO（不含 correctOption）返回
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

---

## 7. 前端关键实现

### 7.1 Element Plus 组件使用

| 页面 | 使用的 Element Plus 组件 |
|------|-------------------------|
| HomePage | `el-input`, `el-dialog`, `el-button`, `el-collapse` |
| QuizPage | `el-progress`, `el-radio-group`, `el-radio`, `el-button`, `el-alert`, `el-message-box` |
| ResultPage | `el-card`, `el-tag`, `el-button`, `el-statistic` (或自定义) |
| CertificatePage | `el-button`, `el-drawer`, `el-message` |
| 全局 | `el-message` (操作反馈), `v-loading` (加载态) |

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
  questions: Question[],           // 10 题
  currentIndex: number,            // 0-9
  answers: Map<number, string>,    // index → selectedOption
  results: Map<number, boolean>,   // index → wasCorrect
  revivalRemaining: number,        // 初始 1
  startTime: number,
  endTime: number | null,
  phase: 'idle' | 'playing' | 'finished'
}
```

**userStore**（localStorage 持久化）：
```typescript
state: {
  nickname: string,                // 默认 "匿名杰迷"
  gameHistory: GameRecord[],       // 上限 20 条
  totalGamesPlayed: number,
  bestScore: number,
  bestLevel: string
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

## 8. 部署架构

### 8.1 开发环境

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

### 8.2 生产部署方案（Fat JAR）

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

前端 `npm run build` → 复制 `dist/` 到 `src/main/resources/static/` → `mvn package` → 单个 JAR 部署。

### 8.3 关键配置

```yaml
# application.yml
server.port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jaymetest?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&createDatabaseIfNotExist=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config.id-type: auto

knife4j:
  enable: true
  setting.language: zh-CN
```

---

## 9. 安全设计

| 安全点 | 措施 |
|--------|------|
| 答案泄露 | GET `/round` 返回的 DTO 不含 `correctOption`，仅通过 POST `/check` 服务端比对 |
| Round 伪造 | `roundId` 为服务端生成 UUID，30 分钟过期，无法预测 |
| 数据篡改 | 答案校验在服务端完成，前端仅展示结果 |
| SQL 注入 | MyBatis Plus 使用 `#{}` 参数化查询 |
| XSS | Vue 默认对 `{{ }}` 插值做 HTML 转义 |

---

## 10. 开发规范

### 10.1 后端规范

- 统一响应：所有 Controller 返回 `R<T>` 包装
- 异常处理：业务异常抛 `BusinessException`，由 `GlobalExceptionHandler` 统一捕获
- 日志：使用 `@Slf4j`（Lombok），关键操作打 INFO 日志
- Knife4j：Controller 类加 `@Tag(name = "xxx")`，方法加 `@Operation(summary = "xxx")`
- 测试：Service 层单元测试覆盖率 > 80%

### 10.2 前端规范

- Vue 组件使用 `<script setup lang="ts">` 语法
- 文件名：组件 PascalCase（`QuizPage.vue`），工具/Composable camelCase（`useQuiz.ts`）
- Props/Emits 使用 TypeScript 类型声明
- API 调用统一通过 `api/` 模块，不在组件中直接写 `axios`
- Element Plus 组件按需自动导入，不手动注册

---

## 11. 依赖清单

### 11.1 后端 pom.xml

| GroupId | ArtifactId | Version | Scope |
|---------|-----------|---------|-------|
| org.springframework.boot | spring-boot-starter-web | 3.3.0 | compile |
| com.baomidou | mybatis-plus-spring-boot3-starter | 3.5.7 | compile |
| com.mysql | mysql-connector-j | 8.3.0 | runtime |
| com.github.xiaoymin | knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 | compile |
| org.springframework.boot | spring-boot-starter-validation | 3.3.0 | compile |
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
