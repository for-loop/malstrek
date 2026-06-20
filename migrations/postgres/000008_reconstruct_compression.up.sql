-- 1. Strip away old automation policies to unlock hypertable metadata catalog locks
SELECT remove_compression_policy('starters', if_exists => TRUE);
SELECT remove_compression_policy('finishers', if_exists => TRUE);

-- 2. Alter columnstore rules to apply optimized segment layouts
ALTER TABLE starters SET (
    timescaledb.compress = true,
    timescaledb.compress_segmentby = 'race_number',
    timescaledb.compress_orderby = 'timestamp ASC'
);

ALTER TABLE finishers SET (
    timescaledb.compress = true,
    timescaledb.compress_segmentby = 'race_number',
    timescaledb.compress_orderby = 'timestamp ASC'
);

-- 3. Spin background automation workers back up safely
SELECT add_compression_policy('starters', INTERVAL '72 hours', if_not_exists => TRUE);
SELECT add_compression_policy('finishers', INTERVAL '72 hours', if_not_exists => TRUE);
