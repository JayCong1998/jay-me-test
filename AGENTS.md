# AGENTS.md

本文件是 `jay-me-test` 仓库的协作与开发指南。详细的系统架构、Round 生命周期和接口边界见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)；产品需求和技术设计分别见 [docs/PRD.md](docs/PRD.md) 与 [docs/TECH.md](docs/TECH.md)。

## 沟通方式

- 默认中文回复；代码、命令、变量名、文件路径保持英文。
- 结论先行，简洁直接；发现方案或实现存在问题时直接指出，并给出更好的做法。
- 修改前先阅读相关代码和现有文档，优先沿用项目已有的模块边界、命名和技术栈。

## Git 与红线操作

- 不自动执行 `git commit` 或 `git push`，除非用户明确要求。
- 提交前先展示将要提交的变更摘要；commit message 使用简洁英文。
- 以下操作必须先征得用户同意：删除文件、目录或 Git 历史；修改 `.env`、密钥、token、证书、CI/CD 配置；`git push`、`git rebase`、`git reset --hard`、强制推送；公开发布或生产部署。
- 保留用户已有的未提交修改。不要通过回滚、重置或覆盖的方式清理与当前任务无关的变更。

## 项目概述

**杰迷结业考试 (jay-me-test)** 是一个周杰伦粉丝答题 H5 应用，当前包含三种玩法：

- **经典模式**：游客可玩，后端生成随机题目、服务端校验答案并缓存 Round；默认题目策略由 `ClassicGameStrategy` 实现。
- **专辑闯关**：登录用户挑战专辑题目，按专辑记录通关和解锁进度；由 `AlbumGameStrategy`、`AlbumProgressService` 和 `AlbumController` 协作实现。
- **无尽深渊**：登录用户按批次持续答题，一题一校验并累计 streak；由 `AbyssGameStrategy`、`AbyssDifficultyPolicy` 和 `AbyssController` 实现，难度阶梯在 `application.yml` 的 `game.abyss.difficulty` 中配置。

此外，项目包含用户注册登录、排行榜、电子证书、个人答题记录、AI 问答和独立运营管理端。

## 项目结构

```text
jay-me-test/
├── AGENTS.md
├── docs/
│   ├── ARCHITECTURE.md                 # 数据流、安全边界、Round 生命周期、关键文件索引
│   ├── PRD.md                          # 产品需求
│   ├── TECH.md                         # 技术设计
│   ├── info.md                         # 本地开发信息和题目分类
│   └── superpowers/                    # 设计文档和实现计划
├── backend/                            # Spring Boot 3 + Java 21 后端
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/jaymetest/
│       │   ├── controller/             # 公共业务 API
│       │   ├── controller/admin/       # 管理端 API，路径为 /api/admin/**
│       │   ├── ai/                     # DashScope AI 问答
│       │   ├── service/                # 业务服务和游戏模式编排
│       │   ├── mapper/                 # MyBatis Plus Mapper
│       │   ├── model/entity/           # 数据库实体
│       │   ├── model/dto/              # 公共请求和响应 DTO
│       │   ├── model/admin/             # 管理端 DTO
│       │   ├── config/                 # Web、MyBatis Plus、Knife4j、Sa-Token 配置
│       │   └── exception/              # 业务异常和统一异常响应
│       └── test/                       # JUnit / Mockito 测试
├── frontend/                           # 用户端 Vue 3 + Vite + Pinia + Vant
│   ├── package.json
│   └── src/
│       ├── pages/                      # 首页、答题、结果、专辑、排行榜、个人中心等
│       ├── components/                 # 答题和专辑业务组件
│       ├── composables/                # 答题流程、计时器、无限滚动
│       ├── stores/                     # 游戏、用户、登录、专辑、记录、主题状态
│       ├── api/                        # Axios API 模块
│       ├── router/                     # Hash 路由和用户登录守卫
│       └── utils/                      # 等级、专辑、证书、展示和格式化工具
├── admin/                              # 独立管理端 React 18 + Vite + Tailwind
│   ├── package.json
│   └── src/
│       ├── pages/                      # 登录、看板、题库、用户、记录
│       ├── components/                 # 布局和通用 UI 组件
│       ├── api/                        # /api/admin 客户端
│       └── context/                    # 管理员登录态
└── database/
    ├── baseline/                       # 从零初始化结构和种子数据
    ├── releases/v3/                    # 已归档的 V3 增量 SQL
    ├── snapshots/                      # 当前结构快照，只读参考
    └── README.md                       # SQL 编号、执行顺序和幂等性规范
```

## 开发命令

后端需要 JDK 21、Maven 和 MySQL 8.0+；默认连接 `localhost:3306/jaymetest`。AI 问答需要配置 `AI_DASHSCOPE_API_KEY`。数据库从零初始化时，按 `database/README.md` 执行 `baseline/` 下脚本；已有数据库升级时，按对应 `releases/vX/` 的 README 执行增量脚本。

### 用户端 `frontend/`

