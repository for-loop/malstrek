-- Safely remove composite indexes
DROP INDEX IF EXISTS idx_starters_active_race_time;
DROP INDEX IF EXISTS idx_finishers_race_deleted_time;

-- Restore original simple indexes
CREATE INDEX idx_starters_race_number ON starters(race_number);
CREATE INDEX idx_starters_timestamp ON starters(timestamp);
CREATE INDEX idx_finishers_race_number ON finishers(race_number);
CREATE INDEX idx_finishers_timestamp ON finishers(timestamp);
