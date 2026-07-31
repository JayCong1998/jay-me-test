-- ============================================
-- jay-me-test current schema snapshot
-- Generated from baseline/schema.sql + releases/v3/001_add_admin_console.sql
-- Date: 2026-07-28
-- 用途：查看当前完整表结构，不作为版本升级入口
-- ============================================

-- ============================================
-- 杰迷结业考试 数据库初始化 DDL (MySQL 8.0+)
-- 用途：从零创建本地基础库表结构
-- 前置条件：MySQL 8.0+，当前用户具备 CREATE DATABASE / CREATE TABLE 权限
-- 影响表：question, user, game_record, album_progress
-- 幂等性：表使用 IF NOT EXISTS；函数索引重复执行可能失败，已建库环境不要反复执行本文件
-- ============================================

CREATE DATABASE IF NOT EXISTS jaymetest
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE jaymetest;

-- ============================================
-- 题目表
-- ============================================
CREATE TABLE IF NOT EXISTS question (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    category       VARCHAR(20) NOT NULL COMMENT '分类: LYRICS | WORKS | SCREEN | KNOWLEDGE',
    album          VARCHAR(50) NULL     COMMENT '所属专辑（中文名），跨专辑/NULL=不纳入专辑模式抽题',
    difficulty     VARCHAR(20) NOT NULL COMMENT '难度: EASY | MEDIUM | HARD',
    question_text  TEXT        NOT NULL COMMENT '题目正文',
    option_a       TEXT        NOT NULL COMMENT '选项 A 内容',
    option_b       TEXT        NOT NULL COMMENT '选项 B 内容',
    option_c       TEXT        NOT NULL COMMENT '选项 C 内容',
    option_d       TEXT        NOT NULL COMMENT '选项 D 内容',
    correct_option CHAR(1)     NOT NULL COMMENT '正确答案: A|B|C|D',
    explanation    TEXT        NOT NULL COMMENT '答案解析',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_category     CHECK (category IN ('LYRICS', 'WORKS', 'SCREEN', 'KNOWLEDGE')),
    CONSTRAINT chk_difficulty   CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT chk_correct_opt  CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    INDEX idx_q_category (category),
    INDEX idx_q_difficulty (difficulty),
    INDEX idx_q_album (album),
    INDEX idx_q_cat_diff (category, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱，唯一',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt 哈希',
    nickname    VARCHAR(50)  NOT NULL UNIQUE COMMENT '昵称，唯一',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 游戏记录表
-- ============================================
CREATE TABLE IF NOT EXISTS game_record (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    round_id         VARCHAR(36)  NOT NULL COMMENT 'UUID 去重',
    mode             VARCHAR(20)  NOT NULL COMMENT '游戏模式: CLASSIC | ALBUM | ABYSS',
    album_key        VARCHAR(50)  NULL     COMMENT '专辑模式下的专辑标识（中文名）',
    user_id          BIGINT       NULL     COMMENT 'FK→user.id，游客为NULL',
    nickname         VARCHAR(50)  NULL     COMMENT '昵称快照，冗余避免JOIN',
    total_questions  INT          NOT NULL DEFAULT 10,
    correct_count    INT          NOT NULL COMMENT '答对数量；深渊模式为连续答对数',
    time_spent_secs  INT          NOT NULL COMMENT '答题总用时（秒）',
    used_revival     TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_round_id UNIQUE (round_id),
    CONSTRAINT chk_game_record_mode CHECK (mode IN ('CLASSIC', 'ALBUM', 'ABYSS')),
    CONSTRAINT chk_game_record_album CHECK (
        (mode = 'ALBUM' AND album_key IS NOT NULL AND TRIM(album_key) <> '')
        OR (mode IN ('CLASSIC', 'ABYSS') AND album_key IS NULL)
    ),
    INDEX idx_gr_score (correct_count),
    INDEX idx_gr_created (created_at),
    INDEX idx_gr_mode (mode),
    INDEX idx_gr_user_score (user_id, correct_count, time_spent_secs)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏记录表';

-- MySQL 8.0.13+ 函数索引不能在 CREATE TABLE 内联定义（括号嵌套限制），单独创建
CREATE INDEX idx_gr_date_score ON game_record((DATE(created_at)), correct_count, time_spent_secs);

-- ============================================
-- 专辑闯关进度表
-- ============================================
CREATE TABLE IF NOT EXISTS album_progress (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT      NOT NULL COMMENT 'FK→user.id',
    album_key         VARCHAR(50) NOT NULL COMMENT '专辑标识（中文名）',
    unlocked          TINYINT     NOT NULL DEFAULT 0 COMMENT '0=未解锁 1=已解锁',
    best_score        INT         NOT NULL DEFAULT 0 COMMENT '最佳答对数 (0-10)',
    total_attempts    INT         NOT NULL DEFAULT 0 COMMENT '总挑战次数',
    first_passed_at   DATETIME    NULL     COMMENT '首次通关时间 (≥8/10)',
    last_attempted_at DATETIME    NULL     COMMENT '最近挑战时间',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_album UNIQUE (user_id, album_key),
    INDEX idx_ap_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专辑闯关进度表';

-- ============================================
-- V3 admin console objects
-- ============================================

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
