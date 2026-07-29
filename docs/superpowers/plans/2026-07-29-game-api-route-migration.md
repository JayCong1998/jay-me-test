# 游戏 API 路由迁移实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 以结算、记录查询和全局统计三个领域 API 取代 `/api/stats/**`，并移除旧路由。

**架构：** 将 `StatsController`/`StatsService` 按命令、用户记录查询和聚合统计拆成三组 Controller/Service。DTO、数据库和领域规则不变；前端 API 层按相同边界拆分并更新调用方。旧路由不保留兼容层。

**技术栈：** Spring Boot 3、Sa-Token、MyBatis Plus、JUnit/Mockito、Vue 3、Vite、Vitest。

---

## 文件与职责

- 创建：`backend/src/main/java/com/jaymetest/controller/GameResultController.java` — `POST /api/game-results`。
- 创建：`backend/src/main/java/com/jaymetest/controller/GameRecordController.java` — `GET /api/game-records/me`。
- 创建：`backend/src/main/java/com/jaymetest/controller/StatisticsController.java` — `GET /api/statistics/overview`。
- 创建：`backend/src/main/java/com/jaymetest/service/GameResultService.java` — 迁移当前结算逻辑。
- 创建：`backend/src/main/java/com/jaymetest/service/GameRecordQueryService.java` — 迁移当前个人记录查询。
- 创建：`backend/src/main/java/com/jaymetest/service/StatisticsService.java` — 迁移当前全局概览逻辑。
- 删除：`backend/src/main/java/com/jaymetest/controller/StatsController.java` 与 `backend/src/main/java/com/jaymetest/service/StatsService.java`。
- 修改：`backend/src/main/java/com/jaymetest/config/SaTokenConfig.java` — 替换公开路由白名单。
- 创建：`frontend/src/api/gameResultApi.ts`、`gameRecordApi.ts`、`statisticsApi.ts` — 三个领域 API。
- 修改：所有 `statsApi` 调用方及相关 Vitest 测试。
- 修改：`docs/ARCHITECTURE.md`、`docs/TECH.md` — 记录新路径与职责。

### 任务 1：为新后端服务边界建立失败测试

**文件：**
- 创建：`backend/src/test/java/com/jaymetest/service/GameResultServiceTest.java`
- 创建：`backend/src/test/java/com/jaymetest/service/GameRecordQueryServiceTest.java`
- 创建：`backend/src/test/java/com/jaymetest/service/StatisticsServiceTest.java`

- [ ] **步骤 1：编写失败的测试**

将现有 `StatsServiceTest` 的三种行为分别移到三个尚不存在服务：`submitResult`、`getMyRecords`、`getOverview`。每个测试只构造该服务所需依赖，并保留已有 Round 去重、深渊结算和统计分布断言。

- [ ] **步骤 2：运行测试验证失败**

运行：`mvn -Dtest=GameResultServiceTest,GameRecordQueryServiceTest,StatisticsServiceTest test`

预期：FAIL，原因是三个服务类尚不存在。

- [ ] **步骤 3：实现最少服务代码**

从 `StatsService` 移动方法及其最小依赖：

```java
@Service
@RequiredArgsConstructor
public class GameRecordQueryService {
    private final GameRecordMapper gameRecordMapper;
    private final GameRecordDTOAssembler gameRecordDTOAssembler;

    public List<GameRecordDTO> getMyRecords(int page, int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return gameRecordMapper.selectByUserId(userId, size, (page - 1) * size)
                .stream().map(gameRecordDTOAssembler::toDTO).toList();
    }
}
```

`GameResultService` 保留 `submitResult` 原有语义；`StatisticsService` 保留 `getOverview` 原有语义。

- [ ] **步骤 4：运行测试验证通过**

运行：`mvn -Dtest=GameResultServiceTest,GameRecordQueryServiceTest,StatisticsServiceTest test`

预期：PASS。

### 任务 2：迁移 Controller 与安全路由

