-- ============================================
-- V4 题目分类迁移
-- 用途：将旧题型 ALBUM 迁移为 WORKS，并扩展题目分类约束
-- 前置条件：已执行 baseline/ 及 releases/v3/ 下脚本
-- 影响表：question
-- 幂等性：可重复执行；重复执行时不会重复变更数据或约束
-- 执行风险：执行期间会短暂修改 question 表的 CHECK 约束
-- ============================================

USE jaymetest;

SET @category_constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'question'
      AND constraint_name = 'chk_category'
      AND constraint_type = 'CHECK'
);
SET @drop_category_constraint_sql = IF(
    @category_constraint_exists > 0,
    'ALTER TABLE question DROP CHECK chk_category',
    'SELECT 1'
);
PREPARE drop_category_constraint_statement FROM @drop_category_constraint_sql;
EXECUTE drop_category_constraint_statement;
DEALLOCATE PREPARE drop_category_constraint_statement;

UPDATE question
SET category = 'WORKS'
WHERE category = 'ALBUM';

ALTER TABLE question
    ADD CONSTRAINT chk_category
    CHECK (category IN ('LYRICS', 'WORKS', 'SCREEN', 'KNOWLEDGE'));
