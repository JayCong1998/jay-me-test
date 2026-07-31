# V4 Database Scripts

### `003_add_game_record_score.sql`

- Purpose: adds `game_record.score` to persist final scores. Classic and album modes use a percentage score; abyss uses the streak.
- Affected table: `game_record`
- Prerequisite: existing V4 schema after scripts 001 and 002.
- Idempotency: safe to rerun on MySQL 8.0+.
- Existing rows: retain the default score `0`, because their configured question count is not reliably available.

第四版题库分类增量脚本。

## 执行顺序

1. `001_migrate_question_categories.sql`
2. `002_add_user_nickname_unique.sql`
3. `003_add_game_record_score.sql`

## 脚本说明

### `001_migrate_question_categories.sql`

- 用途：将旧题型 `ALBUM` 迁移为 `WORKS`，并将 `question.category` 的可选值扩展为 `LYRICS`、`WORKS`、`SCREEN`、`KNOWLEDGE`。
- 影响表：`question`
- 前置条件：已存在 `jaymetest` 数据库，并已执行此前版本的数据库脚本。
- 幂等性：可重复执行。
- 注意事项：脚本会短暂调整 `question` 表的 `CHECK` 约束；应在低峰期执行。游戏模式 `ALBUM` 与题目分类无关，不受本脚本影响。

### `002_add_user_nickname_unique.sql`

- 用途：为 `user.nickname` 添加唯一约束，防止并发注册绕过应用层检查。
- 影响表：`user`
- 前置条件：执行脚本第一段查询后不得存在重复昵称；如有重复，先人工处理数据。
- 幂等性：索引已存在时不会重复创建；检测到重复昵称时不执行建索引语句。