**文件：**
- 创建：`backend/src/main/java/com/jaymetest/controller/GameResultController.java`
- 创建：`backend/src/main/java/com/jaymetest/controller/GameRecordController.java`
- 创建：`backend/src/main/java/com/jaymetest/controller/StatisticsController.java`
- 修改：`backend/src/main/java/com/jaymetest/config/SaTokenConfig.java`
- 删除：`backend/src/main/java/com/jaymetest/controller/StatsController.java`

- [ ] **步骤 1：编写失败的 Controller/路由测试**

为三条新路径分别断言 Controller 映射、`GameRecordController` 的 `@SaCheckLogin`，并断言 `SaTokenConfig` 公开白名单包含 `/api/game-results`、`/api/statistics/overview` 而不包含 `/api/stats/**`。

- [ ] **步骤 2：运行测试验证失败**

运行：`mvn -Dtest=*ControllerTest,*SaTokenConfigTest test`

预期：FAIL，原因是新 Controller 和新白名单不存在。

- [ ] **步骤 3：实现新 HTTP 边界**

```java
@RestController
@RequestMapping("/api/game-results")
@RequiredArgsConstructor
public class GameResultController {
    private final GameResultService gameResultService;

    @PostMapping
    public R<GameResultDTO> submit(@Valid @RequestBody GameSubmitRequest request) {
        return R.ok(gameResultService.submitResult(request));
    }
}
```

另外两个 Controller 分别映射 `/api/game-records/me` 与 `/api/statistics/overview`；删除旧 Controller，更新 Sa-Token 白名单。

- [ ] **步骤 4：运行 Controller 测试验证通过**

运行：`mvn -Dtest=*ControllerTest,*SaTokenConfigTest test`

预期：PASS。

### 任务 3：迁移前端 API 模块与调用方

**文件：**
- 创建：`frontend/src/api/gameResultApi.ts`
- 创建：`frontend/src/api/gameRecordApi.ts`
- 创建：`frontend/src/api/statisticsApi.ts`
- 删除：`frontend/src/api/statsApi.ts`
- 修改：`frontend/src/composables/useQuiz.ts`、个人中心及引用 `statsApi.ts` 的页面/测试。

- [ ] **步骤 1：编写失败的 API 路径测试**

在 Vitest 中 mock `client`，分别断言：结算调用 `post('/game-results', request)`；记录调用 `get('/game-records/me', { params })`；概览调用 `get('/statistics/overview')`。

- [ ] **步骤 2：运行测试验证失败**

运行：`npm run test -- api`

预期：FAIL，原因是新模块不存在或仍请求 `/stats/*`。

- [ ] **步骤 3：实现并迁移调用方**

将 `statsApi.ts` 的 DTO 类型按实际消费者移动到三个模块；替换所有导入，删除旧模块。调用方行为不变。

- [ ] **步骤 4：运行前端测试验证通过**

运行：`npm run test`

预期：PASS。

### 任务 4：清理、文档与完整验证

**文件：**
- 删除：`backend/src/main/java/com/jaymetest/service/StatsService.java`
- 删除：`backend/src/test/java/com/jaymetest/service/StatsServiceTest.java`
- 修改：`docs/ARCHITECTURE.md`
- 修改：`docs/TECH.md`

- [ ] **步骤 1：更新文档与旧路径检查**

文档列出新 Controller、Service 与路由；移除 `/api/stats/`、`StatsController`、`StatsService` 的活跃实现说明。保留历史设计文档时标注其历史属性，避免被误当作当前接口。

- [ ] **步骤 2：运行旧路径扫描**

运行：`rg -n '/api/stats|/stats/' backend/src/main frontend/src docs/ARCHITECTURE.md docs/TECH.md`

预期：无当前实现或当前文档命中。

- [ ] **步骤 3：运行完整验证**

运行：

```powershell
Set-Location backend; mvn test
Set-Location ..\frontend; npm run test; npm run build
Set-Location ..\admin; npm run build
```

预期：所有命令退出码为 0。