```bash
npm run dev        # Vite 开发服务器，http://localhost:5173，/api 代理到 localhost:8080
npm run test       # Vitest 单元和源码行为测试
npm run build      # vue-tsc 类型检查 + Vite 生产构建
npm run preview    # 预览生产构建
```

### 管理端 `admin/`

```bash
npm run dev        # Vite 开发服务器，http://localhost:5174，/api 代理到 localhost:8080
npm run build      # tsc -b + Vite 生产构建
npm run preview    # 预览生产构建
```

管理端使用独立的 `jaymetest_admin_auth` 登录态和管理员 API，不要与用户端的 `jaymetest_auth` 混用。当前管理端页面包括 Dashboard、题库、用户和答题记录。

### 后端 `backend/`

```bash
mvn spring-boot:run              # 启动 Spring Boot，端口 8080
mvn test                         # 运行 JUnit / Mockito 测试
mvn package                      # 打包后端 Fat JAR
```

Knife4j 文档地址：`http://localhost:8080/doc.html`。前端生产构建完成后，可将产物复制到 `backend/src/main/resources/static/`，再执行 `mvn package` 打包为包含静态资源的应用。

## 后端模块与关键规则

- `QuestionController` 提供公共抽题、单题校验和复活接口；`AbyssController` 提供深渊开始、批次和校验接口；`StatsController` 负责结果提交、统计概览和个人记录；`AlbumController` 负责专辑列表和专辑抽题；`LeaderboardController` 负责经典、专辑、深渊排行榜。
- `GameStrategy` 是玩法策略扩展点，当前实现为 `ClassicGameStrategy`、`AlbumGameStrategy`、`AbyssGameStrategy`，由 `GameStrategyFactory` 选择。
- Round 由服务端生成 UUID，并在 `RoundCacheManager` 中保存题目与正确答案映射。客户端只能拿到不含正确答案的 `QuestionDTO`；答案必须通过服务端校验。
- `game_record.uk_round_id` 用于结果提交去重。结果提交、排行榜排序、专辑解锁和深渊 streak 规则属于跨模块业务边界，修改时要同步更新测试和架构文档。
- 公共用户登录使用 Sa-Token + JWT；敏感接口按路由要求登录。管理端使用独立的 `AdminStpUtil` 登录类型，`/api/admin/**` 受管理员登录保护，登录接口除外。
- 业务错误统一抛出 `BusinessException`，由 `GlobalExceptionHandler` 转为 `R.fail()`；请求参数使用 `@Valid` 和 `jakarta.validation`。
- MyBatis Plus 使用 MySQL `RAND()` 抽题，当前题库规模可接受；题库明显扩大后应重新评估随机抽题性能。

## 前端约定

- 用户端是 Vue 3 Composition API 应用，使用 Pinia 管理运行时状态，使用 Hash 路由；`/albums` 等需要登录的页面由路由守卫检查 `jaymetest_auth`。
- `useQuiz` 编排答题流程和提交结果，`gameStore` 保存当前 Round 状态；刷新页面会丢失答题状态，需要重新开始。
- 经典和专辑答题在最后一题直接提交整局结果；深渊模式按批次预加载和逐题校验，不能套用固定题量流程。
- 接口失败时，用户端允许按既有规则降级到本地等级展示；这只是展示兜底，服务端结果和排行榜数据仍以服务端为准。
- 用户端 UI 使用 Vant 和项目现有 SCSS；不要按管理端的 React/Tailwind 组件方式修改用户端。
- 管理端是独立 React Router 应用，使用 `lucide-react` 图标和 Tailwind 样式；管理端新增页面应接入现有 `AppLayout` 和 `AuthContext`。

## 数据库脚本规范

- 不使用 Flyway 或 Liquibase，采用人工执行、目录和递增编号管理。
- `baseline/` 用于从零初始化；`releases/vX/` 用于版本增量升级；已经归档的 release SQL 不直接修改，修正时新增下一个编号；`snapshots/` 只用于查看全貌，不作为升级入口。
- 新增 SQL 须在文件顶部说明用途、前置条件、影响表和幂等性；涉及回填时补充执行风险。
- 一个脚本只处理一个清晰的业务变更，命名为 `{三位编号}_{action}_{object}.sql`，例如 `002_add_abyss_record_indexes.sql`。
- 表结构发生变化时同步更新当前 snapshot，并在 release README 记录执行顺序、影响范围和注意事项。

## 注释与测试

- 注释只解释业务规则、设计原因、边界或安全约束，不写翻译代码表面行为的注释。
- 后端复杂策略、结果去重、Round 防作弊、专辑解锁和排行榜排序必须有必要的业务注释和测试。
- 前端复杂答题流程、深渊预加载、游客降级和接口失败兜底需要必要的注释和回归测试。
- 行为变更优先先补测试，再修改实现；完成前至少运行受影响模块的测试和构建命令，并依据实际输出报告结果。
- 服务层使用 Lombok 的 `@RequiredArgsConstructor` 构造器注入；不要新增字段注入。
- 应用启动类位于 `com.jaymetest`，MyBatis Plus 会自动扫描该包下的 Mapper。
