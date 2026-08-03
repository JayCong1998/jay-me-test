# 杰迷试炼技术设计

> 本文档只记录技术栈、接口契约、数据结构、开发命令和工程约定。产品需求见 [PRD.md](PRD.md)；运行时模块协作和安全边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 1. 技术栈

### 1.1 后端

| 分类 | 技术 | 当前用途 |
| --- | --- | --- |
| 运行时 | JDK 21 | Spring Boot 应用运行和构建。 |
| Web 框架 | Spring Boot 3.3.0 | REST API、参数校验、全局异常处理。 |
| ORM | MyBatis Plus 3.5.7 | Mapper、分页插件、基础 CRUD 和自定义 SQL。 |
| 数据库 | MySQL 8.0+ | 题库、用户、答题记录、专辑进度和管理员数据。 |
| 认证 | Sa-Token 1.38.0 + JWT | 用户端和管理端登录态校验。 |
| 密码哈希 | Spring Security Crypto | BCrypt 存储用户和管理员密码。 |
| 缓存 | Guava Cache | 服务端 Round 状态缓存，TTL 30 分钟。 |
| API 文档 | Knife4j 4.5.0 | `/doc.html` 在线接口文档。 |
| 测试 | JUnit 5 / Mockito | Service、Controller、Mapper SQL 行为测试。 |

后端 `pom.xml` 保留了 `spring.ai.alibaba.version` 属性，但当前没有 DashScope 运行依赖和 AI Controller。AI 不属于当前可调用 API。

### 1.2 用户端

| 分类 | 技术 | 当前用途 |
| --- | --- | --- |
| 框架 | Vue 3.5 + TypeScript | H5 单页应用。 |
| 路由 | Vue Router 4 | Hash 路由。 |
| 状态 | Pinia 2 | 答题、登录、专辑、记录和主题状态。 |
| UI | Vant 4 + 项目 SCSS | 移动端交互和样式。 |
| HTTP | Axios | `/api` 代理访问后端。 |
| 构建 | Vite 6 | 开发服务器和生产构建。 |
| 测试 | Vitest | 前端单元和流程测试。 |

### 1.3 管理端

| 分类 | 技术 | 当前用途 |
| --- | --- | --- |
| 框架 | React 18 + TypeScript | 独立后台 SPA。 |
| 路由 | React Router 6 | 管理端页面和登录守卫。 |
| UI | Ant Design 5 | 表格、表单、布局和交互组件。 |
| 样式 | Tailwind CSS / CSS | 基础样式工具链和自定义样式。 |
| HTTP | Axios | `/api/admin` 代理访问后端。 |
| 构建 | Vite 6 | 管理端开发和生产构建。 |

## 2. 本地开发命令

### 2.1 后端

```bash
cd backend
mvn spring-boot:run
mvn test
mvn package
```

默认端口为 `8080`，Knife4j 地址为 `http://localhost:8080/doc.html`。本地数据库默认连接 `localhost:3306/jaymetest`。

### 2.2 用户端

```bash
cd frontend
npm run dev
npm run test
npm run build
npm run preview
```

开发端口为 `5173`，`/api` 代理到 `http://localhost:8080`。

### 2.3 管理端

```bash
cd admin
npm run dev
npm run build
npm run preview
```

开发端口为 `5174`，`/api` 代理到 `http://localhost:8080`。

## 3. 后端模块

| 包或类 | 职责 |
| --- | --- |
| `controller` | 用户端业务 API：经典、题目校验、深渊、专辑、结算、记录、统计、排行榜、认证。 |
| `controller.admin` | 管理端 API，路径统一为 `/api/admin/**`。 |
| `service` | 业务编排：认证、题目入口、结算、记录查询、统计、专辑进度、排行榜。 |
| `service.game.strategy` | 三种玩法策略抽象和实现。 |
| `service.game.cache` | Round 缓存对象和 TTL 管理。 |
| `service.game.level` | 按配置计算等级。 |
| `service.game.abyss` | 深渊难度阶梯和随机难度选择。 |
| `service.game.hook` | 结算后置钩子，当前用于专辑解锁。 |
| `mapper` | MyBatis Plus Mapper 和自定义 SQL。 |
| `model.entity` | 数据库实体。 |
| `model.dto` | 用户端请求和响应 DTO。 |
| `model.admin` | 管理端请求和响应 DTO。 |
| `config` | Web、Sa-Token、MyBatis Plus、Knife4j 和游戏规则配置。 |
| `exception` | `BusinessException` 和 `GlobalExceptionHandler`。 |

## 4. 用户端模块

| 文件或目录 | 职责 |
| --- | --- |
| `src/router/index.ts` | Hash 路由、页面标题和登录守卫。 |
| `src/api/client.ts` | Axios 实例、token 注入、业务错误和 401 处理。 |
| `src/api/questionApi.ts` | 经典、通用校验和深渊接口。 |
| `src/api/albumApi.ts` | 专辑列表和专辑抽题接口。 |
| `src/api/statsApi.ts` | 结算、统计概览和个人记录接口。 |
| `src/api/leaderboardApi.ts` | 排行榜接口。 |
| `src/composables/useQuiz.ts` | 答题流程编排。 |
| `src/stores/gameStore.ts` | 当前 Round 的内存状态。 |
| `src/stores/authStore.ts` | `jaymetest_auth` 登录态。 |
| `src/stores/albumStore.ts` | 专辑列表和进度状态。 |
| `src/stores/recordStore.ts` | 个人记录分页和加载更多。 |
| `src/pages` | 首页、答题、结果、证书、登录、注册、专辑、排行榜、我的。 |

