CREATE TABLE IF NOT EXISTS starters (
    starter_id INT GENERATED ALWAYS AS IDENTITY,
    race_number INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (starter_id, timestamp)
);

-- Create explicit indexes for query performance
CREATE INDEX idx_starters_race_number ON starters(race_number);
CREATE INDEX idx_starters_timestamp ON starters(timestamp);

-- Create TimescaleDB hypertable for time-series optimization
-- This enables automatic time-based partitioning and compression
SELECT create_hypertable('starters', 'timestamp', if_not_exists => TRUE);

-- Enable compression on the hypertable
ALTER TABLE starters SET (timescaledb.compress = true);

-- Add compression policy: compress chunks older than 72 hours (per ADR 0008)
SELECT add_compression_policy('starters', INTERVAL '72 hours', if_not_exists => TRUE);
