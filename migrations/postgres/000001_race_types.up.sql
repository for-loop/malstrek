-- Create TimescaleDB extension (required for all hypertable operations)
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

CREATE TABLE IF NOT EXISTS race_types (
    race_type_id SMALLINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO race_types (type) VALUES
    ('Standard Road'),
    ('Trail Runs'),
    ('Fun Runs / Novelty Races'),
    ('Track Events'),
    ('Cross Country');
