# 游戏 API 路由迁移设计

## 目标

将混合“结算、记录查询、聚合统计”职责的 `/api/stats/**` 拆分为三个明确的资源边界，并移除旧路由。

## 路由契约

| 旧路由 | 新路由 | 控制器 | 登录要求 |
| --- | --- | --- | --- |
| `POST /api/stats/submit` | `POST /api/game-results` | `GameResultController` | 公开，保留游客结算 |
| `GET /api/stats/my-records` | `GET /api/game-records/me` | `GameRecordController` | 必须登录 |
| `GET /api/stats/overview` | `GET /api/statistics/overview` | `StatisticsController` | 公开 |

旧的 `/api/stats/**` 不保留转发、别名或废弃兼容层；请求将返回 404。用户端与后端须同次发布。

## 服务边界

- `GameResultService`：读取并校验 Round、计算成绩、持久化 `game_record`、运行玩法的提交后钩子、构建 `GameResultDTO`。
- `GameRecordQueryService`：仅按当前登录用户分页读取答题记录并组装 DTO。
- `StatisticsService`：仅提供全局人数、平均分和等级分布等聚合统计。

拆分不改变 DTO、数据库 schema、Round 防作弊规则或排行榜逻辑。

## 安全与错误处理

- `SaTokenConfig` 放行新公开路由：`/api/game-results` 与 `/api/statistics/overview`。
- `GameRecordController` 使用 `@SaCheckLogin`，并继续由服务层从当前 Sa-Token 身份取用户 ID。
- 结果提交的幂等校验、Round 过期校验与深渊失败态校验保留在 `GameResultService`。

## 前端迁移

将单一 `statsApi.ts` 拆为 `gameResultApi.ts`、`gameRecordApi.ts`、`statisticsApi.ts`。调用方只迁移导入和 URL，不改变页面行为或响应模型。

## 验证

- 后端控制器/服务测试验证三个新边界和旧路由引用已消失。
- 前端测试验证三个 API 模块请求新路径。
- 运行 `mvn test`、前端 `npm run test && npm run build`、管理端 `npm run build`。