## 5. 管理端模块

| 文件或目录 | 职责 |
| --- | --- |
| `admin/src/main.tsx` | React Router、Ant Design 主题和受保护路由。 |
| `admin/src/context/AuthContext.tsx` | 管理员登录态，使用 `jaymetest_admin_auth`。 |
| `admin/src/api/client.ts` | `/api/admin` Axios 实例和 401/403 处理。 |
| `admin/src/pages/DashboardPage.tsx` | 运营概览。 |
| `admin/src/pages/QuestionPage.tsx` | 题库列表、筛选、新增、编辑。 |
| `admin/src/pages/UserPage.tsx` | 用户列表查询。 |
| `admin/src/pages/RecordPage.tsx` | 答题记录查询。 |

## 6. REST API

### 6.1 统一响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1721000000000
}
```

前端 Axios 响应拦截器会解包 `R<T>`；当 `code != 200` 时按业务错误处理。

### 6.2 用户端接口

| Method | Path | 登录 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/health` | 否 | 健康检查。 |
| `POST` | `/api/auth/register` | 否 | 注册并返回 token 和用户信息。 |
| `POST` | `/api/auth/login` | 否 | 登录并返回 token 和用户信息。 |
| `GET` | `/api/auth/me` | 是 | 当前用户信息。 |
| `GET` | `/api/classic/round` | 否 | 经典模式开局抽题。 |
| `POST` | `/api/questions/check` | 否 | 固定题量模式单题校验。 |
| `POST` | `/api/abyss/start` | 是 | 深渊开局，返回首批题目。 |
| `POST` | `/api/abyss/batch` | 是 | 深渊下一批题目。 |
| `POST` | `/api/abyss/check` | 是 | 深渊单题校验。 |
| `POST` | `/api/abyss/revive` | 是 | 深渊续命并重答当前题。 |
| `GET` | `/api/albums/list` | 是 | 专辑列表和解锁状态。 |
| `GET` | `/api/albums/round?albumKey=` | 是 | 专辑关卡开局抽题。 |
| `POST` | `/api/game-results` | 否 | 提交并持久化本局结果。 |
| `GET` | `/api/statistics/overview` | 否 | 首页统计概览。 |
| `GET` | `/api/game-records/me?page=&size=` | 是 | 当前用户答题记录。 |
| `GET` | `/api/leaderboard?type=&page=&size=` | 是 | `classic`、`album`、`abyss` 三类榜单。 |

### 6.3 管理端接口

| Method | Path | 说明 |
| --- | --- | --- |
| `POST` | `/api/admin/auth/login` | 管理员登录。 |
| `GET` | `/api/admin/auth/me` | 当前管理员信息。 |
| `POST` | `/api/admin/auth/logout` | 管理员退出。 |
| `GET` | `/api/admin/dashboard/overview` | 运营概览。 |
| `GET` | `/api/admin/questions` | 题库分页和筛选。 |
| `GET` | `/api/admin/questions/{id}` | 题目详情。 |
| `POST` | `/api/admin/questions` | 新增题目。 |
| `PUT` | `/api/admin/questions/{id}` | 更新题目。 |
| `POST` | `/api/admin/questions/rebalance-options` | 重平衡正确选项分布。 |
| `GET` | `/api/admin/users` | 用户分页查询。 |
| `GET` | `/api/admin/records` | 答题记录分页查询。 |

## 7. 数据库设计

| 表 | 说明 |
| --- | --- |
| `question` | 题库，含分类、专辑、难度、4 个选项、正确答案和解析。 |
| `user` | 用户邮箱、BCrypt 密码哈希、唯一昵称。 |
| `game_record` | 游戏结算记录，含 `round_id` 唯一约束、玩法模式、成绩、用时、复活标记。 |
| `album_progress` | 用户专辑解锁和挑战进度，`(user_id, album_key)` 唯一。 |
| `admin_user` | 管理端账号，来自 `releases/v3/001_add_admin_console.sql`。 |

数据库脚本人工管理：

- `database/baseline/` 用于从零初始化。
- `database/releases/vX/` 用于增量升级。
- `database/snapshots/` 只用于查看当前结构全貌。
- 已归档 release SQL 不直接修改，修正时新增下一个编号脚本。

## 8. 关键配置

| 配置 | 当前值 | 说明 |
| --- | --- | --- |
| `game.classic.question-count` | `20` | 经典模式题量。 |
| `game.classic.easy-weight` | `0.6` | 经典模式 EASY 抽题权重。 |
| `game.album.question-count` | `20` | 专辑模式题量。 |
| `game.album.pass-accuracy` | `80` | 专辑通关正确率。 |
| `game.abyss.batch-size` | `5` | 深渊批次题量。 |
| `game.abyss.revival-count` | `1` | 深渊续命次数。 |
| `sa-token.timeout` | `604800` | token 有效期 7 天。 |
| `server.port` | `8080` | 后端端口。 |

## 9. 工程约定

- Controller 返回 `R<T>`，业务异常抛 `BusinessException`。
- 请求 DTO 使用 `@Valid` 和 `jakarta.validation`。
- 服务层使用 Lombok `@RequiredArgsConstructor` 构造器注入。
- 用户端 API 调用统一放在 `frontend/src/api/`，页面不直接使用裸 Axios。
- 用户端答题状态只保存在 Pinia 内存，刷新后重新开局。
- 用户登录态存储键为 `jaymetest_auth`。
- 管理端登录态存储键为 `jaymetest_admin_auth`，不得与用户端混用。
- 涉及结算、排行榜、Round 防作弊、专辑解锁、深渊状态机的变更必须补测试。
