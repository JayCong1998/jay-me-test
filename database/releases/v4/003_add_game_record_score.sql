-- Purpose: Persist the final score for each game record.
-- Prerequisite: V4 scripts 001 and 002 have been applied.
-- Affected table: game_record.
-- Idempotency: checks information_schema before adding the column.

USE jaymetest;

SET @score_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'game_record'
      AND column_name = 'score'
);
SET @score_column_sql := IF(
    @score_column_exists = 0,
    'ALTER TABLE game_record ADD COLUMN score INT NOT NULL DEFAULT 0 COMMENT ''Final game score'' AFTER correct_count',
    'SELECT ''game_record.score already exists'' AS message'
);
PREPARE score_column_statement FROM @score_column_sql;
EXECUTE score_column_statement;
DEALLOCATE PREPARE score_column_statement;
