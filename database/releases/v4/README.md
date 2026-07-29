# V4 Database Scripts

第四版题库分类增量脚本。

## 执行顺序

1. `001_migrate_question_categories.sql`

## 脚本说明

### `001_migrate_question_categories.sql`

- 用途：将旧题型 `ALBUM` 迁移为 `WORKS`，并将 `question.category` 的可选值扩展为 `LYRICS`、`WORKS`、`SCREEN`、`KNOWLEDGE`。
- 影响表：`question`
- 前置条件：已存在 `jaymetest` 数据库，并已执行此前版本的数据库脚本。
- 幂等性：可重复执行。
- 注意事项：脚本会短暂调整 `question` 表的 `CHECK` 约束；应在低峰期执行。游戏模式 `ALBUM` 与题目分类无关，不受本脚本影响。
