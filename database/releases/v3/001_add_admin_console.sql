-- ============================================
-- V3 后台管理控制台
-- 用途：新增后台管理员表，并写入本地默认管理员账号
-- 前置条件：已存在 jaymetest 数据库
-- 影响表：admin_user
-- 幂等性：表和默认账号均可重复执行
-- 默认本地账号：admin / admin123
-- ============================================

USE jaymetest;

CREATE TABLE IF NOT EXISTS admin_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Admin username',
    password      VARCHAR(255) NOT NULL COMMENT 'BCrypt password hash',
    nickname      VARCHAR(50)  NOT NULL COMMENT 'Display name',
    role          VARCHAR(30)  NOT NULL COMMENT 'SUPER_ADMIN | OPERATOR',
    enabled       TINYINT      NOT NULL DEFAULT 1 COMMENT '0=disabled, 1=enabled',
    last_login_at DATETIME     NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_admin_role CHECK (role IN ('SUPER_ADMIN', 'OPERATOR')),
    CONSTRAINT chk_admin_enabled CHECK (enabled IN (0, 1)),
    INDEX idx_admin_user_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Admin user table';

INSERT INTO admin_user (username, password, nickname, role, enabled)
SELECT 'admin',
       '$2a$10$s.1WIarzpHI6AuDezsa79eio2GW5TwxhVZcA8a3IqFUna9KGvgT9m',
       'Super Admin',
       'SUPER_ADMIN',
       1
WHERE NOT EXISTS (
    SELECT 1 FROM admin_user WHERE username = 'admin'
);
