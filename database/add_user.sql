-- ============================================
-- 杰迷结业考试 初始化用户数据
-- BCrypt 哈希对应明文密码: 123456
-- ============================================

USE jaymetest;

INSERT INTO user (email, password, nickname) VALUES
('1223574947@qq.com', '$2a$10$IUTwxa1GztAN4fTML/rlOev5hgIqBiu0KCgGDwSnLM6Txy9bJIkmm', 'JayCong'),
('2042816522@qq.com', '$2a$10$IUTwxa1GztAN4fTML/rlOev5hgIqBiu0KCgGDwSnLM6Txy9bJIkmm', 'YangSusie'),
('lbj23@google.com', '$2a$10$IUTwxa1GztAN4fTML/rlOev5hgIqBiu0KCgGDwSnLM6Txy9bJIkmm', 'Leborn James');
