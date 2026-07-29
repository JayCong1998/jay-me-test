# Database Script Management

本项目不使用 Flyway、Liquibase 等数据库迁移框架。数据库脚本通过目录、编号和人工执行记录管理。

## 目录说明

```text
database/
├── baseline/
│   ├── schema.sql
│   ├── seed_001_initial_questions.sql
│   ├── seed_002_question_expansion.sql
│   └── seed_003_test_users.sql
├── releases/
│   ├── v3/
│   │   ├── 001_add_admin_console.sql
│   │   └── README.md
│   ├── v4/
│   │   ├── 001_migrate_question_categories.sql
│   │   └── README.md
│   └── v5/
│       ├── 001_seed_question_bank_500.sql
│       ├── question_bank_500_sources.md
│       └── README.md
└── snapshots/
    └── 2026-07-28_schema.sql
```

- `baseline/`：从零初始化本地数据库时使用，包含当前基础表结构和基础种子数据。
- `releases/vX/`：每个产品版本的增量 SQL。已经合入的脚本不再修改，后续修正追加新编号文件。
- `releases/vX/README.md`：记录该版本脚本的执行顺序、影响范围和注意事项。
- `snapshots/`：当前表结构快照，只用于查看全貌和人工排查，不作为版本升级入口。

## 命名规范

增量脚本命名：

```text
{三位递增编号}_{action}_{object}.sql
```

示例：

```text
001_add_admin_console.sql
002_add_abyss_record_indexes.sql
003_seed_admin_permissions.sql
```

常用 `action`：

- `create`：新建表或主要对象
- `add`：新增字段、索引、约束或功能相关对象
- `alter`：修改已有结构
- `backfill`：回填历史数据
- `seed`：初始化数据
- `fix`：修正上一版脚本遗留问题

## 编写规则

- 不直接修改已经提交过的历史 SQL；发现问题时新增下一个编号脚本。
- 一个脚本只处理一个清晰的业务变更，避免把多个需求混在一起。
- 涉及数据回填的脚本要写清楚是否可重复执行；能幂等就尽量幂等。
- 脚本顶部写明用途、执行前置条件和影响表。
- 本地从零初始化时按 `baseline/schema.sql`、`baseline/seed_*.sql` 顺序执行。
- 版本升级时按 `releases/vX/` 下的编号顺序执行。
- 结构快照需要随表结构变化同步更新，但不能替代 `releases/vX/` 下的增量脚本。
- 提交数据库相关改动时，变更摘要中必须列出新增或调整的 SQL 文件。

## 执行记录

本项目不在代码里维护自动执行状态。多人协作时，建议在部署说明、PR 描述或版本发布记录中记录：

- 执行环境
- 执行人
- 执行时间
- 已执行到的版本目录和脚本编号
