-- ============================================
-- 杰迷结业考试 初始化用户数据
-- 用途：写入本地测试用户
-- 前置条件：已执行 baseline/schema.sql
-- 影响表：user
-- 幂等性：非幂等；email 有唯一约束，重复执行会因重复邮箱失败
-- BCrypt 哈希对应明文密码: 123456
-- ============================================

USE jaymetest;

INSERT INTO user (email, password, nickname) VALUES
('1223574947@qq.com', '$2a$10$IUTwxa1GztAN4fTML/rlOev5hgIqBiu0KCgGDwSnLM6Txy9bJIkmm', 'JayCong'),
('2042816522@qq.com', '$2a$10$IUTwxa1GztAN4fTML/rlOev5hgIqBiu0KCgGDwSnLM6Txy9bJIkmm', 'YangSusie'),
('lbj23@google.com', '$2a$10$IUTwxa1GztAN4fTML/rlOev5hgIqBiu0KCgGDwSnLM6Txy9bJIkmm', 'Leborn James');
