CREATE TABLE IF NOT EXISTS finishers (
    finisher_id INT GENERATED ALWAYS AS IDENTITY,
    race_number INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    bib_number INT,
    deleted BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (finisher_id, timestamp)
);

-- Create explicit indexes for query performance
CREATE INDEX idx_finishers_race_number ON finishers(race_number);
CREATE INDEX idx_finishers_timestamp ON finishers(timestamp);

-- Create TimescaleDB hypertable for time-series optimization
-- This enables automatic time-based partitioning and compression
SELECT create_hypertable('finishers', 'timestamp', if_not_exists => TRUE);

-- Enable compression on the hypertable
ALTER TABLE finishers SET (timescaledb.compress = true);

-- Add compression policy: compress chunks older than 72 hours (per ADR 0008)
SELECT add_compression_policy('finishers', INTERVAL '72 hours', if_not_exists => TRUE);
