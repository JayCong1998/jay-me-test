-- ============================================
-- 杰迷结业考试 数据库初始化 DDL (MySQL)
-- ============================================

CREATE DATABASE IF NOT EXISTS jaymetest
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE jaymetest;

-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    category       VARCHAR(20) NOT NULL COMMENT '分类: LYRICS | ALBUM',
    difficulty     VARCHAR(20) NOT NULL COMMENT '难度: EASY | MEDIUM',
    question_text  TEXT        NOT NULL COMMENT '题目正文',
    option_a       TEXT        NOT NULL COMMENT '选项 A 内容',
    option_b       TEXT        NOT NULL COMMENT '选项 B 内容',
    option_c       TEXT        NOT NULL COMMENT '选项 C 内容',
    option_d       TEXT        NOT NULL COMMENT '选项 D 内容',
    correct_option CHAR(1)     NOT NULL COMMENT '正确答案: A|B|C|D',
    explanation    TEXT        NOT NULL COMMENT '答案解析',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_category     CHECK (category IN ('LYRICS', 'ALBUM')),
    CONSTRAINT chk_difficulty   CHECK (difficulty IN ('EASY', 'MEDIUM')),
    CONSTRAINT chk_correct_opt  CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    INDEX idx_q_category (category),
    INDEX idx_q_difficulty (difficulty),
    INDEX idx_q_cat_diff (category, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱，唯一',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt 哈希',
    nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 游戏记录表
CREATE TABLE IF NOT EXISTS game_record (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    round_id         VARCHAR(36) NOT NULL COMMENT 'UUID 去重',
    user_id          BIGINT      NULL COMMENT 'FK→user.id，游客为NULL',
    nickname         VARCHAR(50) NULL COMMENT '昵称快照，冗余避免JOIN',
    total_questions  INT         NOT NULL DEFAULT 10,
    correct_count    INT         NOT NULL COMMENT '答对数量 0-10',
    time_spent_secs  INT         NOT NULL COMMENT '答题总用时（秒）',
    used_revival     TINYINT     NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_round_id UNIQUE (round_id),
    INDEX idx_gr_score (correct_count),
    INDEX idx_gr_created (created_at),
    INDEX idx_gr_user_score (user_id, correct_count, time_spent_secs)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏记录表';

-- MySQL 8.0.13+ 函数索引不能在 CREATE TABLE 内联定义（括号嵌套限制），单独创建
CREATE INDEX idx_gr_date_score ON game_record((DATE(created_at)), correct_count, time_spent_secs);
