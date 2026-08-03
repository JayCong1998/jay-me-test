-- ============================================
-- jay-me-test development demo activity seed
-- Purpose: add realistic local users, game records, and album progress for UI/admin testing
-- Preconditions: baseline schema and question seed data have been loaded
-- Affected tables: user, game_record, album_progress
-- Idempotency: safe to rerun; demo users use stable emails and records use stable round_id values
-- Notes: run only in a local/development database
-- ============================================

USE jaymetest;

SET @demo_password_hash = '$2a$10$IUTwxa1GztAN4fTML/rlOev5hgIqBiu0KCgGDwSnLM6Txy9bJIkmm';
SET @base_time = TIMESTAMP(CURRENT_DATE, '10:00:00') - INTERVAL 45 DAY;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_user_seed (
    seq INT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL
);

TRUNCATE TABLE tmp_demo_user_seed;

INSERT INTO tmp_demo_user_seed (seq, nickname) VALUES
(1, 'QiLiXiangFM'),
(2, 'BlueStormJay'),
(3, 'SunnyChord'),
(4, 'MojitoBeat'),
(5, 'NocturneFan'),
(6, 'PianoOrbit'),
(7, 'RiceFieldHero'),
(8, 'SecretSignal'),
(9, 'DragonRider'),
(10, 'SimpleLove'),
(11, 'StarryJay'),
(12, 'NinjaEcho'),
(13, 'ChopinRain'),
(14, 'MagicIsland'),
(15, 'AiyoListener'),
(16, 'BedtimeJay'),
(17, 'CowboyCoder'),
(18, 'RetroCassette'),
(19, 'LyricHunter'),
(20, 'MelodyRunner'),
(21, 'VinylCloud'),
(22, 'GoldenArmor'),
(23, 'RainbowNote'),
(24, 'OrbitDancer'),
(25, 'SilentKeyboard'),
(26, 'CafeMojito'),
(27, 'GardeniaFan'),
(28, 'OpusDreamer'),
(29, 'SeaSideJay'),
(30, 'AbyssWalker'),
(31, 'AlbumPilot'),
(32, 'FlowingKeys'),
(33, 'CyanPorcelain'),
(34, 'NightTrain'),
(35, 'SunChildFan'),
(36, 'FantasyLoop');

INSERT IGNORE INTO user (email, password, nickname, created_at, updated_at)
SELECT
    CONCAT('demo', LPAD(seq, 2, '0'), '@jaymetest.local') AS email,
    @demo_password_hash AS password,
    nickname,
    @base_time + INTERVAL seq HOUR AS created_at,
    @base_time + INTERVAL seq HOUR AS updated_at
FROM tmp_demo_user_seed;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_users AS
SELECT u.id AS user_id, s.seq, u.nickname
FROM tmp_demo_user_seed s
JOIN user u ON u.email = CONCAT('demo', LPAD(s.seq, 2, '0'), '@jaymetest.local');

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_attempts (n INT PRIMARY KEY);
TRUNCATE TABLE tmp_attempts;
INSERT INTO tmp_attempts (n) VALUES (1), (2), (3), (4);

-- CLASSIC: broad score distribution with multiple attempts per user.
INSERT IGNORE INTO game_record (
    round_id, mode, album_key, user_id, nickname, total_questions,
    correct_count, score, time_spent_secs, used_revival, created_at
)
SELECT
    CONCAT('dev-classic-', LPAD(du.seq, 2, '0'), '-', a.n) AS round_id,
    'CLASSIC' AS mode,
    NULL AS album_key,
    du.user_id,
    du.nickname,
    20 AS total_questions,
    LEAST(20, GREATEST(0, 6 + FLOOR(du.seq / 4) + a.n + MOD(du.seq * a.n, 5))) AS correct_count,
    ROUND(LEAST(20, GREATEST(0, 6 + FLOOR(du.seq / 4) + a.n + MOD(du.seq * a.n, 5))) * 100 / 20) AS score,
    85 + MOD(du.seq * 17 + a.n * 29, 220) AS time_spent_secs,
    IF(MOD(du.seq + a.n, 9) = 0, 1, 0) AS used_revival,
    @base_time + INTERVAL (du.seq * 23 + a.n * 5) HOUR AS created_at
FROM tmp_demo_users du
CROSS JOIN tmp_attempts a;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_albums (
    seq INT PRIMARY KEY,
    album_key VARCHAR(50) NOT NULL
);

TRUNCATE TABLE tmp_demo_albums;

INSERT INTO tmp_demo_albums (seq, album_key) VALUES
(1, 'Jay'),
(2, '范特西'),
(3, '八度空间'),
(4, '叶惠美'),
(5, '七里香'),
(6, '十一月的萧邦'),
(7, '依然范特西'),
(8, '我很忙'),
(9, '魔杰座'),
(10, '跨时代'),
(11, '惊叹号'),
(12, '12新作'),
(13, '哎呦，不错哦'),
(14, '周杰伦的床边故事'),
(15, '最伟大的作品'),
(16, '太阳之子');

