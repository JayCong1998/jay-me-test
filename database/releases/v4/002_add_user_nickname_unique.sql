-- ============================================
-- V4 用户昵称唯一约束
-- 用途：保证 user.nickname 在数据库层唯一，防止并发注册绕过服务层检查
-- 前置条件：MySQL 8.0+；执行前确认下方重复昵称查询无结果
-- 影响表：user
-- 幂等性：通过 information_schema 检查索引是否存在；检测到重复昵称时不会创建索引
-- ============================================

USE jaymetest;

SELECT nickname, COUNT(*) AS duplicate_count
FROM user
GROUP BY nickname
HAVING COUNT(*) > 1;

SET @nickname_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND index_name = 'uk_user_nickname'
);
SET @nickname_duplicates := (
    SELECT COUNT(*)
    FROM (
        SELECT nickname
        FROM user
        GROUP BY nickname
        HAVING COUNT(*) > 1
    ) AS duplicates
);
SET @nickname_index_sql := IF(
    @nickname_index_exists = 0 AND @nickname_duplicates = 0,
    'ALTER TABLE user ADD CONSTRAINT uk_user_nickname UNIQUE (nickname)',
    'SELECT ''昵称唯一索引未创建：索引已存在或存在重复昵称，请先处理重复数据'' AS message'
);
PREPARE nickname_index_statement FROM @nickname_index_sql;
EXECUTE nickname_index_statement;
DEALLOCATE PREPARE nickname_index_statement;
