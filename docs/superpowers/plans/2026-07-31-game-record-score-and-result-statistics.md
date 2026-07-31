# 游戏记录分数与结果统计实现计划

> **面向 AI 代理的工作者：** 使用 `superpowers-zh:executing-plans` 逐任务执行本计划；步骤使用复选框跟踪进度。

**目标：** 持久化每局得分，并在结果页按当前模式、已登录用户的最高分计算去重后的总玩家数与击败率。

**架构：** `GameStrategy` 继续作为唯一计分入口；结算服务把其计算结果写入 `game_record.score`。Mapper 通过按 `mode + user_id` 聚合最高分的派生表，计算参与人数和低于本局得分的参与者数，游客记录不参与统计。

**技术栈：** Spring Boot、MyBatis Plus 注解 SQL、MySQL 8、JUnit 5 + Mockito。

---

### 任务 1：添加可升级的 `score` 列

**文件：**
- 创建：`database/releases/v4/002_add_game_record_score.sql`
- 修改：`database/releases/v4/README.md`
- 修改：`database/baseline/schema.sql`
- 修改：`database/snapshots/2026-07-28_schema.sql`

- [ ] **步骤 1：写入升级 SQL**

```sql
ALTER TABLE game_record
    ADD COLUMN score INT NOT NULL DEFAULT 0 COMMENT '本局得分；经典/专辑为百分制，深渊为连续答对数' AFTER correct_count;
```

- [ ] **步骤 2：同步从零初始化和快照表结构**

在 `correct_count` 后声明同一列，确保新环境和升级环境拥有一致字段。

- [ ] **步骤 3：在 release README 记录执行顺序与旧数据回填说明**

记录旧数据默认值为 `0`，不将旧版 `correct_count * 10` 回填，因为旧记录无法可靠得知当局配置题量。

### 任务 2：持久化结算分数并按模式去重统计

**文件：**
- 修改：`backend/src/main/java/com/jaymetest/model/entity/GameRecord.java`
- 修改：`backend/src/main/java/com/jaymetest/mapper/GameRecordMapper.java`
- 修改：`backend/src/main/java/com/jaymetest/service/GameResultService.java`
- 测试：`backend/src/test/java/com/jaymetest/service/StatsServiceTest.java`

- [ ] **步骤 1：先扩展结算服务测试**

```java
when(gameRecordMapper.countDistinctPlayersByMode(GameMode.CLASSIC.name())).thenReturn(8L);
when(gameRecordMapper.countDistinctPlayersByModeWithScoreLessThan(GameMode.CLASSIC.name(), 50)).thenReturn(5L);

assertEquals(8L, result.getTotalPlayers());
assertEquals(62.5, result.getBeatPercentage());
assertEquals(50, stored.getScore());
```

- [ ] **步骤 2：运行目标测试并确认旧实现失败**

运行：`mvn -Dtest=GameResultServiceTest test`

预期：因 Mapper 新方法和 `GameRecord.score` 尚不存在而无法编译。

- [ ] **步骤 3：为实体和 Mapper 加入最小实现**

```java
@Select("""
        SELECT COUNT(*) FROM (
            SELECT user_id, MAX(score) AS best_score
            FROM game_record
            WHERE mode = #{mode} AND user_id IS NOT NULL
            GROUP BY user_id
        ) ranked
        """)
long countDistinctPlayersByMode(@Param("mode") String mode);
```

低分查询复用同一派生表，并以 `best_score < #{score}` 过滤。`GameResultService` 在插入前调用 `record.setScore(score)`，并以新 Mapper 查询替代全表的 `correct_count` 统计。

- [ ] **步骤 4：运行目标测试验证实现**

运行：`mvn -Dtest=GameResultServiceTest test`

预期：通过；测试同时断言经典分数、深渊分数和游客不影响统计口径。

### 任务 3：按实际题量计算经典与专辑百分制得分

**文件：**
- 修改：`backend/src/main/java/com/jaymetest/service/game/ClassicGameStrategy.java`
- 修改：`backend/src/main/java/com/jaymetest/service/game/AlbumGameStrategy.java`
- 测试：`backend/src/test/java/com/jaymetest/service/game/GameRecordDTOAssemblerTest.java`

- [ ] **步骤 1：先补充不同题量的计分测试**

```java
assertEquals(10, classicStrategy.calculateScore(1, 10));
assertEquals(5, albumStrategy.calculateScore(1, 20));
assertEquals(33, classicStrategy.calculateScore(1, 3));
```

- [ ] **步骤 2：运行对应测试并确认旧实现失败**

运行：`mvn -Dtest=GameRecordDTOAssemblerTest test`

预期：固定 `correctCount * 10` 无法通过可配置题量的断言。

- [ ] **步骤 3：改为基于 `totalQuestions` 的百分制计算**

```java
return totalQuestions == 0 ? 0 : (int) Math.round(correctCount * 100.0 / totalQuestions);
```

深渊策略保持 `return correctCount;`，不改变其连续答对计分规则。

- [ ] **步骤 4：运行所有受影响测试与构建**

运行：`mvn test`，随后 `mvn package -DskipTests`

预期：测试和编译均通过。
