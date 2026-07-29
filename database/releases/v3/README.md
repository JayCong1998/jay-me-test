# V3 Database Scripts

第三版数据库增量脚本。

## 执行顺序

1. `001_add_admin_console.sql`

## 脚本说明

### `001_add_admin_console.sql`

- 用途：新增后台管理用户表，并写入本地默认管理员账号。
- 影响表：`admin_user`
- 前置条件：已存在 `jaymetest` 数据库。
- 默认本地账号：`admin / admin123`
- 幂等性：表使用 `CREATE TABLE IF NOT EXISTS`；默认账号使用 `WHERE NOT EXISTS` 避免重复插入。

## 注意事项

- 该目录下脚本按编号顺序执行。
- 已执行到共享环境后，不再修改旧脚本；如需修正，新增下一个编号脚本。
