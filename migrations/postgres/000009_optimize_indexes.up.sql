-- Clean up old inefficient single-column indexes
DROP INDEX IF EXISTS idx_starters_race_number;
DROP INDEX IF EXISTS idx_starters_timestamp;
DROP INDEX IF EXISTS idx_finishers_race_number;
DROP INDEX IF EXISTS idx_finishers_timestamp;

-- Create highly optimized composite indexes for uncompressed 72-hour data
CREATE INDEX idx_starters_active_race_time 
ON starters (deleted, race_number, timestamp ASC);

CREATE INDEX idx_finishers_race_deleted_time 
ON finishers (race_number, deleted, timestamp ASC);