-- ALBUM: each user has a different journey depth; stronger users clear more albums.
INSERT IGNORE INTO game_record (
    round_id, mode, album_key, user_id, nickname, total_questions,
    correct_count, score, time_spent_secs, used_revival, created_at
)
SELECT
    CONCAT('dev-album-', LPAD(du.seq, 2, '0'), '-', LPAD(al.seq, 2, '0')) AS round_id,
    'ALBUM' AS mode,
    al.album_key,
    du.user_id,
    du.nickname,
    20 AS total_questions,
    LEAST(20, GREATEST(5, 11 + FLOOR(du.seq / 5) + MOD(du.seq + al.seq, 5) - FLOOR(al.seq / 6))) AS correct_count,
    ROUND(LEAST(20, GREATEST(5, 11 + FLOOR(du.seq / 5) + MOD(du.seq + al.seq, 5) - FLOOR(al.seq / 6))) * 100 / 20) AS score,
    120 + MOD(du.seq * 31 + al.seq * 19, 260) AS time_spent_secs,
    0 AS used_revival,
    @base_time + INTERVAL (du.seq * 17 + al.seq * 7) HOUR AS created_at
FROM tmp_demo_users du
JOIN tmp_demo_albums al
  ON al.seq <= LEAST(16, 1 + FLOOR(du.seq / 3) + MOD(du.seq, 4));

-- ABYSS: streak-oriented data with a few top performers.
INSERT IGNORE INTO game_record (
    round_id, mode, album_key, user_id, nickname, total_questions,
    correct_count, score, time_spent_secs, used_revival, created_at
)
SELECT
    CONCAT('dev-abyss-', LPAD(du.seq, 2, '0'), '-', a.n) AS round_id,
    'ABYSS' AS mode,
    NULL AS album_key,
    du.user_id,
    du.nickname,
    LEAST(70, 3 + FLOOR(du.seq / 2) + a.n * 2 + MOD(du.seq * a.n, 9)) AS total_questions,
    LEAST(65, 2 + FLOOR(du.seq / 2) + a.n * 2 + MOD(du.seq * a.n, 9)) AS correct_count,
    LEAST(65, 2 + FLOOR(du.seq / 2) + a.n * 2 + MOD(du.seq * a.n, 9)) AS score,
    45 + LEAST(65, 2 + FLOOR(du.seq / 2) + a.n * 2 + MOD(du.seq * a.n, 9)) * (8 + MOD(du.seq, 6)) AS time_spent_secs,
    IF(MOD(du.seq + a.n, 4) = 0, 1, 0) AS used_revival,
    @base_time + INTERVAL (du.seq * 29 + a.n * 11) HOUR AS created_at
FROM tmp_demo_users du
CROSS JOIN tmp_attempts a;

-- Guest CLASSIC records make overview statistics closer to real traffic.
INSERT IGNORE INTO game_record (
    round_id, mode, album_key, user_id, nickname, total_questions,
    correct_count, score, time_spent_secs, used_revival, created_at
)
SELECT
    CONCAT('dev-guest-classic-', LPAD(a.n, 2, '0')) AS round_id,
    'CLASSIC' AS mode,
    NULL AS album_key,
    NULL AS user_id,
    CONCAT('GuestJay', LPAD(a.n, 2, '0')) AS nickname,
    20 AS total_questions,
    5 + MOD(a.n * 4, 15) AS correct_count,
    ROUND((5 + MOD(a.n * 4, 15)) * 100 / 20) AS score,
    110 + a.n * 21 AS time_spent_secs,
    0 AS used_revival,
    @base_time + INTERVAL (a.n * 13) HOUR AS created_at
FROM (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
) a;

-- Album progress mirrors best ALBUM records and unlocks the next album after passing 80%.
INSERT INTO album_progress (
    user_id, album_key, unlocked, best_score, total_attempts,
    first_passed_at, last_attempted_at, created_at, updated_at
)
SELECT
    du.user_id,
    al.album_key,
    1 AS unlocked,
    COALESCE(MAX(gr.correct_count), 0) AS best_score,
    COUNT(gr.id) AS total_attempts,
    MIN(CASE WHEN gr.correct_count * 100 >= gr.total_questions * 80 THEN gr.created_at ELSE NULL END) AS first_passed_at,
    MAX(gr.created_at) AS last_attempted_at,
    MIN(COALESCE(gr.created_at, @base_time + INTERVAL du.seq HOUR)) AS created_at,
    MAX(COALESCE(gr.created_at, @base_time + INTERVAL du.seq HOUR)) AS updated_at
FROM tmp_demo_users du
JOIN tmp_demo_albums al
  ON al.seq <= LEAST(16, 2 + FLOOR(du.seq / 3) + MOD(du.seq, 4))
LEFT JOIN game_record gr
  ON gr.user_id = du.user_id
 AND gr.mode = 'ALBUM'
 AND gr.album_key = al.album_key
 AND gr.round_id LIKE 'dev-album-%'
GROUP BY du.user_id, al.album_key
ON DUPLICATE KEY UPDATE
    unlocked = VALUES(unlocked),
    best_score = GREATEST(album_progress.best_score, VALUES(best_score)),
    total_attempts = GREATEST(album_progress.total_attempts, VALUES(total_attempts)),
    first_passed_at = COALESCE(album_progress.first_passed_at, VALUES(first_passed_at)),
    last_attempted_at = GREATEST(album_progress.last_attempted_at, VALUES(last_attempted_at)),
    updated_at = CURRENT_TIMESTAMP;

DROP TEMPORARY TABLE IF EXISTS tmp_demo_user_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_users;
DROP TEMPORARY TABLE IF EXISTS tmp_attempts;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_albums;
