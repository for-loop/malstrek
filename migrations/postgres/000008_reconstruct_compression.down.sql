-- 1. Clear out active segmented compression job tracking logs
SELECT remove_compression_policy('starters', if_exists => TRUE);
SELECT remove_compression_policy('finishers', if_exists => TRUE);

-- 2. Cleanly revert hypertable storage defaults back to plain configurations
ALTER TABLE starters SET (
    timescaledb.compress = true,
    timescaledb.compress_segmentby = '',
    timescaledb.compress_orderby = 'timestamp DESC'
);

ALTER TABLE finishers SET (
    timescaledb.compress = true,
    timescaledb.compress_segmentby = '',
    timescaledb.compress_orderby = 'timestamp DESC'
);

-- 3. Restore original basic unsegmented policies
SELECT add_compression_policy('starters', INTERVAL '72 hours', if_not_exists => TRUE);
SELECT add_compression_policy('finishers', INTERVAL '72 hours', if_not_exists => TRUE);
